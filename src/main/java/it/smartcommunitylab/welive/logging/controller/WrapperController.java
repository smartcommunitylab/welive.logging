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

import it.smartcommunitylab.welive.logging.manager.GraylogConnector;
import it.smartcommunitylab.welive.logging.manager.LogManager;
import it.smartcommunitylab.welive.logging.model.Counter;
import it.smartcommunitylab.welive.logging.model.LogMsg;
import it.smartcommunitylab.welive.logging.model.Pagination;

import java.io.IOException;
import java.rmi.ServerException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WrapperController {

	private static final Logger logger = Logger
			.getLogger(GraylogConnector.class);

	@Autowired
	private LogManager logManager;

	@RequestMapping(method = RequestMethod.POST, value = "/log/{appId}")
	public void pushLog(@RequestBody LogMsg payload, @PathVariable String appId) {

		// appId in path has priority
		payload.setAppId(appId);
		logManager.saveLog(payload);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/status")
	public String echo() {
		return "Yes I'm up and running!!!";
	}

	@RequestMapping(method = RequestMethod.GET, value = "/log/{appId}")
	public Pagination query(@PathVariable String appId,
			@RequestParam(required = false) Long from,
			@RequestParam(required = false) Long to,
			@RequestParam(required = false) String type,
			@RequestParam(required = false) String pattern,
			@RequestParam(required = false) String msgPattern,
			@RequestParam(required = false) Integer limit,
			@RequestParam(required = false) Integer offset)
			throws ServerException {
		return logManager.query(appId, from, to, type, msgPattern, pattern,
				limit, offset);
	}

	/**
	 * 
	 * count
	 * 
	 * @throws ServerException
	 */

	@RequestMapping(method = RequestMethod.GET, value = "/log/count/{appId}")
	public Counter countQuery(@PathVariable String appId,
			@RequestParam(required = false) Long from,
			@RequestParam(required = false) Long to,
			@RequestParam(required = false) String type,
			@RequestParam(required = false) String pattern,
			@RequestParam(required = false) String msgPattern)
			throws ServerException {

		return logManager
				.queryCount(appId, from, to, type, msgPattern, pattern);
	}

	/**
	 * Facet search
	 */
	// TODO
	@RequestMapping(method = RequestMethod.GET, value = "/log/{appId}/{period}")
	public List<LogMsg> facetQuery(@PathVariable String appId,
			@RequestParam(required = false) Long from,
			@RequestParam(required = false) Long to,
			@RequestParam(required = false) String type,
			@RequestParam(required = false) String pattern,
			@RequestParam(required = false) String msgPattern) {

		// period Values -> daily,weekly,monthly,by-session
		return null;
	}

	@ExceptionHandler(value = ServerException.class)
	public void handleServerException(HttpServletResponse resp, Exception e)
			throws IOException {
		resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				"Exception calling graylog server: " + e.getMessage());
	}
}
