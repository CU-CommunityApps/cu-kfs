package edu.cornell.kfs.sys.businessobject;

import edu.cornell.kfs.sys.batch.service.HistoricalTableLookupCriteriaPurgeService;

/**
 * CU Customization: Historical Purge Table Job : short lived batch job to clear backlog of Initiated documents.
 *
 */

public class HistoricalTableDetailsForPurge {
    protected Class businessObjectForRecordsTablePurge;
    protected String tableToPurge;
    protected boolean useDefaultDaysBeforePurgeParameter;
    protected String nameSpaceCode;
    protected String component;
    protected String parameterName;
    protected HistoricalTableLookupCriteriaPurgeService serviceImplForPurgeTableLookupCriteria;
    protected String fetchRowCountToPurgeParameterName;
    
    public HistoricalTableDetailsForPurge() {
    }
    
    public HistoricalTableDetailsForPurge(Class businessObjectForRecordsTablePurge, String tableToPurge, 
            boolean useDefaultDaysBeforePurgeParameter, String nameSpaceCode, String component, String parameterName, 
            HistoricalTableLookupCriteriaPurgeService serviceImplForPurgeTableLookupCriteria,
            String fetchRowCountToPurgeParameterName) {
        this.businessObjectForRecordsTablePurge = businessObjectForRecordsTablePurge;
        this.tableToPurge = tableToPurge;
        this.useDefaultDaysBeforePurgeParameter = useDefaultDaysBeforePurgeParameter;
        this.nameSpaceCode = nameSpaceCode;
        this.component = component;
        this.parameterName = parameterName;
        this.serviceImplForPurgeTableLookupCriteria = serviceImplForPurgeTableLookupCriteria;
        this.fetchRowCountToPurgeParameterName = fetchRowCountToPurgeParameterName;
    }
    
    public Class getBusinessObjectForRecordsTablePurge() {
        return businessObjectForRecordsTablePurge;
    }
    
    public void setBusinessObjectForRecordsTablePurge(Class businessObjectForRecordsTablePurge) {
        this.businessObjectForRecordsTablePurge = businessObjectForRecordsTablePurge;
    }
    
    public String getTableToPurge() {
        return tableToPurge;
    }

    public void setTableToPurge(String tableToPurge) {
        this.tableToPurge = tableToPurge;
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

    public HistoricalTableLookupCriteriaPurgeService getServiceImplForPurgeTableLookupCriteria() {
        return serviceImplForPurgeTableLookupCriteria;
    }

    public void setServiceImplForPurgeTableLookupCriteria(HistoricalTableLookupCriteriaPurgeService serviceImplForPurgeTableLookupCriteria) {
        this.serviceImplForPurgeTableLookupCriteria = serviceImplForPurgeTableLookupCriteria;
    }

    public String getFetchRowCountToPurgeParameterName() {
        return fetchRowCountToPurgeParameterName;
    }

    public void setFetchRowCountToPurgeParameterName(String fetchRowCountToPurgeParameterName) {
        this.fetchRowCountToPurgeParameterName = fetchRowCountToPurgeParameterName;
    }

}
