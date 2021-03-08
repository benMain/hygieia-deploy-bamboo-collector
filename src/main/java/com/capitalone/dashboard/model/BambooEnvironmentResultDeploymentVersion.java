package com.capitalone.dashboard.model;

import java.util.List;

public class BambooEnvironmentResultDeploymentVersion {
    private long id;
    private String name;
    private long creationDate;
    private List<BambooEnvironmentResultDeploymentVersionItem> items;
    private String planBranchName;
    private long ageZeroPoint;
    private BambooEnvironmentResultOperations operations;

    public long getId() {
        return id;
    }

    public BambooEnvironmentResultOperations getOperations() {
        return operations;
    }

    public void setOperations(BambooEnvironmentResultOperations operations) {
        this.operations = operations;
    }

    public long getAgeZeroPoint() {
        return ageZeroPoint;
    }

    public void setAgeZeroPoint(long ageZeroPoint) {
        this.ageZeroPoint = ageZeroPoint;
    }

    public String getPlanBranchName() {
        return planBranchName;
    }

    public void setPlanBranchName(String planBranchName) {
        this.planBranchName = planBranchName;
    }

    public List<BambooEnvironmentResultDeploymentVersionItem> getItems() {
        return items;
    }

    public void setItems(List<BambooEnvironmentResultDeploymentVersionItem> items) {
        this.items = items;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(long id) {
        this.id = id;
    }
}
