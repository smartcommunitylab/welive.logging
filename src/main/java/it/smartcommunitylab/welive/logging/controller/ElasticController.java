package it.smartcommunitylab.welive.logging.controller;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/elastic/")
public class ElasticController {

	@Autowired
	private Environment env;
	
	@RequestMapping(method = RequestMethod.POST, value = "aggregate")
	public Map<String, Object> aggregate(HttpServletResponse response, @RequestBody Map<String, Object> body) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		
		Map<String, Object> request = new TreeMap<String, Object>();
		if (body.containsKey("aggs")) {
			request.put("aggs", body.get("aggs"));
		}
		if (body.containsKey("query")) {
			request.put("query", body.get("query"));
		}		
		request.put("_source", false);
		request.put("size", 0);
		
		String address = env.getProperty("elastic.url") + "/" + env.getProperty("elastic.index") + "/_search";
		URL url = new URL(address);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setDoInput(true);
		
		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestProperty("Content-Type", "application/json");	
		
		OutputStream out = conn.getOutputStream();
		Writer writer = new OutputStreamWriter(out, "UTF-8");
		writer.write(mapper.writeValueAsString(request));
		writer.close();
		out.close();
		
		Map result = mapper.readValue(conn.getInputStream(), Map.class);
		
		Map filteredResult =  new TreeMap<String, Object>();
		filteredResult.put("hits", result.get("hits"));
		filteredResult.put("aggregations", result.get("aggregations"));

		return filteredResult;
	}	

}
