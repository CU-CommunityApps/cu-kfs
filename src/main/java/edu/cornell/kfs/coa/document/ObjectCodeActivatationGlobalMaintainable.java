package edu.cornell.kfs.coa.document;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.document.FinancialSystemGlobalMaintainable;

import edu.cornell.kfs.coa.businessobject.CuObjectCodeActivationGlobal;
import edu.cornell.kfs.coa.businessobject.CuObjectCodeGlobalDetail;

public class ObjectCodeActivatationGlobalMaintainable extends FinancialSystemGlobalMaintainable {

    @Override
    public List<MaintenanceLock> generateMaintenanceLocks() {
        CuObjectCodeActivationGlobal objectCodeGlobal = (CuObjectCodeActivationGlobal) getBusinessObject();
        List<MaintenanceLock> maintenanceLocks = new ArrayList<MaintenanceLock>();
        
        for (CuObjectCodeGlobalDetail detail : objectCodeGlobal.getCuObjectCodeGlobalDetails()) {
            MaintenanceLock maintenanceLock = new MaintenanceLock();
            StringBuffer lockrep = new StringBuffer();

            lockrep.append(Account.class.getName() + KFSConstants.Maintenance.AFTER_CLASS_DELIM);
            lockrep.append(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE + KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
            lockrep.append(detail.getChartOfAccountsCode() + KFSConstants.Maintenance.AFTER_VALUE_DELIM);
            lockrep.append(KFSPropertyConstants.FISCAL_YEAR + KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
            lockrep.append(detail.getUniversityFiscalYear());
            lockrep.append(KFSPropertyConstants.OBJECT_CODE + KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
            lockrep.append(detail.getFinancialObjectCode());

            maintenanceLock.setDocumentNumber(objectCodeGlobal.getDocumentNumber());
            maintenanceLock.setLockingRepresentation(lockrep.toString());
            maintenanceLocks.add(maintenanceLock);
        }
        
        return maintenanceLocks;
    }

    @Override
    public Class<? extends PersistableBusinessObject> getPrimaryEditedBusinessObjectClass() {
        return ObjectCode.class;
    }

}
