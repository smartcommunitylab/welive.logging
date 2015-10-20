/**
 *    Copyright 2015 Fondazione Bruno Kessler
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package it.smartcommunitylab.welive.logging.manager;

import it.smartcommunitylab.welive.logging.model.Counter;
import it.smartcommunitylab.welive.logging.model.LogMsg;
import it.smartcommunitylab.welive.logging.model.Pagination;

import java.io.IOException;
import java.rmi.ServerException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

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

	private static final String JSON_MESSAGES_FIELD = "messages";
	private static final String JSON_MESSAGE_FIELD = "message";
	private static final String JSON_FIELDS_FIELD = "fields";
	private static final String JSON_TOT_RESULTS_FIELD = "total_results";
	private static final String JSON_ELASTIC_QUERY_FIELD = "built_query";

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
		dateFormatter = new SimpleDateFormat(ISO8601_DATE_PATTERN);

	}

	public void pushLog(LogMsg msg) {
		restClient.postForObject(graylogEndpoint, msg.toGraylogFormat(),
				Map.class);
	}

	public Pagination query(String query, long fromTs, long toTs,
			Integer limit, Integer offset) throws ServerException {
		Map<String, Object> responseObj = graylogQuery(query, fromTs, toTs,
				limit, offset);

		return paginate(responseObj);
	}

	public Counter queryCount(String query, long fromTs, long toTs)
			throws ServerException {
		Map<String, Object> responseObj = graylogQuery(query, fromTs, toTs,
				null, null);

		Integer count = (Integer) responseObj.get(JSON_TOT_RESULTS_FIELD);
		return new Counter(count);
	}

	@SuppressWarnings("unchecked")
	private Pagination paginate(Map<String, Object> resp) {
		Pagination p = new Pagination();
		List<Map<String, Object>> msgs = (List<Map<String, Object>>) resp
				.get(JSON_MESSAGES_FIELD);
		List<String> customFields = extractCustomFields((List<String>) resp
				.get(JSON_FIELDS_FIELD));
		p.setData(convert(msgs, customFields));
		p.setTotalResults((Integer) resp.get(JSON_TOT_RESULTS_FIELD));
		String builtQuery = (String) resp.get(JSON_ELASTIC_QUERY_FIELD);
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> res;
		try {
			res = mapper.readValue(builtQuery, Map.class);
			p.setLimit((Integer) res.get("size"));
			p.setOffset((Integer) res.get("from"));
		} catch (IOException e) {
			logger.error("Exception parsing built_query field of graylog response");
		}

		return p;
	}

	private List<String> extractCustomFields(List<String> fields) {
		List<String> customFields = new ArrayList<>();
		for (String f : fields) {
			if (f.startsWith(LogMsg.CUSTOM_PREFIX)) {
				customFields.add(f);
			}
		}

		return customFields;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> graylogQuery(String query, long fromTs,
			long toTs, Integer limit, Integer offset) throws ServerException {
		Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put("query", query);
		queryParams.put("from", dateFormatter.format(new Date(fromTs)));
		queryParams.put("to", dateFormatter.format(new Date(toTs)));
		queryParams.put("offset", offset);
		queryParams.put("limit", limit);

		try {
			@SuppressWarnings("rawtypes")
			ResponseEntity<Map> resp = restClient
					.getForEntity(
							queryEndpoint
									+ "?query={query}&from={from}&to={to}&limit={limit}&offset={offset}",
							Map.class, queryParams);
			return (Map<String, Object>) resp.getBody();
		} catch (RestClientException e) {
			logger.error(String.format("Graylog response error: %s call %s",
					e.getMessage(), queryParams.toString()));
			throw new ServerException(e.getMessage());
		}

	}

	List<LogMsg> convert(List<Map<String, Object>> list,
			List<String> customFields) {
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

					// set customFields
					Map<String, Object> custom = new HashMap<>();
					for (String customField : customFields) {
						Object value = subElem.get(customField);
						if (value != null) {
							custom.put(customField
									.substring(LogMsg.CUSTOM_PREFIX.length()),
									value);
						}
					}
					msg.setCustomAttributes(custom);
					res.add(msg);
				}

			}
		}
		return res;

	}
}