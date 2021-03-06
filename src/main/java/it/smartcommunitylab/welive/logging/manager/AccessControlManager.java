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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import eu.trentorise.smartcampus.aac.AACException;
import eu.trentorise.smartcampus.aac.AACService;

/**
 * @author raman
 *
 */
@Component
public class AccessControlManager {

	public static final String WRITE_PATTERN = "welive.logging.{app}.write";
	public static final String READ_PATTERN = "welive.logging.{app}.read";

	
	@Autowired
	private Environment env;	
	
	private static final String SECURED = "true";

	private static Map<String, Set<String>> tokenCache = new HashMap<String, Set<String>>();
	private static Set<String> protectedApps = null;

	private AACService aac = null;

	@PostConstruct
	public void init() {
		aac = new AACService(env.getProperty("aac.url"), null, null);
		protectedApps = new HashSet<String>();
		String protectedAppsStr = env.getProperty("logging.protected");
		protectedApps = org.springframework.util.StringUtils.commaDelimitedListToSet(protectedAppsStr.toLowerCase());
	}
	
	/**
	 * Check that the specified BasicAuth token is valid.
	 * @param token
	 */
	public void checkAccess(String token)  throws SecurityException{
		checkAccess(token, null, null);
	}

	
	/**
	 * Check that the specified token is enabled for logging operation defined by the scope pattern and the app.
	 * @param token
	 * @param appId
	 * @param pattern
	 * @throws SecurityException
	 */
	public void checkAccess(String token, String appId, String pattern) throws SecurityException {
		String secured = env.getProperty("logging.secured");
		if (SECURED.equals(secured)) {
			if (StringUtils.isEmpty(token)) {
				throw new SecurityException("Invalid token: '"+token+"' for app "+appId);
			}
			String tokenLC = token.toLowerCase();
			if (tokenLC.startsWith("basic ")) {
				if (!token.substring(6).equals(env.getProperty("logging.basic.token"))) {
					throw new SecurityException("Invalid credentials: '"+token+"' for app "+appId);
				}
			}
			else if (tokenLC.startsWith("bearer ") && appId != null) {
				// bearer token only for 3rd part apps
				if (protectedApps.contains(appId.toLowerCase())) {
					throw new SecurityException("Writing to app "+appId +" is not allowed.");
				}
				
				if (!tokenCache.containsKey(tokenLC) || !tokenCache.get(tokenLC).contains(appId)) {
					try {
						boolean result = aac.isTokenApplicable(token, getScope(pattern, appId));
						if (result) {
							Set<String> apps = tokenCache.get(tokenLC);
							if (apps == null) {
								apps = new HashSet<String>();
								tokenCache.put(tokenLC, apps);
							}
							apps.add(appId);
						} else {
							throw new SecurityException();
						}
					} catch (AACException e) {
						throw new SecurityException(e);
					}
				}
			} else {
				throw new SecurityException("Invalid credentials: '"+token+"' for app "+appId);
			}
		}
	}

	/**
	 * @param pattern
	 * @param appId
	 * @return
	 */
	private String getScope(String pattern, String appId) {
		return pattern.replace("{app}", appId);
	}

}
