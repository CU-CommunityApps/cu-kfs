package edu.cornell.kfs.module.purap.businessobject.xml;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import edu.cornell.kfs.sys.util.CuXMLUnitTestUtils;
import jakarta.xml.bind.JAXBException;

@Execution(ExecutionMode.SAME_THREAD)
public class TestIWantXmlObjects {
    private static final Logger LOG = LogManager.getLogger();
    
    private static final String INPUT_FILE_PATH = "src/test/resources/edu/cornell/kfs/module/purap/businessobject/xml/";
    private static final String MAXIMO_INPUT_FILE_NAME = "maximo-iwant-example.xml";
    private static final String IWANT_INPUT_FILE_NAME = "iwant-example.xml";
    
    private static final String OUTPUT_FILE_PATH = "test/iwantdocxmltest/";
    private static final String MAXIMO_OUTPUT_FILE_NAME = "maximoOutputFile.xml";
    private static final String IWANT_OUTPUT_FILE_NAME = "iwantOutputFile.xml";
    
    private File outputFileDirectory;
    
    private CUMarshalServiceImpl marshalservice;

    @BeforeEach
    void setUp() throws Exception {
        marshalservice = new CUMarshalServiceImpl();
        outputFileDirectory = new File(OUTPUT_FILE_PATH);
        FileUtils.forceMkdir(outputFileDirectory);
    }

    @AfterEach
    void tearDown() throws Exception {
        marshalservice = null;
        FileUtils.forceDelete(outputFileDirectory.getAbsoluteFile());
    }

    @Test
    public void testMaximoExample() throws JAXBException, IOException {
        File maximoFile = new File(INPUT_FILE_PATH + MAXIMO_INPUT_FILE_NAME);
        IWantDocumentWrapperXml iWantDocFile = marshalservice.unmarshalFile(maximoFile, IWantDocumentWrapperXml.class);
        LOG.info("testMaximoExample, iWantDocFile: " + iWantDocFile.toString());
        String outputFileName = OUTPUT_FILE_PATH + MAXIMO_OUTPUT_FILE_NAME;
        marshalservice.marshalObjectToXMLFragment(iWantDocFile, outputFileName);
        
        File outputMaximoFile = new File(outputFileName);
        
        CuXMLUnitTestUtils.compareXML(maximoFile, outputMaximoFile);
    }
    
    @Test
    public void testIwantDocFromObject() throws JAXBException, IOException {
        File iwantExampleFile = new File(INPUT_FILE_PATH + IWANT_INPUT_FILE_NAME);
        
        IWantDocumentWrapperXml docFile = buildIWantDocFile();
        LOG.info("testIwantDocFromObject. docFile : " + docFile.toString());
        
        String outputFileName = OUTPUT_FILE_PATH + IWANT_OUTPUT_FILE_NAME;
        marshalservice.marshalObjectToXMLFragment(docFile, outputFileName);
        
        File outputMaximoFile = new File(outputFileName);
        
        CuXMLUnitTestUtils.compareXML(iwantExampleFile, outputMaximoFile);
        
    }
    
    private IWantDocumentWrapperXml buildIWantDocFile() {
        IWantDocumentWrapperXml file = new IWantDocumentWrapperXml();
        file.getiWantDocuments().add(buildIWantDocument());
        return file;
    }
    
    private IWantDocumentXml buildIWantDocument() {
        IWantDocumentXml doc = new IWantDocumentXml();
        doc.setAccountDescriptionTxt("account description");
        doc.setAdHocRouteToNetID("se12");
        doc.setBusinessPurpose("business purpose");
        doc.setCollegeLevelOrganization("college org");
        doc.setCommentsAndSpecialInstructions("special instructions");
        doc.setDeliverToAddress("deliver address");
        doc.setDeliverToEmailAddress("ccs1@cornell.edu");
        doc.setDeliverToNetID("ccs1");
        doc.setDeliverToPhoneNumber("607-255-9900");
        doc.setDepartmentLevelOrganization("department org");
        doc.setGoods(IWantIndicatorTypeXml.Y);
        doc.setInitiator("ccs1");
        doc.setRequestorAddress("req address");
        doc.setRequestorEmailAddress("jdh34@cornell.edu");
        doc.setRequestorNetID("jdh34");
        doc.setRequestorPhoneNumber("6072559900");
        doc.setSameAsRequestor(IWantIndicatorTypeXml.N);
        doc.setServicePerformedOnCampus(IWantIndicatorTypeXml.Y);
        doc.setSourceNumber("source number");
        doc.setVendorDescription("vendor description");
        doc.setVendorId("vendor id");
        doc.setVendorName("vendor name");
        doc.getAccounts().add(buildAccount());
        doc.getAttachments().add(buildAttachment());
        doc.getItems().add(buildItem());
        doc.getNotes().add(buildNote());
        return doc;
    }
    
    private IWantTransactionLineXml buildAccount() {
        IWantTransactionLineXml account = new IWantTransactionLineXml();
        account.setAccountNumber("account");
        account.setAmountOrPercent(BigDecimal.valueOf(666.66));
        account.setChartOfAccountsCode("chart");
        account.setFinancialObjectCode("object code");
        account.setFinancialSubObjectCode("sub object code");
        account.setOrganizationReferenceId("reference id");
        account.setProjectCode("project code");
        account.setSubAccountNumber("sub account");
        account.setUseAmountOrPercent(IWantAmountOrPercentTypeXml.A);
        return account;
    }
    
    private IWantAttachmentXml buildAttachment() {
        IWantAttachmentXml attach = new IWantAttachmentXml();
        attach.setAttachmentType("attachment type");
        attach.setFileName("file name");
        attach.setMimeTypeCode("mime type");
        return attach;
    }
    
    private IWantItemXml buildItem() {
        IWantItemXml item = new IWantItemXml();
        item.setItemCatalogNumber("cat number");
        item.setItemDescription("item description");
        item.setItemQuantity(BigDecimal.valueOf(2.0));
        item.setItemUnitOfMeasureCode("unit of measure");
        item.setItemUnitPrice(BigDecimal.valueOf(5.29));
        item.setPurchasingCommodityCode("code");
        return item;
    }
    
    private IWantNoteXml buildNote() {
        IWantNoteXml note = new IWantNoteXml();
        note.setNoteText("note text");
        return note;
    }

}
