package edu.cornell.kfs.module.ar.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants.ContractsAndGrantsInvoiceDocumentCreationProcessType;
import org.kuali.kfs.module.ar.businessobject.CustomerAddress;
import org.kuali.kfs.module.ar.document.service.CustomerAddressService;
import org.kuali.kfs.module.cg.businessobject.Award;

import edu.cornell.kfs.module.ar.service.CuCustomerAddressHelperService;

public class CuCustomerAddressHelperServiceImpl implements CuCustomerAddressHelperService {
    
    private static final Logger LOG = LogManager.getLogger();
    
    private CustomerAddressService customerAddressService;
    
    /*
     * CU Customization: KFSPTS-26393
     * When creationProcessType is BATCH, validate that the customer number on the award as part if the customer
     * address composite key matches the customer number associated to the agency associated to the award.
     */
    public boolean agencyCustomerMatchesAwardCustomer(Award award,
            ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType) {
        
        if (ContractsAndGrantsInvoiceDocumentCreationProcessType.BATCH == creationProcessType) {
            if (StringUtils.isNotBlank(obtainCustomerNumberFromAwardAgencyCustomer(award)) &&
                    StringUtils.isNotBlank(award.getCustomerNumber()) &&
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
     * When creationProcessType is BATCH, validate that a customer address exists in the same manner that it is
     * obtained by downstream processing in ContractsGrantsInvoiceCreateDocumentServiceImpl.buildInvoiceAddressDetails.
     */
    public boolean invoicingCustomerAddressExists(Award award,
            ContractsAndGrantsInvoiceDocumentCreationProcessType creationProcessType) {
        
        CustomerAddress customerAddress = null;
        
        if (ContractsAndGrantsInvoiceDocumentCreationProcessType.BATCH == creationProcessType) {
            if (ObjectUtils.isNotNull(award.getCustomerAddressIdentifier()) &&
                    StringUtils.isNotBlank(award.getCustomerNumber())) {
                customerAddress = customerAddressService.getByPrimaryKey(award.getCustomerNumber(),
                        award.getCustomerAddressIdentifier());
            }
            if (ObjectUtils.isNull(customerAddress) && 
                    StringUtils.isNotBlank(award.getCustomerNumber())) {
                customerAddress = customerAddressService.getPrimaryAddress(award.getCustomerNumber());
            }
            if (ObjectUtils.isNotNull(customerAddress)) {
                return true;
            } else {
                LOG.info("invoicingCustomerAddressExists: Could not obtain valid customer address by neither "
                        + "customer address composite key nor customer primary address for" +
                         "Award.CustomerNumber and Award.CustomerAddressIdentifier = " +
                         " Award.CustomerNumber = " + award.getCustomerNumber() +
                         " Award.CustomerAddressIdentifier = " +
                         (ObjectUtils.isNull(award.getCustomerAddressIdentifier()) ? "null" : award.getCustomerAddressIdentifier().intValue()));
                 return false;
            } 
        } else {
            LOG.info("invoicingCustomerAddressExists: Validation bypassed due to creationProcessType being " + creationProcessType.getName());
            return true;
        }
    }

    /*
     * CU Customization: KFSPTS-26393
     */
    private String obtainCustomerNumberFromAwardAgencyCustomer(Award award) {
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

    public CustomerAddressService getCustomerAddressService() {
        return customerAddressService;
    }

    public void setCustomerAddressService(CustomerAddressService customerAddressService) {
        this.customerAddressService = customerAddressService;
    }

}
