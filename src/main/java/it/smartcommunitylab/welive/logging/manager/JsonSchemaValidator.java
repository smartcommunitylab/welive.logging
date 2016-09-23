package it.smartcommunitylab.welive.logging.manager;

import it.smartcommunitylab.welive.exception.WeLiveLoggerException;
import it.smartcommunitylab.welive.logging.model.LogMsg;
import it.smartcommunitylab.welive.logging.model.ValidationErrorLogMsg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.report.ProcessingMessage;
import com.github.fge.jsonschema.report.ProcessingReport;
import com.github.fge.jsonschema.util.JsonLoader;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

@Component
public class JsonSchemaValidator {

	private ObjectMapper mapper = new ObjectMapper();

	private JSONObject schemaCache = new JSONObject();
	private final String COLLECTION_NAME = "schema";
	private final String CACHE_KEY_SEPARATOR = "_";
	private final String TYPE_FIELD = "type";
	private final String APPID_FIELD = "appId";
	private final String SCHEMA_FIELD = "schema";

	@Autowired
	private Logger logManager;

	@Autowired
	private Environment env;

	@Autowired
	private MongoTemplate mongoTemplate;

	private DBCollection collection;

	@PostConstruct
	public void init() {

		try {
			collection = mongoTemplate.getCollection(COLLECTION_NAME);
			String loggingValidationApps = env.getProperty("logging.valitation.apps");
			String[] logVApps = org.springframework.util.StringUtils
					.commaDelimitedListToStringArray(loggingValidationApps.toLowerCase());
			for (String appId : logVApps) {
				InputStream is = Thread.currentThread().getContextClassLoader()
						.getResourceAsStream(appId + "-schema.json");
				if (is != null) {
					String jsonSchema = readFile(is);
					JSONObject typeSchema = new JSONObject(jsonSchema);

					Iterator x = typeSchema.keys();

					while (x.hasNext()) {
						String key = (String) x.next();
						if (!exists(key, appId)) {
							Map<String, Object> map = new HashMap<String, Object>();
							JSONObject schema = (JSONObject) typeSchema.get(key);
							map.put(TYPE_FIELD, key);
							map.put(APPID_FIELD, appId);
							map.put(SCHEMA_FIELD, schema.toString());
							DBObject dbObject = new BasicDBObject(map);
							collection.save(dbObject);
						}
					}
				}
			}

			initCache();

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void initCache() throws JSONException {

		DBCursor curs = collection.find();

		while (curs.hasNext()) {
			DBObject o = curs.next();

			String type = (String) o.get(TYPE_FIELD);
			String appId = (String) o.get(APPID_FIELD);
			BasicDBObject basicDBObject = (BasicDBObject) JSON.parse(o.get(SCHEMA_FIELD).toString());

			JSONObject schema = mapper.convertValue(basicDBObject.toString(), JSONObject.class);

			schemaCache.put(getCacheKey(appId, type), schema);
		}

	}

	private boolean exists(String key, String appId) {

		BasicDBObject whereQuery = new BasicDBObject();
		whereQuery.put(TYPE_FIELD, key);
		whereQuery.put(APPID_FIELD, appId);
		
		DBCursor cursor = collection.find(whereQuery);
		if (cursor.hasNext()) {
			return true;
		}
		return false;
	}

	public boolean validate(String appId, LogMsg logMsg) throws WeLiveLoggerException {

		ProcessingReport report = null;
		boolean result = false;
		Map<String, Object> customAttributes = new HashMap<String, Object>();
		
		if (logMsg.getCustomAttributes() != null) {
			for (String a : logMsg.getCustomAttributes().keySet()) {
				customAttributes.put(a.toLowerCase(), logMsg.getCustomAttributes().get(a));
			}
		}
		String eventType = logMsg.getType();

		// validate only those components that have schema in cache.
		if (schemaCache.has(getCacheKey(appId, eventType))) {
			try {

				JsonNode schemaNode = JsonLoader.fromString(schemaCache.getString(getCacheKey(appId,eventType)));
				JsonNode data = mapper.convertValue(customAttributes, JsonNode.class);
				JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
				JsonSchema schema = factory.getJsonSchema(schemaNode);
				report = schema.validate(data);

			} catch (JsonParseException jpex) {

				ValidationErrorLogMsg msg = new ValidationErrorLogMsg();
				msg.setMsg(jpex.getMessage());
				customAttributes.put("type", eventType);
				customAttributes.put("senderAppId", appId);
				msg.setCustomAttributes(customAttributes);
				saveLog(msg);

				throw new WeLiveLoggerException(HttpStatus.PRECONDITION_FAILED.value(),
						"EventType: " + eventType + " Body: " + customAttributes + "Error: " + jpex.getMessage());

			} catch (ProcessingException pex) {

				ValidationErrorLogMsg msg = new ValidationErrorLogMsg();
				msg.setMsg(pex.getMessage());
				customAttributes.put("type", eventType);
				customAttributes.put("senderAppId", appId);
				msg.setCustomAttributes(customAttributes);
				saveLog(msg);

				throw new WeLiveLoggerException(HttpStatus.PRECONDITION_FAILED.value(),
						"EventType: " + eventType + " Body: " + customAttributes + "Error: " + pex.getMessage());

			} catch (IOException e) {

				ValidationErrorLogMsg msg = new ValidationErrorLogMsg();
				msg.setMsg(e.getMessage());
				customAttributes.put("type", eventType);
				customAttributes.put("senderAppId", appId);
				msg.setCustomAttributes(customAttributes);
				saveLog(msg);

				throw new WeLiveLoggerException(HttpStatus.PRECONDITION_FAILED.value(),
						"EventType: " + eventType + " Body: " + customAttributes + "Error : " + e.getMessage());

			} catch (JSONException e) {
				ValidationErrorLogMsg msg = new ValidationErrorLogMsg();
				msg.setMsg(e.getMessage());
				customAttributes.put("type", eventType);
				customAttributes.put("senderAppId", appId);
				msg.setCustomAttributes(customAttributes);
				saveLog(msg);

				throw new WeLiveLoggerException(HttpStatus.PRECONDITION_FAILED.value(),
						"EventType: " + eventType + " Body: " + customAttributes + "Error : " + e.getMessage());
			}
		}

		if (report != null) {
			result = report.isSuccess();

			if (!result) {
				Iterator<ProcessingMessage> iter = report.iterator();
				String error = "";
				while (iter.hasNext()) {
					ProcessingMessage pm = iter.next();
					error = error + pm.getMessage();
				}

				ValidationErrorLogMsg msg = new ValidationErrorLogMsg();
				msg.setMsg(error);
				customAttributes.put(TYPE_FIELD, eventType);
				msg.setCustomAttributes(customAttributes);

				saveLog(msg);

				throw new WeLiveLoggerException(HttpStatus.PRECONDITION_FAILED.value(),
						"EventType: " + eventType + "Body: " + customAttributes + "Error. " + error);

			}

		}

		return result;
	}

	private String getCacheKey(String appId, String type) {
		return appId + CACHE_KEY_SEPARATOR + type;
	}

	private void saveLog(ValidationErrorLogMsg msg) {
		logManager.saveLog(msg);

	}

	public static String readFile(InputStream is) {
		String result = "";
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				line = br.readLine();
			}
			result = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public void updateCache(String appId, String type, String schema) throws WeLiveLoggerException {

		JSONObject data = mapper.convertValue(JSON.parse(schema).toString(), JSONObject.class);
		
		try {
			// update mongo
			updateDB(appId, type, data);
			// update cache.
			schemaCache.put(getCacheKey(appId, type), data.get(type));
		} catch (JSONException e) {
			throw new WeLiveLoggerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
		}

	}

	private void updateDB(String appId, String type, JSONObject schema) throws WeLiveLoggerException {
		BasicDBObject whereQuery = new BasicDBObject();
		whereQuery.put(TYPE_FIELD, type);
		whereQuery.put(APPID_FIELD, appId);
		DBCursor cursor = collection.find(whereQuery);
		try {
		if (cursor.hasNext()) {
			DBObject temp = cursor.next();
			temp.put(SCHEMA_FIELD, schema.get(type).toString());
			collection.save(temp);
		} else {
			DBObject newSchema = new BasicDBObject();
			newSchema.put(TYPE_FIELD, type);
			newSchema.put(APPID_FIELD, appId);
			newSchema.put(SCHEMA_FIELD, schema.get(type).toString());
			collection.save(newSchema);
		}
		} catch (JSONException e) {
			throw new WeLiveLoggerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
		}
	}

	public static void main(String[] args) throws WeLiveLoggerException, JSONException {
		// System.out.println("Starting Json Validation.");
		JsonSchemaValidator jsonSchemaValidator = new JsonSchemaValidator();

		// player access.
		LogMsg playerAccessLogEvent = new LogMsg();
		Map<String, Object> attr1 = new HashMap<String, Object>();
		attr1.put("userid", "3");
		playerAccessLogEvent.setType("PlayerAccess");
		playerAccessLogEvent.setCustomAttributes(attr1);

		// player app access.
		LogMsg playerAppAccessLogEvent = new LogMsg();
		Map<String, Object> attr2 = new HashMap<String, Object>();
		attr2.put("userid", new ArrayList<String>());
		attr2.put("pilotid", "weliveplayer");
		playerAppAccessLogEvent.setType("PlayerAppsAccess");
		playerAppAccessLogEvent.setCustomAttributes(attr2);

		jsonSchemaValidator.validate("test", playerAccessLogEvent);
		jsonSchemaValidator.validate("test", playerAppAccessLogEvent);
		jsonSchemaValidator.validate("test", playerAppAccessLogEvent);
		jsonSchemaValidator.validate("test", playerAccessLogEvent);

	}

}
