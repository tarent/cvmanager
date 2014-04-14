package org.tarent.cvio.server.doc;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import static org.junit.Assert.*;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.tarent.cvio.server.ConfigurationHelper;
import org.tarent.cvio.server.common.CVIOConfiguration;
import org.tarent.cvio.server.cv.CVDB;
import org.tarent.cvio.server.cv.CVResource;
import org.tarent.cvio.server.skill.Skill;
import org.tarent.cvio.server.skill.SkillDB;

public class DocumentResourceTest {

	private static final String CV_ID = "THE-CV-ID";
	private static final String DEMO_JSON = "{'demo': 'data'}";
    private static Map<String, Object> DEMO_CV_BY_MAP = null;
    private List<Skill> skills; 
	
	@Mock
	CVDB cvdb;
	
	@Mock
	SkillDB skilldb;
	
	CVIODocumentGenerator docGen;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		docGen = new CVIODocumentGenerator();
		createTestData();
	}

	private void createTestData() {
		DEMO_CV_BY_MAP = new HashMap<String, Object>();
		
		DEMO_CV_BY_MAP.put("familyName", "Mustermann");
		DEMO_CV_BY_MAP.put("givenName", "Max");
		DEMO_CV_BY_MAP.put("locality", "Musterstadt");
		DEMO_CV_BY_MAP.put("placeOfBirth", "Musterstadt");
		DEMO_CV_BY_MAP.put("familyStatus", "ledig / keine Kinder");
		DEMO_CV_BY_MAP.put("languages", "Deutsch, Englisch");

		HashMap<String, Object> skillMap = new HashMap<String, Object>();
		skillMap.put("9j6XxJx9QK-6Ky1RcNezOA", "1");
		skillMap.put("QToA8FNSSgWSPM7d2irwDA", "2");
		skillMap.put("rytxjW42RgywXEdjRY26NA", "3");
		
		DEMO_CV_BY_MAP.put("skills", skillMap);

		skills = new ArrayList<Skill>();
		Skill s1 = new Skill();
		s1.setCategory("other");
		s1.setId("9j6XxJx9QK-6Ky1RcNezOA");
		s1.setName("Elasticsearch");		
		
		Skill s2 = new Skill();
		s2.setCategory("other");
		s2.setId("QToA8FNSSgWSPM7d2irwDA");
		s2.setName("Oracle");
		
		Skill s3 = new Skill();
		s3.setCategory("other");
		s3.setId("rytxjW42RgywXEdjRY26NA");
		s3.setName("Derby");
		
		skills.add(s1);
		skills.add(s2);
		skills.add(s3);
		
	}
	
	@Test
	public void testExportCV() throws URISyntaxException {
		
		//given
		DocumentResource res = new DocumentResource(cvdb, skilldb, docGen);
		CVIOConfiguration aConfiguration = ConfigurationHelper.cfg();
        CVResource cvResource = new CVResource(cvdb, aConfiguration);
        
        //create a cv
        Response cvResponse = cvResource.createCV(DEMO_JSON, true);
        assertEquals(201, cvResponse.getStatus());
        
        //then use the test data to create the document
		Mockito.when(cvdb.createCV(DEMO_JSON)).thenReturn(CV_ID);
		Mockito.when(cvdb.getCVMapById(CV_ID)).thenReturn(DEMO_CV_BY_MAP);
		Mockito.when(skilldb.getAllSkills()).thenReturn(skills);

		//create the cv document
		Response exportCV = res.exportCV(CV_ID, true);
		
		//and check if the document was created
		assertNotNull(exportCV);
		assertEquals(HttpStatus.OK_200, exportCV.getStatus());
		File file = (File) exportCV.getEntity();
		assertEquals("cv.odt", file.getName());
	}
}
