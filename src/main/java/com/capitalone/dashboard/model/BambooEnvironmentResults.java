package com.capitalone.dashboard.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BambooEnvironmentResults {
    private String expand;
    private int size;
    private List<BambooEnvironmentResult> results;
    private int startIndex;
    private int maxResult;

    public String getExpand() {
        return expand;
    }

    @JsonProperty("max-result")
    public int getMaxResult() {
        return maxResult;
    }

    public void setMaxResult(int maxResult) {
        this.maxResult = maxResult;
    }

    @JsonProperty("start-index")
    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public List<BambooEnvironmentResult> getResults() {
        return results;
    }

    public void setResults(List<BambooEnvironmentResult> results) {
        this.results = results;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setExpand(String expand) {
        this.expand = expand;
    }
}
