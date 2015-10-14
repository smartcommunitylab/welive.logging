package it.smartcommunitylab.welive.logging.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Counter {
	@JsonProperty(value = "total_results")
	private Integer totalResults;

	public Counter() {
	}

	public Counter(final Integer result) {
		totalResults = result;
	}

	public Integer getTotalResults() {
		return totalResults;
	}

	public void setTotalResults(Integer totalResults) {
		this.totalResults = totalResults;
	}

}
