package edu.cornell.kfs.vnd.batch.dto;

import java.util.List;

import edu.cornell.kfs.sys.util.CemiUtils;
import edu.cornell.kfs.vnd.CemiVendorConstants;

public class CemiSupplierBankAccount {

    private final String supplierId;

    private final List<CemiSupplierBankAccountSubEntry> accounts;

    public CemiSupplierBankAccount(final String supplierId, final CemiSupplierBankAccountSubEntry... accounts) {
        this.supplierId = supplierId;
        this.accounts = CemiUtils.createListPaddedToMinimumSizeIfNecessary(
                CemiSupplierBankAccountSubEntry.EMPTY, CemiVendorConstants.MAX_SUPPLIER_BANK_ACCOUNT_ENTRIES, accounts);
    }

    public String getSupplierId() {
        return supplierId;
    }

    public List<CemiSupplierBankAccountSubEntry> getAccounts() {
        return accounts;
    }

}
