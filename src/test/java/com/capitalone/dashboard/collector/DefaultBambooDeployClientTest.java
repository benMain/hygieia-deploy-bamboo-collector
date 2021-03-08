package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.Environment;
import com.capitalone.dashboard.model.BambooDeployApplication;
import com.capitalone.dashboard.model.BambooDeployEnvResCompData;
import com.capitalone.dashboard.model.BambooEnvironmentResults;
import com.capitalone.dashboard.util.Supplier;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultBambooDeployClientTest {
        @Mock
        private Supplier<RestOperations> restOperationsSupplier;
        @Mock
        private RestOperations restOps;
        @Mock
        private BambooDeploySettings settings;

        private DefaultBambooDeployClient defaultBambooDeployClient;

        // private static final String URL = "URL";

        @Before
        public void init() {
                when(restOperationsSupplier.get()).thenReturn(restOps);
                defaultBambooDeployClient = new DefaultBambooDeployClient(settings, restOperationsSupplier);
        }

        @Test
        public void testGetApplications() throws Exception {
                String appJson = getJson("application.json");

                String instanceUrl = "https://bamboo.com/";
                String appListUrl = "https://bamboo.com/rest/api/latest/deploy/project/all";

                when(restOps.exchange(eq(appListUrl), eq(HttpMethod.GET), Matchers.any(HttpEntity.class),
                                eq(String.class))).thenReturn(new ResponseEntity<>(appJson, HttpStatus.OK));
                when(settings.getServers()).thenReturn(Arrays.asList(instanceUrl));
                when(settings.getUsernames()).thenReturn(Arrays.asList("Username"));
                when(settings.getPasswords()).thenReturn(Arrays.asList("password"));
                List<BambooDeployApplication> apps = defaultBambooDeployClient.getApplications(instanceUrl);
                verify(restOps, times(1)).exchange(eq(appListUrl), eq(HttpMethod.GET), Matchers.any(HttpEntity.class),
                                eq(String.class));
                assertEquals(2, apps.size());
                assertEquals("834Aggregator Deployment", apps.get(0).getApplicationName());
                assertEquals("834Aggregator Pharmacy Deployment", apps.get(1).getApplicationName());

        }

        @Test
        public void testGetEnvironments() throws Exception {
                String appJson = getJson("application.json");

                String instanceUrl = "https://bamboo.com/";
                String appListUrl = "https://bamboo.com/rest/api/latest/deploy/project/all";

                when(restOps.exchange(eq(appListUrl), eq(HttpMethod.GET), Matchers.any(HttpEntity.class),
                                eq(String.class))).thenReturn(new ResponseEntity<>(appJson, HttpStatus.OK));
                when(settings.getServers()).thenReturn(Arrays.asList(instanceUrl));
                when(settings.getUsernames()).thenReturn(Arrays.asList("Username"));
                when(settings.getPasswords()).thenReturn(Arrays.asList("password"));
                List<BambooDeployApplication> apps = defaultBambooDeployClient.getApplications(instanceUrl);

                verify(restOps, times(1)).exchange(eq(appListUrl), eq(HttpMethod.GET), Matchers.any(HttpEntity.class),
                                eq(String.class));

                String environments = getJson("environments.json");
                String envUrl = "https://bamboo.com/rest/api/latest/deploy/project/66814034";

                when(restOps.exchange(eq(envUrl), eq(HttpMethod.GET), Matchers.any(HttpEntity.class), eq(String.class)))
                                .thenReturn(new ResponseEntity<>(environments, HttpStatus.OK));
                List<Environment> envs = defaultBambooDeployClient.getEnvironments(apps.get(0));

                verify(restOps, times(1)).exchange(eq(envUrl), eq(HttpMethod.GET), Matchers.any(HttpEntity.class),
                                eq(String.class));

                assertEquals(5, envs.size());
                assertEquals("DEV", envs.get(0).getName());
                assertEquals("66912615", envs.get(0).getId());

        }

        @Test
        public void testGetEnvironmentResourceStatusData() throws Exception {
                String appJson = getJson("application.json");

                String instanceUrl = "https://bamboo.com/";
                String appListUrl = "https://bamboo.com/rest/api/latest/deploy/project/all";

                when(restOps.exchange(eq(appListUrl), eq(HttpMethod.GET), Matchers.any(HttpEntity.class),
                                eq(String.class))).thenReturn(new ResponseEntity<>(appJson, HttpStatus.OK));
                when(settings.getServers()).thenReturn(Arrays.asList(instanceUrl));
                when(settings.getUsernames()).thenReturn(Arrays.asList("Username"));
                when(settings.getPasswords()).thenReturn(Arrays.asList("password"));
                List<BambooDeployApplication> apps = defaultBambooDeployClient.getApplications(instanceUrl);

                verify(restOps, times(1)).exchange(eq(appListUrl), eq(HttpMethod.GET), Matchers.any(HttpEntity.class),
                                eq(String.class));

                String environments = getJson("environments.json");
                String envUrl = "https://bamboo.com/rest/api/latest/deploy/project/66814034";

                when(restOps.exchange(eq(envUrl), eq(HttpMethod.GET), Matchers.any(HttpEntity.class), eq(String.class)))
                                .thenReturn(new ResponseEntity<>(environments, HttpStatus.OK));
                List<Environment> envs = defaultBambooDeployClient.getEnvironments(apps.get(0));

                verify(restOps, times(1)).exchange(eq(envUrl), eq(HttpMethod.GET), Matchers.any(HttpEntity.class),
                                eq(String.class));

                String envResultsUrl = "https://bamboo.com/rest/api/latest/deploy/environment/" + envs.get(0).getId()
                                + "/results";

                String environmentResults = getJson("environment-results.json");
                BambooEnvironmentResults parsedEnvironmentResults = null;
                ObjectMapper mapper = new ObjectMapper();
                parsedEnvironmentResults = mapper.readValue(environmentResults, BambooEnvironmentResults.class);

                when(restOps.exchange(eq(envResultsUrl), eq(HttpMethod.GET), Matchers.any(HttpEntity.class),
                                eq(BambooEnvironmentResults.class))).thenReturn(
                                                new ResponseEntity<BambooEnvironmentResults>(parsedEnvironmentResults,
                                                                HttpStatus.OK));

                List<BambooDeployEnvResCompData> data = defaultBambooDeployClient
                                .getEnvironmentResourceStatusData(apps.get(0), envs.get(0));

                verify(restOps, times(1)).exchange(eq(envResultsUrl), eq(HttpMethod.GET),
                                Matchers.any(HttpEntity.class), eq(BambooEnvironmentResults.class));

                assertEquals(data.size(), 2);
                assertEquals("Executable", data.get(0).getComponentName());
                assertEquals("release-7", data.get(0).getResourceName());
                assertEquals(true, data.get(0).isDeployed());
                assertEquals(true, data.get(0).isOnline());
                assertEquals("DEV", data.get(0).getEnvironmentName());

        }

        private String getJson(String fileName) throws IOException {
                InputStream inputStream = DefaultBambooDeployClientTest.class.getResourceAsStream(fileName);
                return IOUtils.toString(inputStream);
        }
}