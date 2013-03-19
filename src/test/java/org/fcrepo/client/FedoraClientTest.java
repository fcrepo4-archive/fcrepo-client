package org.fcrepo.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.mockito.Mockito.*;

import static org.fcrepo.client.GetMatcher.getLike;
import static org.fcrepo.client.GetMatcher.getNotLike;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.core.Request;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;

import org.fcrepo.api.FedoraDatastreams;
import org.fcrepo.api.FedoraObjects;

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
	public void testObjectProfileRequest() {
		HashMap<String, String> pathParams = new HashMap<String, String>(1);
		pathParams.put("pid", "object");
		HttpGet expected = getRequest(FedoraObjects.class, "getObject", pathParams, new Class[]{String.class});
		HttpGet actual = testObj.getObjectProfileRequest("object");
		assertEquals(expected.getURI(),actual.getURI());
	}

	
	@Test
	public void testObjectProfile() throws ClientProtocolException, IOException {
		String id = "test:object1";
		HttpGet expected = testObj.getObjectProfileRequest(id);
		HttpResponse good = mock(HttpResponse.class);
		mock200(good, "good-profile.xml");
		when(mockClient.execute(getLike(expected))).thenReturn(good);
		testObj.getObjectProfile(id);
		verify(mockClient).execute(getLike(expected));
		HttpResponse bad = mock(HttpResponse.class);
		mock404(bad);
		when(mockClient.execute(getNotLike(expected))).thenReturn(bad);
		try{
			testObj.getObjectProfile("fail:object");
		} catch (HttpResponseException e) {
			if (!(e.getStatusCode() == 404)){
				throw e;
			}
		}
	}
	
	@Test
	public void testObjectDatastreamsRequest() {
		HashMap<String, String> pathParams = new HashMap<String, String>(1);
		pathParams.put("pid", "object");
		HttpGet expected = getRequest(FedoraDatastreams.class, "getDatastreams", pathParams, new Class[]{String.class});
		HttpGet actual = testObj.getObjectDatastreamsRequest("object");
		assertEquals(expected.getURI(),actual.getURI());
	}
	
	@Test
	public void testObjectDatastreams() throws IllegalStateException, IOException {
		String id = "test:object1";
		HttpGet expected = testObj.getObjectDatastreamsRequest(id);
		HttpResponse good = mock(HttpResponse.class);
		mock200(good, "good-datastreams.xml");
		when(mockClient.execute(getLike(expected))).thenReturn(good);
		testObj.getObjectDatastreams(id);
		verify(mockClient).execute(getLike(expected));

        HttpResponse bad = mock(HttpResponse.class);
        mock404(bad);
		when(mockClient.execute(getNotLike(expected))).thenReturn(bad);
		try{
			testObj.getObjectDatastreams("fail:object");
		} catch (HttpResponseException e) {
			if (!(e.getStatusCode() == 404)){
				throw e;
			}
		}
	}
	
	@Test
	public void testDatastreamProfileRequest() {
		HashMap<String, String> pathParams = new HashMap<String, String>(2);
		pathParams.put("pid", "object");
		pathParams.put("dsid", "dsid");
		HttpGet expected = getRequest(FedoraDatastreams.class, "getDatastream", pathParams, new Class[]{String.class, String.class});
		HttpGet actual = testObj.getDatastreamProfileRequest("object", "dsid");
		assertEquals(expected.getURI(),actual.getURI());
	}
	
	@Test
	public void testDatastreamProfile() throws ClientProtocolException, IOException{
		String id = "test:object1";
		String dsid = "test:ds1";
		HttpGet expected = testObj.getDatastreamProfileRequest(id, dsid);
		HttpResponse good = mock(HttpResponse.class);
		mock200(good, "good-datastream-profile.xml");
		when(mockClient.execute(getLike(expected))).thenReturn(good);
		testObj.getDatastreamProfile(id, dsid);
		verify(mockClient).execute(getLike(expected));

        HttpResponse bad = mock(HttpResponse.class);
        mock404(bad);
		when(mockClient.execute(getNotLike(expected))).thenReturn(bad);
		try{
			testObj.getDatastreamProfile("fail:object", dsid);
		} catch (HttpResponseException e) {
			if (!(e.getStatusCode() == 404)){
				throw e;
			}
		}
		try{
			testObj.getDatastreamProfile(id, "fail:dsid");
		} catch (HttpResponseException e) {
			if (!(e.getStatusCode() == 404)){
				throw e;
			}
		}
	}
	
	@Test
	public void testDatastreamFixityRequest(){
		HashMap<String, String> pathParams = new HashMap<String, String>(2);
		pathParams.put("pid", "object");
		pathParams.put("dsid", "dsid");
		HttpGet expected = getRequest(FedoraDatastreams.class, "getDatastreamFixity", pathParams, new Class[]{String.class, String.class});
		HttpGet actual = testObj.getDatastreamFixityRequest("object", "dsid");
		assertEquals(expected.getURI(),actual.getURI());
	}
	
	@Test
	public void testDatastreamFixity() throws IOException{
		String id = "test:object1";
		String dsid = "test:ds1";
		HttpGet expected = testObj.getDatastreamFixityRequest(id, dsid);
		HttpResponse good = mock(HttpResponse.class);
		mock200(good,"good-datastream-fixity.xml");
		when(mockClient.execute(getLike(expected))).thenReturn(good);
		testObj.getDatastreamFixity(id, dsid);
		verify(mockClient).execute(getLike(expected));
		
        HttpResponse bad = mock(HttpResponse.class);
        mock404(bad);
		when(mockClient.execute(getNotLike(expected))).thenReturn(bad);
		try{
			testObj.getDatastreamFixity("fail:object", dsid);
		} catch (HttpResponseException e) {
			if (!(e.getStatusCode() == 404)){
				throw e;
			}
		}
		try{
			testObj.getDatastreamFixity(id, "fail:dsid");
		} catch (HttpResponseException e) {
			if (!(e.getStatusCode() == 404)){
				throw e;
			}
		}
	}
	
	@Test
	public void testDatastreamContentRequest(){
		HashMap<String, String> pathParams = new HashMap<String, String>(2);
		pathParams.put("pid", "object");
		pathParams.put("dsid", "dsid");
		HttpGet expected = getRequest(FedoraDatastreams.class, "getDatastreamContent", pathParams, new Class[]{String.class, String.class, Request.class});
		HttpGet actual = testObj.getDatastreamContentRequest("object", "dsid");
		assertEquals(expected.getURI(),actual.getURI());
	}

	@Test
	public void testDatastreamContent() throws ClientProtocolException, IOException{
		String id = "test:object1";
		String dsid = "test:ds1";
		HttpGet expected = testObj.getDatastreamContentRequest(id, dsid);
		HttpResponse good = mock(HttpResponse.class);
		mock200(good,"good-datastream-fixity.xml");
		when(mockClient.execute(getLike(expected))).thenReturn(good);
		testObj.getDatastreamContent(id, dsid);
		verify(mockClient).execute(getLike(expected));
		
        HttpResponse bad = mock(HttpResponse.class);
        mock404(bad);
		when(mockClient.execute(getNotLike(expected))).thenReturn(bad);
		try{
			testObj.getDatastreamContent("fail:object", dsid);
		} catch (HttpResponseException e) {
			if (!(e.getStatusCode() == 404)){
				throw e;
			}
		}
		try{
			testObj.getDatastreamContent(id, "fail:dsid");
		} catch (HttpResponseException e) {
			if (!(e.getStatusCode() == 404)){
				throw e;
			}
		}	}

	@Test
	public void testPidsRequest(){
		HashMap<String, String> pathParams = new HashMap<String, String>(0);
		HttpGet expected = getRequest(FedoraObjects.class, "getObjects", pathParams, new Class[]{});
		HttpGet actual = testObj.getPidsRequest();
		assertEquals(expected.getURI(),actual.getURI());
	}
	
	@Test
	public void testPids() throws ClientProtocolException, IOException{
		HttpGet expected = testObj.getPidsRequest();
		HttpResponse good = mock(HttpResponse.class);
		mock200(good,"good-pids.txt");
		when(mockClient.execute(getLike(expected))).thenReturn(good);
		List<String> pids = testObj.getPids();
		verify(mockClient).execute(getLike(expected));
		assertTrue(pids.contains("test:object1"));
	}
	
	HttpGet getRequest(Class c, String method, Map<String, String> pathParams, Class<?>... parameterTypes) {
		Method m = null;
		try {
			m = c.getDeclaredMethod(method, parameterTypes);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			fail(e.toString());
		} catch (SecurityException e) {
			e.printStackTrace();
			fail(e.toString());
		}
		String path = getJaxrsPath(c, m);
		for (String paramName: pathParams.keySet()) {
			path = path.replaceAll("\\{" + paramName + "\\}", pathParams.get(paramName));
		}
		String uri = MOCK_URI + path;
		return new HttpGet(uri);
	}
	
	static void mock200(HttpResponse response, String entityResource) throws IllegalStateException, IOException {
		StatusLine status200 = mock(StatusLine.class);
		when(response.getStatusLine()).thenReturn(status200);
		HttpEntity mockEntity = mock(HttpEntity.class);
		if (entityResource != null) {
		  when(mockEntity.getContent()).thenReturn(FedoraClientTests.class.getResourceAsStream(entityResource));
		} else {
			  when(mockEntity.getContent()).thenReturn(mock(InputStream.class));
		}
		when(response.getEntity()).thenReturn(mockEntity);
		when(status200.getStatusCode()).thenReturn(200);
	}
	
	static void mock404(HttpResponse response) throws IllegalStateException, IOException {
		StatusLine status404 = mock(StatusLine.class);
		when(status404.getStatusCode()).thenReturn(404);
		when(response.getStatusLine()).thenReturn(status404);
		HttpEntity mockEntity = mock(HttpEntity.class);
		when(mockEntity.getContent()).thenReturn(mock(InputStream.class));
		when(response.getEntity()).thenReturn(mockEntity);
	}
	
	static String getJaxrsPath(Class c, Method m) {
		Path path = (Path)c.getAnnotation(Path.class);
		Path mPath = m.getAnnotation(Path.class);
		String result = "";
		if (path != null) result = path.value();
		if (mPath != null) result += mPath.value();
		return result;
	}
}
