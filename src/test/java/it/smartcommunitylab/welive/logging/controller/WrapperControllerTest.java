package it.smartcommunitylab.welive.logging.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import it.smartcommunitylab.welive.logging.config.AppConfig;
import it.smartcommunitylab.welive.logging.model.LogMsg;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
@ContextConfiguration(classes = AppConfig.class)
public class WrapperControllerTest {

	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}

	@Test
	public void storeLogWithAppId() throws Exception {
		final String appId = "wer123";

		LogMsg msg = new LogMsg();
		msg.setAppId("uvz890");
		msg.setType("AppCustom");
		msg.setMsg("logging");

		mockMvc.perform(
				post("/log/{appId}", appId).contentType("application/json")
						.content(toJson(msg))).andExpect(status().isOk());

	}

	@Test
	public void storeLogNoAppId() throws Exception {
		final String appId = "wer123";

		LogMsg msg = new LogMsg();
		msg.setType("AppCustom");
		msg.setMsg("logging");

		mockMvc.perform(
				post("/log/{appId}", appId).contentType("application/json")
						.content(toJson(msg))).andExpect(status().isOk());

	}

	private String toJson(Object o) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(o);
	}
}
