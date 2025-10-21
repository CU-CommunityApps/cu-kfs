package edu.cornell.kfs.kns.web.struts.form;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.kns.web.struts.form.InquiryForm;
import org.kuali.kfs.krad.datadictionary.exception.UnknownBusinessClassAttributeException;

public class CuInquiryForm extends InquiryForm {
    private static final Logger LOG = LogManager.getLogger();
    
    @Override
    protected void populatePKFieldValues(HttpServletRequest request, String boClassName,
            boolean passedFromPreviousInquiry) {
        super.populatePKFieldValues(request, boClassName, passedFromPreviousInquiry);
        
        try {
            Class<?> businessObjectClass = Class.forName(boClassName);
            
            if (Person.class.isAssignableFrom(businessObjectClass)) {
                processPrincipalNameParameter(request, businessObjectClass);
            }
        } catch (ClassNotFoundException e) {
            LOG.warn("BO class {} not found.", boClassName, e);
        }
    }
    
    private void processPrincipalNameParameter(HttpServletRequest request, Class<?> businessObjectClass) {
        String parameter = request.getParameter(KIMPropertyConstants.Principal.PRINCIPAL_NAME);
        
        if (parameter == null) {
            return;
        }
        
        DataDictionaryService dataDictionaryService = KNSServiceLocator.getDataDictionaryService();
        Boolean forceUppercase = Boolean.FALSE;
        
        try {
            forceUppercase = dataDictionaryService.getAttributeForceUppercase(
                    businessObjectClass, KIMPropertyConstants.Principal.PRINCIPAL_NAME);
        } catch (UnknownBusinessClassAttributeException ex) {
            LOG.warn("BO class {} property {} should probably have a DD definition.", 
                    businessObjectClass.getName(), KIMPropertyConstants.Principal.PRINCIPAL_NAME, ex);
        }
        
        if (Boolean.TRUE.equals(forceUppercase)) {
            parameter = parameter.toUpperCase(Locale.US);
        }
        
        getInquiryPrimaryKeys().put(KIMPropertyConstants.Principal.PRINCIPAL_NAME, parameter);
        retrieveInquiryDecryptedPrimaryKeys().put(KIMPropertyConstants.Principal.PRINCIPAL_NAME, parameter);
    }
}
