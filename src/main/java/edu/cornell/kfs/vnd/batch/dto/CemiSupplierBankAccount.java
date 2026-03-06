package edu.cornell.kfs.vnd.batch.dto;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CemiSupplierBankAccount {

    private final String supplierId;

    private final List<CemiSupplierBankAccountSubEntry> accounts;

    public CemiSupplierBankAccount(final String supplierId, final Stream<CemiSupplierBankAccountSubEntry> accounts) {
        this.supplierId = supplierId;
        this.accounts = accounts.collect(Collectors.toUnmodifiableList());
    }

    public String getSupplierId() {
        return supplierId;
    }

    public List<CemiSupplierBankAccountSubEntry> getAccounts() {
        return accounts;
    }

}
