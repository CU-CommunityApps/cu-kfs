package edu.cornell.kfs.module.purap.fixture;

import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.krad.service.DocumentService;

public enum PaymentRequestFixture {
	PAYMENT_REQ_DOC(new Integer(311), // purapDocumentIdentifier
			new Integer(21), // purchaseOrderIdentifier
			new Integer(2009), // postingYear
			"1001", // invoiceNumber
			new KualiDecimal(19000), // vendorInvoiceAmount
			"00N30", // vendorPaymentTermsCode
			"AL", // vendorShippingPaymentTermsCode
			"EST", // paymentRequestCostSourceCode
			false, // paymentRequestedCancelIndicator
			false, // paymentAttachmentIndicator
			false, // immediatePaymentIndicator
			false, // holdIndicator
			false, // paymentRequestElectronicInvoiceIndicator
			new Integer(5314), // vendorHeaderGeneratedIdentifier
			new Integer(0), // vendorDetailAssignedIdentifier
			"BESCO WATER TREATMENT INC", // vendorName
			"PO BOX 1309",// vendorLine1Address
			"BATTLE CREEK", // vendorCityName
			"MI", // vendorStateCode
			"49016-1309", // vendorPostalCode
			"US", // vendorCountryCode
			"2133704704", // accountsPayableProcessorIdentifier
			"IN", // processingCampusCode
			new Integer(5314), // originalVendorHeaderGeneratedIdentifier
			new Integer(0), // originalVendorDetailAssignedIdentifier
			false, // continuationAccountIndicator
			new Integer(21), // accountsPayablePurchasingDocumentLinkIdentifier
			false, // closePurchaseOrderIndicator
			false, // reopenPurchaseOrderIndicator
			false, // receivingDocumentRequiredIndicator
			false, // paymentRequestPositiveApprovalIndicator
			true // useTaxIndicator
	);

	public final Integer purapDocumentIdentifier;
	public final Integer purchaseOrderIdentifier;
	public final Integer postingYear;
	public final String invoiceNumber;
	public final KualiDecimal vendorInvoiceAmount;
	public final String vendorPaymentTermsCode;
	public final String vendorShippingPaymentTermsCode;
	// public final String paymentRequestPayDate(date);
	public final String paymentRequestCostSourceCode;
	public final boolean paymentRequestedCancelIndicator;
	public final boolean paymentAttachmentIndicator;
	public final boolean immediatePaymentIndicator;
	public final boolean holdIndicator;
	public final boolean paymentRequestElectronicInvoiceIndicator;
	public final Integer vendorHeaderGeneratedIdentifier;
	public final Integer vendorDetailAssignedIdentifier;

	public final String vendorName;
	public final String vendorLine1Address;
	public final String vendorCityName;
	public final String vendorStateCode;
	public final String vendorPostalCode;
	public final String vendorCountryCode;
	public final String accountsPayableProcessorIdentifier;
	// obj.setLastActionPerformedByPersonId("2133704704");
	public final String processingCampusCode;
	// obj.setAccountsPayableApprovalTimestamp(timeStamp);

	public final Integer originalVendorHeaderGeneratedIdentifier;
	public final Integer originalVendorDetailAssignedIdentifier;
	public final boolean continuationAccountIndicator;
	public final Integer accountsPayablePurchasingDocumentLinkIdentifier;
	public final boolean closePurchaseOrderIndicator;
	public final boolean reopenPurchaseOrderIndicator;
	public final boolean receivingDocumentRequiredIndicator;
	public final boolean paymentRequestPositiveApprovalIndicator;
	public final boolean useTaxIndicator;

	private PaymentRequestFixture(Integer purapDocumentIdentifier,
			Integer purchaseOrderIdentifier, Integer postingYear,
			String invoiceNumber, KualiDecimal vendorInvoiceAmount,
			String vendorPaymentTermsCode, String paymentRequestCostSourceCode,
			String vendorShippingPaymentTermsCode,
			boolean paymentRequestedCancelIndicator,
			boolean paymentAttachmentIndicator,
			boolean immediatePaymentIndicator, boolean holdIndicator,
			boolean paymentRequestElectronicInvoiceIndicator,
			Integer vendorHeaderGeneratedIdentifier,
			Integer vendorDetailAssignedIdentifier, String vendorName,
			String vendorLine1Address, String vendorCityName,
			String vendorStateCode, String vendorPostalCode,
			String vendorCountryCode,
			String accountsPayableProcessorIdentifier,
			String processingCampusCode,
			Integer originalVendorHeaderGeneratedIdentifier,
			Integer originalVendorDetailAssignedIdentifier,
			boolean continuationAccountIndicator,
			Integer accountsPayablePurchasingDocumentLinkIdentifier,
			boolean closePurchaseOrderIndicator,
			boolean reopenPurchaseOrderIndicator,
			boolean receivingDocumentRequiredIndicator,
			boolean paymentRequestPositiveApprovalIndicator,
			boolean useTaxIndicator) {
		this.purapDocumentIdentifier = purapDocumentIdentifier;
		this.purchaseOrderIdentifier = purchaseOrderIdentifier;
		this.postingYear = postingYear;
		this.invoiceNumber = invoiceNumber;
		this.vendorInvoiceAmount = vendorInvoiceAmount;
		this.vendorPaymentTermsCode = vendorPaymentTermsCode;

		this.paymentRequestCostSourceCode = paymentRequestCostSourceCode;
		this.vendorShippingPaymentTermsCode = vendorShippingPaymentTermsCode;
		this.paymentRequestedCancelIndicator = paymentRequestedCancelIndicator;
		this.paymentAttachmentIndicator = paymentAttachmentIndicator;
		this.immediatePaymentIndicator = immediatePaymentIndicator;
		this.holdIndicator = holdIndicator;
		this.paymentRequestElectronicInvoiceIndicator = paymentRequestElectronicInvoiceIndicator;
		this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
		this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;

		this.vendorName = vendorName;
		this.vendorLine1Address = vendorLine1Address;
		this.vendorCityName = vendorCityName;
		this.vendorStateCode = vendorStateCode;
		this.vendorPostalCode = vendorPostalCode;
		this.vendorCountryCode = vendorCountryCode;
		this.accountsPayableProcessorIdentifier = accountsPayableProcessorIdentifier;
		// obj.setLastActionPerformedByPersonId("2133704704");
		this.processingCampusCode = processingCampusCode;
		// obj.setAccountsPayableApprovalTimestamp(timeStamp);

		this.originalVendorHeaderGeneratedIdentifier = originalVendorHeaderGeneratedIdentifier;
		this.originalVendorDetailAssignedIdentifier = originalVendorDetailAssignedIdentifier;
		this.continuationAccountIndicator = continuationAccountIndicator;
		this.accountsPayablePurchasingDocumentLinkIdentifier = accountsPayablePurchasingDocumentLinkIdentifier;
		this.closePurchaseOrderIndicator = closePurchaseOrderIndicator;
		this.reopenPurchaseOrderIndicator = reopenPurchaseOrderIndicator;
		this.receivingDocumentRequiredIndicator = receivingDocumentRequiredIndicator;
		this.paymentRequestPositiveApprovalIndicator = paymentRequestPositiveApprovalIndicator;
		this.useTaxIndicator = useTaxIndicator;
	}

//	public PaymentRequestDocument createPaymentRequestDocument(Integer purchaseOrderIdentifier)
//			throws WorkflowException {
//		PaymentRequestDocument paymentRequestDocument = (PaymentRequestDocument) SpringContext
//				.getBean(DocumentService.class).getNewDocument("PREQ");
//
//		paymentRequestDocument
//				.setPurapDocumentIdentifier(purapDocumentIdentifier);
//		paymentRequestDocument
//				.setPurchaseOrderIdentifier(purchaseOrderIdentifier);
//		paymentRequestDocument.setPostingYear(postingYear);
//		paymentRequestDocument.setInvoiceNumber(invoiceNumber);
//		paymentRequestDocument.setVendorInvoiceAmount(vendorInvoiceAmount);
//		paymentRequestDocument
//				.setVendorPaymentTermsCode(vendorPaymentTermsCode);
//
//		paymentRequestDocument
//				.setPaymentRequestCostSourceCode(paymentRequestCostSourceCode);
//		paymentRequestDocument
//				.setVendorShippingPaymentTermsCode(vendorShippingPaymentTermsCode);
//		paymentRequestDocument
//				.setPaymentRequestedCancelIndicator(paymentRequestedCancelIndicator);
//		paymentRequestDocument
//				.setPaymentAttachmentIndicator(paymentAttachmentIndicator);
//		paymentRequestDocument
//				.setImmediatePaymentIndicator(immediatePaymentIndicator);
//		paymentRequestDocument.setHoldIndicator(holdIndicator);
//		paymentRequestDocument
//				.setPaymentRequestElectronicInvoiceIndicator(paymentRequestElectronicInvoiceIndicator);
//		paymentRequestDocument
//				.setVendorHeaderGeneratedIdentifier(vendorHeaderGeneratedIdentifier);
//		paymentRequestDocument
//				.setVendorDetailAssignedIdentifier(vendorDetailAssignedIdentifier);
//
//		paymentRequestDocument.setVendorName(vendorName);
//		paymentRequestDocument.setVendorLine1Address(vendorLine1Address);
//		paymentRequestDocument.setVendorCityName(vendorCityName);
//		paymentRequestDocument.setVendorStateCode(vendorStateCode);
//		paymentRequestDocument.setVendorPostalCode(vendorPostalCode);
//		paymentRequestDocument.setVendorCountryCode(vendorCountryCode);
//		paymentRequestDocument
//				.setAccountsPayableProcessorIdentifier(accountsPayableProcessorIdentifier);
//		paymentRequestDocument
//				.setLastActionPerformedByPersonId(UserNameFixture.mls398
//						.getPerson().getPrincipalId());// obj.setLastActionPerformedByPersonId("2133704704");
//		paymentRequestDocument.setProcessingCampusCode(processingCampusCode);
//		paymentRequestDocument
//				.setAccountsPayableApprovalTimestamp(SpringContext.getBean(
//						DateTimeService.class).getCurrentTimestamp());// obj.setAccountsPayableApprovalTimestamp(timeStamp);
//
//		paymentRequestDocument
//				.setOriginalVendorHeaderGeneratedIdentifier(originalVendorHeaderGeneratedIdentifier);
//		paymentRequestDocument
//				.setOriginalVendorDetailAssignedIdentifier(originalVendorDetailAssignedIdentifier);
//		paymentRequestDocument
//				.setContinuationAccountIndicator(continuationAccountIndicator);
//		paymentRequestDocument
//				.setAccountsPayablePurchasingDocumentLinkIdentifier(accountsPayablePurchasingDocumentLinkIdentifier);
//		paymentRequestDocument
//				.setClosePurchaseOrderIndicator(closePurchaseOrderIndicator);
//		paymentRequestDocument
//				.setReopenPurchaseOrderIndicator(reopenPurchaseOrderIndicator);
//		paymentRequestDocument
//				.setReceivingDocumentRequiredIndicator(receivingDocumentRequiredIndicator);
//		paymentRequestDocument
//				.setPaymentRequestPositiveApprovalIndicator(paymentRequestPositiveApprovalIndicator);
//		paymentRequestDocument.setUseTaxIndicator(useTaxIndicator);
//
//		return paymentRequestDocument;
//
//	}

	public PaymentRequestDocument createPaymentRequestDocument(
			Integer purapDocumentIdentifier2) throws WorkflowException {
		PaymentRequestDocument paymentRequestDocument = (PaymentRequestDocument) SpringContext
				.getBean(DocumentService.class).getNewDocument("PREQ");

		paymentRequestDocument
				.setPurapDocumentIdentifier(purapDocumentIdentifier);
		paymentRequestDocument
				.setPurchaseOrderIdentifier(purapDocumentIdentifier2);
		paymentRequestDocument.setPostingYear(postingYear);
		paymentRequestDocument.setInvoiceNumber(invoiceNumber);
		paymentRequestDocument.setVendorInvoiceAmount(vendorInvoiceAmount);
		paymentRequestDocument
				.setVendorPaymentTermsCode(vendorPaymentTermsCode);

		paymentRequestDocument
				.setPaymentRequestCostSourceCode(paymentRequestCostSourceCode);
		paymentRequestDocument
				.setVendorShippingPaymentTermsCode(vendorShippingPaymentTermsCode);
		paymentRequestDocument
				.setPaymentRequestedCancelIndicator(paymentRequestedCancelIndicator);
		paymentRequestDocument
				.setPaymentAttachmentIndicator(paymentAttachmentIndicator);
		paymentRequestDocument
				.setImmediatePaymentIndicator(immediatePaymentIndicator);
		paymentRequestDocument.setHoldIndicator(holdIndicator);
		paymentRequestDocument
				.setPaymentRequestElectronicInvoiceIndicator(paymentRequestElectronicInvoiceIndicator);
		paymentRequestDocument
				.setVendorHeaderGeneratedIdentifier(vendorHeaderGeneratedIdentifier);
		paymentRequestDocument
				.setVendorDetailAssignedIdentifier(vendorDetailAssignedIdentifier);

		paymentRequestDocument.setVendorName(vendorName);
		paymentRequestDocument.setVendorLine1Address(vendorLine1Address);
		paymentRequestDocument.setVendorCityName(vendorCityName);
		paymentRequestDocument.setVendorStateCode(vendorStateCode);
		paymentRequestDocument.setVendorPostalCode(vendorPostalCode);
		paymentRequestDocument.setVendorCountryCode(vendorCountryCode);
		paymentRequestDocument
				.setAccountsPayableProcessorIdentifier(accountsPayableProcessorIdentifier);
		paymentRequestDocument
				.setLastActionPerformedByPersonId(UserNameFixture.mls398
						.getPerson().getPrincipalId());// obj.setLastActionPerformedByPersonId("2133704704");
		paymentRequestDocument.setProcessingCampusCode(processingCampusCode);
		paymentRequestDocument
				.setAccountsPayableApprovalTimestamp(SpringContext.getBean(
						DateTimeService.class).getCurrentTimestamp());// obj.setAccountsPayableApprovalTimestamp(timeStamp);

		paymentRequestDocument
				.setOriginalVendorHeaderGeneratedIdentifier(originalVendorHeaderGeneratedIdentifier);
		paymentRequestDocument
				.setOriginalVendorDetailAssignedIdentifier(originalVendorDetailAssignedIdentifier);
		paymentRequestDocument
				.setContinuationAccountIndicator(continuationAccountIndicator);
		paymentRequestDocument
				.setAccountsPayablePurchasingDocumentLinkIdentifier(accountsPayablePurchasingDocumentLinkIdentifier);
		paymentRequestDocument
				.setClosePurchaseOrderIndicator(closePurchaseOrderIndicator);
		paymentRequestDocument
				.setReopenPurchaseOrderIndicator(reopenPurchaseOrderIndicator);
		paymentRequestDocument
				.setReceivingDocumentRequiredIndicator(receivingDocumentRequiredIndicator);
		paymentRequestDocument
				.setPaymentRequestPositiveApprovalIndicator(paymentRequestPositiveApprovalIndicator);
		paymentRequestDocument.setUseTaxIndicator(useTaxIndicator);
		
		paymentRequestDocument.setInvoiceDate(SpringContext.getBean(DateTimeService.class).getCurrentSqlDate());
		
		paymentRequestDocument.setInvoiceReceivedDate(SpringContext.getBean(DateTimeService.class).getCurrentSqlDate());
		
		paymentRequestDocument.getDocumentHeader().setDocumentDescription("Description");

		return paymentRequestDocument;

	}

}
