package edu.cornell.kfs.vnd.jsonobject;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import edu.cornell.kfs.sys.util.CUJsonUtils;
import edu.cornell.kfs.vnd.jsonobject.fixture.WorkdayKfsVendorLookupRootEnum;

class WorkdayKfsVendorLookupRootTest {
    private static final Logger LOG = LogManager.getLogger();
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = CUJsonUtils.buildObjectMapperUsingDefaultTimeZone();
    }

    @AfterEach
    void tearDown() throws Exception {
        objectMapper = null;
    }
    
    @ParameterizedTest
    @EnumSource
    public void testWorkdayKfsVendorLookupRoot(WorkdayKfsVendorLookupRootEnum rootEnum) throws StreamReadException, DatabindException, IOException {
        File jsonFile = new File(rootEnum.fileName);
        WorkdayKfsVendorLookupRoot actualLookupRoot = objectMapper.readValue(jsonFile, WorkdayKfsVendorLookupRoot.class);
        LOG.info("testWorkdayKfsVendorLookupRoot, results {}", actualLookupRoot.toString());
        assertEquals(rootEnum.toWorkdayKfsVendorLookupRoot(), actualLookupRoot);
    }

}

