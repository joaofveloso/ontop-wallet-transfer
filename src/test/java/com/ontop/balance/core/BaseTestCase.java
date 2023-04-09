package com.ontop.balance.core;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

public abstract class BaseTestCase {

    @BeforeEach
    void init_mocks() throws Exception {

        try (AutoCloseable closeable = MockitoAnnotations.openMocks(this)){
            // Empty on purpose
        }
    }

}
