package edu.cornell.kfs.module.purap.fixture;

import org.kuali.kfs.module.purap.businessobject.PurApAccountingLine;
import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.krad.service.DocumentService;

import edu.cornell.kfs.vnd.businessobject.CuVendorAddressExtension;

public enum RequisitionFixture {

	REQ_B2B("Description", "B2B", 0, 4130, 4216, "line 1 address",
			"line 2 address", "city", "NY", "14850", "US", "abc@email.com",
			"6072203712", "attn name", 1, "Delivery Line 1 address",
			"Delivery Line 2 address", "Delivery City Name", "110", "US", "NY",
			"14850", "billing City Name", "US", "abc@email.com",
			"billing line 1 address", "607-220-3712", "14850", "NY",
			"Billing name", RequisitionItemFixture.REQ_ITEM, null),

	REQ_B2B_INVALID("Description", "B2B", 0, 4130, 4216, null,
			"line 2 address", "city", "NY", "14850", "US", "abc@email.com",
			"6072203712", "attn name", 1, "Delivery Line 1 address",
			"Delivery Line 2 address", "Delivery City Name", "110", "US", "NY",
			"14850", "billing City Name", "US", "abc@email.com",
			"billing line 1 address", "607-220-3712", "14850", "NY",
			"Billing name", RequisitionItemFixture.REQ_ITEM, null),

	REQ_B2B_CXML("Description", "B2B", 0, 4130, 4216, "line 1 address",
			"line 2 address", "city", "NY", "14850", "US", "abc@email.com",
			"6072203712", "attn name", 1, "Delivery Line 1 address",
			"Delivery Line 2 address", "Delivery City Name", "110", "US", "NY",
			"14850", "billing City Name", "US", "abc@email.com",
			"billing line 1 address", "607-220-3712", "14850", "NY",
			"Billing name", RequisitionItemFixture.REQ_ITEM, null),

	REQ_B2B_CXML_INVALID("Description", "B2B", 0, 4130, 4216, null,
			"line 2 address", "city", "NY", "14850", "US", "abc@email.com",
			"6072203712", "attn name", 1, "Delivery Line 1 address",
			"Delivery Line 2 address", "Delivery City Name", "110", "US", null,
			null, "billing City Name", "US", "abc@email.com",
			"billing line 1 address", "607-220-3712", "14850", "NY",
			"Billing name", RequisitionItemFixture.REQ_ITEM, null),

	REQ_NON_B2B("Description", "STAN", 0, 4291, null, "line 1 address",
			"line 2 address", "city", "NY", "14850", "US", "abc@email.com",
			"6072203712", "attn name", 1, "Delivery Line 1 address",
			"Delivery Line 2 address", "Delivery City Name", "110", "US", null,
			null, "billing City Name", "US", "abc@email.com",
			"billing line 1 address", "607-220-3712", "14850", "NY",
			"Billing name", null, null),
			
	REQ_NON_B2B_WITH_ITEMS("Description", "STAN", 0, 104677, null,
			"line 1 address", "line 2 address", "city", "NY", "14850", "US",
			"abc@email.com", "6072203712", "attn name", 1,
			"Delivery Line 1 address", "Delivery Line 2 address",
			"Delivery City Name", "110", "US", null, null, "billing City Name",
			"US", "abc@email.com", "billing line 1 address", "607-220-3712",
			"14850", "NY", "Billing name", RequisitionItemFixture.REQ_ITEM,
			null),

	REQ_NON_B2B_CAP_ASSET_ITEM("Description", "STAN", 0, 4291, null,
			"line 1 address", "line 2 address", "city", "NY", "14850", "US",
			"abc@email.com", "6072203712", "attn name", 1,
			"Delivery Line 1 address", "Delivery Line 2 address",
			"Delivery City Name", "110", "US", null, null, "billing City Name",
			"US", "abc@email.com", "billing line 1 address", "607-220-3712",
			"14850", "NY", "Billing name", RequisitionItemFixture.REQ_ITEM,
			RequisitionCapitalAssetFixture.REC1),

	REQ_NON_B2B_CAP_ASSET_ITEM_INACTIVE_COMM_CODE("Description", "STAN", 0,
			4291, null, "line 1 address", "line 2 address", "city", "NY",
			"14850", "US", "abc@email.com", "6072203712", "attn name", 1,
			"Delivery Line 1 address", "Delivery Line 2 address",
			"Delivery City Name", "110", "US", null, null, "billing City Name",
			"US", "abc@email.com", "billing line 1 address", "607-220-3712",
			"14850", "NY", "Billing name",
			RequisitionItemFixture.REQ_ITEM_INACTIVE_COMM_CD, null),

	REQ_NON_B2B_TRADE_IN_ITEMS("Description", "STAN", 0, 4291, null,
			"line 1 address", "line 2 address", "city", "NY", "14850", "US",
			"abc@email.com", "6072203712", "attn name", 1,
			"Delivery Line 1 address", "Delivery Line 2 address",
			"Delivery City Name", "110", "US", null, null, "billing City Name",
			"US", "abc@email.com", "billing line 1 address", "607-220-3712",
			"14850", "NY", "Billing name",
			RequisitionItemFixture.REQ_ITEM_TRADE_IN, null),

	REQ_NON_B2B_WITH_NON_QTY_ITEM_BELOW_5K("Description", "STAN", 0, 4291, null,
			"line 1 address", "line 2 address", "city", "NY", "14850", "US",
			"abc@email.com", "6072203712", "attn name", 1,
			"Delivery Line 1 address", "Delivery Line 2 address",
			"Delivery City Name", "110", "US", null, null, "billing City Name",
			"US", "abc@email.com", "billing line 1 address", "607-220-3712",
			"14850", "NY", "Billing name",
			RequisitionItemFixture.REQ_NON_QTY_ITEM_AMOUNT_BELOW_5K, null),

	REQ_NON_B2B_WITH_NON_QTY_ITEM_AT_5K("Description", "STAN", 0, 4291, null,
			"line 1 address", "line 2 address", "city", "NY", "14850", "US",
			"abc@email.com", "6072203712", "attn name", 1,
			"Delivery Line 1 address", "Delivery Line 2 address",
			"Delivery City Name", "110", "US", null, null, "billing City Name",
			"US", "abc@email.com", "billing line 1 address", "607-220-3712",
			"14850", "NY", "Billing name",
			RequisitionItemFixture.REQ_NON_QTY_ITEM_AMOUNT_AT_5K, null),			

	REQ_NON_B2B_WITH_NON_QTY_ITEM_ABOVE_5K("Description", "STAN", 0, 4291, null,
			"line 1 address", "line 2 address", "city", "NY", "14850", "US",
			"abc@email.com", "6072203712", "attn name", 1,
			"Delivery Line 1 address", "Delivery Line 2 address",
			"Delivery City Name", "110", "US", null, null, "billing City Name",
			"US", "abc@email.com", "billing line 1 address", "607-220-3712",
			"14850", "NY", "Billing name",
			RequisitionItemFixture.REQ_NON_QTY_ITEM_AMOUNT_ABOVE_5K, null),

	REQ_NON_B2B_WITH_QTY_ITEM_BELOW_5K("Description", "STAN", 0, 4291, null,
			"line 1 address", "line 2 address", "city", "NY", "14850", "US",
			"abc@email.com", "6072203712", "attn name", 1,
			"Delivery Line 1 address", "Delivery Line 2 address",
			"Delivery City Name", "110", "US", null, null, "billing City Name",
			"US", "abc@email.com", "billing line 1 address", "607-220-3712",
			"14850", "NY", "Billing name",
			RequisitionItemFixture.REQ_QTY_ITEM_AMOUNT_BELOW_5K, null),

	REQ_NON_B2B_WITH_QTY_ITEM_AT_5K("Description", "STAN", 0, 4291, null,
			"line 1 address", "line 2 address", "city", "NY", "14850", "US",
			"abc@email.com", "6072203712", "attn name", 1,
			"Delivery Line 1 address", "Delivery Line 2 address",
			"Delivery City Name", "110", "US", null, null, "billing City Name",
			"US", "abc@email.com", "billing line 1 address", "607-220-3712",
			"14850", "NY", "Billing name",
			RequisitionItemFixture.REQ_QTY_ITEM_AMOUNT_AT_5K, null),

	REQ_NON_B2B_WITH_QTY_ITEM_ABOVE_5K("Description", "STAN", 0, 4291, null,
			"line 1 address", "line 2 address", "city", "NY", "14850", "US",
			"abc@email.com", "6072203712", "attn name", 1,
			"Delivery Line 1 address", "Delivery Line 2 address",
			"Delivery City Name", "110", "US", null, null, "billing City Name",
			"US", "abc@email.com", "billing line 1 address", "607-220-3712",
			"14850", "NY", "Billing name",
			RequisitionItemFixture.REQ_QTY_ITEM_AMOUNT_ABOVE_5K, null);

	public final String documentDescription;
	public final String requisitionSourceCode;
	public final Integer vendorDetailAssignedIdentifier;
	public final Integer vendorHeaderGeneratedIdentifier;
	public final Integer vendorContractGeneratedIdentifier;
	public final String vendorLine1Address;
	public final String vendorLine2Address;
	public final String vendorCityName;
	public final String vendorStateCode;
	public final String vendorPostalCode;
	public final String vendorCountryCode;
	public final String vendorEmailAddress;
	public final String vendorFaxNumber;
	public final String vendorAttentionName;
	public final Integer vendorAddressGeneratedIdentifier;

	public final String deliveryBuildingLine1Address;
	public final String deliveryBuildingLine2Address;
	public final String deliveryCityName;
	public final String deliveryBuildingRoomNumber;
	public final String deliveryCountryCode;
	public final String deliveryStateCode;
	public final String deliveryPostalCode;

	public final String billingCityName;
	public final String billingCountryCode;
	public final String billingEmailAddress;
	public final String billingLine1Address;
	public final String billingPhoneNumber;
	public final String billingPostalCode;
	public final String billingStateCode;
	public final String billingName;

	public final RequisitionItemFixture item;
	public final RequisitionCapitalAssetFixture capitalAssetFixture;

	private boolean addAccountingLine = true;

	private RequisitionFixture(String documentDescription,
			String requisitionSourceCode,
			Integer vendorDetailAssignedIdentifier,
			Integer vendorHeaderGeneratedIdentifier,
			Integer vendorContractGeneratedIdentifier,
			String vendorLine1Address, String vendorLine2Address,
			String vendorCityName, String vendorStateCode,
			String vendorPostalCode, String vendorCountryCode,
			String vendorEmailAddress, String vendorFaxNumber,
			String vendorAttentionName,
			Integer vendorAddressGeneratedIdentifier,
			String deliveryBuildingLine1Address,
			String deliveryBuildingLine2Address, String deliveryCityName,
			String deliveryBuildingRoomNumber, String deliveryCountryCode,
			String deliveryStateCode, String deliveryPostalCode,

			String billingCityName, String billingCountryCode,
			String billingEmailAddress, String billingLine1Address,
			String billingPhoneNumber, String billingPostalCode,
			String billingStateCode, String billingName,
			RequisitionItemFixture item,
			RequisitionCapitalAssetFixture capitalAssetFixture) {

		this.documentDescription = documentDescription;
		this.requisitionSourceCode = requisitionSourceCode;
		this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
		this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
		this.vendorContractGeneratedIdentifier = vendorContractGeneratedIdentifier;
		this.vendorLine1Address = vendorLine1Address;
		this.vendorLine2Address = vendorLine2Address;
		this.vendorCityName = vendorCityName;
		this.vendorStateCode = vendorStateCode;
		this.vendorPostalCode = vendorPostalCode;
		this.vendorCountryCode = vendorCountryCode;
		this.vendorEmailAddress = vendorEmailAddress;
		this.vendorFaxNumber = vendorFaxNumber;
		this.vendorAttentionName = vendorAttentionName;
		this.vendorAddressGeneratedIdentifier = vendorAddressGeneratedIdentifier;

		this.deliveryBuildingLine1Address = deliveryBuildingLine1Address;
		this.deliveryBuildingLine2Address = deliveryBuildingLine2Address;
		this.deliveryCityName = deliveryCityName;
		this.deliveryBuildingRoomNumber = deliveryBuildingRoomNumber;
		this.deliveryCountryCode = deliveryCountryCode;
		this.deliveryStateCode = deliveryStateCode;
		this.deliveryPostalCode = deliveryPostalCode;

		this.billingCityName = billingCityName;
		this.billingCountryCode = billingCountryCode;
		this.billingEmailAddress = billingEmailAddress;
		this.billingLine1Address = billingLine1Address;
		this.billingPhoneNumber = billingPhoneNumber;
		this.billingPostalCode = billingPostalCode;
		this.billingStateCode = billingStateCode;
		this.billingName = billingName;

		this.item = item;

		this.capitalAssetFixture = capitalAssetFixture;

	}

	public RequisitionDocument createRequisition() throws WorkflowException {
		RequisitionDocument requisitionDocument = (RequisitionDocument) SpringContext
				.getBean(DocumentService.class).getNewDocument(
						RequisitionDocument.class);
		requisitionDocument.initiateDocument();
		requisitionDocument.getDocumentHeader().setDocumentDescription(
				documentDescription);

		requisitionDocument.setRequisitionSourceCode(requisitionSourceCode);
		// set vendor info
		requisitionDocument
				.setVendorDetailAssignedIdentifier(vendorDetailAssignedIdentifier);
		requisitionDocument
				.setVendorHeaderGeneratedIdentifier(vendorHeaderGeneratedIdentifier);

		requisitionDocument
				.setVendorContractGeneratedIdentifier(vendorContractGeneratedIdentifier);
		requisitionDocument.refreshReferenceObject("vendorContract");

		// retrieve vendor based on selection from vendor lookup
		requisitionDocument.refreshReferenceObject("vendorDetail");
		requisitionDocument.templateVendorDetail(requisitionDocument
				.getVendorDetail());

		// populate default address based on selected vendor
		VendorAddress defaultAddress = SpringContext.getBean(
				VendorService.class).getVendorDefaultAddress(
				requisitionDocument.getVendorDetail().getVendorAddresses(),
				requisitionDocument.getVendorDetail().getVendorHeader()
						.getVendorType().getAddressType()
						.getVendorAddressTypeCode(),
				requisitionDocument.getDeliveryCampusCode());
		requisitionDocument.templateVendorAddress(defaultAddress);

		// vendor address holds method of po transmission that should be used
		requisitionDocument
				.setPurchaseOrderTransmissionMethodCode(((CuVendorAddressExtension) defaultAddress
						.getExtension())
						.getPurchaseOrderTransmissionMethodCode());

		requisitionDocument.setVendorLine1Address(vendorLine1Address);
		requisitionDocument.setVendorLine2Address(vendorLine2Address);
		requisitionDocument.setVendorCityName(vendorCityName);
		requisitionDocument.setVendorStateCode(vendorStateCode);
		requisitionDocument.setVendorPostalCode(vendorPostalCode);
		requisitionDocument.setVendorCountryCode(vendorCountryCode);
		requisitionDocument.setVendorEmailAddress(vendorEmailAddress);
		requisitionDocument.setVendorFaxNumber(vendorFaxNumber);
		requisitionDocument.setVendorAttentionName(vendorAttentionName);
		requisitionDocument
				.setVendorAddressGeneratedIdentifier(vendorAddressGeneratedIdentifier);

		requisitionDocument
				.setDeliveryBuildingLine1Address(deliveryBuildingLine1Address);
		requisitionDocument
				.setDeliveryBuildingLine2Address(deliveryBuildingLine2Address);
		requisitionDocument.setDeliveryCityName(deliveryCityName);
		requisitionDocument
				.setDeliveryBuildingRoomNumber(deliveryBuildingRoomNumber);
		requisitionDocument.setDeliveryCountryCode(deliveryCountryCode);
		requisitionDocument.setDeliveryStateCode(deliveryStateCode);
		requisitionDocument.setDeliveryPostalCode(deliveryPostalCode);

		requisitionDocument.setBillingCityName(billingCityName);
		requisitionDocument.setBillingCountryCode(billingCountryCode);
		requisitionDocument.setBillingEmailAddress(billingEmailAddress);
		requisitionDocument.setBillingLine1Address(billingLine1Address);
		requisitionDocument.setBillingPhoneNumber(billingPhoneNumber);
		requisitionDocument.setBillingPostalCode(billingPostalCode);
		requisitionDocument.setBillingStateCode(billingStateCode);
		requisitionDocument.setBillingName(billingName);

		if (item != null) {
			requisitionDocument.addItem(item.createRequisitionItem(addAccountingLine));
		}

		if (capitalAssetFixture != null) {
			requisitionDocument.getPurchasingCapitalAssetItems().add(
					capitalAssetFixture.REC1.newRecord());
		}

		//requisitionDocument.refreshNonUpdateableReferences();

		return requisitionDocument;
	}

	public RequisitionDocument createRequisition(DocumentService documentService)
			throws WorkflowException {
		addAccountingLine = false;
		RequisitionDocument requisitionDocument = this.createRequisition();

		// Save the requisition with items, but without accounting lines and then add the accounting lines and save again
		// This odd methodology is to workaround an NPE that occurs when access security is enabled and refreshNonUpdatableReferences
		// is called on the account. For some reason the RequisitionItem cannot be found in ojb's cache and so when
		// it is attempted to be instantiated and constructor methods called, an NPE is thrown. This little dance works around the exception.
		// More analysis could probably be done to determine the root cause and address it, but for now this is good enough.
		documentService.saveDocument(requisitionDocument);
		requisitionDocument.refreshNonUpdateableReferences();

		if (item != null && item.accountingLineFixture != null) {
			for (Object item : requisitionDocument.getItems()) {
				PurApAccountingLine accountingLine = this.item.accountingLineFixture.createRequisitionAccount(((PurApItem) item).getItemIdentifier());
				((PurApItem) item).getSourceAccountingLines().add(accountingLine);
			}
		}

		documentService.saveDocument(requisitionDocument);
		requisitionDocument.refreshNonUpdateableReferences();

		return requisitionDocument;
	}
}
