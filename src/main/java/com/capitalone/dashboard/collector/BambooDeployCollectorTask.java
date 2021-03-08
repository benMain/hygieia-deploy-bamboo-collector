package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.CollectorItem;
import com.capitalone.dashboard.model.CollectorType;
import com.capitalone.dashboard.model.Configuration;
import com.capitalone.dashboard.model.Environment;
import com.capitalone.dashboard.model.EnvironmentComponent;
import com.capitalone.dashboard.model.EnvironmentStatus;
import com.capitalone.dashboard.model.BambooDeployApplication;
import com.capitalone.dashboard.model.BambooDeployCollector;
import com.capitalone.dashboard.model.BambooDeployEnvResCompData;
import com.capitalone.dashboard.repository.BaseCollectorRepository;
import com.capitalone.dashboard.repository.ComponentRepository;
import com.capitalone.dashboard.repository.ConfigurationRepository;
import com.capitalone.dashboard.repository.EnvironmentComponentRepository;
import com.capitalone.dashboard.repository.EnvironmentStatusRepository;
import com.capitalone.dashboard.repository.BambooDeployApplicationRepository;
import com.capitalone.dashboard.repository.BambooDeployCollectorRepository;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;

/**
 * Collects {@link EnvironmentComponent} and {@link EnvironmentStatus} data from
 * {@link BambooDeployApplication}s.
 */
@Component
public class BambooDeployCollectorTask extends CollectorTask<BambooDeployCollector> {
    @SuppressWarnings({ "unused", "PMD.UnusedPrivateField" })
    private static final Logger LOGGER = LoggerFactory.getLogger(BambooDeployCollectorTask.class);

    private final BambooDeployCollectorRepository bambooDeployCollectorRepository;
    private final BambooDeployApplicationRepository bambooDeployApplicationRepository;
    private final BambooDeployClient bambooDeployClient;
    private final BambooDeploySettings bambooDeploySettings;
    private final EnvironmentComponentRepository envComponentRepository;
    private final EnvironmentStatusRepository environmentStatusRepository;
    private final ConfigurationRepository configurationRepository;
    private final ComponentRepository dbComponentRepository;

    @SuppressWarnings("squid:S00107")
    @Autowired
    public BambooDeployCollectorTask(TaskScheduler taskScheduler,
            BambooDeployCollectorRepository bambooDeployCollectorRepository,
            BambooDeployApplicationRepository bambooDeployApplicationRepository,
            EnvironmentComponentRepository envComponentRepository,
            EnvironmentStatusRepository environmentStatusRepository, BambooDeploySettings bambooDeploySettings,
            BambooDeployClient bambooDeployClient, ConfigurationRepository configurationRepository,
            ComponentRepository dbComponentRepository) {
        super(taskScheduler, "BambooDeploy");
        this.bambooDeployCollectorRepository = bambooDeployCollectorRepository;
        this.bambooDeployApplicationRepository = bambooDeployApplicationRepository;
        this.bambooDeploySettings = bambooDeploySettings;
        this.bambooDeployClient = bambooDeployClient;
        this.envComponentRepository = envComponentRepository;
        this.environmentStatusRepository = environmentStatusRepository;
        this.dbComponentRepository = dbComponentRepository;
        this.configurationRepository = configurationRepository;
    }

    @Override
    public BambooDeployCollector getCollector() {
        Configuration config = configurationRepository.findByCollectorName("BambooDeploy");
        if (config != null) {
            config.decryptOrEncrptInfo();
            // TO clear the username and password from existing run and
            // pick the latest
            bambooDeploySettings.getUsernames().clear();
            bambooDeploySettings.getServers().clear();
            bambooDeploySettings.getPasswords().clear();
            for (Map<String, String> bambooServer : config.getInfo()) {
                bambooDeploySettings.getServers().add(bambooServer.get("url"));
                bambooDeploySettings.getUsernames().add(bambooServer.get("userName"));
                bambooDeploySettings.getPasswords().add(bambooServer.get("password"));
            }
        }
        return BambooDeployCollector.prototype(bambooDeploySettings.getServers(), bambooDeploySettings.getNiceNames());
    }

    @Override
    public BaseCollectorRepository<BambooDeployCollector> getCollectorRepository() {
        return bambooDeployCollectorRepository;
    }

    @Override
    public String getCron() {
        return bambooDeploySettings.getCron();
    }

    @Override
    public void collect(BambooDeployCollector collector) {
        for (String instanceUrl : collector.getBambooServers()) {

            logBanner(instanceUrl);

            long start = System.currentTimeMillis();

            clean(collector);

            addNewApplications(bambooDeployClient.getApplications(instanceUrl), collector);
            updateData(enabledApplications(collector, instanceUrl));

            log("Finished", start);
        }
    }

    /**
     * Clean up unused deployment collector items
     *
     * @param collector the {@link BambooDeployCollector}
     */
    @SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
    private void clean(BambooDeployCollector collector) {
        deleteUnwantedJobs(collector);
        Set<ObjectId> uniqueIDs = new HashSet<>();
        for (com.capitalone.dashboard.model.Component comp : dbComponentRepository.findAll()) {
            if (comp.getCollectorItems() == null || comp.getCollectorItems().isEmpty())
                continue;
            List<CollectorItem> itemList = comp.getCollectorItems().get(CollectorType.Deployment);
            if (itemList == null)
                continue;
            for (CollectorItem ci : itemList) {
                if (ci == null)
                    continue;
                uniqueIDs.add(ci.getId());
            }
        }
        List<BambooDeployApplication> appList = new ArrayList<>();
        Set<ObjectId> udId = new HashSet<>();
        udId.add(collector.getId());
        for (BambooDeployApplication app : bambooDeployApplicationRepository.findByCollectorIdIn(udId)) {
            if (app != null) {
                app.setEnabled(uniqueIDs.contains(app.getId()));
                appList.add(app);
            }
        }
        bambooDeployApplicationRepository.save(appList);
    }

    private void deleteUnwantedJobs(BambooDeployCollector collector) {

        List<BambooDeployApplication> deleteAppList = new ArrayList<>();
        Set<ObjectId> udId = new HashSet<>();
        udId.add(collector.getId());
        for (BambooDeployApplication app : bambooDeployApplicationRepository.findByCollectorIdIn(udId)) {
            if (!collector.getBambooServers().contains(app.getInstanceUrl())
                    || (!app.getCollectorId().equals(collector.getId()))) {
                deleteAppList.add(app);
            }
        }

        bambooDeployApplicationRepository.delete(deleteAppList);

    }

    private List<EnvironmentComponent> getEnvironmentComponent(List<BambooDeployEnvResCompData> dataList,
            Environment environment, BambooDeployApplication application) {
        List<EnvironmentComponent> returnList = new ArrayList<>();
        for (BambooDeployEnvResCompData data : dataList) {
            EnvironmentComponent component = new EnvironmentComponent();
            component.setComponentName(data.getComponentName());
            component.setCollectorItemId(data.getCollectorItemId());
            component.setComponentVersion(data.getComponentVersion());
            component.setDeployed(data.isDeployed());
            component.setEnvironmentName(data.getEnvironmentName());

            component.setEnvironmentName(environment.getName());
            component.setAsOfDate(data.getAsOfDate());
            String environmentURL = StringUtils.removeEnd(application.getInstanceUrl(), "/")
                    + "/deploy/viewEnvironment.action?id=" + environment.getId();
            component.setEnvironmentUrl(environmentURL);

            returnList.add(component);
        }
        return returnList;
    }

    private List<EnvironmentStatus> getEnvironmentStatus(List<BambooDeployEnvResCompData> dataList) {
        List<EnvironmentStatus> returnList = new ArrayList<>();
        for (BambooDeployEnvResCompData data : dataList) {
            EnvironmentStatus status = new EnvironmentStatus();
            status.setCollectorItemId(data.getCollectorItemId());
            status.setComponentID(data.getComponentID());
            status.setComponentName(data.getComponentName());
            status.setEnvironmentName(data.getEnvironmentName());
            status.setOnline(data.isOnline());
            status.setResourceName(data.getResourceName());

            returnList.add(status);
        }
        return returnList;
    }

    /**
     * For each {@link BambooDeployApplication}, update the current
     * {@link EnvironmentComponent}s and {@link EnvironmentStatus}.
     *
     * @param bambooDeployApplications list of {@link BambooDeployApplication}s
     */
    private void updateData(List<BambooDeployApplication> bambooDeployApplications) {
        for (BambooDeployApplication application : bambooDeployApplications) {
            List<EnvironmentComponent> compList = new ArrayList<>();
            List<EnvironmentStatus> statusList = new ArrayList<>();
            long startApp = System.currentTimeMillis();

            for (Environment environment : bambooDeployClient.getEnvironments(application)) {

                List<BambooDeployEnvResCompData> combinedDataList = bambooDeployClient
                        .getEnvironmentResourceStatusData(application, environment);

                compList.addAll(getEnvironmentComponent(combinedDataList, environment, application));
                statusList.addAll(getEnvironmentStatus(combinedDataList));
            }
            if (!compList.isEmpty()) {
                List<EnvironmentComponent> existingComponents = envComponentRepository
                        .findByCollectorItemId(application.getId());
                envComponentRepository.delete(existingComponents);
                envComponentRepository.save(compList);
            }
            if (!statusList.isEmpty()) {
                List<EnvironmentStatus> existingStatuses = environmentStatusRepository
                        .findByCollectorItemId(application.getId());
                environmentStatusRepository.delete(existingStatuses);
                environmentStatusRepository.save(statusList);
            }

            log(" " + application.getApplicationName(), startApp);
        }
    }

    private List<BambooDeployApplication> enabledApplications(BambooDeployCollector collector, String instanceUrl) {
        return bambooDeployApplicationRepository.findEnabledApplications(collector.getId(), instanceUrl);
    }

    /**
     * Add any new {@link BambooDeployApplication}s.
     *
     * @param applications list of {@link BambooDeployApplication}s
     * @param collector    the {@link BambooDeployCollector}
     */
    private void addNewApplications(List<BambooDeployApplication> applications, BambooDeployCollector collector) {
        long start = System.currentTimeMillis();
        int count = 0;

        log("All apps", start, applications.size());
        for (BambooDeployApplication application : applications) {
            BambooDeployApplication existing = findExistingApplication(collector, application);

            String niceName = getNiceName(application, collector);
            if (existing == null) {
                application.setCollectorId(collector.getId());
                application.setEnabled(false);
                application.setDescription(application.getApplicationName());
                if (StringUtils.isNotEmpty(niceName)) {
                    application.setNiceName(niceName);
                }
                try {
                    bambooDeployApplicationRepository.save(application);
                } catch (org.springframework.dao.DuplicateKeyException ce) {
                    log("Duplicates items not allowed", 0);

                }
                count++;
            } else if (StringUtils.isEmpty(existing.getNiceName()) && StringUtils.isNotEmpty(niceName)) {
                existing.setNiceName(niceName);
                bambooDeployApplicationRepository.save(existing);
            }

        }
        log("New apps", start, count);
    }

    private BambooDeployApplication findExistingApplication(BambooDeployCollector collector,
            BambooDeployApplication application) {
        return bambooDeployApplicationRepository.findBambooDeployApplication(collector.getId(),
                application.getInstanceUrl(), application.getApplicationId());
    }

    private String getNiceName(BambooDeployApplication application, BambooDeployCollector collector) {
        if (CollectionUtils.isEmpty(collector.getBambooServers()))
            return "";
        List<String> servers = collector.getBambooServers();
        List<String> niceNames = collector.getNiceNames();
        if (CollectionUtils.isEmpty(niceNames))
            return "";
        for (int i = 0; i < servers.size(); i++) {
            if (servers.get(i).equalsIgnoreCase(application.getInstanceUrl()) && niceNames.size() > i) {
                return niceNames.get(i);
            }
        }
        return "";
    }

    @SuppressWarnings("unused")
    private boolean changed(EnvironmentStatus status, EnvironmentStatus existing) {
        return existing.isOnline() != status.isOnline();
    }

    @SuppressWarnings("unused")
    private EnvironmentStatus findExistingStatus(final EnvironmentStatus proposed,
            List<EnvironmentStatus> existingStatuses) {

        return Iterables.tryFind(existingStatuses, new Predicate<EnvironmentStatus>() {
            @Override
            public boolean apply(EnvironmentStatus existing) {
                return existing.getEnvironmentName().equals(proposed.getEnvironmentName())
                        && existing.getComponentName().equals(proposed.getComponentName())
                        && existing.getResourceName().equals(proposed.getResourceName());
            }
        }).orNull();
    }

    @SuppressWarnings("unused")
    private boolean changed(EnvironmentComponent component, EnvironmentComponent existing) {
        return existing.isDeployed() != component.isDeployed() || existing.getAsOfDate() != component.getAsOfDate()
                || !existing.getComponentVersion().equalsIgnoreCase(component.getComponentVersion());
    }

    @SuppressWarnings("unused")
    private EnvironmentComponent findExistingComponent(final EnvironmentComponent proposed,
            List<EnvironmentComponent> existingComponents) {

        return Iterables.tryFind(existingComponents, new Predicate<EnvironmentComponent>() {
            @Override
            public boolean apply(EnvironmentComponent existing) {
                return existing.getEnvironmentName().equals(proposed.getEnvironmentName())
                        && existing.getComponentName().equals(proposed.getComponentName());

            }
        }).orNull();
    }
}
