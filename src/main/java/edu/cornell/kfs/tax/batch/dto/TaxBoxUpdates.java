package edu.cornell.kfs.tax.batch.dto;

public class TaxBoxUpdates {

    private String form1042SBox;
    private String form1042SOverriddenBox;

    public String getForm1042SBox() {
        return form1042SBox;
    }

    public void setForm1042SBox(final String form1042sBox) {
        form1042SBox = form1042sBox;
    }

    public String getForm1042SOverriddenBox() {
        return form1042SOverriddenBox;
    }

    public void setForm1042SOverriddenBox(final String form1042sOverriddenBox) {
        form1042SOverriddenBox = form1042sOverriddenBox;
    }

}
