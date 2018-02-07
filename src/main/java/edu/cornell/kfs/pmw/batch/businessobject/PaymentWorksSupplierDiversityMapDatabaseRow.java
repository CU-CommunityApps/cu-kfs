package edu.cornell.kfs.pmw.batch.businessobject;

public class PaymentWorksSupplierDiversityMapDatabaseRow {
    
    private String kfsSupplierDiversityCode;
    private String kfsSupplierDiversityDescription;
    private String pmwSupplierDiversityDescription;
    
    public PaymentWorksSupplierDiversityMapDatabaseRow() {
        this.kfsSupplierDiversityCode = null;
        this.kfsSupplierDiversityDescription = null;
        this.pmwSupplierDiversityDescription = null;
    }
    
    public PaymentWorksSupplierDiversityMapDatabaseRow(String kfsSupplierDiversityCode, String kfsSupplierDiversityDescription, String pmwSupplierDiversityDescription) {
        this.kfsSupplierDiversityCode = kfsSupplierDiversityCode;
        this.kfsSupplierDiversityDescription = kfsSupplierDiversityDescription;
        this.pmwSupplierDiversityDescription = pmwSupplierDiversityDescription;
    }

    public String getKfsSupplierDiversityCode() {
        return kfsSupplierDiversityCode;
    }

    public void setKfsSupplierDiversityCode(String kfsSupplierDiversityCode) {
        this.kfsSupplierDiversityCode = kfsSupplierDiversityCode;
    }

    public String getKfsSupplierDiversityDescription() {
        return kfsSupplierDiversityDescription;
    }

    public void setKfsSupplierDiversityDescription(String kfsSupplierDiversityDescription) {
        this.kfsSupplierDiversityDescription = kfsSupplierDiversityDescription;
    }

    public String getPmwSupplierDiversityDescription() {
        return pmwSupplierDiversityDescription;
    }

    public void setPmwSupplierDiversityDescription(String pmwSupplierDiversityDescription) {
        this.pmwSupplierDiversityDescription = pmwSupplierDiversityDescription;
    }
}
