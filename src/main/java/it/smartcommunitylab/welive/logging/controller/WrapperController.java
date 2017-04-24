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

package it.smartcommunitylab.welive.logging.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.ServerException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.smartcommunitylab.welive.exception.WeLiveLoggerException;
import it.smartcommunitylab.welive.logging.manager.AccessControlManager;
import it.smartcommunitylab.welive.logging.manager.JsonSchemaManager;
import it.smartcommunitylab.welive.logging.manager.Logger;
import it.smartcommunitylab.welive.logging.model.Counter;
import it.smartcommunitylab.welive.logging.model.LogMsg;
import it.smartcommunitylab.welive.logging.model.Pagination;
import it.smartcommunitylab.welive.logging.model.Response;

@Api(value = "/", description = "Log operations.")
@RestController
public class WrapperController {

	private static org.slf4j.Logger logger = LoggerFactory.getLogger(WrapperController.class);

	@Autowired
	private Logger logManager;

	@Autowired
	private AccessControlManager accessControl;

	@Autowired
	private JsonSchemaManager jsonSchemaManager;

	@Autowired
	private Environment env;

	@ApiOperation(value = "Save a log message on the service.")
	@RequestMapping(method = RequestMethod.POST, value = "/log/{appId}")
	public void pushLog(@ApiParam(value = "Log message", required = true) @RequestBody LogMsg msg,
			@ApiParam(value = "Application identifier", required = true) @PathVariable String appId,
			@RequestHeader(required = false, name = "Authorization") String token) throws WeLiveLoggerException {
		accessControl.checkAccess(token, appId.toLowerCase(), AccessControlManager.WRITE_PATTERN);

		jsonSchemaManager.validate(appId, msg);

		// appId in path has priority
		msg.setAppId(appId.toLowerCase());
		logManager.saveLog(msg);
	}

	@ApiOperation(value = "Update log schema in service.")
	@RequestMapping(method = RequestMethod.POST, value = "/log/update/schema/{appId}/{type}")
	public void updateSchema(@ApiParam(value = "Schema of log msg", required = true) @RequestBody String schema,
			@ApiParam(value = "Application identifier", required = true) @PathVariable String appId,
			@ApiParam(value = "Type of log msg", required = true) @PathVariable String type,
			@RequestHeader(required = false, name = "Authorization") String token) throws WeLiveLoggerException {
		try {
			accessControl.checkAccess(token, appId.toLowerCase(), AccessControlManager.WRITE_PATTERN);
			jsonSchemaManager.updateCache(appId, type, schema);
		} catch (WeLiveLoggerException wle) {
			throw new WeLiveLoggerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), wle.getMessage());
		}
	}

	@ApiOperation(value = "Read all events log schema for application")
	@RequestMapping(method = RequestMethod.GET, value = "/log/read/schema/{appId}")
	public String readSchema(HttpServletResponse response,
			@ApiParam(value = "Application identifier", required = true) @PathVariable String appId,
			@RequestHeader(required = false, name = "Authorization") String token)
			throws Exception {

		return jsonSchemaManager.readSchema(appId);

	}

	@ApiOperation(value = "Ping.")
	@RequestMapping(method = RequestMethod.GET, value = "/status")
	public String echo() {
		return "Yes I'm up and running!!!";
	}

	@ApiOperation(value = "Return the paginate list of result matching query criteria.")
	@RequestMapping(method = RequestMethod.GET, value = "/log/{appId}")
	public Pagination query(@ApiParam(value = "Application identifier", required = true) @PathVariable String appId,
			@ApiParam(value = "Timerange start. Express it in millis", required = false) @RequestParam(required = false) Long from,
			@ApiParam(value = "Timerange end. Express it in millis", required = false) @RequestParam(required = false) Long to,

			@ApiParam(value = "Log type to search", required = false) @RequestParam(required = false) String type,
			@ApiParam(value = "Search criteria on custom fields using Lucene syntax. Put in logical AND clause with msgPattern if present.", required = false) @RequestParam(required = false) String pattern,
			@ApiParam(value = "Search the pattern in log text. Put in logical AND clause with pattern if present.s", required = false) @RequestParam(required = false) String msgPattern,
			@ApiParam(value = "Maximum number of messages to return. Default value is 150", required = false) @RequestParam(required = false) Integer limit,
			@ApiParam(value = "Index of first message to return. Default value is 0", required = false) @RequestParam(required = false) Integer offset,
			@RequestHeader(required = false, name = "Authorization") String token) throws ServerException {

		accessControl.checkAccess(token, appId.toLowerCase(), AccessControlManager.READ_PATTERN);

		return logManager.query(appId.toLowerCase(), from, to, type, msgPattern, pattern, limit, offset);
	}

	/**
	 * 
	 * count
	 * 
	 * @throws ServerException
	 */

	@ApiOperation(value = "Count log entries.")
	@RequestMapping(method = RequestMethod.GET, value = "/log/count/{appId}")
	public Counter countQuery(@ApiParam(value = "Application identifier", required = true) @PathVariable String appId,
			@ApiParam(value = "Timerange start. Express it in millis", required = false) @RequestParam(required = false) Long from,
			@ApiParam(value = "Timerange end. Express it in millis", required = false) @RequestParam(required = false) Long to,

			@ApiParam(value = "Log type to search", required = false) @RequestParam(required = false) String type,
			@ApiParam(value = "Search criteria on custom fields using Lucene syntax. Put in logical AND clause with msgPattern if present.", required = false) @RequestParam(required = false) String pattern,
			@ApiParam(value = "Search the pattern in log text. Put in logical AND clause with pattern if present.s", required = false) @RequestParam(required = false) String msgPattern,
			@ApiParam(value = "Maximum number of messages to return. Default value is 150", required = false) @RequestParam(required = false) Integer limit,
			@ApiParam(value = "Index of first message to return. Default value is 0", required = false) @RequestParam(required = false) Integer offset,
			@RequestHeader(required = false, name = "Authorization") String token) throws ServerException {

		accessControl.checkAccess(token, appId.toLowerCase(), AccessControlManager.READ_PATTERN);

		return logManager.queryCount(appId.toLowerCase(), from, to, type, msgPattern, pattern);
	}

	@ApiOperation(value = "Return one or more elasticsearch aggregation(s). See https://www.elastic.co/guide/en/elasticsearch/reference/current/search-search.html and https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations.html")
	@RequestMapping(method = RequestMethod.POST, value = "/log/aggregate")
	public String aggregate(HttpServletResponse response,
			@ApiParam(value = "An elasticsearch search query", required = true) @RequestBody String request,
			@RequestHeader(required = false, name = "Authorization") String token) throws Exception {

		accessControl.checkAccess(token);

		String address = env.getProperty("elastic.url") + "/" + env.getProperty("elastic.index") + "/_search";
		URL url = new URL(address);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setDoInput(true);

		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestProperty("Content-Type", "application/json");

		OutputStream out = conn.getOutputStream();
		Writer writer = new OutputStreamWriter(out, "UTF-8");
		writer.write(request);
		writer.close();
		out.close();

		BufferedReader sr = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String s;
		while ((s = sr.readLine()) != null) {
			sb.append(s);
		}

		return sb.toString();
	}

	/**
	 * Facet search
	 */
	// TODO
	@RequestMapping(method = RequestMethod.GET, value = "/log/{appId}/{period}")
	public List<LogMsg> facetQuery(@PathVariable String appId, @RequestParam(required = false) Long from,
			@RequestParam(required = false) Long to, @RequestParam(required = false) String type,
			@RequestParam(required = false) String pattern, @RequestParam(required = false) String msgPattern) {

		// period Values -> daily,weekly,monthly,by-session
		return null;
	}

	@ExceptionHandler(value = ServerException.class)
	public void handleServerException(HttpServletResponse resp, Exception e) throws IOException {
		resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				"Exception calling graylog server: " + e.getMessage());
	}

	@ExceptionHandler(value = IllegalArgumentException.class)
	public void handleIllegalArgumentException(HttpServletResponse resp, Exception e) throws IOException {
		resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
	}

	@ExceptionHandler(value = SecurityException.class)
	public void handleSecurityException(HttpServletResponse resp, Exception e) throws IOException {

		logger.error(e.getMessage());
		resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
	}

	@ExceptionHandler(value = WeLiveLoggerException.class)
	public @ResponseBody Response<Void> handleExceptions(HttpServletResponse response, Exception exception) {
		Response<Void> res = exception instanceof WeLiveLoggerException ? ((WeLiveLoggerException) exception).getBody()
				: new Response<Void>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
		response.setStatus(res.getErrorCode());
		return res;
	}

}
