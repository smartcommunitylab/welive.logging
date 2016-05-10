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

package it.smartcommunitylab.welive.logging.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class LogMsg {
	private String appId;
	private String type;
	private String msg;
	private Double duration;
	private String session;
	private long timestamp;

	@JsonProperty(value = "custom_attr")
	private Map<String, Object> customAttributes;

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

		if (customAttributes != null) {
			for (Entry<String, Object> entry : customAttributes.entrySet()) {
				res.put("_" + CUSTOM_PREFIX + entry.getKey().toLowerCase(), entry.getValue());
			}
		}
		return res;
	}

	public Double getDuration() {
		return duration;
	}

	public void setDuration(Double duration) {
		this.duration = duration;
	}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
	}

	public Map<String, Object> getCustomAttributes() {
		return customAttributes;
	}

	public void setCustomAttributes(Map<String, Object> customAttributes) {
		this.customAttributes = customAttributes;
	}

	public static final String CUSTOM_PREFIX = "custom_";
}
