package edu.cornell.kfs.module.purap.jaggaer.xml;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import jakarta.xml.bind.JAXBException;

class SupplierSyncMessageTest {
    private static final Logger LOG = LogManager.getLogger();
    
    private SupplierSyncMessage supplierSyncMessage;
    private CUMarshalService marshalService;

    @BeforeEach
    void setUpBeforeClass() throws Exception {
        supplierSyncMessage = new SupplierSyncMessage();
        marshalService = new CUMarshalServiceImpl();
    }

    @AfterEach
    void tearDownAfterClass() throws Exception {
        supplierSyncMessage = null;
        marshalService = null;
    }

    @Test
    void test() throws JAXBException, IOException {
        String actualResults  = marshalService.marshalObjectToXmlString(supplierSyncMessage);
        LOG.info("actualResults: " + actualResults);
    }

}
