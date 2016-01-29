package it.smartcommunitylab.welive.logging.model;

import io.swagger.annotations.ApiModel;

import java.util.Map;

@ApiModel(value="AggregationRequest", description="Aggregation query.")
public class AggregationRequest {

	private Map<String, Object> query;
	private Map<String, Object> aggregations;
	
	public Map<String, Object> getQuery() {
		return query;
	}
	public void setQuery(Map<String, Object> query) {
		this.query = query;
	}
	public Map<String, Object> getAggregations() {
		return aggregations;
	}
	public void setAggregations(Map<String, Object> aggregations) {
		this.aggregations = aggregations;
	}
	
	
	
}
