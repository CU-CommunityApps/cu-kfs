package edu.cornell.kfs.pdp.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.service.impl.AchServiceImpl;

import edu.cornell.kfs.pdp.service.CuAchService;

public class CuAchServiceImpl extends AchServiceImpl implements CuAchService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuAchServiceImpl.class);

    private BusinessObjectService businessObjectService;

    // This implementation is nearly identical to that from the AchServiceImpl.getAchInformation method.
    @Override
    public PayeeACHAccount getPotentiallyInactiveAchInformation(String idType, String payeeId, String achTransactionType) {
        LOG.debug("getAchInformation() started");

        Map<String, Object> fields = new HashMap<String, Object>();

        fields.put(PdpPropertyConstants.PAYEE_IDENTIFIER_TYPE_CODE, idType);
        fields.put(PdpPropertyConstants.ACH_TRANSACTION_TYPE, achTransactionType);
        fields.put(PdpPropertyConstants.PAYEE_ID_NUMBER, payeeId);

        Collection<PayeeACHAccount> rows = businessObjectService.findMatching(PayeeACHAccount.class, fields);
        if (rows.size() != 1) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getAchInformation() not found rows = " + rows.size());
            }

            return null;
        } else {
            LOG.debug("getAchInformation() found");

            return rows.iterator().next();
        }
    }

    @Override
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        super.setBusinessObjectService(businessObjectService);
        this.businessObjectService = businessObjectService;
    }

}
