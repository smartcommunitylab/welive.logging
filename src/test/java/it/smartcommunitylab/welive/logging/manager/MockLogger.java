/*******************************************************************************
 * Copyright 2015 Fondazione Bruno Kessler
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
 ******************************************************************************/
package it.smartcommunitylab.welive.logging.manager;

import it.smartcommunitylab.welive.logging.model.Counter;
import it.smartcommunitylab.welive.logging.model.LogMsg;
import it.smartcommunitylab.welive.logging.model.Pagination;
import it.smartcommunitylab.welive.logging.model.ValidationErrorLogMsg;

import java.rmi.ServerException;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author raman
 *
 */
public class MockLogger implements Logger {

	private static org.slf4j.Logger logger = LoggerFactory.getLogger(MockLogger.class);
	
	@Override
	public void saveLog(LogMsg msg) {
		try {
			logger.info("Saving "+new ObjectMapper().writeValueAsString(msg));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Pagination query(String appId, Long from, Long to, String type,
			String msgPattern, String pattern, Integer limit, Integer offset)
			throws ServerException {
		logger.info("query "+appId);
		return new Pagination();
	}

	@Override
	public Counter queryCount(String appId, Long from, Long to, String type,
			String msgPattern, String pattern) throws ServerException {
		logger.info("queryCount "+appId);
		return null;
	}

	@Override
	public boolean isTypeValid(LogMsg msg) {
		return true;
	}

	@Override
	public void saveLog(ValidationErrorLogMsg msg) {
		try {
			System.err.println("Saving "+new ObjectMapper().writeValueAsString(msg));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
