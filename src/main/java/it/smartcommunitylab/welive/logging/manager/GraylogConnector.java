package it.smartcommunitylab.welive.logging.manager;

import it.smartcommunitylab.welive.logging.model.Counter;
import it.smartcommunitylab.welive.logging.model.LogMsg;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GraylogConnector {

	private static final Logger logger = Logger
			.getLogger(GraylogConnector.class);

	@Autowired
	private Environment env;

	private RestTemplate restClient = new RestTemplate();
	private SimpleDateFormat dateFormatter;

	private String graylogEndpoint;
	private String queryEndpoint;
	private String adminUsername;
	private String adminPassword;
	private String fieldsToRetrieve;

	private static final String JSON_MESSAGES_FIELD = "messages";
	private static final String JSON_MESSAGE_FIELD = "message";
	private static final String JSON_TOT_RESULTS_FIELD = "total_results";

	private static final String[] FIELDS_REQUESTED = new String[] { "appId",
			"type", "message", "timestamp" };

	private static final String ISO8601_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	@PostConstruct
	@SuppressWarnings("unused")
	private void init() {

		graylogEndpoint = env.getProperty("graylog.store.endpoint");
		queryEndpoint = env.getProperty("graylog.query.endpoint");
		queryEndpoint += "/search/universal/absolute";

		adminUsername = env.getProperty("graylog.admin.username");
		adminPassword = env.getProperty("graylog.admin.password");

		final List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>();
		interceptors.add(new BasicAuthRequestInterceptor(adminUsername,
				adminPassword));
		restClient.setInterceptors(interceptors);
		fieldsToRetrieve = StringUtils.join(FIELDS_REQUESTED, ",");
		dateFormatter = new SimpleDateFormat(ISO8601_DATE_PATTERN);

	}

	public void pushLog(LogMsg msg) {
		restClient.postForObject(graylogEndpoint, msg.toGraylogFormat(),
				Map.class);
	}

	public List<LogMsg> query(String query, long fromTs, long toTs) {
		Map<String, Object> responseObj = graylogQuery(query, fromTs, toTs);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> msgs = (List<Map<String, Object>>) responseObj
				.get(JSON_MESSAGES_FIELD);
		return convert(msgs);
	}

	public Counter queryCount(String query, long fromTs, long toTs) {
		Map<String, Object> responseObj = graylogQuery(query, fromTs, toTs);

		Integer count = (Integer) responseObj.get(JSON_TOT_RESULTS_FIELD);
		return new Counter(count);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> graylogQuery(String query, long fromTs,
			long toTs) {
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("query", query);
		queryParams.put("from", dateFormatter.format(new Date(fromTs)));
		queryParams.put("to", dateFormatter.format(new Date(toTs)));
		queryParams.put("fields", fieldsToRetrieve);

		@SuppressWarnings("rawtypes")
		ResponseEntity<Map> resp = restClient.getForEntity(queryEndpoint
				+ "?query={query}&from={from}&to={to}&fields={fields}",
				Map.class, queryParams);

		return (Map<String, Object>) resp.getBody();

	}

	List<LogMsg> convert(List<Map<String, Object>> list) {
		List<LogMsg> res = new ArrayList<>();
		if (list != null) {
			for (Map<String, Object> elem : list) {

				@SuppressWarnings("unchecked")
				Map<String, Object> subElem = (Map<String, Object>) elem
						.get(JSON_MESSAGE_FIELD);
				if (subElem != null) {
					LogMsg msg = new LogMsg();
					try {
						msg.setAppId((String) subElem.get("appId"));
					} catch (Exception e) {
						logger.error(String
								.format("no appId field in message object"));
					}
					try {
						msg.setMsg((String) subElem.get("message"));
					} catch (Exception e) {
						logger.error(String
								.format("no message field in message object"));
					}
					try {
						msg.setTimestamp(dateFormatter.parse(
								(String) subElem.get("timestamp")).getTime());
					} catch (Exception e) {
						logger.error(String
								.format("no timestamp field in message object"));
					}
					try {
						msg.setType((String) subElem.get("type"));
					} catch (Exception e) {
						logger.error(String
								.format("no type field in message object"));
					}
					res.add(msg);
				}

			}
		}
		return res;

	}
}