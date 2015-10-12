package it.smartcommunitylab.welive.logging.controller;

import it.smartcommunitylab.welive.logging.manager.GraylogConnector;
import it.smartcommunitylab.welive.logging.model.LogMsg;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WrapperController {

	private static final Logger logger = Logger
			.getLogger(GraylogConnector.class);

	@Autowired
	private GraylogConnector connector;

	@RequestMapping(method = RequestMethod.POST, value = "/log")
	public void pushLog(@RequestBody LogMsg payload) {
		connector.storeLog(payload);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/status")
	public String echo(@PathVariable String message) {
		return "Yes I'm up and running!!!";
	}

}
