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
