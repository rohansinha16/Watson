package edu.arizona.cs;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Stack;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.tartarus.snowball.ext.PorterStemmer;

import edu.stanford.nlp.simple.Sentence;

// used to create indexes
public class IndexCreator {
	
	// set to true for additional printed output, used for debugging
	public boolean test;
	
	// Settings
	private String indexPath;
	private boolean removeTPL;
	private boolean useLemma; // index lemmas
	private boolean useStem; // index stems
	
	// Constructor for index creation settings
	public IndexCreator(String indexPath, boolean useLemma, boolean useStem, boolean removeTPL) {
		this.indexPath = indexPath;
		this.useLemma = useLemma;
		this.useStem = useStem;
		this.removeTPL = removeTPL;
		this.test = false;
	}
	
	// prints text if test setting is true
	private void printt(String text) {
		if(test) System.out.println(text);
	}
	
	/* Reads all files in a given directory and indexes them
	 * @param String directory - directory of files to parse
	 */
	public void parseFiles(String directory) throws java.io.FileNotFoundException,java.io.IOException {
		// 0. Specify the analyzer for tokenizing text.
		//    The same analyzer should be used for indexing and searching
		StandardAnalyzer analyzer = new StandardAnalyzer();

		// 1. create the index
		Directory index = FSDirectory.open(new File(indexPath).toPath());
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		IndexWriter w = new IndexWriter(index, config);

		// Get directory
		File dir = new File(directory);
		// For files in directory
		int i = 0;
		for(String file : dir.list()) {
			System.out.println("Processing document " + i + ": " + file);
			// parse each file
			parseFile(directory + "/" + file, w);
			i++;
		}
		w.close();
		index.close();
	}
	
	/* Reads a file and creates an index
	 * @param String filename - the file to index
	 * @param IndexWriter w - the index writer
	 */
	public void parseFile(String filename, IndexWriter w) {
		String title = ""; // title field
		String categories = ""; // category field
		StringBuilder result = new StringBuilder(); // text field
		File file = new File(filename); // open file
		Stack<Boolean> tpl = new Stack<Boolean>(); // stack for tpl tags
		
		// create scanner and loop through lines
		try (Scanner scanner = new Scanner(file)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				int len = line.length();
				// titles
				if(line.length() > 4 && line.substring(0, 2).equals("[[") && line.substring(len - 2, len).equals("]]")) {
					
					// check if blank or attachment
					if(!title.equals("") && !title.contains("File:") && !title.contains("Image:")) {
						// add previous page
						// empty result field
						addDoc(w, title, categories.trim(), result.toString().trim());
					}
					
					// start new page
					title = line.substring(2, len - 2);
					result = new StringBuilder();
					// guarentee the tpl stack is emtpy before moving on to the next document
					if(!tpl.empty()) {
						printt("Tpl stack not empty at end of: " + title);
						tpl.clear();
					}
				}
				// categories
				else if(line.indexOf("CATEGORIES:") == 0) {
					categories = line.substring(12);
				}
				// headers
				else if(line.length() > 2 && line.charAt(0) == '=' && line.charAt(len - 1) == '=') {
					while(line.length() > 2 && line.charAt(0) == '=' && line.charAt(line.length() - 1) == '=') {
						line = line.substring(1, line.length() - 1);
					}
					if(!line.equals("See also") && !line.equals("References") && !line.equals("Further reading") && !line.equals("External links")) {
						result.append(line + " ");
					}
					//printt(line);
				}
				else if(removeTPL){
					tplRemove(line, tpl, result);
				}
				else {
					result.append(line + " ");
				}
			}
			// add last doc
			addDoc(w, title, categories, result.toString().trim());
			// printt(title + "\nCat: " +  categories + "\n " + result.toString().trim());
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/* tplRemove, used to remove tpl tags
	 * @param String line - the current line being processed
	 * @param Stack<Boolean> tpl - stack to keep track of open and closing tpl tags
	 * @param StringBuilder result - all parts of line not enclosed in tpl tags will be added to result
	 */
	private void tplRemove(String line, Stack<Boolean> tpl, StringBuilder result) {
		// remove tpl tag and contents
		
		// split by tpl tags
		String[] splitByTpl = line.split("\\[tpl\\]");
		for(int i = 0; i < splitByTpl.length; i++) {
			// encountered a tpl tag
			if(i > 0) {
				tpl.push(true);
			}
			// split by /tpl tags
			String[] splitByEndTpl = splitByTpl[i].split("\\[/tpl\\]");
			for(int j = 0; j < splitByEndTpl.length; j++) {
				// encountered a /tpl tag
				if(j > 0) {
					if(!tpl.empty()) {
						tpl.pop();
					}
					else {
						printt("Encountered [/tpl] when tpl stack is empty.");
					}
				}
				// if tpl stack is empty then not in tpl tag
				if(tpl.empty()) {
					result.append(splitByEndTpl[j] + " ");
				}
				// else in tpl tag, do not include
			}
			// get /tpl tags at end of current section of line
			int endTplAtEnd = numAtEnd(splitByTpl[i], "[/tpl]");
			for(int k = 0; k < endTplAtEnd; k++) {
				// encountered a /tpl tag
				if(!tpl.empty()) {
					tpl.pop();
				}
				else {
					printt("Encountered [/tpl] when tpl stack is empty.");
				}
			}
		}
		// get tpl tags at end of current line
		int tplAtEnd = numAtEnd(line, "[tpl]");
		// encountered a tpl tag
		for(int k = 0; k < tplAtEnd; k++) {
			tpl.push(true);
		}
	}
	
	/* numAtEnd
	 * @param String src - the string to check
	 * @param String token - the token to look for at the end
	 * @return int - the number of times token appears at the end of String
	 * Used for tpl tags at ends of lines as split doesn't do the trick
	 */
	private int numAtEnd(String src, String token) {
		int i = 0;
		// src is larger than token and end of src = token
		while(src.length() >= token.length() && src.substring(src.length() - token.length()).equals(token)) {
			i++;
			src = src.substring(0, src.length() - token.length()); // remove token from end
		}
		return i;
	}
	
	/* addDoc
	 * @param IndexWriter w - the index writer
	 * @param String title - title of doc
	 * @param String categories - categories of doc
	 * @param String text - content of doc
	 * Creates a new document for the parameters and adds it to the index. Also takes care of
	 * stemming/lemmatization based on the global booleans passed to and set by the constructor
	 */
	private void addDoc(IndexWriter w, String title, String categories, String text) throws IOException {
		Document doc = new Document();
		if(text.equals("")) {
			// printt(title + " had an empty result\nCat: " +  categories );
			text = ".";
		}
		// empty categories field
		if(categories.equals("")) {
			categories = ".";
		}
		StringBuilder cat = new StringBuilder("");
		StringBuilder txt = new StringBuilder("");
		if(useLemma) {
			// append lemmas
			convertLemma(cat, categories);
			convertLemma(txt, text);
		}
		else if(useStem) {
			// append stems
			convertStem(cat, categories);
			convertStem(txt, text);
		}
		else {
			// neither
			cat.append(categories.toLowerCase());
			txt.append(text.toLowerCase());
		}
		// use a string field for title because we don't want it tokenized
		doc.add(new StringField("title", title, Field.Store.YES));
		doc.add(new TextField("categories", cat.toString().trim(), Field.Store.YES));
		// include title and categories in text field
		doc.add(new TextField("text", title + " " + cat.toString().trim() + " " + txt.toString().trim(), Field.Store.YES)); 
		w.addDocument(doc);
	}
	
	// adds lemma version of s to b
	private void convertLemma(StringBuilder b, String s) {
		for (String lemma: new Sentence(s.toLowerCase()).lemmas()){
			b.append(lemma + " ");
		}
	}
	
	// adds stemmed version of s to b
	private void convertStem(StringBuilder b, String s) {
		for(String word: new Sentence(s.toLowerCase()).words()) {
			b.append(getStem(word) + " ");
		}
	}
	
	// gets stem of term
	private String getStem(String term) {
		PorterStemmer stemmer = new PorterStemmer();
		stemmer.setCurrent(term);
		stemmer.stem();
		return stemmer.getCurrent();
	}
}