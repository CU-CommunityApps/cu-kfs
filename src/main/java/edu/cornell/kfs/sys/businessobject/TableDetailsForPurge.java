package edu.cornell.kfs.sys.businessobject;

public class TableDetailsForPurge {
    protected Class businessObjectForRecordsTablePurge;
    protected boolean useDefaultDaysBeforePurgeParameter;
    protected String nameSpaceCode;
    protected String component;
    protected String parameterName;
    protected String serviceImplForPurgeTableLookupCriteria; 
    
    public TableDetailsForPurge(Class businessObjectForRecordsTablePurge, boolean useDefaultDaysBeforePurgeParameter, String nameSpaceCode,
                                String component, String parameterName, String serviceImplForPurgeTableLookupCriteria) {
        this.businessObjectForRecordsTablePurge = businessObjectForRecordsTablePurge;
        this.useDefaultDaysBeforePurgeParameter = useDefaultDaysBeforePurgeParameter;
        this.nameSpaceCode = nameSpaceCode;
        this.component = component;
        this.parameterName = parameterName;
        this.serviceImplForPurgeTableLookupCriteria = serviceImplForPurgeTableLookupCriteria;
    }
    
    public Class getBusinessObjectForRecordsTablePurge() {
        return businessObjectForRecordsTablePurge;
    }
    
    public void setBusinessObjectForRecordsTablePurge(Class businessObjectForRecordsTablePurge) {
        this.businessObjectForRecordsTablePurge = businessObjectForRecordsTablePurge;
    }
    
    public boolean isUseDefaultDaysBeforePurgeParameter() {
        return useDefaultDaysBeforePurgeParameter;
    }
    
    public void setUseDefaultDaysBeforePurgeParameter(boolean useDefaultDaysBeforePurgeParameter) {
        this.useDefaultDaysBeforePurgeParameter = useDefaultDaysBeforePurgeParameter;
    }
    
    public String getNameSpaceCode() {
        return nameSpaceCode;
    }
    
    public void setNameSpaceCode(String nameSpaceCode) {
        this.nameSpaceCode = nameSpaceCode;
    }
    
    public String getComponent() {
        return component;
    }
    
    public void setComponent(String component) {
        this.component = component;
    }
    
    public String getParameterName() {
        return parameterName;
    }
    
    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getServiceImplForPurgeTableLookupCriteria() {
        return serviceImplForPurgeTableLookupCriteria;
    }

    public void setServiceImplForPurgeTableLookupCriteria(String serviceImplForPurgeTableLookupCriteria) {
        this.serviceImplForPurgeTableLookupCriteria = serviceImplForPurgeTableLookupCriteria;
    }

}
