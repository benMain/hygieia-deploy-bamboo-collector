package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.Environment;
import com.capitalone.dashboard.model.EnvironmentStatus;
import com.capitalone.dashboard.model.BambooDeployApplication;
import com.capitalone.dashboard.model.BambooDeployEnvResCompData;

import java.util.List;

/**
 * Client for fetching information from Bamboo Deploy.
 */
public interface BambooDeployClient {

    /**
     * Fetches all {@link BambooDeployApplication}s for a given instance URL.
     *
     * @param instanceUrl instance URL
     * @return list of {@link BambooDeployApplication}s
     */
    List<BambooDeployApplication> getApplications(String instanceUrl);

    /**
     * Fetches all {@link Environment}s for a given {@link BambooDeployApplication}.
     *
     * @param application a {@link BambooDeployApplication}
     * @return list of {@link Environment}s
     */
    List<Environment> getEnvironments(BambooDeployApplication application);

    /**
     * Fetches all {@link EnvironmentStatus}es for a given
     * {@link BambooDeployApplication} and {@link Environment}.
     *
     * @param application a {@link BambooDeployApplication}
     * @param environment an {@link Environment}
     * @return list of {@link EnvironmentStatus}es
     */
    List<BambooDeployEnvResCompData> getEnvironmentResourceStatusData(BambooDeployApplication application,
            Environment environment);
}
