package it.smartcommunitylab.welive.logging.manager;

import it.smartcommunitylab.welive.logging.model.Counter;
import it.smartcommunitylab.welive.logging.model.LogMsg;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogManager {

	private static final Logger logger = Logger.getLogger(LogManager.class);

	private Format formatter;

	@Autowired
	private GraylogConnector connector;

	@PostConstruct
	@SuppressWarnings("unused")
	private void init() {
		formatter = new SimpleDateFormat("dd/MM/YYYY HH:mm");
	}

	public void saveLog(LogMsg msg) {
		// check type
		if (!isTypeValid(msg)) {
			msg.setType(DEFAULT_TYPE);
		}
		connector.pushLog(msg);
	}

	public List<LogMsg> query(String appId, Long from, Long to, String type,
			String msgPattern, String pattern) {
		Long[] ts = timestampCheck(from, to);
		String q = patternConstructor(appId, msgPattern, type, pattern);
		return connector.query(q, ts[0], ts[1]);
	}

	public Counter queryCount(String appId, Long from, Long to, String type,
			String msgPattern, String pattern) {
		Long[] ts = timestampCheck(from, to);
		String q = patternConstructor(appId, msgPattern, type, pattern);
		return connector.queryCount(q, ts[0], ts[1]);
	}

	private static final String[] validTypes = new String[] { "AppStart",
			"AppStop", "AppLogin", "AppConsume", "AppProsume", "AppODConsume",
			"AppCollaborate", "AppDataQueryInitiate", "AppDataQueryComplete",
			"AppDataQueryError", "AppQuestionnaire" };

	private static final String DEFAULT_TYPE = "AppCustom";

	public boolean isTypeValid(LogMsg msg) {
		return !StringUtils.isBlank(msg.getType())
				&& ArrayUtils.contains(validTypes, msg.getType());
	}

	private Long[] timestampCheck(Long from, Long to) {
		boolean setDefault = (from == null || from <= 0)
				&& (to == null || to <= 0);
		boolean setFrom = !setDefault && from == null;
		boolean setTo = !setDefault && to == null;
		Calendar cal = new GregorianCalendar();
		if (setDefault) {
			to = System.currentTimeMillis();
			cal.setTimeInMillis(to);
			cal.add(Calendar.DAY_OF_WEEK, -7);
			from = cal.getTimeInMillis();
			logger.debug(String.format("set default from and to: %s / %s",
					formatter.format(new Date(from)),
					formatter.format(new Date(to))));
		} else if (setFrom) {
			cal.setTimeInMillis(to);
			cal.add(Calendar.DAY_OF_WEEK, -7);
			from = cal.getTimeInMillis();
			logger.debug(String.format("set from: %s",
					formatter.format(new Date(from))));
		} else if (setTo) {
			cal.setTimeInMillis(from);
			cal.add(Calendar.DAY_OF_WEEK, 7);
			to = cal.getTimeInMillis();
			logger.debug(String.format("set to: %s",
					formatter.format(new Date(to))));
		} else {
			logger.debug(String.format("set and from already setted: %s / %s",
					formatter.format(new Date(from)),
					formatter.format(new Date(to))));
		}

		return new Long[] { from, to };
	}

	private String patternConstructor(String appId, String msgPattern,
			String type, String pattern) {
		StringBuffer buf = new StringBuffer();
		if (!StringUtils.isBlank(appId)) {
			buf.append("appId:").append(appId);
		}

		if (!StringUtils.isBlank(type)) {
			if (buf.length() > 0) {
				buf.append(" AND ");
			}
			buf.append("type:").append(type);
		}

		if (!StringUtils.isBlank(msgPattern)) {
			if (buf.length() > 0) {
				buf.append(" AND ");
			}
			buf.append("message:").append(msgPattern);
		}

		if (!StringUtils.isBlank(pattern)) {
			if (buf.length() > 0) {
				buf.append(" AND ");
			}
			buf.append(pattern);
		}

		logger.debug("graylog search pattern: " + buf.toString());
		return buf.toString();

	}
}
