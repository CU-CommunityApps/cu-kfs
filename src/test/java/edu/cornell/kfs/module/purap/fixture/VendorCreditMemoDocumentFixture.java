package edu.cornell.kfs.module.purap.fixture;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.easymock.EasyMock;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentTestUtils;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.exception.WorkflowException;

import edu.cornell.kfs.module.cam.CuCamsTestConstants;
import edu.cornell.kfs.module.purap.document.CuVendorCreditMemoDocument;

public enum VendorCreditMemoDocumentFixture {

	VENDOR_CREDIT_MEMO("Description", 0, 4291, "12345", "12-05-2014",
			new KualiDecimal(100)),

	VENDOR_CREDIT_MEMO_5319793(CuCamsTestConstants.DOC_5319793, "Test CM 1", 0, 1234, 667, "24680", "12-01-2016", new KualiDecimal(50)),
	VENDOR_CREDIT_MEMO_5686500(CuCamsTestConstants.DOC_5686500, "Test CM 2", 1, 8888, 544, "77553", "05-15-2017", new KualiDecimal(33)),
	VENDOR_CREDIT_MEMO_5686501(CuCamsTestConstants.DOC_5686501, "Test CM 3", 1, 8899, null, "77335", "05-16-2017", new KualiDecimal(34));

	public static final String CM_DATE_FORMAT = "MM-dd-yyyy";

	public final String documentNumber;
	public final String documentDescription;
	public final Integer vendorDetailAssignedIdentifier;
	public final Integer vendorHeaderGeneratedIdentifier;
	public final Integer purchaseOrderIdentifier;
	public final String creditMemoNumber;
	public final String creditMemoDate;

	public final KualiDecimal creditMemoAmount;

	private VendorCreditMemoDocumentFixture(String documentDescription,
			Integer vendorDetailAssignedIdentifier,
			Integer vendorHeaderGeneratedIdentifier, String creditMemoNumber,
			String creditMemoDate, KualiDecimal creditMemoAmount) {
	    this("0", documentDescription, vendorDetailAssignedIdentifier, vendorHeaderGeneratedIdentifier, null,
	            creditMemoNumber, creditMemoDate, creditMemoAmount);
	}

	private VendorCreditMemoDocumentFixture(String documentNumber, String documentDescription,
	        Integer vendorDetailAssignedIdentifier,
	        Integer vendorHeaderGeneratedIdentifier, Integer purchaseOrderIdentifier, String creditMemoNumber,
	        String creditMemoDate, KualiDecimal creditMemoAmount) {
	    this.documentNumber = documentNumber;
	    this.documentDescription = documentDescription;
	    this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
	    this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
	    this.creditMemoNumber = creditMemoNumber;
	    this.creditMemoDate = creditMemoDate;
	    this.purchaseOrderIdentifier = purchaseOrderIdentifier;
	    this.creditMemoAmount = creditMemoAmount;
	}

	public VendorCreditMemoDocument createVendorCreditMemoDocument()
			throws WorkflowException {
		VendorCreditMemoDocument creditMemoDocument = (VendorCreditMemoDocument) SpringContext.getBean(DocumentService.class).getNewDocument(VendorCreditMemoDocument.class);
		creditMemoDocument.initiateDocument();

		creditMemoDocument.getDocumentHeader().setDocumentDescription(this.documentDescription);
		creditMemoDocument.setVendorDetailAssignedIdentifier(this.vendorDetailAssignedIdentifier);
		creditMemoDocument.setVendorHeaderGeneratedIdentifier(this.vendorHeaderGeneratedIdentifier);
		creditMemoDocument.setCreditMemoNumber(this.creditMemoNumber);
		creditMemoDocument.setCreditMemoDate(SpringContext.getBean(DateTimeService.class).getCurrentSqlDate());
		creditMemoDocument.setCreditMemoAmount(this.creditMemoAmount);

		creditMemoDocument.prepareForSave();
		AccountingDocumentTestUtils.saveDocument(creditMemoDocument, SpringContext.getBean(DocumentService.class));
		return creditMemoDocument;
	}

	public CuVendorCreditMemoDocument createVendorCreditMemoDocumentForMicroTest() {
	    CuVendorCreditMemoDocument creditMemoDocument = EasyMock.partialMockBuilder(CuVendorCreditMemoDocument.class)
	            .createNiceMock();
	    FinancialSystemDocumentHeader documentHeader = new FinancialSystemDocumentHeader();
	    EasyMock.replay(creditMemoDocument);
	    
	    documentHeader.setDocumentNumber(this.documentNumber);
	    documentHeader.setDocumentDescription(this.documentDescription);
	    creditMemoDocument.setDocumentHeader(documentHeader);
	    creditMemoDocument.setDocumentNumber(this.documentNumber);
	    creditMemoDocument.setVendorDetailAssignedIdentifier(this.vendorDetailAssignedIdentifier);
	    creditMemoDocument.setVendorHeaderGeneratedIdentifier(this.vendorHeaderGeneratedIdentifier);
	    creditMemoDocument.setPurchaseOrderIdentifier(this.purchaseOrderIdentifier);
	    creditMemoDocument.setCreditMemoNumber(this.creditMemoNumber);
	    creditMemoDocument.setCreditMemoDate(getParsedCreditMemoDate());
	    creditMemoDocument.setCreditMemoAmount(this.creditMemoAmount);
	    
	    return creditMemoDocument;
	}

	public java.sql.Date getParsedCreditMemoDate() {
	    try {
	        SimpleDateFormat dateFormat = new SimpleDateFormat(CM_DATE_FORMAT);
	        java.util.Date parsedDate = dateFormat.parse(this.creditMemoDate);
	        return new java.sql.Date(parsedDate.getTime());
	    } catch (ParseException e) {
	        throw new RuntimeException(e);
	    }
	}

}
