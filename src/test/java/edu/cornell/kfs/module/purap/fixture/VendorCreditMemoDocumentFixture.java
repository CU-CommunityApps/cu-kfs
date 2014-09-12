package edu.cornell.kfs.module.purap.fixture;

import java.sql.Date;

import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocumentTestUtils;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.krad.service.DocumentService;

public enum VendorCreditMemoDocumentFixture {

	VENDOR_CREDIT_MEMO("Description", 0, 4291, "12345", "12-5-2014",
			new KualiDecimal(100));

	public final String documentDescription;
	public final Integer vendorDetailAssignedIdentifier;
	public final Integer vendorHeaderGeneratedIdentifier;
	public final String creditMemoNumber;
	public final String creditMemoDate;

	public final KualiDecimal creditMemoAmount;

	// public final Timestamp creditMemoPaidTimestamp;
	// public final String itemMiscellaneousCreditDescription;
	// public final Date purchaseOrderEndDate;
	// public final String vendorAttentionName;

	private VendorCreditMemoDocumentFixture(String documentDescription,
			Integer vendorDetailAssignedIdentifier,
			Integer vendorHeaderGeneratedIdentifier, String creditMemoNumber,
			String creditMemoDate, KualiDecimal creditMemoAmount) {
		this.documentDescription = documentDescription;
		this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
		this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
		this.creditMemoNumber = creditMemoNumber;
		this.creditMemoDate = creditMemoDate;
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

}
