package com.capitalone.dashboard.model;

import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collector implementation for Bamboo Deploy that stores Bamboo server URLs.
 */
public class BambooDeployCollector extends Collector {
    private List<String> bambooServers = new ArrayList<>();
    private List<String> niceNames = new ArrayList<>();

    public List<String> getBambooServers() {
        return bambooServers;
    }

    public List<String> getNiceNames() {
        return niceNames;
    }

    public static BambooDeployCollector prototype(List<String> servers, List<String> niceNames) {
        BambooDeployCollector protoType = new BambooDeployCollector();
        protoType.setName("BambooDeploy");
        protoType.setCollectorType(CollectorType.Deployment);
        protoType.setOnline(true);
        protoType.setEnabled(true);
        protoType.getBambooServers().addAll(servers);
        if (!CollectionUtils.isEmpty(niceNames)) {
            protoType.getNiceNames().addAll(niceNames);
        }

        Map<String, Object> allOptions = new HashMap<>();
        allOptions.put(BambooDeployApplication.INSTANCE_URL, "");
        allOptions.put(BambooDeployApplication.APP_NAME, "");
        allOptions.put(BambooDeployApplication.APP_ID, "");
        protoType.setAllFields(allOptions);

        Map<String, Object> uniqueOptions = new HashMap<>();
        uniqueOptions.put(BambooDeployApplication.INSTANCE_URL, "");
        uniqueOptions.put(BambooDeployApplication.APP_NAME, "");
        protoType.setUniqueFields(uniqueOptions);
        return protoType;
    }
}
