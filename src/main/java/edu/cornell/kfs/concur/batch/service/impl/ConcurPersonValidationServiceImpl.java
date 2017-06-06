package edu.cornell.kfs.concur.batch.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.service.PayeeACHService;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;

import edu.cornell.kfs.concur.batch.service.ConcurPersonValidationService;

public class ConcurPersonValidationServiceImpl implements ConcurPersonValidationService {
    private static final Logger LOG = Logger.getLogger(ConcurPersonValidationServiceImpl.class);
    
    protected PersonService personService;
    protected PayeeACHService payeeACHService;

    @Override
    public boolean validPerson(String employeeId) {
        Person employee = findPerson(employeeId);
        boolean valid = ObjectUtils.isNotNull(employee);
        if (LOG.isDebugEnabled()) {
            if (valid) {
                LOG.debug("validPerson, the employee id " + employeeId + " was built into the person: " + employee.getName());
            } else {
                LOG.debug("validPerson, " + getCouldNotBuildPersonMessage(employeeId));
            }
        }
        return valid;
    }
    
    private String getCouldNotBuildPersonMessage(String employeeId) {
        return "The employee ID " + employeeId + " could not be built into a person";
    }
    
    private Person findPerson(String employeeId) {
        if (StringUtils.isNotBlank(employeeId)) {
            try {
                Person employee = getPersonService().getPersonByEmployeeId(employeeId);
                return employee;
            } catch (Exception e) {
                LOG.error("findPerson, Unable to create a person from employee ID: " + employeeId, e);
            }
        }
        return null;
    }

    @Override
    public boolean validPdpAddress(String employeeId) {
        Person employee = findPerson(employeeId);
        boolean validPerson = ObjectUtils.isNotNull(employee);
        boolean validAddress = validPerson;
        if (validPerson) {
            String state = employee.getAddressStateProvinceCodeUnmasked();
            String country = employee.getAddressCountryCodeUnmasked();
            if (StringUtils.isBlank(state) && StringUtils.isBlank(country)) {
                validAddress = false;
                LOG.error("validPdpAddress, " + employee.getName() + " does not have a Country Code or a State/Province code.");
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("validPdpAddress, The employee " + employee.getName() + " had a country code or state code." );
                }
            }
        } else {
            LOG.error("validPdpAddress " + getCouldNotBuildPersonMessage(employeeId));
        }
        return validAddress;
    }
    
    @Override
    public boolean isPayeeSignedUpForACH(String employeeId) {
        boolean isACH = getPayeeACHService().isPayeeSignedUpForACH("E", employeeId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("isPayeeSignedUpForACH, is employee ID " + employeeId + " signed up for ACH: " + isACH );
        }
        return isACH;
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

}
