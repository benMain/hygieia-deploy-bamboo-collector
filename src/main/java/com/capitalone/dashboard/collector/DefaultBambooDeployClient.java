package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.Environment;
import com.capitalone.dashboard.model.BambooDeployApplication;
import com.capitalone.dashboard.model.BambooDeployEnvResCompData;
import com.capitalone.dashboard.model.BambooEnvironmentResult;
import com.capitalone.dashboard.model.BambooEnvironmentResultDeploymentVersionItem;
import com.capitalone.dashboard.model.BambooEnvironmentResults;
import com.capitalone.dashboard.util.Supplier;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DefaultBambooDeployClient implements BambooDeployClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultBambooDeployClient.class);
    private static final String BAMBOO_API_BASE = "/rest/api/latest/";

    private final BambooDeploySettings bambooDeploySettings;
    private final RestOperations restOperations;

    @Autowired
    public DefaultBambooDeployClient(BambooDeploySettings bambooDeploySettings,
            Supplier<RestOperations> restOperationsSupplier) {
        this.bambooDeploySettings = bambooDeploySettings;
        this.restOperations = restOperationsSupplier.get();
    }

    @Override
    public List<BambooDeployApplication> getApplications(String instanceUrl) {
        List<BambooDeployApplication> applications = new ArrayList<>();

        for (Object item : paresAsArray(makeRestCall(instanceUrl, "deploy/project/all"))) {
            JSONObject jsonObject = (JSONObject) item;
            BambooDeployApplication application = new BambooDeployApplication();
            application.setInstanceUrl(instanceUrl);
            application.setApplicationName(str(jsonObject, "name"));
            application.setApplicationId(str(jsonObject, "id"));
            applications.add(application);
        }
        return applications;
    }

    @Override
    public List<Environment> getEnvironments(BambooDeployApplication application) {
        List<Environment> environments = new ArrayList<>();
        String appSpecificUrl = "deploy/project/" + application.getApplicationId();
        JSONObject deployPlan = parseAsObject(makeRestCall(application.getInstanceUrl(), appSpecificUrl));
        JSONArray envs = (JSONArray) deployPlan.get("environments");
        for (Object env : envs) {
            JSONObject jsonObject = (JSONObject) env;
            environments.add(new Environment(str(jsonObject, "id"), str(jsonObject, "name")));
        }

        return environments;
    }

    // Called by DefaultEnvironmentStatusUpdater
    // @SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts") // agreed, this method
    // needs refactoring.
    @Override
    public List<BambooDeployEnvResCompData> getEnvironmentResourceStatusData(BambooDeployApplication application,
            Environment environment) {

        List<BambooDeployEnvResCompData> environmentStatuses = new ArrayList<>();
        String urlInventory = "deploy/environment/" + environment.getId() + "/results";

        BambooEnvironmentResults envResults = makeEnvironmentRestCall(application.getInstanceUrl(), urlInventory)
                .getBody();

        Optional<BambooEnvironmentResult> latestOptionalResult = envResults.getResults().stream()
                .sorted((a, b) -> (int) (a.getStartedDate() - b.getStartedDate())).findFirst();

        if (latestOptionalResult.isPresent()) {
            BambooEnvironmentResult latestResult = latestOptionalResult.get();
            environmentStatuses.addAll(latestResult.getDeploymentVersion().getItems().stream()
                    .map(x -> buildBambooDeployEnvResCompData(environment, application, latestResult, x))
                    .collect(Collectors.toList()));
        } else {
            LOGGER.info(String.format("No Deployments for application %s in environment %s",
                    application.getApplicationName(), environment.getName()));
        }

        return environmentStatuses;
    }

    private BambooDeployEnvResCompData buildBambooDeployEnvResCompData(Environment environment,
            BambooDeployApplication application, BambooEnvironmentResult latestResult,
            BambooEnvironmentResultDeploymentVersionItem artifact) {
        BambooDeployEnvResCompData data = new BambooDeployEnvResCompData();
        data.setEnvironmentName(environment.getName());
        data.setCollectorItemId(application.getId());
        data.setComponentVersion(latestResult.getDeploymentVersionName());
        data.setAsOfDate(latestResult.getStartedDate());

        data.setDeployed(latestResult.getDeploymentState().toUpperCase().equals("SUCCESS"));
        data.setComponentName(artifact.getLabel());
        data.setOnline(latestResult.getDeploymentState().toUpperCase().equals("SUCCESS"));
        data.setResourceName(latestResult.getDeploymentVersionName());

        return data;
    }

    public JSONObject getParentAgent(JSONObject childObject) {
        JSONObject parentAgent = null;
        String resourceType = null;
        String hasAgent = null;

        JSONObject parentObject = (JSONObject) childObject.get("parent");

        if (parentObject != null) {
            resourceType = str(parentObject, "type");
            hasAgent = str(parentObject, "hasAgent");

            if (resourceType != null && resourceType.equalsIgnoreCase("agent")) {
                parentAgent = parentObject;
            } else {
                if ("true".equalsIgnoreCase(hasAgent)) {
                    parentAgent = getParentAgent(parentObject);
                }
            }
        }

        return parentAgent;
    }
    // ////// Helpers

    private ResponseEntity<BambooEnvironmentResults> makeEnvironmentRestCall(String instanceUrl, String endpoint) {
        String url = normalizeUrl(instanceUrl, BAMBOO_API_BASE + endpoint);
        ResponseEntity<BambooEnvironmentResults> response = null;
        try {
            response = restOperations.exchange(url, HttpMethod.GET, new HttpEntity<>(createHeaders(instanceUrl)),
                    BambooEnvironmentResults.class);

        } catch (RestClientException re) {
            LOGGER.error("Error with REST url: " + url);
            LOGGER.error(re.getMessage());
        }
        return response;
    }

    private ResponseEntity<String> makeRestCall(String instanceUrl, String endpoint) {
        String url = normalizeUrl(instanceUrl, BAMBOO_API_BASE + endpoint);
        ResponseEntity<String> response = null;
        try {
            response = restOperations.exchange(url, HttpMethod.GET, new HttpEntity<>(createHeaders(instanceUrl)),
                    String.class);

        } catch (RestClientException re) {
            LOGGER.error("Error with REST url: " + url);
            LOGGER.error(re.getMessage());
        }
        return response;
    }

    private String normalizeUrl(String instanceUrl, String remainder) {
        return StringUtils.removeEnd(instanceUrl, "/") + remainder;
    }
    // If we are putting token in the application.properties file
    // Then it overrides all usernames and passwords given in the UI

    protected HttpHeaders createHeaders(String instanceUrl) {
        String authHeader = null;
        int i = bambooDeploySettings.getServers().indexOf(instanceUrl);
        String auth = bambooDeploySettings.getUsernames().get(i) + ":" + bambooDeploySettings.getPasswords().get(i);
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.US_ASCII));
        authHeader = "Basic " + new String(encodedAuth);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        return headers;
    }

    private JSONObject parseAsObject(ResponseEntity<String> response) {
        if (response == null)
            return new JSONObject();
        try {
            return (JSONObject) new JSONParser().parse(response.getBody());
        } catch (ParseException pe) {
            LOGGER.debug(response.getBody());
            LOGGER.error(pe.getMessage());
        }
        return new JSONObject();
    }

    private JSONArray paresAsArray(ResponseEntity<String> response) {
        if (response == null)
            return new JSONArray();
        try {
            return (JSONArray) new JSONParser().parse(response.getBody());
        } catch (ParseException pe) {
            LOGGER.debug(response.getBody());
            LOGGER.error(pe.getMessage());
        }
        return new JSONArray();
    }

    private String str(JSONObject json, String key) {
        Object value = json.get(key);
        return value == null ? null : value.toString();
    }
}
