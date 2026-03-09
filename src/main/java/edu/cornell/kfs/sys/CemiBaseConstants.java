package edu.cornell.kfs.sys;

public final class CemiBaseConstants {

    public static final String CEMI_OUTPUT_DEFINITION_FILE_TYPE_IDENTIFIER = "cemiOutputDefinitionFileType";

    public enum CemiFieldDefinitionType {
        STATIC,
        STRING;
    }
    
    // This is a boolean KFS local configuration property value.
    // When set to true, the amount of data retrieved for Supplier CEMI file creation WILL be restricted
    // to what is currently HARD CODED in CuVendorDaoOjb.getVendorsForCemiSupplierExtractAsCloseableStream
    // The batch job WILL successfully execute when this local configuration property is not defined.
    public static final String CU_CEMI_DEVELOPMENT_USE_SMALLER_DATA_SET_KEY = "cu.cemi.development.use.smaller.data.set";
    
    public static final String UNMASK = "UNMASK";
    

}
