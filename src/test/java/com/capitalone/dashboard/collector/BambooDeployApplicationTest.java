package com.capitalone.dashboard.collector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.capitalone.dashboard.model.BambooDeployApplication;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BambooDeployApplicationTest {

    @Test
    public void testEquality() {
        String appId = "4663215689";
        String instanceUrl = "https://bamboo.mycompany.com";
        BambooDeployApplication appOne = new BambooDeployApplication();
        appOne.setApplicationId(appId);
        appOne.setInstanceUrl(instanceUrl);
        BambooDeployApplication appTwo = new BambooDeployApplication();
        appTwo.setApplicationId(appId);
        appTwo.setInstanceUrl(instanceUrl);
        assertTrue("Equality override", appOne.equals(appTwo));
        appOne.hashCode();
    }
}
