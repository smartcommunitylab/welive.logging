package it.smartcommunitylab.welive.logging.controller;

import it.smartcommunitylab.welive.logging.manager.GraylogConnector;
import it.smartcommunitylab.welive.logging.model.LogMsg;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
	private GraylogConnector connector;

	@RequestMapping(method = RequestMethod.POST, value = "/log/{appId}")
	public void pushLog(@RequestBody LogMsg payload, @PathVariable String appId) {

		// appId in path has priority
		payload.setAppId(appId);
		connector.storeLog(payload);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/status")
	public String echo(@PathVariable String message) {
		return "Yes I'm up and running!!!";
	}

	// paginable
	@RequestMapping(method = RequestMethod.GET, value = "/log/{appId}")
	public List<LogMsg> query(@PathVariable String appId,
			@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			@RequestParam(required = true) String type,
			@RequestParam(required = false) String pattern) {
		return null;
	}

	// paginable
	@RequestMapping(method = RequestMethod.GET, value = "/log/{appId}")
	public List<LogMsg> queryByMessage(@PathVariable String appId,
			@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			@RequestParam(required = true) String type,
			@RequestParam(required = false) String msgPattern) {
		return null;
	}

	/**
	 * 
	 * count
	 */

	@RequestMapping(method = RequestMethod.GET, value = "/log/count/{appId}")
	public String countQuery(@PathVariable String appId,
			@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			@RequestParam(required = true) String type,
			@RequestParam(required = false) String pattern) {

		return null;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/log/count/{appId}")
	public String countQueryByMessage(@PathVariable String appId,
			@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			@RequestParam(required = true) String type,
			@RequestParam(required = false) String msgPattern) {

		return null;
	}

	/**
	 * Facet search
	 */

	@RequestMapping(method = RequestMethod.GET, value = "/log/{appId}/{period}")
	public List<LogMsg> facetQuery(@PathVariable String appId,
			@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			@RequestParam(required = true) String type,
			@RequestParam(required = false) String pattern) {

		// period Values -> daily,weekly,monthly,by-session
		return null;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/log/{appId}/{period}")
	public List<LogMsg> facetQueryByMessage(@PathVariable String appId,
			@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			@RequestParam(required = true) String type,
			@RequestParam(required = false) String msgPattern) {

		// period Values -> daily,weekly,monthly,by-session
		return null;
	}

}
