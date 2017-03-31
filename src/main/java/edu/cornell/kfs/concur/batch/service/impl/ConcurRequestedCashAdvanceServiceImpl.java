package edu.cornell.kfs.concur.batch.service.impl;

import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurPropertyConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestedCashAdvance;
import edu.cornell.kfs.concur.batch.service.ConcurRequestedCashAdvanceService;

public class ConcurRequestedCashAdvanceServiceImpl implements ConcurRequestedCashAdvanceService {
    protected BusinessObjectService businessObjectService;

    @Override
    public void saveConcurRequestedCashAdvance(ConcurRequestedCashAdvance concurRequestedCashAdvance) {
        businessObjectService.save(concurRequestedCashAdvance);
    }

    @Override
    public boolean isDuplicateConcurRequestCashAdvance(ConcurRequestedCashAdvance concurRequestedCashAdvance) {
        Collection<ConcurRequestedCashAdvance> concurRequestedCashAdvances = null;
        Map<String, String> fieldValues = new HashMap<String, String>();
        
        fieldValues.put(ConcurPropertyConstants.ConcurRequestedCashAdvance.EMPLOYEE_ID, concurRequestedCashAdvance.getEmployeeId());
        fieldValues.put(ConcurPropertyConstants.ConcurRequestedCashAdvance.REQUEST_ID, concurRequestedCashAdvance.getRequestId());
        fieldValues.put(ConcurPropertyConstants.ConcurRequestedCashAdvance.PAYMENT_AMOUNT, concurRequestedCashAdvance.getPaymentAmount().toString());
        
        concurRequestedCashAdvances = businessObjectService.findMatching(ConcurRequestedCashAdvance.class, fieldValues);
        
        boolean isDuplicate = (CollectionUtils.isEmpty(concurRequestedCashAdvances)) ? false : true;

        return isDuplicate;

    }
    
    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
