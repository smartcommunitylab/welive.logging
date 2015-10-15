package it.smartcommunitylab.welive.logging.manager;

import it.smartcommunitylab.welive.logging.config.AppConfig;
import it.smartcommunitylab.welive.logging.model.LogMsg;
import it.smartcommunitylab.welive.logging.model.Pagination;

import java.rmi.ServerException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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
	public void query() throws ParseException, ServerException {
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

		Pagination res = connector.query("type:AppCustom",
				formatter.parse("2015-10-12 13:50").getTime(),
				formatter.parse("2015-10-13 13:50").getTime(), null, null);
		System.out.println(res.getData().size());
		Assertions.assertThat(res.getData()).hasAtLeastOneElementOfType(
				LogMsg.class);
	}
}
