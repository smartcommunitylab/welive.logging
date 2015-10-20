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

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Base64Utils;

public class BasicAuthRequestInterceptor implements
		ClientHttpRequestInterceptor {

	private final String username;
	private final String password;

	public BasicAuthRequestInterceptor(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException {

		// Build the auth-header
		final String auth = username + ":" + password;
		final byte[] encodedAuth = Base64Utils.encode(auth.getBytes());
		final String authHeader = "Basic " + new String(encodedAuth);

		// Add the auth-header
		request.getHeaders().add("Authorization", authHeader);

		return execution.execute(request, body);
	}

}
