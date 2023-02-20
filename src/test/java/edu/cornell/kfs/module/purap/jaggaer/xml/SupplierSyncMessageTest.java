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
        supplierSyncMessage.setVersion("1.0");
        Header header = new Header();
        Authentication auth = new Authentication();
        auth.setIdentity("Cornell");
        auth.setSharedSecret("SuperCoolPassword");
        header.setAuthentication(auth);
        header.setMessageId("message id");
        header.setRelatedMessageId("related id");
        header.setTimestamp("20210218");
        supplierSyncMessage.setHeader(header);
        
        SupplierRequestMessage srm = new SupplierRequestMessage();
        Supplier supplier = new Supplier();
        Name name = new Name();
        name.setvalue("Acme Test Company");
        supplier.setName(name);
        srm.getSupplier().add(supplier);
        
        Supplier supplier2 = new Supplier();
        Name name2 = new Name();
        name2.setvalue("Acme Test Company oart 2");
        supplier2.setName(name);
        srm.getSupplier().add(supplier2);
        
        supplierSyncMessage.getSupplierRequestMessageOrSupplierResponseMessageOrLookupRequestMessageOrLookupResponseMessage().add(srm);
        
        String actualResults  = marshalService.marshalObjectToXmlString(supplierSyncMessage);
        LOG.info("actualResults: " + actualResults);
    }

}
