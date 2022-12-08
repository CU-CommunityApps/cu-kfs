/*
 * Copyright 2009 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rsmart.kuali.kfs.sys.batch.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import net.sf.jasperreports.engine.JRParameter;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSConstants.ReportGeneration;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.kfs.sys.report.ReportInfo;
import org.kuali.kfs.sys.service.ReportGenerationService;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.datadictionary.AttributeDefinition;
import org.kuali.kfs.datadictionary.legacy.DataDictionary;
import org.kuali.kfs.krad.datadictionary.DataDictionaryEntry;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.AttachmentService;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;

import com.rsmart.kuali.kfs.sys.KFSKeyConstants;
import com.rsmart.kuali.kfs.sys.batch.service.BatchFeedHelperService;
import com.rsmart.kuali.kfs.sys.businessobject.BatchFeedStatusBase;

/**
 * @see com.rsmart.kuali.kfs.sys.batch.service.BatchFeedHelperService
 */
public class BatchFeedHelperServiceImpl implements BatchFeedHelperService {
	private static final Logger LOG = LogManager.getLogger(BatchFeedHelperServiceImpl.class);

    private BatchInputFileService batchInputFileService;
    private DataDictionaryService dataDictionaryService;
    private PersonService personService;
    private ConfigurationService kualiConfigurationService;
    private AttachmentService attachmentService;
    private ReportGenerationService reportGenerationService;
    private DateTimeService dateTimeService;

    private Properties mimeTypeProperties;

    /**
     * @see com.rsmart.kuali.kfs.sys.batch.service.BatchFeedHelperService#parseBatchFile(org.kuali.kfs.sys.batch.BatchInputFileType,
     *      java.lang.String, com.rsmart.kuali.kfs.sys.businessobject.BatchFeedStatusBase)
     */
    public Object parseBatchFile(BatchInputFileType batchInputFileType, String incomingFileName, BatchFeedStatusBase batchStatus) {
        FileInputStream fileContents;
        try {
            fileContents = new FileInputStream(incomingFileName);
        }
        catch (FileNotFoundException e1) {
            LOG.error("file to load not found " + incomingFileName, e1);
            throw new RuntimeException("Cannot find the file requested to be loaded " + incomingFileName, e1);
        }

        // do the parse
        Object parsed = null;
        try {
            byte[] fileByteContent = IOUtils.toByteArray(fileContents);
            parsed = batchInputFileService.parse(batchInputFileType, fileByteContent);
        }
        catch (IOException e) {
            LOG.error("error while getting file bytes:  " + e.getMessage(), e);
            throw new RuntimeException("Error encountered while attempting to get file bytes: " + e.getMessage(), e);
        }
        catch (ParseException e1) {
            LOG.error("Error parsing xml " + e1.getMessage());
            batchStatus.setXmlParseExceptionMessage(e1.getMessage());
        }

        return parsed;
    }

    /**
     * @see com.rsmart.kuali.kfs.sys.batch.service.BatchFeedHelperService#generateAuditReport(org.kuali.kfs.sys.report.ReportInfo,
     *      com.rsmart.kuali.kfs.sys.businessobject.BatchFeedStatusBase)
     */
    public void generateAuditReport(ReportInfo reportInfo, BatchFeedStatusBase batchStatus) {
        String reportFileName = reportInfo.getReportFileName();
        String reportDirectory = reportInfo.getReportsDirectory();
        String reportTemplateClassPath = reportInfo.getReportTemplateClassPath();
        String reportTemplateName = reportInfo.getReportTemplateName();
        ResourceBundle resourceBundle = reportInfo.getResourceBundle();
        String subReportTemplateClassPath = reportInfo.getSubReportTemplateClassPath();
        Map<String, String> subReports = reportInfo.getSubReports();

        Map<String, Object> reportData = batchStatus.getReportData();
        reportData.put(JRParameter.REPORT_RESOURCE_BUNDLE, resourceBundle);
        reportData.put(ReportGeneration.PARAMETER_NAME_SUBREPORT_DIR, subReportTemplateClassPath);
        reportData.put(ReportGeneration.PARAMETER_NAME_SUBREPORT_TEMPLATE_NAME, subReports);

        String template = reportTemplateClassPath + reportTemplateName;
        String fullReportFileName = reportGenerationService.buildFullFileName(dateTimeService.getCurrentDate(), reportDirectory, reportFileName, "");

        reportGenerationService.generateReportToPdfFile(reportData, template, fullReportFileName);
    }

    /**
     * @see com.rsmart.kuali.kfs.sys.batch.service.BatchFeedHelperService#getAuditMessage(java.lang.String, java.lang.String,
     *      org.kuali.kfs.kns.util.ErrorMap)
     */
    public String getAuditMessage(String successfulErrorKey, String documentNumber, MessageMap errorMap) {
        String auditMessage = "";
        if (errorMap.hasErrors()) {
            for (String errorProperty : errorMap.getAllPropertiesWithErrors()) {
                for (Object errorMessage : errorMap.getMessages(errorProperty)) {
                    String errorMsg = kualiConfigurationService.getPropertyValueAsString(((ErrorMessage) errorMessage).getErrorKey());
                    if (errorMsg == null) {
                        throw new RuntimeException("Cannot find message for error key: " + ((ErrorMessage) errorMessage).getErrorKey());
                    }
                    else {
                        Object[] arguments = (Object[]) ((ErrorMessage) errorMessage).getMessageParameters();
                        if (arguments != null && arguments.length != 0) {
                            errorMsg = MessageFormat.format(errorMsg, arguments);
                        }
                    }
                    auditMessage += errorMsg + " ";
                }
            }
        }

        // add error prefix
        if (!StringUtils.isBlank(auditMessage)) {
            auditMessage = com.rsmart.kuali.kfs.sys.KFSConstants.AUDIT_REPORT_ERROR_PREFIX + auditMessage;
        }
        else {
            String successMessage = kualiConfigurationService.getPropertyValueAsString(successfulErrorKey);
            auditMessage = MessageFormat.format(successMessage, documentNumber);
        }

        return auditMessage;
    }

    /**
     * @see com.rsmart.kuali.kfs.sys.batch.service.BatchFeedHelperService#performForceUppercase(java.lang.String, java.lang.Object)
     */
    public void performForceUppercase(String entryName, Object businessObject) {
        DataDictionaryEntry entry = dataDictionaryService.getDictionaryObjectEntry(entryName);
        if (entry == null) {
            return;
        }

        List<AttributeDefinition> attributes = ((DataDictionaryEntry) entry).getAttributes();
        for (AttributeDefinition attribute : attributes) {
            try {
                if (!attribute.getForceUppercase() || !PropertyUtils.isWriteable(businessObject, attribute.getName())) {
                    continue;
                }

                Object currentValue = ObjectUtils.getPropertyValue(businessObject, attribute.getName());
                if (currentValue != null && String.class.isAssignableFrom(currentValue.getClass())) {
                    try {
                        ObjectUtils.setObjectProperty(businessObject, attribute.getName(), currentValue.toString().toUpperCase(Locale.US));
                    }
                    catch (Exception e) {
                        LOG.error("cannot uppercase property " + attribute.getName() + " in bo class " + entryName, e);
                        throw new RuntimeException("cannot uppercase property " + attribute.getName() + " in bo class " + entryName, e);
                    }
                }
            }
            catch (Exception e) {
                LOG.warn("cannot uppercase property: " + attribute.getName() + "; " + e.getMessage());
                continue;
            }
        }
    }

    /**
     * @see com.rsmart.kuali.kfs.sys.batch.service.BatchFeedHelperService#loadDocumentAttachments(org.kuali.kfs.kns.document.Document,
     *      java.util.List, java.lang.String, java.lang.String, org.kuali.kfs.kns.util.ErrorMap)
     */
    public void loadDocumentAttachments(Document document, List<Attachment> attachments, String attachmentsPath, String attachmentType, MessageMap errorMap) {
        for (Attachment attachment : attachments) {
            Note note = new Note();

            note.setNoteText(kualiConfigurationService.getPropertyValueAsString(KFSKeyConstants.IMAGE_ATTACHMENT_NOTE_TEXT));
            note.setRemoteObjectIdentifier(document.getObjectId());
            note.setAuthorUniversalIdentifier(getSystemUser().getPrincipalId());
            note.setNoteTypeCode(KFSConstants.NoteTypeEnum.BUSINESS_OBJECT_NOTE_TYPE.getCode());
            note.setNotePostedTimestampToCurrent();

            // attempt to load file
            String fileName = attachmentsPath + "/" + attachment.getAttachmentFileName();
            File attachmentFile = new File(fileName);
            if (!attachmentFile.exists()) {
                errorMap.putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_BATCH_FEED_ATTACHMENT, new String[] { attachment.getAttachmentFileName(), attachmentsPath });
                continue;
            }

            try {
                FileInputStream fileInputStream = new FileInputStream(fileName);
                Integer fileSize = Integer.parseInt(Long.toString(attachmentFile.length()));

                String mimeTypeCode = attachment.getAttachmentMimeTypeCode();
                String fileExtension = "." + StringUtils.substringAfterLast(fileName, ".");
                if (StringUtils.isNotBlank(fileExtension) && mimeTypeProperties.containsKey(fileExtension)) {
                    if (StringUtils.isBlank(mimeTypeCode)) {
                        mimeTypeCode = mimeTypeProperties.getProperty(fileExtension);
                    }
                }
                else {
                    errorMap.putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_BATCH_FEED_ATTACHMENT_TYPE, new String[] { fileName, fileExtension });
                }

                Attachment noteAttachment = attachmentService.createAttachment(document.getDocumentHeader(), fileName, mimeTypeCode, fileSize, fileInputStream, attachmentType);

                note.addAttachment(noteAttachment);
                document.addNote(note);
            }
            catch (FileNotFoundException e) {
                errorMap.putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_BATCH_FEED_ATTACHMENT, new String[] { attachment.getAttachmentFileName(), attachmentsPath });
                continue;
            }
            catch (IOException e1) {
                throw new RuntimeException("Unable to create attachment for image: " + fileName, e1);
            }
        }
    }

    /**
     * @see com.rsmart.kuali.kfs.sys.batch.service.BatchFeedHelperService#performExistenceAndActiveValidation(org.kuali.kfs.kns.bo.PersistableBusinessObject,
     *      java.lang.String, java.lang.String, org.kuali.kfs.kns.util.ErrorMap)
     */
    public void performExistenceAndActiveValidation(PersistableBusinessObject businessObject, String referenceName, String propertyName, MessageMap errorMap) {
        Object propertyValue = ObjectUtils.getPropertyValue(businessObject, propertyName);

        if (propertyValue != null) {
            businessObject.refreshReferenceObject(referenceName);

            Object referenceValue = ObjectUtils.getPropertyValue(businessObject, referenceName);
            if (ObjectUtils.isNull(referenceValue)) {
                addExistenceError(propertyName, propertyValue.toString(), errorMap);
            }
            else if (MutableInactivatable.class.isAssignableFrom(referenceValue.getClass())) {
                if (!((MutableInactivatable) referenceValue).isActive()) {
                    addInactiveError(propertyName, propertyValue.toString(), errorMap);
                }
            }
        }
    }

    /**
     * @see com.rsmart.kuali.kfs.sys.batch.service.BatchFeedHelperService#addRequiredError(org.kuali.kfs.kns.bo.PersistableBusinessObject,
     *      java.lang.String, org.kuali.kfs.kns.util.ErrorMap)
     */
    public void addRequiredError(PersistableBusinessObject businessObject, String propertyName, MessageMap errorMap) {
        String propertyLabel = dataDictionaryService.getAttributeLabel(businessObject.getClass(), propertyName);
        errorMap.putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_BATCH_PURCAHSE_ORDER_REQUIRED, new String[] { propertyLabel });
    }

    /**
     * @see com.rsmart.kuali.kfs.sys.batch.service.BatchFeedHelperService#addExistenceError(java.lang.String, java.lang.String,
     *      org.kuali.kfs.kns.util.ErrorMap)
     */
    public void addExistenceError(String propertyName, String propertyValue, MessageMap errorMap) {
        String propertyLabel = dataDictionaryService.getAttributeLabel(PurchaseOrderDocument.class, propertyName);
        errorMap.putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_BATCH_PURCAHSE_ORDER_EXISTENCE, new String[] { propertyLabel, propertyValue });
    }

    /**
     * @see com.rsmart.kuali.kfs.sys.batch.service.BatchFeedHelperService#addInactiveError(java.lang.String, java.lang.String,
     *      org.kuali.kfs.kns.util.ErrorMap)
     */
    public void addInactiveError(String propertyName, String propertyValue, MessageMap errorMap) {
        String propertyLabel = dataDictionaryService.getAttributeLabel(PurchaseOrderDocument.class, propertyName);
        errorMap.putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_BATCH_PURCAHSE_ORDER_INACTIVE, new String[] { propertyLabel, propertyValue });
    }

    /**
     * @see com.rsmart.kuali.kfs.sys.batch.service.BatchFeedHelperService#removeDoneFile(java.lang.String)
     */
    public void removeDoneFile(String dataFileName) {
        File doneFile = new File(StringUtils.substringBeforeLast(dataFileName, ".") + ".done");
        if (doneFile.exists()) {
            doneFile.delete();
        }
    }

    /**
     * @see com.rsmart.kuali.kfs.sys.batch.service.BatchFeedHelperService#getSystemUser()
     */
    public Person getSystemUser() {
        return personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER);
    }

    /**
     * @return Returns the batchInputFileService.
     */
    protected BatchInputFileService getBatchInputFileService() {
        return batchInputFileService;
    }

    /**
     * @param batchInputFileService The batchInputFileService to set.
     */
    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    /**
     * @return Returns the dataDictionaryService.
     */
    protected DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    /**
     * @param dataDictionaryService The dataDictionaryService to set.
     */
    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    /**
     * @return Returns the personService.
     */
    protected PersonService getPersonService() {
        return personService;
    }

    /**
     * @param personService The personService to set.
     */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    /**
     * @return Returns the kualiConfigurationService.
     */
    protected ConfigurationService getKualiConfigurationService() {
        return kualiConfigurationService;
    }

    /**
     * @param kualiConfigurationService The kualiConfigurationService to set.
     */
    public void setKualiConfigurationService(ConfigurationService kualiConfigurationService) {
        this.kualiConfigurationService = kualiConfigurationService;
    }

    /**
     * @return Returns the attachmentService.
     */
    protected AttachmentService getAttachmentService() {
        return attachmentService;
    }

    /**
     * @param attachmentService The attachmentService to set.
     */
    public void setAttachmentService(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    /**
     * @return Returns the reportGenerationService.
     */
    protected ReportGenerationService getReportGenerationService() {
        return reportGenerationService;
    }

    /**
     * @param reportGenerationService The reportGenerationService to set.
     */
    public void setReportGenerationService(ReportGenerationService reportGenerationService) {
        this.reportGenerationService = reportGenerationService;
    }

    /**
     * @return Returns the dateTimeService.
     */
    protected DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    /**
     * @param dateTimeService The dateTimeService to set.
     */
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    /**
     * @return Returns the mimeTypeProperties.
     */
    protected Properties getMimeTypeProperties() {
        return mimeTypeProperties;
    }

    /**
     * @param mimeTypeProperties The mimeTypeProperties to set.
     */
    public void setMimeTypeProperties(Properties mimeTypeProperties) {
        this.mimeTypeProperties = mimeTypeProperties;
    }

}
