package it.smartcommunitylab.welive.logging.manager;

import it.smartcommunitylab.welive.logging.config.AppConfig;
import it.smartcommunitylab.welive.logging.model.LogMsg;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
@WebAppConfiguration
public class GraylogConnectorTest {

	@Autowired
	private GraylogConnector connector;

	@Test
	public void query() {
		List<LogMsg> res = connector.query("appId:9001 AND myfield:10",
				"2015-09-01T09:22:50.392Z", "2015-10-08T09:22:50.392Z");
		System.out.println(res.size());
		Assertions.assertThat(res).hasAtLeastOneElementOfType(LogMsg.class);
	}
}
