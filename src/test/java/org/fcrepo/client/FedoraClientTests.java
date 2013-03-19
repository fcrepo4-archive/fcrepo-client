package org.fcrepo.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.mockito.Mockito.*;

import static org.fcrepo.client.GetMatcher.getLike;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.fcrepo.jaxb.responses.access.ObjectDatastreams;
import org.fcrepo.jaxb.responses.access.ObjectProfile;
import org.fcrepo.jaxb.responses.management.DatastreamFixity;
import org.fcrepo.jaxb.responses.management.DatastreamProfile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.Equals;

public class FedoraClientTests {
	
	private static final String MOCK_URI = "http://testhost.info:8080/fcrepo";
	
	private HttpClient mockClient;
	private FedoraClient testObj;
	
	@Before
	public void setUp(){
		mockClient = mock(HttpClient.class);
		testObj = new FedoraClient(mockClient);
		testObj.setFedoraUri(MOCK_URI);
	}
	
	@After
	public void tearDown() {
		testObj = null;
		mockClient = null;
	}

	@Test
	public void testContext() throws JAXBException, InstantiationException, IllegalAccessException{
		JAXBContext context = testObj.getContext();
		Class [] expected = new Class[]{ObjectProfile.class, ObjectDatastreams.class, DatastreamProfile.class, DatastreamFixity.class};
		Marshaller m = context.createMarshaller();
		for (Class c:expected){
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			m.marshal(c.newInstance(), bos);
			byte [] bytes = bos.toByteArray();
			assertTrue(bytes.length > 0);
		}

		try {
			m.marshal(testObj, new ByteArrayOutputStream());
			fail("Unexpected class marshalled by JAXB context");
		} catch (JAXBException e) {
			// this should fail
		}
	}
	
	@Test
	public void testUnmarshaller() throws JAXBException {
		assertNotNull(testObj.getUnmarshaller());
	}
	
	@Test
	public void testUriSetter() {
		URI expected = URI.create(MOCK_URI);
		assertEquals(expected, testObj.getFedoraUri());
	}
	
	@Test
	public void testObjectProfile() throws ClientProtocolException, IOException {
		HttpResponse mockResponse = mock(HttpResponse.class);
		StatusLine mockStatus = mock(StatusLine.class);
		URI fedoraUri = testObj.getFedoraUri();
		String id = "test:object1";
		HttpGet expected = new HttpGet(fedoraUri.toASCIIString()
				+ FedoraClient.PATH_OBJECT_PROFILE + id);
		
		when(mockClient.execute(getLike(expected))).thenReturn(mockResponse);
		when(mockResponse.getStatusLine()).thenReturn(mockStatus);
		HttpEntity mockEntity = mock(HttpEntity.class);
		when(mockEntity.getContent()).thenReturn(FedoraClientTests.class.getResourceAsStream("good-profile.xml"));
		when(mockResponse.getEntity()).thenReturn(mockEntity);
		when(mockStatus.getStatusCode()).thenReturn(200);
		testObj.getObjectProfile(id);
		verify(mockClient).execute(
				getLike(expected));
	}
}
