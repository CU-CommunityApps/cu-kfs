package edu.cornell.kfs.module.purap.fixture;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.kuali.kfs.module.purap.businessobject.PurApItem;
import org.kuali.kfs.module.purap.document.RequisitionDocument;

import edu.cornell.kfs.module.purap.CuPurapTestConstants;
import edu.cornell.kfs.sys.util.MockDocumentUtils;

public enum RequisitionLiteFixture {
    REQS_DEFAULT_DATA(),
    REQS_LINE_WITHOUT_ACCOUNT(RequisitionItemFixture.REQ_QTY_ITEM_AMOUNT_AT_8K_NO_ACCT_LINE),
    REQS_NON_CFDA_LINE(RequisitionItemFixture.REQ_QTY_ITEM_AMOUNT_AT_8K_NO_CFDA),
    REQS_CFDA_LINE(RequisitionItemFixture.REQ_QTY_ITEM_AMOUNT_AT_8K_WITH_CFDA),

    REQS_MULTI_LINE_ONE_CFDA(RequisitionItemFixture.REQ_QTY_ITEM_AMOUNT_AT_15K_NO_CFDA,
            RequisitionItemFixture.REQ_QTY_ITEM_AMOUNT_AT_8K_WITH_CFDA),

    REQS_MULTI_LINE_NO_CFDA(RequisitionItemFixture.REQ_QTY_ITEM_AMOUNT_AT_15K_NO_CFDA,
            RequisitionItemFixture.REQ_QTY_ITEM_AMOUNT_AT_8K_NO_CFDA),

    REQS_CFDA_LINE_AND_VENDOR_CONTRACT_WITH_LIMIT(
            CuPurapTestConstants.TEST_CONTRACT_ID_1357, CuPurapTestConstants.TEST_CONTRACT_CHART,
            CuPurapTestConstants.TEST_CONTRACT_ORG, CuPurapTestConstants.COST_SOURCE_INVOICE,
            RequisitionItemFixture.REQ_QTY_ITEM_AMOUNT_AT_8K_WITH_CFDA),

    REQS_CFDA_LINE_AND_VENDOR_CONTRACT_WITHOUT_LIMIT(
            CuPurapTestConstants.TEST_CONTRACT_ID_6666, CuPurapTestConstants.TEST_CONTRACT_CHART, CuPurapTestConstants.TEST_CONTRACT_ORG,
            CuPurapTestConstants.COST_SOURCE_INVOICE, RequisitionItemFixture.REQ_QTY_ITEM_AMOUNT_AT_8K_WITH_CFDA),

    REQS_CFDA_LINE_AND_INELIGIBLE_COST_SOURCE_AND_VENDOR_CONTRACT_WITHOUT_LIMIT(
            CuPurapTestConstants.TEST_CONTRACT_ID_6666, CuPurapTestConstants.TEST_CONTRACT_CHART, CuPurapTestConstants.TEST_CONTRACT_ORG,
            CuPurapTestConstants.COST_SOURCE_EDU_AND_INST_COOP, RequisitionItemFixture.REQ_QTY_ITEM_AMOUNT_AT_8K_WITH_CFDA),

    REQS_CFDA_LINE_AND_ORG_PARM(
            null, CuPurapTestConstants.TEST_PARM_CHART, CuPurapTestConstants.TEST_PARM_ORG,
            CuPurapTestConstants.COST_SOURCE_ESTIMATE, RequisitionItemFixture.REQ_QTY_ITEM_AMOUNT_AT_8K_WITH_CFDA),

    REQS_CFDA_LINE_AND_ORG_PARM_AND_VENDOR_CONTRACT_WITH_LIMIT(
            CuPurapTestConstants.TEST_CONTRACT_ID_1357, CuPurapTestConstants.TEST_PARM_CHART, CuPurapTestConstants.TEST_PARM_ORG,
            CuPurapTestConstants.COST_SOURCE_INVOICE, RequisitionItemFixture.REQ_QTY_ITEM_AMOUNT_AT_8K_WITH_CFDA),

    REQS_CFDA_LINE_AND_ORG_PARM_AND_VENDOR_CONTRACT_WITHOUT_LIMIT(
            CuPurapTestConstants.TEST_CONTRACT_ID_6666, CuPurapTestConstants.TEST_PARM_CHART, CuPurapTestConstants.TEST_PARM_ORG,
            CuPurapTestConstants.COST_SOURCE_INVOICE, RequisitionItemFixture.REQ_QTY_ITEM_AMOUNT_AT_8K_WITH_CFDA);

    public static final int BASE_DOCUMENT_NUMBER = 15000;

    public final Integer vendorContractGeneratedIdentifier;
    public final String chartOfAccountsCode;
    public final String organizationCode;
    public final String purchaseOrderCostSourceCode;
    public final List<RequisitionItemFixture> items;

    private RequisitionLiteFixture(RequisitionItemFixture... items) {
        this(null, null, null, CuPurapTestConstants.COST_SOURCE_ESTIMATE, items);
    }

    private RequisitionLiteFixture(Integer vendorContractGeneratedIdentifier,
            String chartOfAccountsCode, String organizationCode,
            String purchaseOrderCostSourceCode, RequisitionItemFixture... items) {
        this.vendorContractGeneratedIdentifier = vendorContractGeneratedIdentifier;
        this.chartOfAccountsCode = chartOfAccountsCode;
        this.organizationCode = organizationCode;
        this.purchaseOrderCostSourceCode = purchaseOrderCostSourceCode;
        this.items = Collections.unmodifiableList(Arrays.asList(items));
    }

    public String getOrdinalBasedDocumentNumber() {
        return String.valueOf(BASE_DOCUMENT_NUMBER + ordinal());
    }

    public RequisitionDocument toRequisitionDocument() {
        RequisitionDocument document = MockDocumentUtils.buildMockDocument(RequisitionDocument.class);
        
        document.setDocumentNumber(getOrdinalBasedDocumentNumber());
        document.getDocumentHeader().setDocumentNumber(document.getDocumentNumber());
        document.setVendorContractGeneratedIdentifier(vendorContractGeneratedIdentifier);
        document.setChartOfAccountsCode(chartOfAccountsCode);
        document.setOrganizationCode(organizationCode);
        document.setPurchaseOrderCostSourceCode(purchaseOrderCostSourceCode);
        addItemsToDocument(document);
        
        return document;
    }

    private void addItemsToDocument(RequisitionDocument document) {
        List<PurApItem> itemList = document.getItems();
        items.stream()
                .map(RequisitionItemFixture::createRequisitionItemForMicroTest)
                .forEach(itemList::add);
    }

}
