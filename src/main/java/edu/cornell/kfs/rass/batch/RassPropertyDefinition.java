package edu.cornell.kfs.rass.batch;

public class RassPropertyDefinition {

    private String xmlPropertyName;
    private String boPropertyName;
    private boolean required;
    private RassValueConverter valueConverter;
    private boolean truncateWithEllipsis;
    
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

	public RassValueConverter getValueConverter() {
		return valueConverter;
	}

	public void setValueConverter(RassValueConverter valueConverter) {
		this.valueConverter = valueConverter;
	}

    public boolean isTruncateWithEllipsis() {
        return truncateWithEllipsis;
    }

    public void setTruncateWithEllipsis(boolean truncateWithEllipsis) {
        this.truncateWithEllipsis = truncateWithEllipsis;
    }

}
