package edu.cornell.kfs.pdp.batch;

/**
 * Enum defining the column value types from Payee ACH Account Extract .csv files.
 * An extra "headerName" field has been added because the actual
 * column header names have spaces or non-alphanumeric chars in them.
 * For convenience, toString() will return the "headerName" value
 * instead of the enum constant name, though the latter can still
 * be retrieved via the name() method.
 */
public enum PayeeACHAccountExtractCsv {
    employeeId("EMPL ID"),
    netID("NET ID"),
    lastName("LAST NAME"),
    firstName("FIRST NAME"),
    paymentType("PAYMENT TYPE"),
    balanceAccount("BALANCE ACCT?"),
    completedDate("COMPLETE DT"),
    bankName("BANK NAME"),
    bankRoutingNumber("ROUTING NO"),
    bankAccountNumber("ACCOUNT NO"),
    bankAccountType("ACCOUNT TYPE");

    private final String headerName;

    PayeeACHAccountExtractCsv(String headerName) {
        this.headerName = headerName;
    }

    /**
     * Overridden to return the actual header name that is
     * expected to be in the input file.
     * 
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return headerName;
    }

}
