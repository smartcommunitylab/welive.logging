package it.smartcommunitylab.welive.logging.model;

import java.util.HashMap;
import java.util.Map;

public class LogMsg {
	private String appId;
	private String type;
	private String msg;
	private double duration;
	private String session;
	private long timestamp;

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Map<String, Object> toGraylogFormat() {
		Map<String, Object> res = new HashMap<String, Object>();
		if (appId != null) {
			res.put("_appId", appId);
		}

		if (timestamp > 0) {
			res.put("timestamp", timestamp);
		}

		if (type != null) {
			res.put("_type", type);
		}

		if (msg != null) {
			res.put("short_message", msg);
		}

		return res;
	}

	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
	}

}
