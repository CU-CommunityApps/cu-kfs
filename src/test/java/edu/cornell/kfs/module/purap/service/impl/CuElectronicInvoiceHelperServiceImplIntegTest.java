package edu.cornell.kfs.module.purap.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.kfs;

import java.io.File;
import java.io.FileWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.module.purap.batch.ElectronicInvoiceInputFileType;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceLoad;
import org.kuali.kfs.module.purap.document.ElectronicInvoiceRejectDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.IntegTestUtils;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentTestUtils;
import org.kuali.kfs.sys.fixture.UserNameFixture;

import edu.cornell.kfs.module.purap.fixture.CuElectronicInvoiceHelperServiceFixture;
import edu.cornell.kfs.module.purap.fixture.PurchaseOrderFixture;
import edu.cornell.kfs.module.purap.fixture.RequisitionFixture;

@ConfigureContext(session = UserNameFixture.kfs)
public class CuElectronicInvoiceHelperServiceImplIntegTest extends KualiIntegTestBase {

	private static final Logger LOG = LogManager.getLogger();

    private CuElectronicInvoiceHelperServiceImpl cuElectronicInvoiceHelperService;
    private DocumentService documentService;
    private ElectronicInvoiceInputFileType electronicInvoiceInputFileType;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        cuElectronicInvoiceHelperService = (CuElectronicInvoiceHelperServiceImpl) IntegTestUtils.getUnproxiedService("electronicInvoiceHelperService");
        documentService = SpringContext.getBean(DocumentService.class);
        electronicInvoiceInputFileType = SpringContext.getBean(ElectronicInvoiceInputFileType.class);

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testRejectDocumentCreationInvalidData() throws Exception {

        String rejectFile = "reject.xml";
        RequisitionDocument reqDoc = RequisitionFixture.REQ_NON_B2B_WITH_ITEMS.createRequisition();
        Integer reqId = reqDoc.getPurapDocumentIdentifier();

        PurchaseOrderDocument poDocument = createPODoc(reqId);
        poDocument.setVendorShippingPaymentTermsCode("AL");
        poDocument.setVendorPaymentTermsCode("00N30");
        poDocument.refreshNonUpdateableReferences();
        AccountingDocumentTestUtils.saveDocument(poDocument, documentService);

        String poNumber = String.valueOf(poDocument.getPurapDocumentIdentifier());
        String vendorDUNS = "055646846";
        String xmlChunk = CuElectronicInvoiceHelperServiceFixture.getCXMLForRejectDocCreation(vendorDUNS, poNumber);
        writeXMLFile(xmlChunk, rejectFile);

        ElectronicInvoiceLoad load = cuElectronicInvoiceHelperService.loadElectronicInvoices();

        assertTrue(load.containsRejects());

        ElectronicInvoiceRejectDocument rejectDoc = (ElectronicInvoiceRejectDocument) load.getRejectDocuments().get(0);
        assertNotNull(rejectDoc);
        assertEquals(rejectDoc.getInvoiceFileName(), rejectFile);
        assertEquals(1, rejectDoc.getInvoiceRejectReasons().size());

        File rejectedFileInRejectDir = new File(electronicInvoiceInputFileType.getDirectoryPath() + File.separator + "reject" + File.separator + rejectFile);
        assertTrue(rejectedFileInRejectDir.exists());

    }

    public void testPaymentRequestDocumentCreation() throws Exception {

        String acceptFile = "accept.xml";

        changeCurrentUser(UserNameFixture.ccs1);
        RequisitionDocument reqDoc = RequisitionFixture.REQ_NON_B2B_WITH_ITEMS.createRequisition();
        Integer reqId = reqDoc.getPurapDocumentIdentifier();

        changeCurrentUser(kfs);
        PurchaseOrderDocument poDocument = createPODoc(reqId);
        poDocument.setVendorShippingPaymentTermsCode("AL");
        poDocument.setVendorPaymentTermsCode("00N30");
        poDocument.refreshNonUpdateableReferences();
        AccountingDocumentTestUtils.saveDocument(poDocument, documentService);

        String poNumber = String.valueOf(poDocument.getPurapDocumentIdentifier());
        String vendorDUNS = "133251074";
        String xmlChunk = CuElectronicInvoiceHelperServiceFixture.getCXMLForPaymentDocCreation(vendorDUNS, poNumber);
        writeXMLFile(xmlChunk, acceptFile);

        ElectronicInvoiceLoad load = cuElectronicInvoiceHelperService.loadElectronicInvoices();

        assertFalse(load.containsRejects());
        File acceptedFileInAcceptDir = new File(electronicInvoiceInputFileType.getDirectoryPath() + File.separator + "accept" + File.separator + acceptFile);
        assertTrue(acceptedFileInAcceptDir.exists());
    }

    private PurchaseOrderDocument createPODoc(Integer reqId) throws Exception {
        PurchaseOrderDocument poDocument = PurchaseOrderFixture.PO_NON_B2B_OPEN_WITH_ITEMS.createPurchaseOrderdDocument(documentService);
        if (reqId != null) {
            poDocument.setRequisitionIdentifier(reqId);
        }

        return poDocument;
    }

    private void writeXMLFile(String xmlChunk, String fileName) throws Exception {
        FileWriter fileWriter = new FileWriter(new File(electronicInvoiceInputFileType.getDirectoryPath() + File.separator + fileName));
        fileWriter.write(xmlChunk);
        fileWriter.flush();
        fileWriter.close();
    }

}
