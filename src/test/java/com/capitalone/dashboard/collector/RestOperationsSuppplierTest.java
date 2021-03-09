package com.capitalone.dashboard.collector;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.client.RestOperations;

@RunWith(MockitoJUnitRunner.class)
public class RestOperationsSuppplierTest {

    @Test
    public void testRestSupplier() {
        RestOperationsSupplier supplier = new RestOperationsSupplier();
        RestOperations ops = supplier.get();
        assertNotNull(ops);
    }
}
