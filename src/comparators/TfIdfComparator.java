package comparators;

import documents.DocumentId;
import index.SearchEngine;

import java.util.Comparator;

/**
 * Compare two documents in a search engine by tf-idf using a given term.
 * It breaks ties by using the lexicographic ordering of the document IDs (that
 * is, by using
 * o1.id.compareTo(o2.id)).
 * 
 * 
 *
 */
public class TfIdfComparator implements Comparator<DocumentId> {
	private final SearchEngine searchEngine;
	private final String term;

	public TfIdfComparator(SearchEngine searchEngine, String term) {
		this.searchEngine = searchEngine;
		this.term = term;
	}

	@Override
	public int compare(DocumentId o1, DocumentId o2) {
		if (searchEngine.tfIdf(o1, term) > searchEngine.tfIdf(o2, term)) {
			return -1;
		} else if (searchEngine.tfIdf(o2, term) > searchEngine.tfIdf(o1, term)) {
			return 1;
		} else {
			return o1.id.compareTo(o2.id);
		}
	}
}
