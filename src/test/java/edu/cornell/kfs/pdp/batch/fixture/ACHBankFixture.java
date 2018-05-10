package edu.cornell.kfs.pdp.batch.fixture;

import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.pdp.businessobject.ACHBank;

public enum ACHBankFixture {
    FIRST_BANK("111000999", "First Bank", true),
    SECOND_BANK("666777888", "Second Bank", true);

    public final String bankRoutingNumber;
    public final String bankName;
    public final boolean active;

    private ACHBankFixture(String bankRoutingNumber, String bankName, boolean active) {
        this.bankRoutingNumber = bankRoutingNumber;
        this.bankName = bankName;
        this.active = active;
    }

    public String getBankRoutingNumber() {
        return bankRoutingNumber;
    }

    public ACHBank toACHBank() {
        ACHBank achBank = new ACHBank();
        achBank.setBankRoutingNumber(bankRoutingNumber);
        achBank.setBankName(bankName);
        achBank.setActive(active);
        return achBank;
    }

    public static Optional<ACHBankFixture> findBankByRoutingNumber(String bankRoutingNumber) {
        return Arrays.stream(ACHBankFixture.values())
                .filter((fixture) -> StringUtils.equals(bankRoutingNumber, fixture.bankRoutingNumber))
                .findFirst();
    }

}
