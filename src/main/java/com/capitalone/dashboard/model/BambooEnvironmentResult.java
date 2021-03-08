package com.capitalone.dashboard.model;

public class BambooEnvironmentResult {
    private BambooEnvironmentResultDeploymentVersion deploymentVersion;
    private String deploymentVersionName;
    private int id;
    private String deploymentState;
    private String lifeCycleState;
    private long startedDate;
    private long queuedDate;
    private long executedDate;
    private long finishedDate;
    private String reasonSummary;
    private BambooEnvironmentResultKey key;
    private BambooEnvironmentResultAgent agent;
    private BambooEnvironmentResultOperations operations;

    public BambooEnvironmentResultDeploymentVersion getDeploymentVersion() {
        return deploymentVersion;
    }

    public BambooEnvironmentResultOperations getOperations() {
        return operations;
    }

    public void setOperations(BambooEnvironmentResultOperations operations) {
        this.operations = operations;
    }

    public BambooEnvironmentResultAgent getAgent() {
        return agent;
    }

    public void setAgent(BambooEnvironmentResultAgent agent) {
        this.agent = agent;
    }

    public BambooEnvironmentResultKey getKey() {
        return key;
    }

    public void setKey(BambooEnvironmentResultKey key) {
        this.key = key;
    }

    public String getReasonSummary() {
        return reasonSummary;
    }

    public void setReasonSummary(String reasonSummary) {
        this.reasonSummary = reasonSummary;
    }

    public long getFinishedDate() {
        return finishedDate;
    }

    public void setFinishedDate(long finishedDate) {
        this.finishedDate = finishedDate;
    }

    public long getExecutedDate() {
        return executedDate;
    }

    public void setExecutedDate(long executedDate) {
        this.executedDate = executedDate;
    }

    public long getQueuedDate() {
        return queuedDate;
    }

    public void setQueuedDate(long queuedDate) {
        this.queuedDate = queuedDate;
    }

    public long getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(long startedDate) {
        this.startedDate = startedDate;
    }

    public String getLifeCycleState() {
        return lifeCycleState;
    }

    public void setLifeCycleState(String lifeCycleState) {
        this.lifeCycleState = lifeCycleState;
    }

    public String getDeploymentState() {
        return deploymentState;
    }

    public void setDeploymentState(String deploymentState) {
        this.deploymentState = deploymentState;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeploymentVersionName() {
        return deploymentVersionName;
    }

    public void setDeploymentVersionName(String deploymentVersionName) {
        this.deploymentVersionName = deploymentVersionName;
    }

    public void setDeploymentVersion(BambooEnvironmentResultDeploymentVersion deploymentVersion) {
        this.deploymentVersion = deploymentVersion;
    }

}
