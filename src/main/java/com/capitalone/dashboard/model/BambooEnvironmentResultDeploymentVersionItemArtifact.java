package com.capitalone.dashboard.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BambooEnvironmentResultDeploymentVersionItemArtifact {
    private long id;
    private String label;
    private long size;
    private boolean isSharedArtifact;
    private boolean isGloballyStored;
    private String linkType;
    private BambooEnvironmentResultKey planResultKey;
    private String archiverType;

    public long getId() {
        return id;
    }

    @JsonProperty(value = "isSharedArtifact")
    public boolean isSharedArtifact() {
        return isSharedArtifact;
    }

    public void setSharedArtifact(boolean isSharedArtifact) {
        this.isSharedArtifact = isSharedArtifact;
    }

    public String getArchiverType() {
        return archiverType;
    }

    public void setArchiverType(String archiverType) {
        this.archiverType = archiverType;
    }

    public BambooEnvironmentResultKey getPlanResultKey() {
        return planResultKey;
    }

    public void setPlanResultKey(BambooEnvironmentResultKey planResultKey) {
        this.planResultKey = planResultKey;
    }

    public String getLinkType() {
        return linkType;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    @JsonProperty(value = "isGloballyStored")
    public boolean isGloballyStored() {
        return isGloballyStored;
    }

    public void setGloballyStored(boolean isGloballyStored) {
        this.isGloballyStored = isGloballyStored;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setId(long id) {
        this.id = id;
    }
}
