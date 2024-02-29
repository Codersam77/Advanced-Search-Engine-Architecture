package index;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import comparators.TfIdfComparator;
import documents.DocumentId;

/**
 * A document indexer and search engine.
 * 
 * Documents are added to the engine one-by-one, and uniquely identified by a
 * DocumentId.
 *
 * Documents are internally represented as "terms", which are lowercased
 * versions of each word
 * in the document.
 * 
 * Queries for terms are also made on the lowercased version of the term. Terms
 * are
 * therefore case-insensitive.
 * 
 * Lookups for documents can be done by term, and the most relevant document(s)
 * to a specific term
 * (as computed by tf-idf) can also be retrieved.
 */
public class SearchEngine {

	private Map<String, Set<DocumentId>> mapping;
	private Map<DocumentId, Map<String, Integer>> count;

	public SearchEngine() {
		mapping = new HashMap<String, Set<DocumentId>>();
		count = new HashMap<DocumentId, Map<String, Integer>>();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Mapping:\n");
		sb.append(mapping.toString());
		sb.append("\ncount:\n");
		sb.append(count);
		return sb.toString();
	}

	/**
	 * Inserts a document into the search engine for later analysis and retrieval.
	 * 
	 * The document is uniquely identified by a documentId; attempts to re-insert
	 * the same
	 * document are ignored.
	 * 
	 * The document is supplied as a Reader; this method stores the document
	 * contents for
	 * later analysis and retrieval.
	 * 
	 * @param documentId
	 * @param reader
	 * @throws IOException iff the reader throws an exception
	 */
	public void addDocument(DocumentId documentId, Reader reader) throws IOException {
		Map<String, Integer> toAdd = new HashMap<>();
		try (Scanner scanner = new Scanner(reader)) {
			scanner.useDelimiter("\\W+");
			while (scanner.hasNext()) {
				String word = scanner.next();
				if (mapping.containsKey(word)) {
					mapping.get(word).add(documentId);
					if (toAdd.containsKey(word)) {
						toAdd.put(word, toAdd.get(word) + 1);
					} else {
						toAdd.put(word, 1);
					}
				} else {
					Set<DocumentId> newset = new HashSet<>();
					newset.add(documentId);
					mapping.put(word, newset);
					toAdd.put(word, 1);
				}
				// System.out.print(toAdd +"\n");
				// printAll();
			}

			count.put(documentId, toAdd);
		}
	}

	/**
	 * Returns the set of DocumentIds contained within the search engine that
	 * contain a given term.
	 * 
	 * @param term
	 * @return the set of DocumentIds that contain a given term
	 */
	public Set<DocumentId> indexLookup(String term) {
		for (String key : mapping.keySet()) {
			if (key.equalsIgnoreCase(term)) {
				return mapping.get(key);
			}
		}
		Set<DocumentId> empty = new HashSet<>();
		return empty;
	}

	/**
	 * Returns the term frequency of a term in a particular document.
	 * 
	 * The term frequency is number of times the term appears in a document.
	 * 
	 * @param term
	 * @return the term frequency of a term in a particular document
	 * @throws IllegalArgumentException if the documentId has not been added to the
	 *                                  engine
	 */
	public int termFrequency(DocumentId documentId, String term) throws IllegalArgumentException {
		// how do you use document id to find the contents
		Collection<Set<DocumentId>> values = mapping.values();
		boolean found = false;
		for (Set<DocumentId> current : values) {
			if (current.contains(documentId)) {
				found = true;
			}
		}
		if (found == false) {
			throw new IllegalArgumentException();
		}
		return count.get(documentId).getOrDefault(term, 0);
	}

	/**
	 * Returns the inverse document frequency of a term across all documents in the
	 * index.
	 * 
	 * For our purposes, IDF is defined as log ((1 + N) / (1 + M)) where
	 * N is the number of documents in total, and M
	 * is the number of documents where the term appears.
	 * 
	 * @param term
	 * @return the inverse document frequency of term
	 */
	public double inverseDocumentFrequency(String term) {
		System.out.println(this);
		int n;
		if (count.isEmpty()) {
			n = 0;
		} else {
			n = count.size();
		}
		int m;
		if (!mapping.containsKey(term)) {
			m = 0;
		} else {
			m = indexLookup(term).size();
		}
		return Math.log((1 + n * 1.0) / (1 + m));
	}

	/**
	 * Returns the tfidf score of a particular term for a particular document.
	 * 
	 * tfidf is the product of term frequency and inverse document frequency for the
	 * given term and document.
	 * 
	 * @param documentId
	 * @param term
	 * @return the tfidf of the the term/document
	 * @throws IllegalArgumentException if the documentId has not been added to the
	 *                                  engine
	 */
	public double tfIdf(DocumentId documentId, String term) throws IllegalArgumentException {
		int termfreq = termFrequency(documentId, term);
		double inverse = inverseDocumentFrequency(term);
		return termfreq * inverse;
	}

	/**
	 * Returns a sorted list of documents, most relevant to least relevant, for the
	 * given term.
	 * 
	 * A document with a larger tfidf score is more relevant than a document with a
	 * lower tfidf score.
	 * 
	 * Each document in the returned list must contain the term.
	 * 
	 * @param term
	 * @return a list of documents sorted in descending order by tfidf
	 */
	public List<DocumentId> relevanceLookup(String term) {
		List<DocumentId> toReturn = new ArrayList<>();
		if (mapping.keySet().contains(term)) {
			Set<DocumentId> values = mapping.get(term);
			for (DocumentId curr : values) {
				toReturn.add(curr);
			}
		} else {
			return toReturn;
		}
		Collections.sort(toReturn, new TfIdfComparator(this, term));
		return toReturn;
	}

	public void printAll() {
		System.out.print(count.entrySet() + "\n");
		System.out.print(count.keySet() + "\n");
		System.out.print("\n\n\n\n\n");
	}

	public static void main(String args[]) throws IOException {
		String DOCUMENT1 = "this is a a sample";
		DocumentId DOCUMENT1_ID = new DocumentId("DOCUMENT1");
		String DOCUMENT2 = "this is another another example example example";
		DocumentId DOCUMENT2_ID = new DocumentId("DOCUMENT2");
		SearchEngine searchEngine = new SearchEngine();

		searchEngine.addDocument(DOCUMENT1_ID, new StringReader(DOCUMENT1));
		searchEngine.addDocument(DOCUMENT2_ID, new StringReader(DOCUMENT2));

		// System.out.print(count.isEmpty()+"\n");
		// System.out.print(count.size()+"\n");
		// System.out.print(mapping.containsKey("sample")+"\n");
		// System.out.print(mapping.get("sample").size()+"\n");
		// searchEngine.printAll();

		// assertEquals(Math.log(3.0 / 2.0),
		// searchEngine.inverseDocumentFrequency("sample"), 0.0);
	}
}
