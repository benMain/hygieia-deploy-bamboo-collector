package com.capitalone.dashboard.model;

public class BambooEnvironmentResultOperations {
    private boolean canView;
    private boolean canEdit;
    private boolean canDelete;
    private boolean allowedToExecute;
    private boolean canExecute;
    private boolean allowedToCreateVersion;
    private boolean allowedToSetVersionStatus;

    public boolean isCanView() {
        return canView;
    }

    public boolean isAllowedToSetVersionStatus() {
        return allowedToSetVersionStatus;
    }

    public void setAllowedToSetVersionStatus(boolean allowedToSetVersionStatus) {
        this.allowedToSetVersionStatus = allowedToSetVersionStatus;
    }

    public boolean isAllowedToCreateVersion() {
        return allowedToCreateVersion;
    }

    public void setAllowedToCreateVersion(boolean allowedToCreateVersion) {
        this.allowedToCreateVersion = allowedToCreateVersion;
    }

    public boolean isCanExecute() {
        return canExecute;
    }

    public void setCanExecute(boolean canExecute) {
        this.canExecute = canExecute;
    }

    public boolean isAllowedToExecute() {
        return allowedToExecute;
    }

    public void setAllowedToExecute(boolean allowedToExecute) {
        this.allowedToExecute = allowedToExecute;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public void setCanView(boolean canView) {
        this.canView = canView;
    }
}
