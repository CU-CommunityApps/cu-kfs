package edu.cornell.kfs.vnd.batch.dto;

import java.util.List;

public class CemiSupplierBankAccount {

    private final String supplierId;

    private final List<CemiSupplierBankAccountSubEntry> subEntries;

    public CemiSupplierBankAccount(final String supplierId, final List<CemiSupplierBankAccountSubEntry> subEntries) {
        this.supplierId = supplierId;
        this.subEntries = subEntries;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public List<CemiSupplierBankAccountSubEntry> getSubEntries() {
        return subEntries;
    }

}
