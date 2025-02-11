package edu.cornell.kfs.tax.batch.service.impl;

import java.util.Map;

import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.exception.ClassNotPersistableException;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;

import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoFieldEnum;
import edu.cornell.kfs.tax.batch.dto.DocumentHeaderLite.DocumentHeaderField;
import edu.cornell.kfs.tax.batch.dto.NoteLite.NoteField;
import edu.cornell.kfs.tax.batch.dto.VendorAddressLite.VendorAddressField;
import edu.cornell.kfs.tax.batch.dto.VendorDetailLite.VendorField;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.businessobject.TransactionDetail.TransactionDetailField;

/**
 * Default non-ORM-tool metadata service implementation that hard-codes the various table and column mappings.
 * Uses the mapped BO class itself as the "metadata" object.
 * 
 * Because the table and column names are not ORM-tool-derived, THE DEVELOPER TEAM IS RESPONSIBLE FOR MANUALLY
 * UPDATING THESE MAPPINGS WHEN STRUCTURAL CHANGES ARE MADE TO THE MAPPED TABLES!
 */
public class TaxTableMetadataLookupServiceDefaultImpl
        extends TaxTableMetadataLookupServiceBase<Class<? extends BusinessObject>> {

    private static final Map<Class<? extends BusinessObject>, String> MAPPED_TABLES = Map.ofEntries(
            Map.entry(DocumentHeader.class, "FS_DOC_HEADER_T"),
            Map.entry(Note.class, "KRNS_NTE_T"),
            Map.entry(VendorAddress.class, "PUR_VNDR_ADDR_T"),
            Map.entry(VendorDetail.class, "PUR_VNDR_DTL_T"),
            Map.entry(VendorHeader.class, "PUR_VNDR_HDR_T"),
            Map.entry(TransactionDetail.class, "TX_TRANSACTION_DETAIL_T")
    );

    private static final Map<Enum<?>, String> MAPPED_COLUMNS = Map.ofEntries(
            Map.entry(DocumentHeaderField.documentNumber, "FDOC_NBR"),
            Map.entry(DocumentHeaderField.objectId, "OBJ_ID"),

            Map.entry(NoteField.noteIdentifier, "NTE_ID"),
            Map.entry(NoteField.remoteObjectIdentifier, "RMT_OBJ_ID"),
            Map.entry(NoteField.noteText, "TXT"),

            Map.entry(VendorAddressField.vendorAddressGeneratedIdentifier, "VNDR_ADDR_GNRTD_ID"),
            Map.entry(VendorAddressField.vendorHeaderGeneratedIdentifier, "VNDR_HDR_GNRTD_ID"),
            Map.entry(VendorAddressField.vendorDetailAssignedIdentifier, "VNDR_DTL_ASND_ID"),
            Map.entry(VendorAddressField.vendorAddressTypeCode, "VNDR_ADDR_TYP_CD"),
            Map.entry(VendorAddressField.vendorLine1Address, "VNDR_LN1_ADDR"),
            Map.entry(VendorAddressField.vendorLine2Address, "VNDR_LN2_ADDR"),
            Map.entry(VendorAddressField.vendorCityName, "VNDR_CTY_NM"),
            Map.entry(VendorAddressField.vendorStateCode, "VNDR_ST_CD"),
            Map.entry(VendorAddressField.vendorZipCode, "VNDR_ZIP_CD"),
            Map.entry(VendorAddressField.vendorCountryCode, "VNDR_CNTRY_CD"),
            Map.entry(VendorAddressField.vendorAttentionName, "VNDR_ATTN_NM"),
            Map.entry(VendorAddressField.vendorAddressInternationalProvinceName, "VNDR_ADDR_INTL_PROV_NM"),
            Map.entry(VendorAddressField.vendorAddressEmailAddress, "VNDR_ADDR_EMAIL_ADDR"),
            Map.entry(VendorAddressField.active, "DOBJ_MAINT_CD_ACTV_IND"),

            Map.entry(VendorField.vendorHeaderGeneratedIdentifier_forHeader, "VNDR_HDR_GNRTD_ID"),
            Map.entry(VendorField.vendorHeaderGeneratedIdentifier_forDetail, "VNDR_HDR_GNRTD_ID"),
            Map.entry(VendorField.vendorDetailAssignedIdentifier, "VNDR_DTL_ASND_ID"),
            Map.entry(VendorField.vendorParentIndicator, "VNDR_PARENT_IND"),
            Map.entry(VendorField.vendorFirstLastNameIndicator, "VNDR_1ST_LST_NM_IND"),
            Map.entry(VendorField.vendorName, "VNDR_NM"),
            Map.entry(VendorField.vendorTaxNumber, "VNDR_US_TAX_NBR"),
            Map.entry(VendorField.vendorTypeCode, "VNDR_TYP_CD"),
            Map.entry(VendorField.vendorOwnershipCode, "VNDR_OWNR_CD"),
            Map.entry(VendorField.vendorOwnershipCategoryCode, "VNDR_OWNR_CTGRY_CD"),
            Map.entry(VendorField.vendorForeignIndicator, "VNDR_FRGN_IND"),
            Map.entry(VendorField.vendorGIIN, "VNDR_GIIN"),
            Map.entry(VendorField.vendorChapter4StatusCode, "VNDR_CHAP_4_STAT_CD"),

            Map.entry(TransactionDetailField.transactionDetailId, "IRS_1099_1042S_DETAIL_ID"),
            Map.entry(TransactionDetailField.reportYear, "REPORT_YEAR"),
            Map.entry(TransactionDetailField.documentNumber, "FDOC_NBR"),
            Map.entry(TransactionDetailField.documentType, "DOC_TYPE"),
            Map.entry(TransactionDetailField.financialDocumentLineNumber, "FDOC_LINE_NBR"),
            Map.entry(TransactionDetailField.finObjectCode, "FIN_OBJECT_CD"),
            Map.entry(TransactionDetailField.netPaymentAmount, "NET_PMT_AMT"),
            Map.entry(TransactionDetailField.documentTitle, "DOC_TITLE"),
            Map.entry(TransactionDetailField.vendorTaxNumber, "VENDOR_TAX_NBR"),
            Map.entry(TransactionDetailField.incomeCode, "INCOME_CODE"),
            Map.entry(TransactionDetailField.incomeCodeSubType, "INCOME_CODE_SUB_TYPE"),
            Map.entry(TransactionDetailField.dvCheckStubText, "DV_CHK_STUB_TXT"),
            Map.entry(TransactionDetailField.payeeId, "PAYEE_ID"),
            Map.entry(TransactionDetailField.vendorName, "VENDOR_NAME"),
            Map.entry(TransactionDetailField.parentVendorName, "PARENT_VENDOR_NAME"),
            Map.entry(TransactionDetailField.vendorTypeCode, "VNDR_TYP_CD"),
            Map.entry(TransactionDetailField.vendorOwnershipCode, "VNDR_OWNR_CD"),
            Map.entry(TransactionDetailField.vendorOwnershipCategoryCode, "VNDR_OWNR_CTGRY_CD"),
            Map.entry(TransactionDetailField.vendorForeignIndicator, "VNDR_FRGN_IND"),
            Map.entry(TransactionDetailField.vendorEmailAddress, "VNDR_EMAIL_ADDR"),
            Map.entry(TransactionDetailField.vendorChapter4StatusCode, "VNDR_CHAP_4_STAT_CD"),
            Map.entry(TransactionDetailField.vendorGIIN, "VNDR_GIIN"),
            Map.entry(TransactionDetailField.vendorLine1Address, "VNDR_LN1_ADDR"),
            Map.entry(TransactionDetailField.vendorLine2Address, "VNDR_LN2_ADDR"),
            Map.entry(TransactionDetailField.vendorCityName, "VNDR_CTY_NM"),
            Map.entry(TransactionDetailField.vendorStateCode, "VNDR_ST_CD"),
            Map.entry(TransactionDetailField.vendorZipCode, "VNDR_ZIP_CD"),
            Map.entry(TransactionDetailField.vendorForeignLine1Address, "VNDR_FRGN_LN1_ADDR"),
            Map.entry(TransactionDetailField.vendorForeignLine2Address, "VNDR_FRGN_LN2_ADDR"),
            Map.entry(TransactionDetailField.vendorForeignCityName, "VNDR_FRGN_CTY_NM"),
            Map.entry(TransactionDetailField.vendorForeignZipCode, "VNDR_FRGN_ZIP_CD"),
            Map.entry(TransactionDetailField.vendorForeignProvinceName, "VNDR_FRGN_PROV_NM"),
            Map.entry(TransactionDetailField.vendorForeignCountryCode, "VNDR_FRGN_CNTRY_CD"),
            Map.entry(TransactionDetailField.nraPaymentIndicator, "NRA_PAYMENT_IND"),
            Map.entry(TransactionDetailField.paymentDate, "PMT_DT"),
            Map.entry(TransactionDetailField.paymentPayeeName, "PMT_PAYEE_NM"),
            Map.entry(TransactionDetailField.incomeClassCode, "INC_CLS_CD"),
            Map.entry(TransactionDetailField.incomeTaxTreatyExemptIndicator, "INC_TAX_TRTY_EXMPT_IND"),
            Map.entry(TransactionDetailField.foreignSourceIncomeIndicator, "FRGN_SRC_INC_IND"),
            Map.entry(TransactionDetailField.federalIncomeTaxPercent, "FED_INC_TAX_PCT"),
            Map.entry(TransactionDetailField.paymentDescription, "PMT_DESC"),
            Map.entry(TransactionDetailField.paymentLine1Address, "PMT_LN1_ADDR"),
            Map.entry(TransactionDetailField.paymentCountryName, "PMT_CNTRY_NM"),
            Map.entry(TransactionDetailField.chartCode, "KFS_CHART"),
            Map.entry(TransactionDetailField.accountNumber, "KFS_ACCOUNT"),
            Map.entry(TransactionDetailField.initiatorNetId, "INITIATOR_NETID"),
            Map.entry(TransactionDetailField.form1099Type, "FORM_1099_TYPE"),
            Map.entry(TransactionDetailField.form1099Box, "FORM_1099_BOX"),
            Map.entry(TransactionDetailField.form1099OverriddenType, "FORM_1099_OVERRIDDEN_TYPE"),
            Map.entry(TransactionDetailField.form1099OverriddenBox, "FORM_1099_OVERRIDDEN_BOX"),
            Map.entry(TransactionDetailField.form1042SBox, "FORM_1042S_BOX"),
            Map.entry(TransactionDetailField.form1042SOverriddenBox, "FORM_1042S_OVERRIDDEN_BOX"),
            Map.entry(TransactionDetailField.paymentReasonCode, "PMT_REASON_CD"),
            Map.entry(TransactionDetailField.disbursementNbr, "DISB_NBR"),
            Map.entry(TransactionDetailField.paymentStatusCode, "PMT_STAT_CD"),
            Map.entry(TransactionDetailField.disbursementTypeCode, "DISB_TYP_CD"),
            Map.entry(TransactionDetailField.ledgerDocumentTypeCode, "LEDGER_DOC_TYP_CD")
    );

    @Override
    protected Class<? extends BusinessObject> getMetadataForBusinessObject(            
            final Class<? extends BusinessObject> businessObjectClass) {
        if (!MAPPED_TABLES.containsKey(businessObjectClass)) {
            final RuntimeException nestedException = new RuntimeException(
                    "Business object is not mapped for tax use: " + businessObjectClass.getName());
            throw new ClassNotPersistableException(
                    "Invalid/Non-persistable business object: " + businessObjectClass.getName(), nestedException);
        }
        return businessObjectClass;
    }

    @Override
    protected String getTableName(Class<? extends BusinessObject> classAsMetadata) {
        return MAPPED_TABLES.get(classAsMetadata);
    }

    @Override
    protected String getColumnLabel(TaxDtoFieldEnum fieldMapping, Class<? extends BusinessObject> classAsMetadata) {
        return MAPPED_COLUMNS.get((Enum<?>) fieldMapping);
    }

}
