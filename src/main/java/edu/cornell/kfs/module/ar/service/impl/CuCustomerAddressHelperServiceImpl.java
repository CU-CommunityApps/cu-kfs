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
    public boolean agencyCustomerMatchesAwardCustomer(ContractsAndGrantsBillingAward award,
            ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType) {
        
        if (ContractsAndGrantsInvoiceDocumentCreationProcessType.BATCH == creationProcessType) {
            if (StringUtils.isNotBlank(obtainCustomerNumberFromAwardAgencyCustomer(award)) &&
                    StringUtils.isNotBlank(award.getCustomerNumber()) &&
                    ObjectUtils.isNotNull(award.getCustomerAddressIdentifier()) &&
                    obtainCustomerNumberFromAwardAgencyCustomer(award).equalsIgnoreCase(award.getCustomerNumber())) {
                return true;
            } else {
                LOG.info("agencyCustomerMatchesAwardCustomer: Mismatched Customer records detected for " +
                         "Award.Agency and Award.CustomerAddress : Award.Agency.CustomerNumber = " +
                         (StringUtils.isBlank(obtainCustomerNumberFromAwardAgencyCustomer(award)) ? null : obtainCustomerNumberFromAwardAgencyCustomer(award)) +
                         " Award.CustomerNumber = " + award.getCustomerNumber() +
                         " Award.CustomerAddressIdentifier = " +
                         (ObjectUtils.isNull(award.getCustomerAddressIdentifier()) ? "null" : award.getCustomerAddressIdentifier().intValue()));
                return false;
            } 
        } else {
            LOG.info("agencyCustomerMatchesAwardCustomer: Validation bypassed due to creationProcessType being " + creationProcessType.getName());
            return true;
        }
    }
    
    /*
     * CU Customization: KFSPTS-26393
     * When creationProcessType is BATCH, validate that the customer address identifier on the award exists.
     */
    public boolean customerAddressIdentifierExists(ContractsAndGrantsBillingAward award,
            ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType) {
        
        if (ContractsAndGrantsInvoiceDocumentCreationProcessType.BATCH == creationProcessType) {
            if (ObjectUtils.isNotNull(award.getCustomerAddressIdentifier())) {
                return true;
            } else {
                LOG.info("customerAddressIdentifierExists: Award.CustomerAddressIdentifier is null. ");
                return false;
            } 
        } else {
            LOG.info("customerAddressIdentifierExists: Validation bypassed due to creationProcessType being " + creationProcessType.getName());
            return true;
        }
    }

    /*
     * CU Customization: KFSPTS-26393
     */
    private String obtainCustomerNumberFromAwardAgencyCustomer(ContractsAndGrantsBillingAward award) {
        if (ObjectUtils.isNull(award)) {
           return null;
        }
        if (ObjectUtils.isNull(award.getAgency())) {
           return null;
        }
        if (ObjectUtils.isNull(award.getAgency().getCustomer())) {
           return null;
        }
        return award.getAgency().getCustomer().getCustomerNumber();
    }

}
