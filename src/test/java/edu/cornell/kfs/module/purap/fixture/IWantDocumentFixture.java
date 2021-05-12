package edu.cornell.kfs.module.purap.fixture;

import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.krad.service.DocumentService;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.document.IWantDocument;

public enum IWantDocumentFixture {

	I_WANT_DOC("Description",
			CUPurapConstants.IWantDocumentSteps.REGULAR, // step
			UserNameFixture.ccs1.getPerson().getPrincipalName(),
			UserNameFixture.ccs1.getPerson().getName(), UserNameFixture.ccs1
					.getPerson().getEmailAddress(), UserNameFixture.ccs1
					.getPerson().getPhoneNumber(), UserNameFixture.ccs1
					.getPerson().getAddressLine1(), true,
			UserNameFixture.ccs1.getPerson().getPrincipalName(),
			UserNameFixture.ccs1.getPerson().getName(),
			UserNameFixture.ccs1.getPerson().getEmailAddress(),
			UserNameFixture.ccs1.getPerson().getPhoneNumber(),
			UserNameFixture.ccs1.getPerson().getAddressLine1(),

			// Vendor Data
			"5314-0",// vendorNumber;
			0, // vendorDetailAssignedIdentifier;
			5314, // vendorHeaderGeneratedIdentifier;
			"1234",// vendorCustomerNumber;
			"Vendor Name", // vendorName;
			"Vendor attn name", // vendorAttentionName;
			"vendorLine1Address", // vendorLine1Address;
			"vendorLine2Address", // vendorLine2Address;
			"NY", // vendorStateCode;
			"", // vendorAddressInternationalProvinceName;
			"14850", // vendorPostalCode;
			"Ithaca", // vendorCityName;
			"US", // vendorCountryCode;
			"vendorAddress", // vendorAddress;
			"6072203712", // vendorPhoneNumber;
			"www.vendorURL.com", // vendorWebURL;
			"6072203712", // vendorFaxNumber;
			"abc@email.com", // vendorEmail;
			"vendorDescription", // vendorDescription
			"6100", // collegeLevelOrganization;
			"6104", // departmentLevelOrganization;

			true, // useCollegeAndDepartmentAsDefault;

			true, // setDeliverToInfoAsDefault;

			"attachment Description", // attachmentDescription;
			"note Label", // noteLabel;

			// private String completeOption;
			// private boolean completed;

			// routing fields
			"IT", // routingChart;
			"6100",// routingOrganization;

			// adhoc routing
			// private String currentRouteToNetId;
			IWantItemFixture.I_WANT_ITEM, IWantAccountFixture.I_WANT_ACCOUNT,
			"accountDescriptionTxt", // accountDescriptionTxt
			false, // documentationAttached
			"commentsAndSpecialInstructions", //
			false // goods
	);

	private String description;
	private String step;

	private String initiatorNetID;
	private String initiatorName;
	private String initiatorEmailAddress;
	private String initiatorPhoneNumber;
	private String initiatorAddress;

	private boolean sameAsInitiator;
	private String deliverToNetID;
	private String deliverToName;
	private String deliverToEmailAddress;
	private String deliverToPhoneNumber;
	private String deliverToAddress;

	// Vendor Data
	private String vendorNumber;
	private Integer vendorDetailAssignedIdentifier;
	private Integer vendorHeaderGeneratedIdentifier;
	private String vendorCustomerNumber;
	private String vendorName;
	private String vendorAttentionName;
	private String vendorLine1Address;
	private String vendorLine2Address;
	private String vendorStateCode;
	private String vendorAddressInternationalProvinceName;
	private String vendorPostalCode;
	private String vendorCityName;
	private String vendorCountryCode;
	private String vendorAddress;
	private String vendorPhoneNumber;
	private String vendorWebURL;
	private String vendorFaxNumber;
	private String vendorEmail;
	private String vendorDescription;

	private String collegeLevelOrganization;
	private String departmentLevelOrganization;

	private boolean useCollegeAndDepartmentAsDefault;

	private boolean setDeliverToInfoAsDefault;

	private String attachmentDescription;
	private String noteLabel;

	// routing fields
	private String routingChart;
	private String routingOrganization;

	// Items
	private IWantItemFixture itemFixture;
	private IWantAccountFixture iWantAccountFixture;

	// Account Description free form field
	private String accountDescriptionTxt;

	// Checkbox that tells whether documentation has been attached to this edoc
	private boolean documentationAttached;

	// Notes

	// Comments/special instructions free form text
	private String commentsAndSpecialInstructions;

	// Goods versus Services
	private boolean goods;


	private IWantDocumentFixture(
			String description,
			String step,

			String initiatorNetID,
			String initiatorName,
			String initiatorEmailAddress,
			String initiatorPhoneNumber,
			String initiatorAddress,

			boolean sameAsInitiator,
			String deliverToNetID,
			String deliverToName,
			String deliverToEmailAddress,
			String deliverToPhoneNumber,
			String deliverToAddress,

			// Vendor Data
			String vendorNumber, Integer vendorDetailAssignedIdentifier,
			Integer vendorHeaderGeneratedIdentifier,
			String vendorCustomerNumber, String vendorName,
			String vendorAttentionName, String vendorLine1Address,
			String vendorLine2Address, String vendorStateCode,
			String vendorAddressInternationalProvinceName,
			String vendorPostalCode, String vendorCityName,
			String vendorCountryCode, String vendorAddress,
			String vendorPhoneNumber, String vendorWebURL,
			String vendorFaxNumber, String vendorEmail,
			String vendorDescription,

			String collegeLevelOrganization,
			String departmentLevelOrganization,

			boolean useCollegeAndDepartmentAsDefault,

			boolean setDeliverToInfoAsDefault,

			String attachmentDescription, String noteLabel,

			// routing fields
			String routingChart, String routingOrganization,

			// Items
			IWantItemFixture itemFixture,
			IWantAccountFixture iWantAccountFixture,

			// Account Description free form field
			String accountDescriptionTxt,

			// Checkbox that tells whether documentation has been attached to
			// this edoc
			boolean documentationAttached,

			// Notes

			// Comments/special instructions free form text
			String commentsAndSpecialInstructions,

			// Goods versus Services
			boolean goods) {
		this.description = description;
		this.step = step;

		this.initiatorNetID = initiatorNetID;
		this.initiatorName = initiatorName;
		this.initiatorEmailAddress = initiatorEmailAddress;
		this.initiatorPhoneNumber = initiatorPhoneNumber;
		this.initiatorAddress = initiatorAddress;

		this.sameAsInitiator = sameAsInitiator;
		this.deliverToNetID = deliverToNetID;
		this.deliverToName = deliverToName;
		this.deliverToEmailAddress = deliverToEmailAddress;
		this.deliverToPhoneNumber = deliverToPhoneNumber;
		this.deliverToAddress = deliverToAddress;

		// Vendor Data
		this.vendorNumber = vendorNumber;
		this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
		this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
		this.vendorCustomerNumber = vendorCustomerNumber;
		this.vendorName = vendorName;
		this.vendorAttentionName = vendorAttentionName;
		this.vendorLine1Address = vendorLine1Address;
		this.vendorLine2Address = vendorLine2Address;
		this.vendorStateCode = vendorStateCode;
		this.vendorAddressInternationalProvinceName = vendorAddressInternationalProvinceName;
		this.vendorPostalCode = vendorPostalCode;
		this.vendorCityName = vendorCityName;
		this.vendorCountryCode = vendorCountryCode;
		this.vendorAddress = vendorAddress;
		this.vendorPhoneNumber = vendorPhoneNumber;
		this.vendorWebURL = vendorWebURL;
		this.vendorFaxNumber = vendorFaxNumber;
		this.vendorEmail = vendorEmail;
		this.vendorDescription = vendorDescription;

		this.collegeLevelOrganization = collegeLevelOrganization;
		this.departmentLevelOrganization = departmentLevelOrganization;

		this.useCollegeAndDepartmentAsDefault = useCollegeAndDepartmentAsDefault;

		this.setDeliverToInfoAsDefault = setDeliverToInfoAsDefault;

		this.attachmentDescription = attachmentDescription;
		this.noteLabel = noteLabel;

		// routing fields
		this.routingChart = routingChart;
		this.routingOrganization = routingOrganization;

		// Items
		this.itemFixture = itemFixture;
		this.iWantAccountFixture = iWantAccountFixture;

		// Account Description free form field
		this.accountDescriptionTxt = accountDescriptionTxt;

		// Checkbox that tells whether documentation has been attached to this
		// edoc
		this.documentationAttached = documentationAttached;

		// Notes

		// Comments/special instructions free form text
		this.commentsAndSpecialInstructions = commentsAndSpecialInstructions;

		// Goods versus Services
		this.goods = goods;
	}

	public IWantDocument createIWantDocument() throws WorkflowException {
		IWantDocument iWantDocument = (IWantDocument) SpringContext.getBean(
				DocumentService.class).getNewDocument(IWantDocument.class);
		iWantDocument.getDocumentHeader().setDocumentDescription(description);

		iWantDocument.setStep(step);

		iWantDocument.setInitiatorNetID(initiatorNetID);
		iWantDocument.setInitiatorName(initiatorName);
		iWantDocument.setInitiatorEmailAddress(initiatorEmailAddress);
		iWantDocument.setInitiatorPhoneNumber(initiatorPhoneNumber);
		iWantDocument.setInitiatorAddress(initiatorAddress);

		iWantDocument.setSameAsInitiator(sameAsInitiator);
		iWantDocument.setDeliverToNetID(deliverToNetID);
		iWantDocument.setDeliverToName(deliverToName);
		iWantDocument.setDeliverToEmailAddress(deliverToEmailAddress);
		iWantDocument.setDeliverToPhoneNumber(deliverToPhoneNumber);
		iWantDocument.setDeliverToAddress(deliverToAddress);

		// Vendor Data
		iWantDocument.setVendorNumber(vendorNumber);
		iWantDocument
				.setVendorDetailAssignedIdentifier(vendorDetailAssignedIdentifier);
		iWantDocument
				.setVendorHeaderGeneratedIdentifier(vendorHeaderGeneratedIdentifier);
		iWantDocument.setVendorCustomerNumber(vendorCustomerNumber);
		iWantDocument.setVendorName(vendorName);
		iWantDocument.setVendorAttentionName(vendorAttentionName);
		iWantDocument.setVendorLine1Address(vendorLine1Address);
		iWantDocument.setVendorLine2Address(vendorLine2Address);
		iWantDocument.setVendorStateCode(vendorStateCode);
		iWantDocument
				.setVendorAddressInternationalProvinceName(vendorAddressInternationalProvinceName);
		iWantDocument.setVendorPostalCode(vendorPostalCode);
		iWantDocument.setVendorCityName(vendorCityName);
		iWantDocument.setVendorCountryCode(vendorCountryCode);
		iWantDocument.setVendorAddress(vendorAddress);
		iWantDocument.setVendorPhoneNumber(vendorPhoneNumber);
		iWantDocument.setVendorWebURL(vendorWebURL);
		iWantDocument.setVendorFaxNumber(vendorFaxNumber);
		iWantDocument.setVendorEmail(vendorEmail);
		iWantDocument.setVendorDescription(vendorDescription);

		iWantDocument.setCollegeLevelOrganization(collegeLevelOrganization);
		iWantDocument
				.setDepartmentLevelOrganization(departmentLevelOrganization);

		iWantDocument
				.setUseCollegeAndDepartmentAsDefault(useCollegeAndDepartmentAsDefault);

		iWantDocument.setSetDeliverToInfoAsDefault(setDeliverToInfoAsDefault);

		iWantDocument.setAttachmentDescription(attachmentDescription);
		iWantDocument.setNoteLabel(noteLabel);

		// routing fields
		iWantDocument.setRoutingChart(routingChart);
		iWantDocument.setRoutingOrganization(routingOrganization);

		// Items
		iWantDocument.addItem(itemFixture.createIWantItem(iWantDocument
				.getDocumentNumber()));
		iWantDocument.addAccount(iWantAccountFixture
				.createIWantAccount(iWantDocument.getDocumentNumber()));

		// Account Description free form field
		iWantDocument.setAccountDescriptionTxt(accountDescriptionTxt);

		// Checkbox that tells whether documentation has been attached to this
		// edoc
		iWantDocument.setDocumentationAttached(documentationAttached);

		// Notes

		// Comments/special instructions free form text
		iWantDocument
				.setCommentsAndSpecialInstructions(commentsAndSpecialInstructions);

		// Goods versus Services
		iWantDocument.setGoods(goods);

		return iWantDocument;
	}

}
