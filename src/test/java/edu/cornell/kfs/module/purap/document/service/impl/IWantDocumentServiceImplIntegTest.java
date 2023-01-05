package edu.cornell.kfs.module.purap.document.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.fp.document.web.struts.DisbursementVoucherForm;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.web.struts.RequisitionForm;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.fixture.UserNameFixture;

import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.document.IWantDocument;
import edu.cornell.kfs.module.purap.document.service.IWantDocumentService;
import edu.cornell.kfs.module.purap.fixture.IWantDocumentFixture;

@ConfigureContext(session = UserNameFixture.ccs1)
public class IWantDocumentServiceImplIntegTest extends KualiIntegTestBase {

	private static final Logger LOG = LogManager.getLogger();
	
	private static final String I_WANT_DOC_NBR_THAT_CREATED_DV = "5252439";
	private static final String DV_DOC_NBR_FROM_I_WANT_DOC = "5266453";

	private IWantDocumentService iWantDocumentService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		iWantDocumentService = SpringContext.getBean(IWantDocumentService.class);
	}

	public void testSetUpRequisitionDetailsFromIWantDoc() throws Exception {
		IWantDocument iWantDocument = IWantDocumentFixture.I_WANT_DOC.createIWantDocument();
		RequisitionDocument requisitionDocumentBase = (RequisitionDocument) SpringContext.getBean(DocumentService.class).getNewDocument(RequisitionDocument.class);
		requisitionDocumentBase.initiateDocument();
		RequisitionForm requisitionForm = new RequisitionForm();
		iWantDocumentService.setUpRequisitionDetailsFromIWantDoc(iWantDocument, requisitionDocumentBase, requisitionForm);

		assertEquals(CUPurapConstants.RequisitionSources.IWNT, requisitionDocumentBase.getRequisitionSourceCode());
		assertEquals(iWantDocument.getVendorHeaderGeneratedIdentifier(), requisitionDocumentBase.getVendorHeaderGeneratedIdentifier());
	}

	public void testSetUpDVDetailsFromIWantDoc() throws Exception {
		IWantDocument iWantDocument = IWantDocumentFixture.I_WANT_DOC.createIWantDocument();
		CuDisbursementVoucherDocument disbursementVoucherDocument = (CuDisbursementVoucherDocument) SpringContext.getBean(DocumentService.class).getNewDocument(CuDisbursementVoucherDocument.class);
		disbursementVoucherDocument.initiateDocument();
		DisbursementVoucherForm disbursementVoucherForm = new DisbursementVoucherForm();
		iWantDocumentService.setUpDVDetailsFromIWantDoc(iWantDocument,disbursementVoucherDocument, disbursementVoucherForm);

		assertEquals(1, disbursementVoucherDocument.getSourceAccountingLines().size());
		assertEquals(iWantDocument.getTotalDollarAmount(), disbursementVoucherDocument.getDisbVchrCheckTotalAmount());
	}

	public void testGetIWantDocIDByDVId() throws Exception {
		
		String retrievedDoc = iWantDocumentService.getIWantDocIDByDVId(DV_DOC_NBR_FROM_I_WANT_DOC);

		assertEquals(I_WANT_DOC_NBR_THAT_CREATED_DV, retrievedDoc);

	}

	public void testIsDVgeneratedByIWantDoc() throws Exception {
		
		boolean result = iWantDocumentService.isDVgeneratedByIWantDoc(DV_DOC_NBR_FROM_I_WANT_DOC);

		assertTrue(result);

	}
}
