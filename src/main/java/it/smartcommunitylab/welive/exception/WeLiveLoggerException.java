package it.smartcommunitylab.welive.exception;

import it.smartcommunitylab.welive.logging.model.Response;

public class WeLiveLoggerException extends Exception {

	public String erroMsg;
	public int errorCode;
	public Response body;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public WeLiveLoggerException(String msg) {
		super(msg);
	}

	public WeLiveLoggerException(int errorCode, String msg) {
		super(msg);
		this.errorCode = errorCode;
		this.erroMsg = msg;
		this.body = new Response<Void>(errorCode, msg);
	}

	public Response getBody() {
		return body;
	}

}
