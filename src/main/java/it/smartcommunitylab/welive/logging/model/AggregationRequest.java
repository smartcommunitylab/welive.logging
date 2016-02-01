package it.smartcommunitylab.welive.logging.model;

import io.swagger.annotations.ApiModel;

import java.util.Map;

@ApiModel(value="AggregationRequest", description="Aggregation query.")
public class AggregationRequest {

	private Map<String, Object> query;
	private Map<String, Object> aggs;
	
	public Map<String, Object> getQuery() {
		return query;
	}
	public void setQuery(Map<String, Object> query) {
		this.query = query;
	}
	public Map<String, Object> getAggs() {
		return aggs;
	}
	public void setAggs(Map<String, Object> aggregations) {
		this.aggs = aggregations;
	}
	
	
	
}
