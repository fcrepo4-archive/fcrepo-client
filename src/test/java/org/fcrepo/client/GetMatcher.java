package org.fcrepo.client;

import static org.mockito.Mockito.*;

import org.apache.http.client.methods.HttpGet;
import org.mockito.ArgumentMatcher;

public class GetMatcher extends ArgumentMatcher<HttpGet> {
	
	private final HttpGet expected;
	private final boolean match;
	
	private GetMatcher(HttpGet expected, boolean match) {
		this.expected = expected;
		this.match = match;
	}

	@Override
	public boolean matches(Object argument) {
		boolean result = (argument != null) 
				? expected.getURI().toString().equals(((HttpGet)argument).getURI().toString())
				: false;
		return match == result;
	}
	
	public static HttpGet getLike(HttpGet expected) {
		return argThat(new GetMatcher(expected, true));
	}

	public static HttpGet getNotLike(HttpGet expected) {
		return argThat(new GetMatcher(expected, false));
	}

}
