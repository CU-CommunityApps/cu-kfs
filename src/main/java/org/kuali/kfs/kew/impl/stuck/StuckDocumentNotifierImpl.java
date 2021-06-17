/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.kew.impl.stuck;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.core.impl.config.property.Config;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSParameterKeyConstants;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * CU Customization: Backported this file from the 2021-04-08 financials patch to bring in the FINP-7470 changes.
 * This overlay should be removed when we upgrade to the 2021-04-08 financials patch.
 */
public class StuckDocumentNotifierImpl implements StuckDocumentNotifier, InitializingBean {

    private static final Logger LOG = LogManager.getLogger();

    private static final String NOTIFICATION_SUBJECT_TEMPLATE_NAME = "notificationSubject";
    private static final String NOTIFICATION_EMAIL_TEMPLATE_NAME = "notificationEmail";
    private static final String AUTOFIX_SUBJECT_TEMPLATE_NAME = "autofixSubject";
    private static final String AUTOFIX_EMAIL_TEMPLATE_NAME = "autofixEmail";

    private static final String NOTIFICATION_EMAIL_TEMPLATE =
            "${numStuckDocuments} stuck documents have been identified within the workflow system:\n\n" +
                    "Document ID, Document Type, Create Date\n" +
                    "---------------------------------------\n" +
                    "<#list stuckDocuments as stuckDocument>${stuckDocument.documentId}, ${stuckDocument.documentTypeLabel}, ${stuckDocument.createDate}\n</#list>";
    private static final String AUTOFIX_EMAIL_TEMPLATE =
            "Failed to autofix document ${documentId}, ${documentTypeLabel}.\n\nIncident details:\n\tStarted: ${startDate}\n\tEnded: ${endDate}\n\n" +
                    "Attempts occurred at the following times: <#list autofixAttempts as autofixAttempt>\n\t${autofixAttempt.timestamp}</#list>";

    private Configuration freemarkerConfig;
    private StringTemplateLoader templateLoader;

    private EmailService emailService;
    private ParameterService parameterService;

    public void afterPropertiesSet() {
        this.freemarkerConfig = new Configuration(Configuration.VERSION_2_3_21);
        this.templateLoader = new StringTemplateLoader();
        this.freemarkerConfig.setTemplateLoader(templateLoader);
    }

    @Override
    public void notify(List<StuckDocument> stuckDocuments) {
        if (!stuckDocuments.isEmpty()) {
            updateNotificationTemplates();
            Map<String, Object> dataModel = buildNotificationTemplateDataModel(stuckDocuments);
            String subject = processTemplate(NOTIFICATION_SUBJECT_TEMPLATE_NAME, dataModel);
            String body = processTemplate(NOTIFICATION_EMAIL_TEMPLATE_NAME, dataModel);
            send(subject, body);
        }
    }

    private void updateNotificationTemplates() {
        this.templateLoader.putTemplate(NOTIFICATION_SUBJECT_TEMPLATE_NAME, notificationSubject());
        this.templateLoader.putTemplate(NOTIFICATION_EMAIL_TEMPLATE_NAME, NOTIFICATION_EMAIL_TEMPLATE);
        this.freemarkerConfig.clearTemplateCache();
    }

    private String notificationSubject() {
        return parameterService.getParameterValueAsString(StuckDocumentNotificationStep.class,
                KFSParameterKeyConstants.NOTIFICATION_SUBJECT, "Stuck Documents Found");
    }

    /**
     * Supported values include:
     * <p>
     * - numStuckDocuments
     * - stuckDocuments (List of StuckDocument)
     * - environment
     * - applicationUrl
     */
    private Map<String, Object> buildNotificationTemplateDataModel(List<StuckDocument> stuckDocuments) {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("numStuckDocuments", stuckDocuments.size());
        dataModel.put("stuckDocuments", stuckDocuments);
        addGlobalDataModel(dataModel);
        return dataModel;
    }

    @Override
    public void notifyIncidentFailure(StuckDocumentIncident incident, List<StuckDocumentFixAttempt> attempts) {
        updateAutofixTemplates();
        Map<String, Object> dataModel = buildIncidentFailureTemplateDataModel(incident, attempts);
        String subject = processTemplate(AUTOFIX_SUBJECT_TEMPLATE_NAME, dataModel);
        String body = processTemplate(AUTOFIX_EMAIL_TEMPLATE_NAME, dataModel);
        send(subject, body);
    }

    private void updateAutofixTemplates() {
        this.templateLoader.putTemplate(AUTOFIX_SUBJECT_TEMPLATE_NAME, autofixSubject());
        this.templateLoader.putTemplate(AUTOFIX_EMAIL_TEMPLATE_NAME, AUTOFIX_EMAIL_TEMPLATE);
        this.freemarkerConfig.clearTemplateCache();
    }

    private String autofixSubject() {
        return parameterService.getParameterValueAsString(StuckDocumentAutofixStep.class,
                KFSParameterKeyConstants.NOTIFICATION_SUBJECT, "Failed to autofix document #{ '$' + '{documentId}'}");
    }

    /**
     * Supported values include:
     * <p>
     * - documentId
     * - documentTypeLabel
     * - startDate
     * - endDate
     * - numberOfAutofixAttempts
     * - attempts (List of StuckDocumentFixAttempt)
     * - environment
     * - applicationUrl
     */
    private Map<String, Object> buildIncidentFailureTemplateDataModel(
            StuckDocumentIncident incident, List<StuckDocumentFixAttempt> attempts) {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("documentId", incident.getDocumentId());
        dataModel.put("documentTypeLabel", resolveDocumentTypeLabel(incident.getDocumentId()));
        dataModel.put("startDate", incident.getStartDate());
        dataModel.put("endDate", incident.getEndDate());
        dataModel.put("numberOfAutofixAttempts", attempts.size());
        dataModel.put("autofixAttempts", attempts);
        addGlobalDataModel(dataModel);
        return dataModel;
    }

    private void addGlobalDataModel(Map<String, Object> dataModel) {
        dataModel.put(Config.ENVIRONMENT, ConfigContext.getCurrentContextConfig().getEnvironment());
        dataModel.put("applicationUrl",
                ConfigContext.getCurrentContextConfig().getProperty(KFSConstants.APPLICATION_URL_KEY));
    }

    private String processTemplate(String templateName, Object dataModel) {
        try {
            StringWriter writer = new StringWriter();
            Template template = freemarkerConfig.getTemplate(templateName);
            template.process(dataModel, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            throw new IllegalStateException("Failed to execute template " + templateName, e);
        }
    }

    private String resolveDocumentTypeLabel(String documentId) {
        return KEWServiceLocator.getDocumentTypeService().findByDocumentId(documentId).getLabel();
    }

    private void send(String subject, String messageBody) {
        if (checkCanSend()) {
            BodyMailMessage message = new BodyMailMessage();
            message.setFromAddress(fromAddress());
            message.setToAddresses(Collections.singleton(toAddress()));
            message.setSubject(subject);
            message.setMessage(messageBody);
            try {
                emailService.sendMessage(message, false);
            } catch (Exception e) {
                // we don't want some email configuration issue to mess up our stuck document processing, just log the error
                LOG.error("Failed to send stuck document notification email with the body:\n" + messageBody, e);
            }
        }
    }

    private String fromAddress() {
        return parameterService.getParameterValueAsString(StuckDocumentNotificationStep.class,
                KFSParameterKeyConstants.FROM_EMAIL);

    }

    private String toAddress() {
        return parameterService.getParameterValueAsString(StuckDocumentNotificationStep.class,
                KFSParameterKeyConstants.TO_EMAIL);
    }

    private boolean checkCanSend() {
        boolean canSend = true;
        if (StringUtils.isBlank(fromAddress())) {
            LOG.error("Cannot send stuck documentation notification because no 'from' address is configured.");
            canSend = false;
        }
        if (StringUtils.isBlank(toAddress())) {
            LOG.error("Cannot send stuck documentation notification because no 'to' address is configured.");
            canSend = false;
        }
        return canSend;
    }

    @Required
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    @Required
    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }
}
