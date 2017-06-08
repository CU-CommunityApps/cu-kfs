package edu.cornell.kfs.concur.batch.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.service.PayeeACHService;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.batch.service.ConcurEmployeeInfoValidationService;

public class ConcurEmployeeInfoValidationServiceImpl implements ConcurEmployeeInfoValidationService {
    private static final Logger LOG = Logger.getLogger(ConcurEmployeeInfoValidationServiceImpl.class);
    
    protected PersonService personService;
    protected PayeeACHService payeeACHService;
    protected ConfigurationService configurationService;

    @Override
    public boolean validPerson(String employeeId) {
        Person employee = findPerson(employeeId);
        return validPerson(employee);
    }

    private boolean validPerson(Person employee) {
        boolean valid = ObjectUtils.isNotNull(employee);
        if (LOG.isDebugEnabled()) {
            if (valid) {
                LOG.debug("validPerson, the employee id " + employee.getEmployeeId() + " was built into the person: " + employee.getName());
            }
        }
        return valid;
    }
    
    private Person findPerson(String employeeId) {
        if (StringUtils.isNotBlank(employeeId)) {
            try {
                Person employee = getPersonService().getPersonByEmployeeId(employeeId);
                return employee;
            } catch (Exception e) {
                LOG.error("findPerson, Unable to retrieve a person from employee ID: " + employeeId, e);
            }
        } else {
            LOG.error("findPerson, No employee ID was provided");
        }
        return null;
    }

    @Override
    public boolean validPdpAddress(String employeeId) {
        Person employee = findPerson(employeeId);
        boolean validPerson = validPerson(employee);
        boolean validAddress = validPerson;
        if (validPerson) {
            String state = employee.getAddressStateProvinceCodeUnmasked();
            String country = employee.getAddressCountryCodeUnmasked();
            if (StringUtils.isBlank(state) && StringUtils.isBlank(country)) {
                validAddress = false;
                LOG.error("validPdpAddress, " + employee.getName() + " does not have a Country Code or a State/Province code.");
            } else {
                if (LOG.isDebugEnabled()) {
                    StringBuilder sb = new StringBuilder("validPdpAddress, The employee ");
                    sb.append(employee.getName()).append(" has a valid PDP address.  The state code was '").append(state);
                    sb.append("' and the country code was '").append(country).append("'.");
                    LOG.debug(sb.toString());
                }
            }
        }
        return validAddress;
    }
    
    @Override
    public boolean isPayeeSignedUpForACH(String employeeId) {
        boolean isACH = getPayeeACHService().isPayeeSignedUpForACH(ConcurConstants.EMPLOYEE_PAYEE_STATUS_TYPE_CODE, employeeId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("isPayeeSignedUpForACH, is employee ID " + employeeId + " signed up for ACH: " + isACH );
        }
        return isACH;
    }
    
    @Override
    public String getAddressValidationMessageIfCheckPayment(String employeeId) {
        String validationMessage = StringUtils.EMPTY;
        if (isPayeeSignedUpForACH(employeeId)) {
            LOG.info("validateAddressIfCheckPayment, the employee ID " + employeeId + " is signed up for ACH so no need to validdate address.");
        } else {
            if (!validPdpAddress(employeeId)) {
                LOG.info("validateAddressIfCheckPayment, the employee ID " + employeeId + " has an INVALID address.");
                validationMessage = getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_INCOMPLETE_ADDRESS);
            } else {
                LOG.info("validateAddressIfCheckPayment, the employee ID " + employeeId + " has an valid address.");
            }
        }
        return validationMessage;
    }

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public PayeeACHService getPayeeACHService() {
        return payeeACHService;
    }

    public void setPayeeACHService(PayeeACHService payeeACHService) {
        this.payeeACHService = payeeACHService;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
