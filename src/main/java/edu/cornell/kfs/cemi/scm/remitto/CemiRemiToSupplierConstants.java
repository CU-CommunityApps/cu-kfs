package edu.cornell.kfs.cemi.scm.remitto;

import java.time.format.DateTimeFormatter;

public final class CemiRemiToSupplierConstants {
    public static final String REMIT_TO_SUPPLIER_TEMPLATE_FILE = "edu/cornell/kfs/cemi/scm/remitto/batch/Remit_To_Supplier.xlsx";
    public static final String REMIT_TO_SUPPLIER_SHEET_NAME = "Remit_To_Supplier";
    public static final int DATA_START_ROW = 6; // Row 7 in Excel (0-based)
    public static final int START_COLUMN_INDEX = 0;

    public static final String OUTPUT_FILE_NAME_FORMAT = "Remit_To_Supplier_{0}.xlsx";
    public static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    
    public static final String SUPPLIER_CONNECTION_ID_FORMAT = "{0}_{1}";
    public static final int MAX_ACCEPTED_PAYMENT_TYPES = 3;

}
