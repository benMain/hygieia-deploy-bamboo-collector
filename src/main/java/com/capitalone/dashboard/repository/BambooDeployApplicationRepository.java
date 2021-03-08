package com.capitalone.dashboard.repository;

import com.capitalone.dashboard.model.BambooDeployApplication;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * Repository for {@link BambooDeployApplication}s.
 */
public interface BambooDeployApplicationRepository extends BaseCollectorItemRepository<BambooDeployApplication> {

    /**
     * Find a {@link BambooDeployApplication} by Bamboo instance URL and Bamboo
     * application id.
     *
     * @param collectorId   ID of the
     *                      {@link com.capitalone.dashboard.model.BambooDeployCollector}
     * @param instanceUrl   Bamboo instance URL
     * @param applicationId Bamboo application ID
     * @return a {@link BambooDeployApplication} instance
     */
    @Query(value = "{ 'collectorId' : ?0, options.instanceUrl : ?1, options.applicationId : ?2}")
    BambooDeployApplication findBambooDeployApplication(ObjectId collectorId, String instanceUrl, String applicationId);

    /**
     * Finds all {@link BambooDeployApplication}s for the given instance URL.
     *
     * @param collectorId ID of the
     *                    {@link com.capitalone.dashboard.model.BambooDeployCollector}
     * @param instanceUrl Bamboo instance URl
     * @return list of {@link BambooDeployApplication}s
     */
    @Query(value = "{ 'collectorId' : ?0, options.instanceUrl : ?1, enabled: true}")
    List<BambooDeployApplication> findEnabledApplications(ObjectId collectorId, String instanceUrl);
}
