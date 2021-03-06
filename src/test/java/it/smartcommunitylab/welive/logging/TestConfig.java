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
package it.smartcommunitylab.welive.logging;

import it.smartcommunitylab.welive.logging.manager.AccessControlManager;
import it.smartcommunitylab.welive.logging.manager.JsonSchemaManager;
import it.smartcommunitylab.welive.logging.manager.Logger;
import it.smartcommunitylab.welive.logging.manager.MockLogger;

import java.net.UnknownHostException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;

/**
 * @author raman
 *
 */
@EnableWebMvc
@Configuration
@ComponentScan(basePackages = { "it.smartcommunitylab.welive.logging.controller" })
@PropertySource("classpath:/application_test.properties")
public class TestConfig {

	@Bean
	public Logger logger() {
		return new MockLogger();
	}
	@Bean
	public AccessControlManager accessControlManager() {
		return new AccessControlManager();
	}
	@Bean
	public JsonSchemaManager jsonSchemaManager() {
		return new JsonSchemaManager();
	}
	
	@Bean
	public MongoTemplate getMongo() throws UnknownHostException, MongoException {

		MongoTemplate template = new MongoTemplate(new MongoClient(), "logging");

		if (!template.collectionExists("schema")) {
			template.createCollection("schema");
		}

		return template;
	}
}
