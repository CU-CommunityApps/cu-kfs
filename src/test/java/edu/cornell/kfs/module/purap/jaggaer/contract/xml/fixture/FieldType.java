package edu.cornell.kfs.module.purap.jaggaer.contract.xml.fixture;

public enum FieldType {

    FIELD_01("Something", "AccountNumber");

    public final String type;
    public final String internalName;

    private FieldType(String type, String internalName) {
        this.type = type;
        this.internalName = internalName;
    }

}
