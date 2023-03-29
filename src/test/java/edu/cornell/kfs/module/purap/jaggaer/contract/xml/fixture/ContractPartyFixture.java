package edu.cornell.kfs.module.purap.jaggaer.contract.xml.fixture;

public enum ContractPartyFixture {

    CORNELL_UNIVERSITY(true, true, "Cornell University", "1002003004", null, null, null),
    TESTING_AND_TRYING_LLC(false, true, "Testing & Trying LLC", "1002345678", "116543-0", null, "203142");

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

}
