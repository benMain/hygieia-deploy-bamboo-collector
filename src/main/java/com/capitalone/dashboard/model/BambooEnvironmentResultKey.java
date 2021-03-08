package com.capitalone.dashboard.model;

public class BambooEnvironmentResultKey {
    private String key;
    private Object entityKey;
    private int resultNumber;

    public String getKey() {
        return key;
    }

    public int getResultNumber() {
        return resultNumber;
    }

    public void setResultNumber(int resultNumber) {
        this.resultNumber = resultNumber;
    }

    public Object getEntityKey() {
        return entityKey;
    }

    public void setEntityKey(Object entityKey) {
        this.entityKey = entityKey;
    }

    public void setKey(String key) {
        this.key = key;
    }
}