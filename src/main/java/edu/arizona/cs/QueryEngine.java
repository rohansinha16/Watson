package edu.arizona.cs;

import org.apache.lucene.analysis.standard.StandardAnalyzer;

import java.io.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.tartarus.snowball.ext.PorterStemmer;

import edu.stanford.nlp.simple.Sentence;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

// used to test indexes with queries
public class QueryEngine {
	
	// set to true for additional printed output, used for debugging
	public boolean test;
	
	// Settings
	private String indexPath;
	private boolean useLemma; // use query lemmas
	private boolean useStem; // use query stems
	private Optional<Similarity> s;
	
	// Constructor for QueryEngine settings
	public QueryEngine(String indexPath, boolean useLemma, boolean useStem) {
		this.indexPath = indexPath;
		this.useLemma = useLemma;
		this.useStem = useStem;
		this.test = false;
		this.s = Optional.empty();
	}

	// overloaded for optional Similarity parameter
	public QueryEngine(String indexPath, boolean useLemma, boolean useStem, Similarity s) {
		this(indexPath, useLemma, useStem);
		this.s = Optional.of(s);
	}
	
	// prints text if test setting is true
	public void printt(String text) {
		if(test) System.out.println(text);
	}
	
	/* runQueries
	 * @param String filename - name of the file with queries
	 * @return String result - string including the P@1 and MRR scores
	 * 
	 * runs all the queries in the file filename with queries in the form of:
	 * |============|
	 * |CATEGORY    |
	 * |query       |
	 * |answer      |
	 * |============|
	 * 
	 * Calculates P@1 and MMR as well
	 */
	public String runQueries(String filename) {
		String result = "";
		// open questions file
		File file = new File(filename);
		try(Scanner scanner = new Scanner(file)) {
			String category = "", query = "", answer = "";
			int total = 0, correct = 0, i = 0;
			double mmr = 0;
			// for each line in the file
			while(scanner.hasNextLine()) {
				if(i % 4 == 0) category = scanner.nextLine();
				else if(i % 4 == 1) query = scanner.nextLine();
				else if(i % 4 == 2) answer = scanner.nextLine();
				else {
					// end of query
					scanner.nextLine();
					printt(category + "\n" + query + "\n" + answer);
					
					// call runQuery on the query
					List<ResultClass> ans = runQuery(category + " " + query);
					// correct answer
					if(ans.get(0).DocName.get("title").equals(answer)) {
						correct++;
						printt("Correct");
						mmr += 1.0;
					}
					// incorrect answer
					else {
						printt("Wrong Answer");
						for (int j = 0; j < ans.size(); j++) {
							if(ans.get(j).DocName.get("title").equals(answer)) {
								mmr += (double)1/(j+1);
								break;
							}
						}
					}
					total++;
					printt("");
				}
				i++;
			}
			result = "\tP@1: " + correct + "/" + total + " = " + (double)correct/total + "\n\tMMR: " + (double)mmr/total;
			printt(result);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/* runQuery runs the query on the index at indexPath, returns top 10 results, and scores using the Optional<Similarity> s
	 * @param String query - the query to test
	 * @return List<ResultClass> - top 10 hits
	 */
	public List<ResultClass> runQuery(String query) {
		List<ResultClass> ans = new ArrayList<ResultClass>();
		// 0. Specify the analyzer for tokenizing text.
		//    The same analyzer should be used for indexing and searching
		StandardAnalyzer analyzer = new StandardAnalyzer();
		// get lemma/stem/none version of query
		query = convertQuery(query);
		printt(query);
		try {
			// 1. create the index
			Directory index = FSDirectory.open(new File(indexPath).toPath());

			// 2. create query, the "text" arg specifies the field to use
			Query q = new QueryParser("text", analyzer).parse(query);

			// 3. search
			int hitsPerPage = 10;
			IndexReader reader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			// set scoring function
			if(s.isPresent()) {
				searcher.setSimilarity(s.get());
			}
			TopDocs docs = searcher.search(q, hitsPerPage);
			ScoreDoc[] hits = docs.scoreDocs;
			// printt("Numdocs: " + reader.numDocs());
			printt("docid\t\tscore");
			
			// 4. results
			for(int i = 0; i < hits.length; i++) {
				ResultClass r = new ResultClass();
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);
				r.DocName = d;
				r.doc_score = hits[i].score;
				printt(d.get("title") + "\t\t" + hits[i].score);
				ans.add(r);
			}
			// reader can only be closed when there
			// is no need to access the documents any more.
			reader.close();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ans;
	}
	
	// converts the given query to lemma/stem/none
	private String convertQuery(String query) {
		// get (converted) query
		StringBuilder qBuilder = new StringBuilder();
		Sentence sent = new Sentence(query.toLowerCase());
		// convert query to lemmas
		if(useLemma) {
			for(String lem : sent.lemmas()) {
				qBuilder.append(lem + " ");
			}
		}
		// convert query to stems
		else if(useStem) {
			for(String word : sent.lemmas()) {
				qBuilder.append(getStem(word) + " ");
			}
		}
		else {
			for(String word : sent.lemmas()) {
				qBuilder.append(word + " ");
			}
		}
		return qBuilder.toString();
	}
	
	// gets the stem of term
	private String getStem(String term) {
		PorterStemmer stemmer = new PorterStemmer();
		stemmer.setCurrent(term);
		stemmer.stem();
		return stemmer.getCurrent();
	}
}