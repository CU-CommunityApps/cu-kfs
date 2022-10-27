package edu.cornell.kfs.module.ar.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.integration.cg.ContractsAndGrantsBillingAward;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants.ContractsAndGrantsInvoiceDocumentCreationProcessType;

import edu.cornell.kfs.module.ar.service.CuCustomerAddressHelperService;

public class CuCustomerAddressHelperServiceImpl implements CuCustomerAddressHelperService {
    
    private static final Logger LOG = LogManager.getLogger();
    
    /*
     * CU Customization: KFSPTS-26393
     * When creationProcessType is BATCH, validate that the customer number on the award as part if the customer
     * address composite key matches the customer number associated to the agency associated to the award.
     */
    public boolean awardCustomerMatchesAddressCustomer(ContractsAndGrantsBillingAward award,
            ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType) {
        
        if (ContractsAndGrantsInvoiceDocumentCreationProcessType.BATCH == creationProcessType) {
            if (ObjectUtils.isNotNull(award.getAgency().getCustomer().getCustomerNumber()) &&
                    ObjectUtils.isNotNull(award.getCustomerNumber()) &&
                    ObjectUtils.isNotNull(award.getCustomerAddressIdentifier()) &&
                    StringUtils.isNotBlank(award.getAgency().getCustomer().getCustomerNumber()) &&
                    StringUtils.isNotBlank(award.getCustomerNumber()) &&
                    award.getAgency().getCustomer().getCustomerNumber().equalsIgnoreCase(award.getCustomerNumber())) {
                LOG.info("awardCustomerMatchesAddressCustomer: ");
                return true;
            } else {
                LOG.info("awardCustomerMatchesAddressCustomer: Mismatched Customer records detected for " +
                         "Award.Agency and Award.CustomerAddress : Award.Agency.CustomerNumber = " +
                            (ObjectUtils.isNull(award.getAgency()) ? "null" : award.getAgency().getCustomer()) +
                        " Award.CustomerNumber = " + award.getCustomerNumber() +
                        " Award.CustomerAddressId = " +
                         (ObjectUtils.isNull(award.getCustomerAddressIdentifier()) ? "null" : award.getCustomerAddressIdentifier().intValue()));
                return false;
            } 
        } else {
            LOG.info("awardCustomerMatchesAddressCustomer: Validation bypassed due to creationProcessType being " + creationProcessType.getName());
            return true;
        }
    }

}
