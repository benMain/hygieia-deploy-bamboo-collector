package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.Environment;
import com.capitalone.dashboard.repository.BambooDeployApplicationRepository;
import com.capitalone.dashboard.repository.BambooDeployCollectorRepository;
import com.capitalone.dashboard.repository.BaseCollectorRepository;
import com.capitalone.dashboard.repository.ComponentRepository;
import com.capitalone.dashboard.repository.ConfigurationRepository;
import com.capitalone.dashboard.repository.EnvironmentComponentRepository;
import com.capitalone.dashboard.repository.EnvironmentStatusRepository;
import com.capitalone.dashboard.model.BambooDeployApplication;
import com.capitalone.dashboard.model.BambooDeployCollector;
import com.capitalone.dashboard.model.Component;
import com.capitalone.dashboard.model.Configuration;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.scheduling.TaskScheduler;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BambooDeployCollectorTaskTest {

    @Mock
    private TaskScheduler taskScheduler;
    @Mock
    private BambooDeployCollectorRepository bambooDeployCollectorRepository;
    @Mock
    private BambooDeployApplicationRepository bambooDeployApplicationRepository;
    @Mock
    private EnvironmentComponentRepository envComponentRepository;
    @Mock
    private EnvironmentStatusRepository environmentStatusRepository;
    @Mock
    private BambooDeploySettings bambooDeploySettings;
    @Mock
    BambooDeployClient bambooDeployClient;
    @Mock
    ConfigurationRepository configurationRepository;
    @Mock
    ComponentRepository dbComponentRepository;
    @InjectMocks
    BambooDeployCollectorTask collectorTask;

    private static String BAMBOO_URL = "https://bamboo.com/";
    private static String BAMBOO_SERVER_NICE_NAME = "CompanyABamboo";
    private static String BAMBOO_USER = "fakey";
    private static String BAMBOO_PASSWORD = "password9";
    private static String APP_ONE_ID = "123";
    private static String APP_TWO_ID = "456";
    private static ObjectId COLLECTOR_ID = new ObjectId();

    @Before
    public void init() {

    }

    @Test
    public void testGetCollector() {
        Configuration config = new Configuration();
        config.setInfo(getConfigInfo());
        when(configurationRepository.findByCollectorName("BambooDeploy")).thenReturn(config);
        when(bambooDeploySettings.getServers()).thenReturn(new ArrayList<String>()).thenReturn(new ArrayList<String>())
                .thenReturn(new ArrayList<String>(Arrays.asList(BAMBOO_URL)));

        when(bambooDeploySettings.getUsernames()).thenReturn(new ArrayList<String>());
        when(bambooDeploySettings.getPasswords()).thenReturn(new ArrayList<String>());
        when(bambooDeploySettings.getNiceNames()).thenReturn(Arrays.asList(BAMBOO_SERVER_NICE_NAME));
        BambooDeployCollector collector = collectorTask.getCollector();
        assertEquals(collector.getBambooServers().get(0), BAMBOO_URL);
        assertEquals(collector.getNiceNames().get(0), BAMBOO_SERVER_NICE_NAME);
        verify(configurationRepository, times(1)).findByCollectorName("BambooDeploy");
        verify(bambooDeploySettings, times(3)).getServers();
        verify(bambooDeploySettings, times(2)).getUsernames();
        verify(bambooDeploySettings, times(2)).getPasswords();
    }

    @Test
    public void testGetCollectorRepository() {
        when(bambooDeployCollectorRepository.count()).thenReturn(1000L);
        BaseCollectorRepository<BambooDeployCollector> repo = collectorTask.getCollectorRepository();
        assertEquals(bambooDeployCollectorRepository.count(), repo.count());
    }

    @Test
    public void testGetCron() {
        when(bambooDeploySettings.getCron()).thenReturn("12 11 * * 1-5");
        String cron = collectorTask.getCron();
        assertEquals("12 11 * * 1-5", cron);
        verify(bambooDeploySettings, times(1)).getCron();
    }

    @Test
    public void testCollect() {
        when(dbComponentRepository.findAll()).thenReturn(new ArrayList<Component>());
        when(bambooDeployApplicationRepository.findByCollectorIdIn(Matchers.anyObject()))
                .thenReturn(new ArrayList<BambooDeployApplication>());
        when(bambooDeployClient.getApplications(Matchers.any(String.class))).thenReturn(getAllApplications());
        when(bambooDeployApplicationRepository.findBambooDeployApplication(COLLECTOR_ID, BAMBOO_URL, APP_ONE_ID))
                .thenReturn(getExistingApp());
        when(bambooDeployApplicationRepository.findBambooDeployApplication(COLLECTOR_ID, BAMBOO_URL, APP_TWO_ID))
                .thenReturn(null);
        when(bambooDeployApplicationRepository.findEnabledApplications(COLLECTOR_ID, BAMBOO_URL))
                .thenReturn(new ArrayList<BambooDeployApplication>());
        collectorTask.collect(getCollector());
    }

    private BambooDeployCollector getCollector() {
        BambooDeployCollector collector = new BambooDeployCollector();
        collector.getBambooServers().add(BAMBOO_URL);
        collector.setId(COLLECTOR_ID);
        return collector;
    }

    private Set<Map<String, String>> getConfigInfo() {
        Set<Map<String, String>> configInfo = new HashSet<>();
        Map<String, String> config = new HashMap<>();
        config.put("url", BAMBOO_URL);
        config.put("username", BAMBOO_USER);
        config.put("password", BAMBOO_PASSWORD);
        configInfo.add(config);
        return configInfo;
    }

    private BambooDeployApplication getExistingApp() {
        BambooDeployApplication app = new BambooDeployApplication();
        app.setInstanceUrl(BAMBOO_URL);
        app.setApplicationName("Super App");
        app.setApplicationId(APP_ONE_ID);
        return app;
    }

    private List<BambooDeployApplication> getAllApplications() {
        List<BambooDeployApplication> apps = new ArrayList<BambooDeployApplication>();
        BambooDeployApplication appOne = new BambooDeployApplication();
        appOne.setInstanceUrl(BAMBOO_URL);
        appOne.setApplicationName("Super App");
        appOne.setApplicationId(APP_ONE_ID);
        apps.add(appOne);

        BambooDeployApplication appTwo = new BambooDeployApplication();
        appTwo.setInstanceUrl(BAMBOO_URL);
        appTwo.setApplicationName("Lame App");
        appTwo.setApplicationId(APP_TWO_ID);
        apps.add(appTwo);
        return apps;
    }
}
