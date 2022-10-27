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
     * When creationProcessType is BATCH, validate whether a customer address exists for the customer
     * number and address id associated to the award, presenting an error when that composite key
     * does not find a corresponding customer address. Prior to this customization, the batch job would
     * stack trace in method ContractsGrantsInvoiceCreateDocumentServiceImpl.buildInvoiceAddressDetails
     * when the attempting to retrive the customer address due to one of those primary composite
     * key values not being valid.
     */
    public boolean hasValidCustomerAddress(ContractsAndGrantsBillingAward award,
            ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType) {
        
        if (ContractsAndGrantsInvoiceDocumentCreationProcessType.BATCH == creationProcessType) {
            if (StringUtils.isNotBlank(obtainAgencyCustomerNumberFromAward(award)) &&
                    StringUtils.isNotBlank(award.getCustomerNumber()) && 
                    ObjectUtils.isNotNull(award.getCustomerAddressIdentifier()) &&
                    obtainAgencyCustomerNumberFromAward(award).equalsIgnoreCase(award.getCustomerNumber())) {
                return true;
            } else {
                LOG.info("hasValidCustomerAddress: Invalid customer address detected for award : " +
                        "Award.Agency.CustomerNumber = " + obtainAgencyCustomerNumberFromAward(award) +
                        " Award.CustomerNumber = " + award.getCustomerNumber() + " Award.CustomerAddressId = " +
                         (ObjectUtils.isNull(award.getCustomerAddressIdentifier()) ? "null" : award.getCustomerAddressIdentifier().intValue()));
                return false;
            } 
        } else {
            LOG.info("hasValidCustomerAddress: Customer address validation bypassed due to creationProcessType being " + creationProcessType.getName());
            return true;
        }
    }
    
    /*
     * CU Customization: KFSPTS-26393
     */
    private String obtainAgencyCustomerNumberFromAward(ContractsAndGrantsBillingAward award) {
         if (ObjectUtils.isNull(award.getAgency())) {
             return null;
         }
         if (ObjectUtils.isNull(award.getAgency().getCustomer())) {
             return null;
         }
         return award.getAgency().getCustomer().getCustomerNumber();
    }
    
}
