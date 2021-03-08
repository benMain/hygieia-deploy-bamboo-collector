package com.capitalone.dashboard.model;

public class BambooEnvironmentResultDeploymentVersionItem {
    private long id;
    private String name;
    private BambooEnvironmentResultKey planResultKey;
    private String type;
    private String label;
    private String location;
    private String copyPattern;
    private long size;
    private BambooEnvironmentResultDeploymentVersionItemArtifact artifact;

    public long getId() {
        return id;
    }

    public BambooEnvironmentResultDeploymentVersionItemArtifact getArtifact() {
        return artifact;
    }

    public void setArtifact(BambooEnvironmentResultDeploymentVersionItemArtifact artifact) {
        this.artifact = artifact;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getCopyPattern() {
        return copyPattern;
    }

    public void setCopyPattern(String copyPattern) {
        this.copyPattern = copyPattern;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BambooEnvironmentResultKey getPlanResultKey() {
        return planResultKey;
    }

    public void setPlanResultKey(BambooEnvironmentResultKey planResultKey) {
        this.planResultKey = planResultKey;
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
