package edu.arizona.cs;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class TestLemma {
	// File Settings
	static String queries = "questions.txt";
	static String indexPath = "index/lemma";
	
	// Settings
	// ONLY ONE OF THE FOLLOWING SHOULD BE TRUE AT A TIME
	// if both are true it will default to useLemma
	static boolean useLemma = true;
	static boolean useStem = false;

	@Test
	public void test() {
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
		System.out.println("For the index at " + indexPath);
		System.out.println("Using the default Vector Space Model and tf/idf:\n" + vmtfidf);
		System.out.println("Using the Boolean Model:\n" + bool);
		System.out.println("Using the BM25 Model:\n" + bm25);
		System.out.println("Using the Jelinek Mercer Model:\n" + jm);
	}
}



