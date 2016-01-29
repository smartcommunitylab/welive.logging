package it.smartcommunitylab.welive.logging.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.smartcommunitylab.welive.logging.model.AggregationRequest;
import it.smartcommunitylab.welive.logging.model.AggregationResponse;

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


@Api(value = "/elastic", description = "Log query via Elasticsearch.")
@RestController
@RequestMapping("/elastic/")
public class ElasticController {

	@Autowired
	private Environment env;

	@ApiOperation(value = "Return one or more elasticsearch aggregation(s). See https://www.elastic.co/guide/en/elasticsearch/reference/current/search-search.html and https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations.html")
	@RequestMapping(method = RequestMethod.POST, value = "aggregate")
	public AggregationResponse aggregate(HttpServletResponse response, 
			@ApiParam(value = "Elasticsearch search request composed by 'query' and 'aggregations'", required = true)
			@RequestBody AggregationRequest request) throws Exception {
		ObjectMapper mapper = new ObjectMapper();

		Map<String, Object> req = new TreeMap<String, Object>();
		if (request.getAggregations() != null) {
			req.put("aggs", request.getAggregations());
		}
		if (request.getQuery() != null) {
			req.put("query", request.getQuery());
		}		
		req.put("_source", false);
		req.put("size", 0);
		
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
		writer.write(mapper.writeValueAsString(req));
		writer.close();
		out.close();
		
		Map res = mapper.readValue(conn.getInputStream(), Map.class);
		
		AggregationResponse result =  new AggregationResponse();
		result.setHits((Map)res.get("hits"));
		result.setAggregations((Map)res.get("aggregations"));

		return result;
	}	

}
