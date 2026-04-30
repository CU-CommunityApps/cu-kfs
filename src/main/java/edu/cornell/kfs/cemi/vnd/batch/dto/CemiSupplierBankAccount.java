package edu.cornell.kfs.cemi.vnd.batch.dto;

import java.util.List;

import edu.cornell.kfs.cemi.sys.batch.dto.CemiDtoWithDateAndIndex;
import edu.cornell.kfs.cemi.sys.util.CemiDtoIndexer;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiVendorConstants;

public class CemiSupplierBankAccount extends CemiDtoWithDateAndIndex {

    private final String supplierId;

    private final List<CemiSupplierBankAccountSubEntry> accounts;

    public CemiSupplierBankAccount(final CemiDtoIndexer indexer, final String supplierId,
            final CemiSupplierBankAccountSubEntry... accounts) {
        super(indexer);
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
