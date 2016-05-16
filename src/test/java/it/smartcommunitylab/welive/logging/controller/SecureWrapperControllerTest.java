package it.smartcommunitylab.welive.logging.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import it.smartcommunitylab.welive.logging.SecureTestConfig;
import it.smartcommunitylab.welive.logging.model.LogMsg;

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

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = SecureTestConfig.class)
public class SecureWrapperControllerTest {

	@Autowired
	private WebApplicationContext wac;

	@Autowired
	private Environment env;
	
	private MockMvc mockMvc;

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	@Test
	public void storeLogWithAppIdBasic() throws Exception {
		final String appId = "wer123";

		LogMsg msg = new LogMsg();
		msg.setAppId("uvz890");
		msg.setType("AppCustom");
		msg.setMsg("logging");

		mockMvc.perform(
				post("/log/{appId}", appId).contentType("application/json")
						.header("Authorization", "Basic "+ env.getProperty("logging.basic.token"))
						.content(toJson(msg))).andExpect(status().isOk());

	}

	@Test
	public void storeLogWithAppIdBasicInvalid() throws Exception {
		final String appId = "wer123";

		LogMsg msg = new LogMsg();
		msg.setAppId("uvz890");
		msg.setType("AppCustom");
		msg.setMsg("logging");

		mockMvc.perform(
				post("/log/{appId}", appId).contentType("application/json")
						.header("Authorization", "Basic invalid")
						.content(toJson(msg))).andExpect(status().isForbidden());

	}

	@Test
	public void storeLogWithAppIdBearer() throws Exception {
		final String appId = env.getProperty("aac.testapp");

		LogMsg msg = new LogMsg();
		msg.setAppId("uvz890");
		msg.setType("AppCustom");
		msg.setMsg("logging");

		mockMvc.perform(
				post("/log/{appId}", appId).contentType("application/json")
						.header("Authorization", "Bearer "+ env.getProperty("aac.clientToken"))
						.content(toJson(msg))).andExpect(status().isOk());

	}
	
	@Test
	public void storeLogWithAppIdBearerInvalid() throws Exception {
		final String appId = env.getProperty("aac.testapp");

		LogMsg msg = new LogMsg();
		msg.setAppId("uvz890");
		msg.setType("AppCustom");
		msg.setMsg("logging");

		mockMvc.perform(
				post("/log/{appId}", appId).contentType("application/json")
						.header("Authorization", "Bearer invalid")
						.content(toJson(msg))).andExpect(status().isForbidden());

	}
	
	private String toJson(Object o) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(o);
	}

}
