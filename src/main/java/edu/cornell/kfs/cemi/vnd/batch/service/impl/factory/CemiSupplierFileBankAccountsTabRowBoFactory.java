package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierBankAccountBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileBankAccountsTabRowBo;

public class CemiSupplierFileBankAccountsTabRowBoFactory {

    private static final Logger LOG = LogManager.getLogger();

    private String supplierId;
    private List<CemiSupplierBankAccountBo> bankAccounts;

    public CemiSupplierFileBankAccountsTabRowBoFactory(final String supplierId,
            final List<CemiSupplierBankAccountBo> bankAccounts) {
        Validate.notBlank(supplierId, "supplierId cannot be blank");
        Validate.isTrue(CollectionUtils.size(bankAccounts) >= CemiSupplierConstants.MAX_SUPPLIER_BANK_ACCOUNT_ENTRIES,
                "bankAccounts list must have at least %s entries, with empty placeholder BOs for any absent accounts",
                CemiSupplierConstants.MAX_SUPPLIER_BANK_ACCOUNT_ENTRIES);
        this.supplierId = supplierId;
        this.bankAccounts = bankAccounts;
    }

    public static CemiSupplierFileBankAccountsTabRowBo creatTabRowBoFrom(final String supplierId,
            final List<CemiSupplierBankAccountBo> bankAccounts) {
        final CemiSupplierFileBankAccountsTabRowBoFactory factory = new CemiSupplierFileBankAccountsTabRowBoFactory(
                supplierId, bankAccounts);
        return factory.createCemiSupplierFileBankAccountsTabRowBo();
    }

    public CemiSupplierFileBankAccountsTabRowBo createCemiSupplierFileBankAccountsTabRowBo() {
        if (bankAccounts.size() > CemiSupplierConstants.MAX_SUPPLIER_BANK_ACCOUNT_ENTRIES) {
            LOG.warn("createCemiSupplierFileBankAccountsTabRowBo, {} bank accounts were specified for Supplier {}. "
                    + "Only the first {} will be used.",
                    bankAccounts.size(), supplierId, CemiSupplierConstants.MAX_SUPPLIER_BANK_ACCOUNT_ENTRIES);
        }

        final CemiSupplierFileBankAccountsTabRowBo bankAccountRowBo = new CemiSupplierFileBankAccountsTabRowBo();
        final CemiSupplierBankAccountBo bankAccount1 = bankAccounts.get(0);
        final CemiSupplierBankAccountBo bankAccount2 = bankAccounts.get(1);
        final CemiSupplierBankAccountBo bankAccount3 = bankAccounts.get(2);

        bankAccountRowBo.setSupplierId(supplierId);
        bankAccountRowBo.setSettlementBankAccountId1(bankAccount1.getSettlementBankAccountId());
        bankAccountRowBo.setBankAccountNickname1(bankAccount1.getBankAccountNickname());
        bankAccountRowBo.setBankAccountType1(bankAccount1.getBankAccountType());
        bankAccountRowBo.setBankName1(bankAccount1.getBankName());
        bankAccountRowBo.setRoutingTransitNumber1(bankAccount1.getRoutingTransitNumber());
        bankAccountRowBo.setBranchId1(bankAccount1.getBranchId());
        bankAccountRowBo.setBranchName1(bankAccount1.getBranchName());
        bankAccountRowBo.setBankAccountNumber1(bankAccount1.getBankAccountNumber());
        bankAccountRowBo.setAcceptsPaymentTypes1_1(bankAccount1.getAcceptsPaymentTypes1());
        bankAccountRowBo.setAcceptsPaymentTypes1_2(bankAccount1.getAcceptsPaymentTypes2());
        bankAccountRowBo.setAcceptsPaymentTypes1_3(bankAccount1.getAcceptsPaymentTypes3());
        bankAccountRowBo.setPaymentTypes1_1(bankAccount1.getPaymentTypes1());
        bankAccountRowBo.setPaymentTypes1_2(bankAccount1.getPaymentTypes2());
        bankAccountRowBo.setPaymentTypes1_3(bankAccount1.getPaymentTypes3());
        bankAccountRowBo.setSettlementBankAccountId2(bankAccount2.getSettlementBankAccountId());
        bankAccountRowBo.setBankAccountNickname2(bankAccount2.getBankAccountNickname());
        bankAccountRowBo.setBankAccountType2(bankAccount2.getBankAccountType());
        bankAccountRowBo.setBankName2(bankAccount2.getBankName());
        bankAccountRowBo.setRoutingTransitNumber2(bankAccount2.getRoutingTransitNumber());
        bankAccountRowBo.setBranchId2(bankAccount2.getBranchId());
        bankAccountRowBo.setBranchName2(bankAccount2.getBranchName());
        bankAccountRowBo.setBankAccountNumber2(bankAccount2.getBankAccountNumber());
        bankAccountRowBo.setAcceptsPaymentTypes2_1(bankAccount2.getAcceptsPaymentTypes1());
        bankAccountRowBo.setAcceptsPaymentTypes2_2(bankAccount2.getAcceptsPaymentTypes2());
        bankAccountRowBo.setAcceptsPaymentTypes2_3(bankAccount2.getAcceptsPaymentTypes3());
        bankAccountRowBo.setPaymentTypes2_1(bankAccount2.getPaymentTypes1());
        bankAccountRowBo.setPaymentTypes2_2(bankAccount2.getPaymentTypes2());
        bankAccountRowBo.setPaymentTypes2_3(bankAccount2.getPaymentTypes3());
        bankAccountRowBo.setSettlementBankAccountId3(bankAccount3.getSettlementBankAccountId());
        bankAccountRowBo.setBankAccountNickname3(bankAccount3.getBankAccountNickname());
        bankAccountRowBo.setBankAccountType3(bankAccount3.getBankAccountType());
        bankAccountRowBo.setBankName3(bankAccount3.getBankName());
        bankAccountRowBo.setRoutingTransitNumber3(bankAccount3.getRoutingTransitNumber());
        bankAccountRowBo.setBranchId3(bankAccount3.getBranchId());
        bankAccountRowBo.setBranchName3(bankAccount3.getBranchName());
        bankAccountRowBo.setBankAccountNumber3(bankAccount3.getBankAccountNumber());
        bankAccountRowBo.setAcceptsPaymentTypes3_1(bankAccount3.getAcceptsPaymentTypes1());
        bankAccountRowBo.setAcceptsPaymentTypes3_2(bankAccount3.getAcceptsPaymentTypes2());
        bankAccountRowBo.setAcceptsPaymentTypes3_3(bankAccount3.getAcceptsPaymentTypes3());
        bankAccountRowBo.setPaymentTypes3_1(bankAccount3.getPaymentTypes1());
        bankAccountRowBo.setPaymentTypes3_2(bankAccount3.getPaymentTypes2());
        bankAccountRowBo.setPaymentTypes3_3(bankAccount3.getPaymentTypes3());

        return bankAccountRowBo;
    }

}
