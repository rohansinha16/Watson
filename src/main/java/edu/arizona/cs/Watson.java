package edu.arizona.cs;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;

public class Watson {
	// File Settings
	static String queries = "questions.txt";
	static String indexPath = "index/lemmaNoTpl";
	static String input_dir = "wiki-subset-20140602";
	
	// Settings
	static boolean removeTPL =  false; // only used in IndexCreator
	
	// ONLY ONE OF THE FOLLOWING SHOULD BE TRUE AT A TIME
	// if both are true it will default to useLemma
	static boolean useLemma = true;
	static boolean useStem = false;
	
	// Functionality Settings
	// BE CAREFUL when setting createIndex to true (if indexPath exists, could append to the index at that location)
	static boolean createIndex = false;
	static boolean runQueries = true;

	public static void main(String[] args ) throws FileNotFoundException, IOException {
		if(createIndex) {
			IndexCreator ic = new IndexCreator(indexPath, useLemma, useStem, removeTPL);
			ic.parseFiles(input_dir);
		}
		if(runQueries) {
			QueryEngine qe = new QueryEngine(indexPath, useLemma, useStem);
			QueryEngine qeBool = new QueryEngine(indexPath, useLemma, useStem, new BooleanSimilarity());
			QueryEngine qeBM25 = new QueryEngine(indexPath, useLemma, useStem, new BM25Similarity());
			QueryEngine qeJM = new QueryEngine(indexPath, useLemma, useStem, new LMJelinekMercerSimilarity((float) 0.5));
			
			System.out.println("Running similarity 1.");
			String vmtfidf = qe.runQueries(queries);
			System.out.println("Running similarity 2.");
			String bool = qeBool.runQueries(queries);
			System.out.println("Running similarity 3.");
			String bm25 = qeBM25.runQueries(queries);
			System.out.println("Running similarity 4.");
			String jm = qeJM.runQueries(queries);
			
			System.out.println();
			System.out.println("Using the default Vector Space Model and tf/idf:\n" + vmtfidf);
			System.out.println("Using the Boolean Model:\n" + bool);
			System.out.println("Using the BM25 Model:\n" + bm25);
			System.out.println("Using the Jelinek Mercer Model:\n" + jm);
		}
	}
}