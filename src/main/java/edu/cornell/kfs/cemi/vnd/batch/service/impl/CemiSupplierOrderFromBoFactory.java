package edu.cornell.kfs.cemi.vnd.batch.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAddressBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierOrderFromBo;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CemiSupplierOrderFromBoFactory {

    private CemiSupplierAddressBo supplierAddress;
    private CemiSupplierBo supplier;
    private String spreadsheetKey;
    private String supplierConnectionRowId;
    private boolean isFirstRowForSupplier;

    public CemiSupplierOrderFromBoFactory withSupplierAddress(final CemiSupplierAddressBo supplierAddress) {
        this.supplierAddress = supplierAddress;
        return this;
    }

    public CemiSupplierOrderFromBoFactory withSupplier(final CemiSupplierBo supplier) {
        this.supplier = supplier;
        return this;
    }

    public CemiSupplierOrderFromBoFactory withSpreadsheetKey(final String spreadsheetKey) {
        this.spreadsheetKey = spreadsheetKey;
        return this;
    }

    public CemiSupplierOrderFromBoFactory withSupplierConnectionRowId(final String supplierConnectionRowId) {
        this.supplierConnectionRowId = supplierConnectionRowId;
        return this;
    }

    public CemiSupplierOrderFromBoFactory withFirstRowForSupplierFlag(final boolean isFirstRowForSupplier) {
        this.isFirstRowForSupplier = isFirstRowForSupplier;
        return this;
    }

    public CemiSupplierOrderFromBo createCemiSupplierOrderFromBo() {
        final CemiSupplierOrderFromBo supplierOrderFrom = new CemiSupplierOrderFromBo();

        final String supplierIdForOutput = isFirstRowForSupplier ? supplier.getSupplierId() : KFSConstants.EMPTY_STRING;
        final String autoComplete = isFirstRowForSupplier ? KRADConstants.YES_INDICATOR_VALUE : KFSConstants.EMPTY_STRING;
        final String connectionName = generateConnectionName();
        final String supplierReferenceId = generateSupplierReferenceId(connectionName);

        supplierOrderFrom.setSpreadsheetKey(spreadsheetKey);
        supplierOrderFrom.setSupplierIdHeader(supplierIdForOutput);
        supplierOrderFrom.setAutoComplete(autoComplete);
        supplierOrderFrom.setComment(KFSConstants.EMPTY_STRING);
        supplierOrderFrom.setWorker(KFSConstants.EMPTY_STRING);
        supplierOrderFrom.setSupplierReferenceId(supplierReferenceId);
        supplierOrderFrom.setSupplierId(supplierIdForOutput);
        supplierOrderFrom.setSupplierConnectionRowId(supplierConnectionRowId);
        supplierOrderFrom.setSupplierConnection(KFSConstants.EMPTY_STRING);
        supplierOrderFrom.setSupplierConnectionId(KFSConstants.EMPTY_STRING);
        supplierOrderFrom.setSupplierConnectionName(connectionName);
        supplierOrderFrom.setDefaultForPoType1(null);
        supplierOrderFrom.setDefaultForPoType2(null);
        supplierOrderFrom.setDefaultForPoType3(null);
        supplierOrderFrom.setShippingMethod(null);
        supplierOrderFrom.setShippingTerms(null);
        supplierOrderFrom.setPurchaseOrderIssueOption(null);
        supplierOrderFrom.setEmailRowId(null);
        supplierOrderFrom.setEmailId(null);
        supplierOrderFrom.setEmailAddress(null);
        supplierOrderFrom.setRemitToSupplierConnection(null);
        supplierOrderFrom.setOrderFromAddressReference(null);
        supplierOrderFrom.setAlternateNameRowId(null);
        supplierOrderFrom.setAlternateName(null);
        supplierOrderFrom.setAlternateNameUsage(null);
        supplierOrderFrom.setIsDefault(null);
        supplierOrderFrom.setIsInactive(null);
        supplierOrderFrom.setMemo(null);

        return supplierOrderFrom;
    }

    private String generateConnectionName() {
        return null;
    }

    private String generateSupplierReferenceId(final String connectionName) {
        return StringUtils.joinWith(CUKFSConstants.UNDERSCORE,
                supplier.getSupplierId(), connectionName, supplierConnectionRowId);
    }

}
