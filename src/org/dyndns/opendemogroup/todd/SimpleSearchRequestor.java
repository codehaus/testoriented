package org.dyndns.opendemogroup.todd;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;

/**
 * A simple extension of {@link SearchRequestor} that collects all searches in
 * a {@link List<SearchMatch>}, which can be obtained with 
 * {@link #getResults()}.
 */
public class SimpleSearchRequestor extends SearchRequestor {

	protected List<SearchMatch> results = new ArrayList<SearchMatch>();

	/**
	 * @see org.eclipse.jdt.core.search.SearchRequestor#acceptSearchMatch(org.eclipse.jdt.core.search.SearchMatch)
	 */
	@Override
	public void acceptSearchMatch(SearchMatch match) throws CoreException {
		results.add(match);
	}

	public List<SearchMatch> getResults() {
		return results;
	}

	
}
