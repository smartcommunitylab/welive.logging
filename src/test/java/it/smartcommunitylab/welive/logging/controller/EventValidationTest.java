package it.smartcommunitylab.welive.logging.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.smartcommunitylab.welive.logging.TestConfig;
import it.smartcommunitylab.welive.logging.model.LogMsg;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = TestConfig.class)
public class EventValidationTest {

	@Autowired
	private WebApplicationContext wac;

	@Autowired
	private Environment env;

	private MockMvc mockMvc;

	// log msg (PlayerAccess)
	LogMsg playerAccessLogEvent = new LogMsg();
	// log msg (PlayerAppAccess)
	LogMsg playerAppAccessLogEvent = new LogMsg();
	// log msg (AppInfoAccess)
	LogMsg appInfoAccess = new LogMsg();
	// log msg (AppOpen)
	LogMsg appOpen = new LogMsg();
	// log msg (AppDownload)
	LogMsg appDownload = new LogMsg();
	// log msg (PlayerAppSearch)
	LogMsg playerAppSearch = new LogMsg();
	// log msg (PlayerAppRecommendation)
	LogMsg playerAppRecommendation = new LogMsg();
	
	// appId.
	String appId = "weliveplayer";

	@Before
	public void setup() {

		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

		// player access.
		Map<String, Object> attr1 = new HashMap<String, Object>();
		attr1.put("userid", "3");
		playerAccessLogEvent.setType("PlayerAccess");
		playerAccessLogEvent.setCustomAttributes(attr1);

		// player app access.
		Map<String, Object> attr2 = new HashMap<String, Object>();
		attr2.put("userid", "3");
		attr2.put("pilotid", "weliveplayer");
		playerAppAccessLogEvent.setType("PlayerAppsAccess");
		playerAppAccessLogEvent.setCustomAttributes(attr2);
		
		// AppInfoAccess.
		Map<String, Object> attr3 = new HashMap<String, Object>();
		attr3.put("pilot", "Trento");
		attr3.put("appname", "Trento Pulizia Strade");
		attr3.put("userid", "314");
		attr3.put("appid", "19803");
		appInfoAccess.setCustomAttributes(attr3);
		appInfoAccess.setType("AppInfoAccess");
        	 
		// AppOpen.
		Map<String, Object> attr4 = new HashMap<String, Object>();
		attr4.put("pilot", "Trento");
		attr4.put("appname", "Trento Pulizia Strade");
		attr4.put("userid", "314");
		attr4.put("appid", "19803");
		appOpen.setCustomAttributes(attr4);
		appOpen.setType("AppOpen");
			 
		// AppDownload.
        Map<String, Object> attr5 = new HashMap<String, Object>();
		attr5.put("pilot", "Bilbao");
		attr5.put("appname", "Auzonet");
		attr5.put("userid", "348");
		attr5.put("appid", "19801");
		appDownload.setCustomAttributes(attr5);
		appDownload.setType("AppDownload");
		
		// PlayerAppSearch
        Map<String, Object> attr6 = new HashMap<String, Object>();
        attr6.put("pilot", "Trento");
        attr6.put("userid", "314");
        playerAppSearch.setCustomAttributes(attr6);
        playerAppSearch.setType("PlayerAppSearch");
		
		// PlayerAppRecommendation.
        Map<String, Object> attr7 = new HashMap<String, Object>();
        attr7.put("pilot", "Bilbao");
        attr7.put("appname", "Auzonet");
        attr7.put("userid", "348");
        attr7.put("appid", "19801");
        playerAppRecommendation.setCustomAttributes(attr7);
        playerAppRecommendation.setType("PlayerAppRecommendation");
        	 

	}

	@Test
	public void validateLogMsgStructure() throws Exception {

		mockMvc.perform(post("/log/{appId}", appId).contentType("application/json")
				.header("Authorization", "Basic " + env.getProperty("logging.basic.token"))
				.content(toJson(playerAccessLogEvent))).andExpect(status().isOk());

		mockMvc.perform(post("/log/{appId}", appId).contentType("application/json")
				.header("Authorization", "Basic " + env.getProperty("logging.basic.token"))
				.content(toJson(playerAppAccessLogEvent))).andExpect(status().isOk());
		
		mockMvc.perform(post("/log/{appId}", appId).contentType("application/json")
				.header("Authorization", "Basic " + env.getProperty("logging.basic.token"))
				.content(toJson(appInfoAccess))).andExpect(status().isOk());
		
		mockMvc.perform(post("/log/{appId}", appId).contentType("application/json")
				.header("Authorization", "Basic " + env.getProperty("logging.basic.token"))
				.content(toJson(appOpen))).andExpect(status().isOk());
		
		mockMvc.perform(post("/log/{appId}", appId).contentType("application/json")
				.header("Authorization", "Basic " + env.getProperty("logging.basic.token"))
				.content(toJson(appDownload))).andExpect(status().isOk());
		
		mockMvc.perform(post("/log/{appId}", appId).contentType("application/json")
				.header("Authorization", "Basic " + env.getProperty("logging.basic.token"))
				.content(toJson(playerAppSearch))).andExpect(status().isOk());
		
		mockMvc.perform(post("/log/{appId}", appId).contentType("application/json")
				.header("Authorization", "Basic " + env.getProperty("logging.basic.token"))
				.content(toJson(playerAppRecommendation))).andExpect(status().isOk());

	}

	@Test
	public void failedPreconditionStatus() throws Exception {

		playerAccessLogEvent.setType("PlayerAppsAccess");

		mockMvc.perform(post("/log/{appId}", appId).contentType("application/json")
				.header("Authorization", "Basic " + env.getProperty("logging.basic.token"))
				.content(toJson(playerAccessLogEvent))).andExpect(status().isPreconditionFailed());

	}
	
	@Test
	public void updateSchema() throws Exception {

		String jsonSchema = readFile("test-schema.json");
		String type = "testSchema"; 

		mockMvc.perform(post("/log/update/schema/{appId}/{type}", appId, type).contentType("application/json")
				.header("Authorization", "Basic " + env.getProperty("logging.basic.token")).content(toJson(jsonSchema)))
				.andExpect(status().isOk());
		
		LogMsg testSchema = new LogMsg();
		testSchema.setType(type);
		Map<String, Object> attr = new HashMap<String, Object>();
	    attr.put("componentname", "Open Innovation Area");
	    testSchema.setCustomAttributes(attr);
	        
		mockMvc.perform(post("/log/{appId}", appId).contentType("application/json")
				.header("Authorization", "Basic " + env.getProperty("logging.basic.token"))
				.content(toJson(testSchema))).andExpect(status().isOk());
		
		// editing schema.
		jsonSchema = jsonSchema.replace("componentname", "cName");
		
		// update schema.
		mockMvc.perform(post("/log/update/schema/{appId}/{type}", appId, type).contentType("application/json")
				.header("Authorization", "Basic " + env.getProperty("logging.basic.token")).content(toJson(jsonSchema)))
				.andExpect(status().isOk());
		
		mockMvc.perform(post("/log/{appId}", appId).contentType("application/json")
				.header("Authorization", "Basic " + env.getProperty("logging.basic.token"))
				.content(toJson(testSchema))).andExpect(status().isPreconditionFailed());
		

	}

	private String readFile(String name) {
		String result = "";
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(Test.class.getClassLoader().getResourceAsStream(name)));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				line = br.readLine();
			}
			result = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	

	private String toJson(Object o) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(o);
	}

}
