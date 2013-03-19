package org.fcrepo.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.fcrepo.jaxb.responses.access.ObjectDatastreams;
import org.fcrepo.jaxb.responses.access.ObjectProfile;
import org.fcrepo.jaxb.responses.management.DatastreamFixity;
import org.fcrepo.jaxb.responses.management.DatastreamProfile;

public class FedoraClient {

	public static final String PROPERTY_FCREPO_URL = "org.fcrepo.fixity.fcrepo.url"; 

    static final String PATH_OBJECT_PROFILE = "objects";
	static final String PATH_DATASTREAMS = "datastreams";
	static final String PATH_DATASTREAM_CONTENT = "content";
	static final String PATH_DATASTREAM_FIXITY = "fixity";

	private HttpClient client = new DefaultHttpClient();
	private URI fedoraUri;

	private Unmarshaller unmarshaller;

	public FedoraClient() {
		super();
	}
	
	FedoraClient(HttpClient client) {
		this.client = client;
	}

	public void setFedoraUri(String fedoraUri) {
		/* Check for a user set java property first to have it override the injected bean value */
		String uriProp = System.getProperty(PROPERTY_FCREPO_URL);
		if (uriProp == null){
			this.fedoraUri = URI.create(fedoraUri);
		}else{
			this.fedoraUri = URI.create(uriProp);
		}
	}
	
	URI getFedoraUri() {
		return this.fedoraUri;
	}
	
	JAXBContext getContext() throws JAXBException {
		return JAXBContext.newInstance(ObjectProfile.class, ObjectDatastreams.class, DatastreamProfile.class, DatastreamFixity.class);
	}

	Unmarshaller getUnmarshaller() throws JAXBException {
		if (unmarshaller == null) {
			unmarshaller = 
					getContext().createUnmarshaller();
		}
		return unmarshaller;
	}
	
	HttpGet getObjectProfileRequest(String id) {
		String[] parts = new String[]{fedoraUri.toASCIIString(), PATH_OBJECT_PROFILE, id};
		return new HttpGet(buildURI(parts));
	}

	public ObjectProfile getObjectProfile(final String id) throws IOException {
		final HttpGet get = getObjectProfileRequest(id);
		final HttpResponse resp = client.execute(get);
		if (resp.getStatusLine().getStatusCode() != 200) {
			throw new HttpResponseException(resp.getStatusLine().getStatusCode(), "Unable to fetch object profile from fedora: " + resp.getStatusLine().getReasonPhrase());
		}
		try {
			ObjectProfile profile = (ObjectProfile) this.getUnmarshaller().unmarshal(resp.getEntity().getContent());
			return profile;
		} catch (JAXBException e) {
			throw new IOException("Unable to deserialize object profile", e);
		} finally {
			IOUtils.closeQuietly(resp.getEntity().getContent());
		}
	}
	
	HttpGet getObjectDatastreamsRequest(String objectId) {
		// the empty string in the uri parts guarantees a terminal slash to match the jaxrs path
		String[] parts = new String[]{fedoraUri.toASCIIString(), PATH_OBJECT_PROFILE, objectId, PATH_DATASTREAMS, ""};
		return new HttpGet(buildURI(parts));
	}

	public ObjectDatastreams getObjectDatastreams(final String objectId) throws IOException {
		final HttpGet get = getObjectDatastreamsRequest(objectId);
		final HttpResponse resp = client.execute(get);
		if (resp.getStatusLine().getStatusCode() != 200) {
			throw new HttpResponseException(resp.getStatusLine().getStatusCode(), "Unable to fetch object datastreams from fedora: " + resp.getStatusLine().getReasonPhrase());
		}
		try {
			ObjectDatastreams datastreams = (ObjectDatastreams) this.getUnmarshaller().unmarshal(resp.getEntity().getContent());
			return datastreams;
		} catch (JAXBException e) {
			throw new IOException("Unable to deserialize object datastreams", e);
		} finally {
			IOUtils.closeQuietly(resp.getEntity().getContent());
		}
	}
	
	HttpGet getDatastreamProfileRequest(String objectId, String dsId) {
		String[] parts = new String[]{fedoraUri.toASCIIString(), PATH_OBJECT_PROFILE, objectId, PATH_DATASTREAMS, dsId};
		return new HttpGet(buildURI(parts));
	}

	public DatastreamProfile getDatastreamProfile(final String objectId, final String dsId) throws IOException {
		final HttpGet get = getDatastreamProfileRequest(objectId, dsId);
		final HttpResponse resp = client.execute(get);
		if (resp.getStatusLine().getStatusCode() != 200) {
			throw new HttpResponseException(resp.getStatusLine().getStatusCode(), "Unable to fetch datastream profile from fedora: " + resp.getStatusLine().getReasonPhrase());
		}
		try {
			DatastreamProfile ds = (DatastreamProfile) this.getUnmarshaller().unmarshal(resp.getEntity().getContent());
			return ds;
		} catch (JAXBException e) {
			throw new IOException("Unable to deserialize datastream profile", e);
		} finally {
			IOUtils.closeQuietly(resp.getEntity().getContent());
		}

	}
	
	HttpGet getDatastreamContentRequest(String objectId, String dsId) {
		String[] parts = new String[]{fedoraUri.toASCIIString(), PATH_OBJECT_PROFILE, objectId, PATH_DATASTREAMS, dsId,
				PATH_DATASTREAM_CONTENT};
		return new HttpGet(buildURI(parts));
	}

	public InputStream getDatastreamContent(final String objectId, final String dsId) throws IOException {
		final HttpGet get = getDatastreamContentRequest(objectId, dsId);
		final HttpResponse resp = client.execute(get);
		if (resp.getStatusLine().getStatusCode() != 200) {
			resp.getEntity().getContent().close();
			throw new HttpResponseException(resp.getStatusLine().getStatusCode(), "Unable to fetch datastream content from fedora: " + resp.getStatusLine().getReasonPhrase());
		}
		return resp.getEntity().getContent();
	}
	
	HttpGet getDatastreamFixityRequest(String objectId, String dsId) {
		String[] parts = new String[]{fedoraUri.toASCIIString(), PATH_OBJECT_PROFILE, objectId, PATH_DATASTREAMS, dsId,
				PATH_DATASTREAM_FIXITY};
		return new HttpGet(buildURI(parts));
	}
	
	public DatastreamFixity getDatastreamFixity(final String objectId, final String dsId) throws IOException {
		final HttpGet get = getDatastreamFixityRequest(objectId, dsId);
		final HttpResponse resp = client.execute(get);
		if (resp.getStatusLine().getStatusCode() != 200) {
			throw new HttpResponseException(resp.getStatusLine().getStatusCode(), "Unable to fetch datastream fixity from fedora: " + resp.getStatusLine().getReasonPhrase());
		}
		try {
			DatastreamFixity df = (DatastreamFixity) this.getUnmarshaller().unmarshal(resp.getEntity().getContent());
			return df;
		} catch (JAXBException e) {
			throw new IOException("Unable to deserialize datastream fixity", e);
		} finally {
			IOUtils.closeQuietly(resp.getEntity().getContent());
		}
	}
	
	HttpGet getPidsRequest() {
		String[] parts = new String[]{fedoraUri.toASCIIString(), PATH_OBJECT_PROFILE};
		return new HttpGet(buildURI(parts));
	}

	public List<String> getPids() throws IOException {
		final HttpGet get = getPidsRequest();
		final HttpResponse resp = client.execute(get);
		if (resp.getStatusLine().getStatusCode() != 200) {
			resp.getEntity().getContent().close();
			throw new HttpResponseException(resp.getStatusLine().getStatusCode(), "Unable to fetch object list from fedora: " + resp.getStatusLine().getReasonPhrase());
		}
		String data = IOUtils.toString(resp.getEntity().getContent());

		/* fedora4 returns a JSON array of strings in square brackets 
		/* e.g. [pid1,pid2] */ 
		/* so strip the square brackets */
		/* and return the split */
		if (data.length() == 2){
			/* the empty "[]" set so we return null */
			return null;
		}
		data = data.substring(1, data.length() - 1);
		return Arrays.asList(data.split(","));
	}
	
	private String buildURI(String [] components) {
		if (components == null) return "";
		int parts = components.length;
		if (parts == 0) return "";
		if (parts == 1) return components[0];
		int len = 0;
		for (int i=0; i<parts;i++) len += components[i].length();
		len += (parts -1);
		StringBuffer result = new StringBuffer(len);
		result.append(components[0]);
		for (int i=1; i<parts;i++) {
			result.append('/');
			result.append(components[i]);
		}
		return result.toString();
	}
}
