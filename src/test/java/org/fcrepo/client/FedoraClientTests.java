package org.fcrepo.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.fcrepo.jaxb.responses.access.ObjectDatastreams;
import org.fcrepo.jaxb.responses.access.ObjectProfile;
import org.fcrepo.jaxb.responses.management.DatastreamFixity;
import org.fcrepo.jaxb.responses.management.DatastreamProfile;
import org.junit.Before;
import org.junit.Test;

public class FedoraClientTests {
	
	private FedoraClient testObj;
	
	@Before
	public void setUp(){
		testObj = new FedoraClient();
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
}
