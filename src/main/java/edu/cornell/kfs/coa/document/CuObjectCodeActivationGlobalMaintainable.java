package edu.cornell.kfs.coa.document;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.ObjectCodeGlobal;
import org.kuali.kfs.coa.businessobject.ObjectCodeGlobalDetail;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.coa.service.SubObjectTrickleDownInactivationService;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.FinancialSystemGlobalMaintainable;

import edu.cornell.kfs.coa.businessobject.CuObjectCodeActivationGlobal;
import edu.cornell.kfs.coa.businessobject.CuObjectCodeGlobalDetail;

public class CuObjectCodeActivationGlobalMaintainable extends FinancialSystemGlobalMaintainable {
    private static final long serialVersionUID = 7651991084589364963L;
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuObjectCodeActivationGlobalMaintainable.class);
    
    protected transient SubObjectTrickleDownInactivationService subObjectTrickleDownInactivationService;
    protected transient ObjectCodeService objectCodeService;
    
    @Override
    public List<MaintenanceLock> generateMaintenanceLocks() {
        LOG.info("generateMaintenanceLocks, entering");
        CuObjectCodeActivationGlobal objectCodeGlobal = (CuObjectCodeActivationGlobal) getBusinessObject();
        List<MaintenanceLock> maintenanceLocks = new ArrayList();

        for (CuObjectCodeGlobalDetail detail : objectCodeGlobal.getObjectCodeGlobalDetails()) {
            MaintenanceLock maintenanceLock = new MaintenanceLock();
            StringBuffer lockrep = new StringBuffer();

            lockrep.append(ObjectCode.class.getName() + KFSConstants.Maintenance.AFTER_CLASS_DELIM);
            lockrep.append(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR + KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
            lockrep.append(detail.getUniversityFiscalYear() + KFSConstants.Maintenance.AFTER_VALUE_DELIM);
            lockrep.append(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE + KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
            lockrep.append(detail.getChartOfAccountsCode() + KFSConstants.Maintenance.AFTER_VALUE_DELIM);
            lockrep.append(KFSPropertyConstants.FINANCIAL_OBJECT_CODE + KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
            lockrep.append(detail.getFinancialObjectCode());

            maintenanceLock.setDocumentNumber(objectCodeGlobal.getDocumentNumber());
            maintenanceLock.setLockingRepresentation(lockrep.toString());
            maintenanceLocks.add(maintenanceLock);

            ObjectCode objectCode = new ObjectCode();
            objectCode.setUniversityFiscalYear(detail.getUniversityFiscalYear());
            objectCode.setChartOfAccountsCode(detail.getChartOfAccountsCode());
            objectCode.setFinancialObjectCode(detail.getFinancialObjectCode());
            objectCode.setActive(objectCodeGlobal.getActivate());

            if (isInactivatingObjectCode(objectCode)) {
                maintenanceLocks.addAll(getSubObjectTrickleDownInactivationService().generateTrickleDownMaintenanceLocks(objectCode, getDocumentNumber()));
            }
        }
        return maintenanceLocks;
    }
    
    protected boolean isInactivatingObjectCode(ObjectCode objectCode) {
        if (!objectCode.isActive()) {
            ObjectCode objectCodeFromDB = getObjectCodeService().getByPrimaryId(objectCode.getUniversityFiscalYear(), objectCode.getChartOfAccountsCode(), objectCode.getFinancialObjectCode());
            if (objectCodeFromDB != null && objectCodeFromDB.isActive()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Class<? extends PersistableBusinessObject> getPrimaryEditedBusinessObjectClass() {
        return ObjectCode.class;
    }

    public SubObjectTrickleDownInactivationService getSubObjectTrickleDownInactivationService() {
        if (subObjectTrickleDownInactivationService == null) {
            subObjectTrickleDownInactivationService = SpringContext.getBean(SubObjectTrickleDownInactivationService.class);
        }
        return subObjectTrickleDownInactivationService;
    }

    public void setSubObjectTrickleDownInactivationService(
            SubObjectTrickleDownInactivationService subObjectTrickleDownInactivationService) {
        this.subObjectTrickleDownInactivationService = subObjectTrickleDownInactivationService;
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
