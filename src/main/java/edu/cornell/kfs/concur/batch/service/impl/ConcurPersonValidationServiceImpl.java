package edu.cornell.kfs.concur.batch.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.service.PayeeACHService;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;

import edu.cornell.kfs.concur.batch.businessobject.AddressValidationResults;
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
    public AddressValidationResults validPdpAddress(String employeeId) {
        List<String> messageList = new ArrayList<String>();
        Person employee = findPerson(employeeId);
        boolean validPerson = ObjectUtils.isNotNull(employee);
        boolean validAddress = validPerson;
        
        if (validPerson) {
            validAddress = doesAnyAddressLineHaveContent(messageList, employee) && validAddress;
            validAddress = validateAddressFieldNotEmpty(employee.getAddressCityUnmasked(), "City", messageList) && validAddress;
            validAddress = validateAddressFieldNotEmpty(employee.getAddressStateProvinceCodeUnmasked(), "State/Province", messageList) && validAddress;
            validAddress = validateAddressFieldNotEmpty(employee.getAddressPostalCodeUnmasked(), "Postal Code", messageList) && validAddress;
            validAddress = validateAddressFieldNotEmpty(employee.getAddressCountryCodeUnmasked(), "Country Code", messageList) && validAddress;
            
        } else {
            messageList.add(getCouldNotBuildPersonMessage(employeeId));
        }
        
        AddressValidationResults results = new AddressValidationResults(employee, validAddress, messageList);
        if (LOG.isDebugEnabled()) {
            LOG.debug("validPdpAddress, " + results.toString());
        }
        return results;
    }

    private boolean doesAnyAddressLineHaveContent(List<String> messageList, Person employee) {
        List<String> addressMessageList = new ArrayList<String>();
        boolean anyLineValid = validateAddressFieldNotEmpty(employee.getAddressLine1Unmasked(), StringUtils.EMPTY, addressMessageList) ||
                validateAddressFieldNotEmpty(employee.getAddressLine2Unmasked(), StringUtils.EMPTY, addressMessageList) ||
                validateAddressFieldNotEmpty(employee.getAddressLine3Unmasked(), StringUtils.EMPTY, addressMessageList);
        if (!anyLineValid) {
            messageList.add("Address is empty. ");
        }
        return anyLineValid;
    }
    
    private boolean validateAddressFieldNotEmpty(String addressField, String addressFieldDescription, List<String> messageList) {
        boolean valid;
        if (StringUtils.isEmpty(addressField)) {
            valid = false;
            String message = addressFieldDescription + " is empty. ";
            messageList.add(message);
        } else {
            valid = true;
        }
        return valid;
    }
    
    @Override
    public boolean isPayeeSignedUpForACH(String employeeId) {
        boolean isACH = getPayeeACHService().isPayeeSignedUpForACH("E", employeeId);
        LOG.info("isPayeeSignedUpForACH, is employee ID " + employeeId + " signed up for ACH: " + isACH );
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
