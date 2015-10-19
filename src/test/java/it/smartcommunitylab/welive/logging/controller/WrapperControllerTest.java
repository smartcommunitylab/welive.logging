package it.smartcommunitylab.welive.logging.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import it.smartcommunitylab.welive.logging.config.AppConfig;
import it.smartcommunitylab.welive.logging.model.LogMsg;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

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
		Map<String, Object> customAttr = new HashMap<>();
		customAttr.put("rating", 1.2d);
		customAttr.put("id", "aaa22");
		msg.setCustomAttributes(customAttr);

		mockMvc.perform(
				post("/log/{appId}", appId).contentType("application/json")
						.content(toJson(msg))).andExpect(status().isOk());

	}

	@Test
	public void storeLogInvalidType() throws Exception {
		final String appId = "wer123";

		LogMsg msg = new LogMsg();
		msg.setMsg("log with invalid type");

		mockMvc.perform(
				post("/log/{appId}", appId).contentType("application/json")
						.content(toJson(msg))).andExpect(status().isOk());

	}

	@Test
	public void storeLogvalidType() throws Exception {
		final String appId = "wer123";

		LogMsg msg = new LogMsg();
		msg.setMsg("appstart logging");
		msg.setType("AppStart");

		mockMvc.perform(
				post("/log/{appId}", appId).contentType("application/json")
						.content(toJson(msg))).andExpect(status().isOk());

	}

	@Test
	public void query() throws Exception {
		final String appId = "wer123";
		mockMvc.perform(get("/log/{appId}", appId).param("type", "AppStart"))
				.andExpect(status().isOk());
	}

	@Test
	public void querySetFrom() throws Exception {
		final String appId = "wer123";

		long from = new GregorianCalendar(2015, 8, 28).getTimeInMillis();
		mockMvc.perform(
				get("/log/{appId}", appId).param("from", Long.toString(from)))
				.andExpect(status().isOk());
	}

	@Test
	public void querySetTo() throws Exception {
		final String appId = "wer123";

		long to = new GregorianCalendar(2015, 9, 02).getTimeInMillis();
		mockMvc.perform(
				get("/log/{appId}", appId).param("to", Long.toString(to)))
				.andExpect(status().isOk());
	}

	@Test
	public void querySetFromTo() throws Exception {
		final String appId = "wer123";

		long from = new GregorianCalendar(2015, 9, 10).getTimeInMillis();
		long to = new GregorianCalendar(2015, 9, 13).getTimeInMillis();
		mockMvc.perform(
				get("/log/{appId}", appId).param("from", Long.toString(from))
						.param("to", Long.toString(to))).andExpect(
				status().isOk());
	}

	@Test
	public void queryComplete() throws Exception {
		final String appId = "wer123";

		long from = new GregorianCalendar(2015, 9, 10).getTimeInMillis();
		long to = new GregorianCalendar(2015, 9, 14).getTimeInMillis();
		mockMvc.perform(
				get("/log/{appId}", appId).param("from", Long.toString(from))
						.param("to", Long.toString(to))
						.param("type", "AppStart")
						.param("msgPattern", "logging")
						.param("pattern", "source: 10.0.2.2")
						.param("offset", "14")).andExpect(status().isOk());
	}

	private String toJson(Object o) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(o);
	}

}
