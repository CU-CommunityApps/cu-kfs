package edu.cornell.kfs.tax.batch.dto;

public class TaxBoxUpdates {

    private String form1042SBoxToUse;
    private String form1042SOverriddenBox;

    public String getForm1042SBoxToUse() {
        return form1042SBoxToUse;
    }

    public void setForm1042SBoxToUse(final String form1042SBoxToUse) {
        this.form1042SBoxToUse = form1042SBoxToUse;
    }

    public String getForm1042SOverriddenBox() {
        return form1042SOverriddenBox;
    }

    public void setForm1042SOverriddenBox(final String form1042sOverriddenBox) {
        this.form1042SOverriddenBox = form1042sOverriddenBox;
    }

}
