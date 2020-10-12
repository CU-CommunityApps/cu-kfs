package edu.cornell.kfs.coa.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.krad.bo.GlobalBusinessObject;
import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetail;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.context.SpringContext;

public class CuObjectCodeActivationGlobal extends PersistableBusinessObjectBase implements GlobalBusinessObject {
    private static final long serialVersionUID = -3772910396694995779L;
    private static final Logger LOG = LogManager.getLogger(CuObjectCodeActivationGlobal.class);
    
    private transient ObjectCodeService objectCodeService;
    
    protected String documentNumber;
    private Boolean activate;
    private List<CuObjectCodeGlobalDetail> objectCodeGlobalDetails;
    
    public CuObjectCodeActivationGlobal() {
        super();
        objectCodeGlobalDetails = new ArrayList<CuObjectCodeGlobalDetail>();
    }

    @Override
    public List<PersistableBusinessObject> generateGlobalChangesToPersist() {
        List<PersistableBusinessObject> objectsToSave = new ArrayList<PersistableBusinessObject>();
        for (CuObjectCodeGlobalDetail detail : objectCodeGlobalDetails) {
            ObjectCode objectCode = getObjectCodeService().getByPrimaryId(detail.getUniversityFiscalYear(), detail.getChartOfAccountsCode(), detail.getFinancialObjectCode());
            if (ObjectUtils.isNotNull(objectCode)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("generateGlobalChangesToPersist, setting active flag to " + activate.booleanValue() + " for object code " + objectCode);
                }
                objectCode.setActive(activate.booleanValue());
                objectsToSave.add(objectCode);
                
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
        
        for (CuObjectCodeGlobalDetail detail : objectCodeGlobalDetails) {
            if (!getPersistenceStructureService().hasPrimaryKeyFieldValues(detail)) {
                LOG.error("isPersistable, detail doesn't have primary keys set: " + detail.toString());
                return false;
            }
        }
        
        return true;
    }

    @Override
    public List<? extends GlobalBusinessObjectDetail> getAllDetailObjects() {
        return objectCodeGlobalDetails;
    }
    
    public void copyDetailsFromOtherCuObjectCodeActivationGlobal(CuObjectCodeActivationGlobal oldGlobal) {
        for (CuObjectCodeGlobalDetail oldDetail : oldGlobal.getObjectCodeGlobalDetails()) {
            CuObjectCodeGlobalDetail newDetail = (CuObjectCodeGlobalDetail) ObjectUtils.deepCopy(oldDetail);
            newDetail.setObjectId(null);
            newDetail.setDocumentNumber(getDocumentNumber());
            newDetail.setVersionNumber(new Long(1));
            getObjectCodeGlobalDetails().add(newDetail);
        }
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

    public List<CuObjectCodeGlobalDetail> getObjectCodeGlobalDetails() {
        return objectCodeGlobalDetails;
    }

    public void setObjectCodeGlobalDetails(List<CuObjectCodeGlobalDetail> objectCodeGlobalDetails) {
        this.objectCodeGlobalDetails = objectCodeGlobalDetails;
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
