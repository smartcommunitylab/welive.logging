package it.smartcommunitylab.welive.logging.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class ValidationErrorLogMsg {

	private String appId = "LOGGING";
	private String type = "VALIDATION_ERROR";
	private String msg;

	public static final String CUSTOM_PREFIX = "custom_";
	@JsonProperty(value = "custom_attr")
	private Map<String, Object> customAttributes;

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

	public Map<String, Object> toGraylogFormat() {
		Map<String, Object> res = new HashMap<String, Object>();
		if (appId != null) {
			res.put("_appId", appId);
		}

		if (type != null) {
			res.put("_type", type);
		}

		if (msg != null) {
			res.put("short_message", msg);
		}

		if (customAttributes != null) {
			for (Entry<String, Object> entry : customAttributes.entrySet()) {
				res.put("_" + CUSTOM_PREFIX + entry.getKey().toLowerCase(), entry.getValue());
			}
		}

		return res;
	}

	public Map<String, Object> getCustomAttributes() {
		return customAttributes;
	}

	public void setCustomAttributes(Map<String, Object> customAttributes) {
		this.customAttributes = customAttributes;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
