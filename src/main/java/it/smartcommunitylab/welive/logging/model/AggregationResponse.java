package it.smartcommunitylab.welive.logging.model;

import io.swagger.annotations.ApiModel;

import java.util.Map;

@ApiModel(value="AggregationResponse", description="Aggregation query result.")
public class AggregationResponse {

	private Map<String, Object> hits;
	private Map<String, Object> aggregations;
	
	public Map<String, Object> getHits() {
		return hits;
	}
	public void setHits(Map<String, Object> query) {
		this.hits = query;
	}
	public Map<String, Object> getAggregations() {
		return aggregations;
	}
	public void setAggregations(Map<String, Object> aggregations) {
		this.aggregations = aggregations;
	}
	
	
	
}
