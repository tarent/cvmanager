package org.tarent.cvio.server.cv;

import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.tarent.cvio.server.CVIOConfiguration;

public class CVResourceTest {

	private static final String CV_ID = "THE-CV-ID";

	private static final String DEMO_JSON = "{'demo': 'data'}";

	@Mock
	CVDB dbMock;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testCreateCV() throws URISyntaxException {
		
		// given
		CVIOConfiguration aConfiguration = getConfiguration();
		CVResource aRessource = new CVResource(dbMock, aConfiguration);
		Mockito.when(dbMock.createCV(DEMO_JSON)).thenReturn(CV_ID);
		
		// when creating a new CV
		Response httpResponse = aRessource.createCV(DEMO_JSON);
		
		// then 
		// cv was created in the database 
		Mockito.verify(dbMock).createCV(DEMO_JSON);

		// and status code == created 
		assertEquals(201, httpResponse.getStatus());
		
		// and the location header
		locationHeaderIsValid(aConfiguration, httpResponse);		
	}
	
	@Test
	public void getCV() {
		// given
		CVIOConfiguration aConfiguration = getConfiguration();
		CVResource aRessource = new CVResource(dbMock, aConfiguration);
		Mockito.when(dbMock.getCVById(CV_ID)).thenReturn(DEMO_JSON);
		
		// when I request a CV
		String result = aRessource.getCV(CV_ID);
		
		// then
		assertEquals(DEMO_JSON, result);
	}
	
	@Test
	public void getAllCVs() {
		// given
		CVIOConfiguration aConfiguration = getConfiguration();
		CVResource aRessource = new CVResource(dbMock, aConfiguration);
		String[] someFields = {"demo"};
		
		List<Map<String, String>> demoResultData = demoResultData();		
		Mockito.when(dbMock.getAllCVs(someFields)).thenReturn(demoResultData);
		
		// when I request a CV
		List<Map<String,String>> result = aRessource.getCVs(Arrays.asList(someFields));
		
		// then
		// data matches:
		assertEquals("Alice", result.get(0).get("name"));		
		assertEquals("Bob", result.get(1).get("name"));
		
		// and entries have a valid uri ref
		assertNotNull(result.get(0).get("ref"));
		assertTrue( result.get(0).get("ref").startsWith(aConfiguration.getUriPrefix()) );

		assertNotNull(result.get(1).get("ref"));
		assertTrue( result.get(1).get("ref").startsWith(aConfiguration.getUriPrefix()) );
	}

	private List<Map<String, String>> demoResultData() {
		return new ArrayList<Map<String, String>>() {{;
			add( new HashMap<String, String>() {
				{
					put("demo", "data");
					put("name", "Alice");
				}
			});
			add( new HashMap<String, String>() {
				{
					put("demo", "data");
					put("name", "Bob");
				}
			});
		}};
	}

	private void locationHeaderIsValid(CVIOConfiguration aConfiguration, Response httpResponse) {
		String locationHeader = httpResponse.getMetadata().get("Location").get(0).toString();		
		assertTrue(  locationHeader.startsWith(aConfiguration.getUriPrefix())  );
		assertTrue(  locationHeader.endsWith(CV_ID) );
	}

	private CVIOConfiguration getConfiguration() {
		return new CVIOConfiguration() {
			@Override
			public String getUriPrefix() {
				return "http://example.org:8080/bla";
			}
		};
	}
}
