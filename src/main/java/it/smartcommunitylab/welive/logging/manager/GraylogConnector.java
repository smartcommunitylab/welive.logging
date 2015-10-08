package it.smartcommunitylab.welive.logging.manager;

import it.smartcommunitylab.welive.logging.model.LogMsg;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GraylogConnector {

	@Autowired
	private Environment env;

	private RestTemplate restClient = new RestTemplate();

	private String graylogEndpoint;

	@PostConstruct
	@SuppressWarnings("unused")
	private void init() {
		graylogEndpoint = env.getProperty("graylog.endpoint");
	}

	public void storeLog(LogMsg msg) {
		restClient.postForObject(graylogEndpoint, msg.toGraylogFormat(),
				Map.class);
	}
}
