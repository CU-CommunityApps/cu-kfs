package edu.cornell.kfs.concur.batch.service.impl;

import org.apache.commons.collections4.CollectionUtils;

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
        
        fieldValues.put(ConcurPropertyConstants.ConcurRequestedCashAdvance.EMPLID, concurRequestedCashAdvance.getEmployeeId());
        fieldValues.put(ConcurPropertyConstants.ConcurRequestedCashAdvance.CASH_ADV_KEY, concurRequestedCashAdvance.getCashAdvanceKey());
        fieldValues.put(ConcurPropertyConstants.ConcurRequestedCashAdvance.PAYMENT_AMOUNT, concurRequestedCashAdvance.getPaymentAmount().toString());

        concurRequestedCashAdvances = businessObjectService.findMatching(ConcurRequestedCashAdvance.class, fieldValues);

        boolean isDuplicate = CollectionUtils.isNotEmpty(concurRequestedCashAdvances);

        return isDuplicate;

    }
    
    @Override
    public Collection<ConcurRequestedCashAdvance> findConcurRequestedCashAdvanceByCashAdvanceKey(String cashAdvanceKey) {
        Collection<ConcurRequestedCashAdvance> concurRequestedCashAdvances = null;
        Map<String, String> fieldValues = new HashMap<String, String>();

        fieldValues.put(ConcurPropertyConstants.ConcurRequestedCashAdvance.CASH_ADV_KEY, cashAdvanceKey);

        concurRequestedCashAdvances = businessObjectService.findMatching(ConcurRequestedCashAdvance.class, fieldValues);

        return concurRequestedCashAdvances;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

}
