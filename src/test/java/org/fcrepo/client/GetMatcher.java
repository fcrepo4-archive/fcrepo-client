package org.fcrepo.client;

import static org.mockito.Mockito.*;

import org.apache.http.client.methods.HttpGet;
import org.mockito.ArgumentMatcher;

public class GetMatcher extends ArgumentMatcher<HttpGet> {
	
	private final HttpGet expected;
	
	private GetMatcher(HttpGet expected) {
		this.expected = expected;
	}

	@Override
	public boolean matches(Object argument) {
		return expected.getURI().toString().equals(((HttpGet)argument).getURI().toString());
	}
	
	public static HttpGet getLike(HttpGet expected) {
		return argThat(new GetMatcher(expected));
	}

}
