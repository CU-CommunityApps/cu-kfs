package edu.cornell.kfs.module.purap.jaggaer.contract.xml.fixture;

import static edu.cornell.kfs.sys.util.CuAssertions.assertStringEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.cornell.kfs.module.purap.jaggaer.contract.xml.ContractPartyBase;
import edu.cornell.kfs.module.purap.jaggaer.contract.xml.FirstParty;
import edu.cornell.kfs.module.purap.jaggaer.contract.xml.SecondParty;

public enum ContractPartyFixture {

    FIRST_PARTY_CORNELL(true, true, "CORNELL UNIVERSITY", null, null, null, null);

    public final boolean isFirstParty;
    public final boolean isPrimary;
    public final String name;
    public final String sciquestId;
    public final String erpNumber;
    public final String contactId;
    public final String addressId;

    private ContractPartyFixture(boolean isFirstParty, boolean isPrimary, String name, String sciquestId,
            String erpNumber, String contactId, String addressId) {
        this.isFirstParty = isFirstParty;
        this.isPrimary = isPrimary;
        this.name = name;
        this.sciquestId = sciquestId;
        this.erpNumber = erpNumber;
        this.contactId = contactId;
        this.addressId = addressId;
    }

    public void assertEqualsParty(ContractPartyBase contractParty) {
        if (isFirstParty) {
            assertTrue(contractParty instanceof FirstParty, "Contract party was not a 'first' party");
        } else {
            assertTrue(contractParty instanceof SecondParty, "Contract party was not a 'second' party");
        }
        assertEquals(isPrimary, contractParty.isPrimary(), "Wrong isPrimary setting");
        assertStringEquals(name, contractParty.getName(), "Wrong name");
        assertStringEquals(sciquestId, contractParty.getSciquestId(), "Wrong sciquestId");
        assertStringEquals(erpNumber, contractParty.getErpNumber(), "Wrong erpNumber");
        assertStringEquals(contactId, contractParty.getContactId(), "Wrong contactId");
        assertStringEquals(addressId, contractParty.getAddressId(), "Wrong addressId");
    }

}
