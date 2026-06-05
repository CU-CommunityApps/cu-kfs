package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiOrderFromSupplierConstants;
import edu.cornell.kfs.cemi.vnd.CemiOrderFromSupplierConstants.DefaultPOTypes;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiOrderFromSupplierBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierEmailBo;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CemiOrderFromSupplierBoFactory {

    private CemiSupplierBo supplier;
    private CemiSupplierEmailBo supplierEmailRow;
    private String emailFromKfsVendorAddress;
    private String spreadsheetKey;
    private String supplierConnectionRowId;
    private boolean isFirstRowForSupplier;
    private boolean isPunchoutSupplier;
    private boolean isPunchoutConnection;

    private String emailRowId;
    private String emailId;
    private String emailAddress;

    public CemiOrderFromSupplierBoFactory withSupplier(final CemiSupplierBo supplier) {
        this.supplier = supplier;
        return this;
    }

    public CemiOrderFromSupplierBoFactory withSupplierEmailRow(final CemiSupplierEmailBo supplierEmailRow) {
        this.supplierEmailRow = supplierEmailRow;
        return this;
    }

    public CemiOrderFromSupplierBoFactory withEmailFromKfsVendorAddress(final String emailFromKfsVendorAddress) {
        this.emailFromKfsVendorAddress = emailFromKfsVendorAddress;
        return this;
    }

    public CemiOrderFromSupplierBoFactory withSpreadsheetKey(final String spreadsheetKey) {
        this.spreadsheetKey = spreadsheetKey;
        return this;
    }

    public CemiOrderFromSupplierBoFactory withSupplierConnectionRowId(final String supplierConnectionRowId) {
        this.supplierConnectionRowId = supplierConnectionRowId;
        return this;
    }

    public CemiOrderFromSupplierBoFactory withFirstRowForSupplierFlag(final boolean isFirstRowForSupplier) {
        this.isFirstRowForSupplier = isFirstRowForSupplier;
        return this;
    }

    public CemiOrderFromSupplierBoFactory withPunchoutSupplierFlag(final boolean isPunchoutSupplier) {
        this.isPunchoutSupplier = isPunchoutSupplier;
        return this;
    }

    public CemiOrderFromSupplierBoFactory withPunchoutConnectionFlag(final boolean isPunchoutConnection) {
        this.isPunchoutConnection = isPunchoutConnection;
        return this;
    }

    public CemiOrderFromSupplierBo createCemiOrderFromSupplierBo() {
        Validate.validState(supplier != null, "A supplier was not defined");
        Validate.validState(supplierEmailRow != null, "A supplier email record was not defined");
        Validate.validState(!isPunchoutConnection || isPunchoutSupplier,
                "Entries marked as punchout connections must also be marked as having a punchout supplier");
        Validate.validState(!isPunchoutConnection || isFirstRowForSupplier,
                "Entries marked as punchout connections must also be marked as being the supplier's first connection");

        initializeEmailData();

        final CemiOrderFromSupplierBo orderFromSupplier = new CemiOrderFromSupplierBo();

        final String supplierIdForOutput = isFirstRowForSupplier ? supplier.getSupplierId() : KFSConstants.EMPTY_STRING;
        final String autoComplete = isFirstRowForSupplier ? KRADConstants.YES_INDICATOR_VALUE : KFSConstants.EMPTY_STRING;
        final String connectionName = generateConnectionName();
        final String supplierReferenceId = generateSupplierReferenceId(connectionName);
        final List<String> defaultPOTypes = determineDefaultPOTypes();
        final String poIssueOption = determinePurchaseOrderIssueOption();
        final String isDefault = determineDefaultConnectionSetting();

        orderFromSupplier.setSpreadsheetKey(spreadsheetKey);
        orderFromSupplier.setSupplierIdHeader(supplierIdForOutput);
        orderFromSupplier.setAutoComplete(autoComplete);
        orderFromSupplier.setComment(KFSConstants.EMPTY_STRING);
        orderFromSupplier.setWorker(KFSConstants.EMPTY_STRING);
        orderFromSupplier.setSupplierReferenceId(supplierReferenceId);
        orderFromSupplier.setSupplierId(supplierIdForOutput);
        orderFromSupplier.setSupplierConnectionRowId(supplierConnectionRowId);
        orderFromSupplier.setSupplierConnection(KFSConstants.EMPTY_STRING);
        orderFromSupplier.setSupplierConnectionId(KFSConstants.EMPTY_STRING);
        orderFromSupplier.setSupplierConnectionName(connectionName);
        orderFromSupplier.setDefaultForPoType1(defaultPOTypes.get(0));
        orderFromSupplier.setDefaultForPoType2(defaultPOTypes.get(1));
        orderFromSupplier.setDefaultForPoType3(defaultPOTypes.get(2));
        orderFromSupplier.setShippingMethod(KFSConstants.EMPTY_STRING);
        orderFromSupplier.setShippingTerms(KFSConstants.EMPTY_STRING);
        orderFromSupplier.setPurchaseOrderIssueOption(poIssueOption);
        orderFromSupplier.setEmailRowId(emailRowId);
        orderFromSupplier.setEmailId(emailId);
        orderFromSupplier.setEmailAddress(emailAddress);
        orderFromSupplier.setRemitToSupplierConnection(KFSConstants.EMPTY_STRING);
        orderFromSupplier.setOrderFromAddressReference(KFSConstants.EMPTY_STRING);
        orderFromSupplier.setAlternateNameRowId(KFSConstants.EMPTY_STRING);
        orderFromSupplier.setAlternateName(KFSConstants.EMPTY_STRING);
        orderFromSupplier.setAlternateNameUsage(KFSConstants.EMPTY_STRING);
        orderFromSupplier.setIsDefault(isDefault);
        orderFromSupplier.setIsInactive(KFSConstants.EMPTY_STRING);
        orderFromSupplier.setMemo(KFSConstants.EMPTY_STRING);

        return orderFromSupplier;
    }

    private void initializeEmailData() {
        if (isPunchoutConnection) {
            emailRowId = KFSConstants.EMPTY_STRING;
            emailId = KFSConstants.EMPTY_STRING;
            emailAddress = KFSConstants.EMPTY_STRING;
        } else if (Strings.CI.equals(emailFromKfsVendorAddress, supplierEmailRow.getEmailAddress1())) {
            emailRowId = Integer.toString(1);
            emailId = supplierEmailRow.getEmailId1();
            emailAddress = supplierEmailRow.getEmailAddress1();
        } else if (Strings.CI.equals(emailFromKfsVendorAddress, supplierEmailRow.getEmailAddress2())) {
            emailRowId = Integer.toString(1);
            emailId = supplierEmailRow.getEmailId2();
            emailAddress = supplierEmailRow.getEmailAddress2();
        } else if (Strings.CI.equals(emailFromKfsVendorAddress, supplierEmailRow.getEmailAddress3())) {
            emailRowId = Integer.toString(1);
            emailId = supplierEmailRow.getEmailId3();
            emailAddress = supplierEmailRow.getEmailAddress3();
        } else {
            throw new IllegalStateException("Email not found for supplier: " + emailFromKfsVendorAddress);
        }
    }

    private String generateConnectionName() {
        if (isPunchoutConnection) {
            return CemiOrderFromSupplierConstants.ORDER_FROM_CONNECTION_NAME_CATALOG;
        } else {
            return CemiOrderFromSupplierConstants.ORDER_FROM_CONNECTION_NAME_EMAIL_PREFIX + emailAddress;
        }
    }

    private String generateSupplierReferenceId(final String connectionName) {
        return StringUtils.joinWith(CUKFSConstants.UNDERSCORE,
                supplier.getSupplierId(), connectionName, supplierConnectionRowId);
    }

    private List<String> determineDefaultPOTypes() {
        if (isPunchoutSupplier) {
            if (isPunchoutConnection) {
                return List.of(DefaultPOTypes.CATALOG, KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING);
            } else {
                return List.of(DefaultPOTypes.STANDARD, DefaultPOTypes.BLANKET_ORDER, DefaultPOTypes.SOLE_SOURCE);
            }
        } else {
            return CemiUtils.createListOfEmptyStrings(3);
        }
    }

    private String determinePurchaseOrderIssueOption() {
        if (isPunchoutConnection) {
            return CemiOrderFromSupplierConstants.PO_ISSUE_OPTION_XML_AUTO;
        } else {
            return CemiOrderFromSupplierConstants.PO_ISSUE_OPTION_EMAIL;
        }
    }

    private String determineDefaultConnectionSetting() {
        return CemiUtils.convertToBooleanValueForEIBFileExtract(isFirstRowForSupplier);
    }

}
