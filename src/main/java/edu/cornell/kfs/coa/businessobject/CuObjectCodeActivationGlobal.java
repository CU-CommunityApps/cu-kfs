package edu.cornell.kfs.coa.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.krad.bo.GlobalBusinessObject;
import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetail;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.PersistenceStructureService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.context.SpringContext;

public class CuObjectCodeActivationGlobal extends PersistableBusinessObjectBase implements GlobalBusinessObject {
    private static final long serialVersionUID = -3772910396694995779L;
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuObjectCodeActivationGlobal.class);
    
    private transient ObjectCodeService objectCodeService;
    
    protected String documentNumber;
    private Boolean activate;
    private List<CuObjectCodeGlobalDetail> cuObjectCodeGlobalDetails;
    
    public CuObjectCodeActivationGlobal() {
        super();
        cuObjectCodeGlobalDetails = new ArrayList<CuObjectCodeGlobalDetail>();
    }

    @Override
    public List<PersistableBusinessObject> generateGlobalChangesToPersist() {
        List<PersistableBusinessObject> objectsToSave = new ArrayList<PersistableBusinessObject>();
        for (CuObjectCodeGlobalDetail detail : cuObjectCodeGlobalDetails) {
            ObjectCode objectCode = objectCodeService.getByPrimaryId(detail.getUniversityFiscalYear(), detail.getChartOfAccountsCode(), detail.getFinancialObjectCode());
            if (ObjectUtils.isNotNull(objectCode)) {
                objectCode.setActive(activate.booleanValue());
            } else {
                LOG.error("generateGlobalChangesToPersist, unable to find object code for fiscal year " + detail.getUniversityFiscalYear() + " chart code: " 
                        + detail.getChartOfAccountsCode() + " financial object code: " + detail.getFinancialObjectCode() );
            }
        }
        return objectsToSave;
    }

    @Override
    public List<PersistableBusinessObject> generateDeactivationsToPersist() {
        List<PersistableBusinessObject>  objectsToDeactivate = new ArrayList<PersistableBusinessObject>();
        return objectsToDeactivate;
    }

    @Override
    public boolean isPersistable() {
        if (StringUtils.isBlank(documentNumber)) {
            LOG.error("isPersistable, no document number");
            return false;
        }
        
        for (CuObjectCodeGlobalDetail detail : cuObjectCodeGlobalDetails) {
            if (!getPersistenceStructureService().hasPrimaryKeyFieldValues(detail)) {
                LOG.error("isPersistable, detail doesn't have primvary keys set: " + detail.toString());
                return false;
            }
        }
        
        return true;
    }

    @Override
    public List<? extends GlobalBusinessObjectDetail> getAllDetailObjects() {
        return cuObjectCodeGlobalDetails;
    }
    
    @Override
    public String getDocumentNumber() {
        return documentNumber;
    }

    @Override
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Boolean getActivate() {
        return activate;
    }

    public void setActivate(Boolean activate) {
        this.activate = activate;
    }

    public List<CuObjectCodeGlobalDetail> getCuObjectCodeGlobalDetails() {
        return cuObjectCodeGlobalDetails;
    }

    public void setCuObjectCodeGlobalDetails(List<CuObjectCodeGlobalDetail> cuObjectCodeGlobalDetails) {
        this.cuObjectCodeGlobalDetails = cuObjectCodeGlobalDetails;
    }

    public ObjectCodeService getObjectCodeService() {
        if (objectCodeService == null) {
            objectCodeService = SpringContext.getBean(ObjectCodeService.class);
        }
        return objectCodeService;
    }

    public void setObjectCodeService(ObjectCodeService objectCodeService) {
        this.objectCodeService = objectCodeService;
    }

}
