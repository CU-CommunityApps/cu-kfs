package edu.cornell.kfs.pmw.batch.service.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksKeyConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksParameterConstants;
import edu.cornell.kfs.pmw.batch.businessobject.KfsVendorDataWrapper;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksVendorToKfsVendorDetailConversionService;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;

public class PaymentWorksBatchUtilityServiceImpl implements PaymentWorksBatchUtilityService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksBatchUtilityServiceImpl.class);

    protected ParameterService parameterService;
    protected PersonService personService;
    
    private Person systemUser = null;

    @Override
    public String retrievePaymentWorksParameterValue(String parameterName) {
        String parameterValue = getParameterService().getParameterValueAsString(PaymentWorksConstants.PAYMENTWORKS_NAMESPACE_CODE, CUKFSParameterKeyConstants.ALL_COMPONENTS, parameterName);
        return parameterValue;
    }
    
    @Override
    public String getFileContents(String fileName) {
        try {
            byte[] fileByteArray = safelyLoadFileBytes(fileName);
            String formattedString = new String(fileByteArray);
            return formattedString;
        } catch (RuntimeException e) {
            LOG.error("getFileContents: unable to read the file.", e);
            return StringUtils.EMPTY;
        }
    }

    protected byte[] safelyLoadFileBytes(String fullyQualifiedFileName) {
        InputStream fileContents;
        byte[] fileByteContent;
        try {
            fileContents = new FileInputStream(fullyQualifiedFileName);
        } catch (FileNotFoundException e1) {
            LOG.error("safelyLoadFileBytes:  Batch file not found [" + fullyQualifiedFileName + "]. " + e1.getMessage());
            throw new RuntimeException("Batch File not found [" + fullyQualifiedFileName + "]. " + e1.getMessage());
        }
        try {
            fileByteContent = IOUtils.toByteArray(fileContents);
        } catch (IOException e1) {
            LOG.error("safelyLoadFileBytes:  IO Exception loading: [" + fullyQualifiedFileName + "]. " + e1.getMessage());
            throw new RuntimeException("IO Exception loading: [" + fullyQualifiedFileName + "]. " + e1.getMessage());
        } finally {
            IOUtils.closeQuietly(fileContents);
        }
        return fileByteContent;
    }

    @Override 
    public KfsVendorDataWrapper createNoteRecordingAnyErrors(KfsVendorDataWrapper kfsVendorDataWrapper, String noteText, String noteErrorDescriptor) {
        if (noteTextSizeIsWithinLimit(noteText)) {
            Note note = createNote(noteText);
            kfsVendorDataWrapper.getVendorNotes().add(note);
        } else {
            LOG.error("createNoteRecordingAnyErrors: The " + noteErrorDescriptor + " Note contained " + noteText.length() + "characters and it can only have " + PaymentWorksConstants.NOTE_TEXT_DEFAULT_MAX_LENGTH);
        }
        return kfsVendorDataWrapper;
    }
    
    private boolean noteTextSizeIsWithinLimit(String noteText) {
        return (noteTextIsNotBlank(noteText) && (noteText.length() <= PaymentWorksConstants.NOTE_TEXT_DEFAULT_MAX_LENGTH));
    }
    
    private boolean noteTextIsNotBlank(String noteText) {
        return (!StringUtils.isBlank(noteText));
    }
    
    private Note createNote(String noteText) {
        Note newNote = new Note();
        newNote.setNoteText(noteText);
        newNote.setAuthorUniversalIdentifier(getSystemUser().getPrincipalId());
        newNote.setNoteTypeCode(KFSConstants.NoteTypeEnum.BUSINESS_OBJECT_NOTE_TYPE.getCode());
        newNote.setNotePostedTimestampToCurrent();
        return newNote;
    }
    
    @Override
    public Person getSystemUser() {
        if (ObjectUtils.isNull(systemUser)) {
            setSystemUser(getPersonService().getPersonByPrincipalName(KFSConstants.SYSTEM_USER));
        }
        return systemUser;
    }
    
    public void setSystemUser(Person systemUser) {
        this.systemUser = systemUser;
    }
    
    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

}
