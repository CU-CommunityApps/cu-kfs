package edu.cornell.kfs.coa.fixture;

public enum ChartFixture {
    CHART_IT("IT", true),
    CHART_CS("CS", true);

    public final String chartOfAccountsCode;
    public final boolean active;

    private ChartFixture(String chartOfAccountsCode, boolean active) {
        this.chartOfAccountsCode = chartOfAccountsCode;
        this.active = active;
    }
}
