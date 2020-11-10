package edu.cornell.kfs.tax.batch;

/**
 * Enum defining the expected headers and columns on Transaction Override input files.
 * Some of the header names may be different from the enum constant name; therefore,
 * the toString() method has been customized to return the expected header name.
 * 
 * NOTE: If the transaction row output file is updated to change the names of its
 * output headers, then this enum class should preferably be updated accordingly.
 * This will allow tax managers to tweak the output file accordingly for use on
 * the Transaction Override upload screen, without having to manually update
 * the header row's column names.
 */
public enum TransactionOverrideCsv {
    Doc_Number,
    Doc_Line_Number,
    Payment_Date,
    Form_1099_Type("1099_Type"),
    Form_1099_Box("1099_Box"),
    Form_1042S_Box("1042S_Box");

    private final String actualHeaderName;

    TransactionOverrideCsv() {
        this.actualHeaderName = null;
    }

    TransactionOverrideCsv(String actualHeaderName) {
        this.actualHeaderName = actualHeaderName;
    }

    /**
     * Overridden to return the header's actual name, which may or may not
     * be the same as the enum constant's name.
     * 
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return (actualHeaderName != null) ? actualHeaderName : name();
    }
}
