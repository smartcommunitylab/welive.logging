package it.smartcommunitylab.welive.logging.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Pagination {
	private Integer offset;
	private Integer limit;
	private List<LogMsg> data;
	@JsonProperty(value = "total_results")
	private Integer totalResults;

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public List<LogMsg> getData() {
		return data;
	}

	public void setData(List<LogMsg> data) {
		this.data = data;
	}

	public Integer getTotalResults() {
		return totalResults;
	}

	public void setTotalResults(Integer totalResults) {
		this.totalResults = totalResults;
	}

}
