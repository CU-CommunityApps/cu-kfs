package edu.cornell.kfs.coa.fixture;

public enum ObjectCodeFixture {
    OC_IT_6600("IT", "6600", true),
    OC_CS_6600("CS", "6600", false),
    OC_IT_1000("IT", "1000", true),
    OC_IT_4020("IT", "4020", true);

    public final String chartOfAccountsCode;
    public final String objectCode;
    public final boolean active;

    private ObjectCodeFixture(String chartOfAccountsCode, String objectCode, boolean active) {
        this.chartOfAccountsCode = chartOfAccountsCode;
        this.objectCode = objectCode;
        this.active = active;
    }
}
