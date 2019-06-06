package edu.cornell.kfs.rass.batch;

public class RassPropertyDefinition {

    private String xmlPropertyName;
    private String boPropertyName;
    private boolean required;
    private String valueConverter;
    
    public String getXmlPropertyName() {
        return xmlPropertyName;
        
    }

    public void setXmlPropertyName(String xmlPropertyName) {
        this.xmlPropertyName = xmlPropertyName;
    }

    public String getBoPropertyName() {
        return boPropertyName;
    }

    public void setBoPropertyName(String boPropertyName) {
        this.boPropertyName = boPropertyName;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

	public String getValueConverter() {
		return valueConverter;
	}

	public void setValueConverter(String valueConverter) {
		this.valueConverter = valueConverter;
	}

}
