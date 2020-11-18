/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
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
package org.kuali.kfs.kns.web.struts.action;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.RedirectingActionForward;
import org.kuali.kfs.kim.service.impl.KimModuleService;
import org.kuali.kfs.kns.datadictionary.BusinessObjectEntry;
import org.kuali.kfs.kns.inquiry.Inquirable;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.kns.util.WebUtils;
import org.kuali.kfs.kns.web.struts.form.InquiryForm;
import org.kuali.kfs.kns.web.ui.Row;
import org.kuali.kfs.kns.web.ui.Section;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.Exporter;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.bo.PersistableAttachment;
import org.kuali.kfs.krad.bo.PersistableAttachmentList;
import org.kuali.kfs.krad.exception.AuthorizationException;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.service.KRADServiceLocatorWeb;
import org.kuali.kfs.krad.service.ModuleService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.KRADUtils;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.rice.core.api.util.RiceConstants;
import org.kuali.rice.core.api.util.RiceKeyConstants;
import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.krad.bo.BusinessObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class handles actions for inquiries of business objects.
 */
public class KualiInquiryAction extends KualiAction {

    private static final Logger LOG = LogManager.getLogger();
    private NoteService noteService;

    @Override
    protected void checkAuthorization(ActionForm form, String methodToCall) throws AuthorizationException {
        if (!(form instanceof InquiryForm)) {
            super.checkAuthorization(form, methodToCall);
        } else {
            try {
                if (!KRADConstants.DOWNLOAD_BO_ATTACHMENT_METHOD.equals(methodToCall)) {
                    Class businessObjectClass = Class.forName(((InquiryForm) form).getBusinessObjectClassName());
                    if (!KimApiServiceLocator.getPermissionService().isAuthorizedByTemplate(
                        GlobalVariables.getUserSession().getPrincipalId(), KRADConstants.KNS_NAMESPACE,
                        KimConstants.PermissionTemplateNames.INQUIRE_INTO_RECORDS,
                        KRADUtils.getNamespaceAndComponentSimpleName(businessObjectClass), Collections.emptyMap())) {

                        throw new AuthorizationException(GlobalVariables.getUserSession().getPerson().getPrincipalName(),
                            "inquire", businessObjectClass.getSimpleName());
                    }
                }
            } catch (ClassNotFoundException e) {
                LOG.warn("Unable to load BusinessObject class: " + ((InquiryForm) form).getBusinessObjectClassName(),
                        e);
                super.checkAuthorization(form, methodToCall);
            }
        }
    }

    @Override
    protected Map<String, String> getRoleQualification(ActionForm form, String methodToCall) {
        Map<String, String> roleQualification = super.getRoleQualification(form, methodToCall);
        if (form instanceof InquiryForm) {
            Map<String, String> primaryKeys = ((InquiryForm) form).getInquiryPrimaryKeys();
            if (primaryKeys != null) {
                for (String keyName : primaryKeys.keySet()) {
                    roleQualification.put(keyName, primaryKeys.get(keyName));
                }
            }
        }
        return roleQualification;
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        request.setAttribute(KRADConstants.PARAM_MAINTENANCE_VIEW_MODE,
                KRADConstants.PARAM_MAINTENANCE_VIEW_MODE_INQUIRY);
        return super.execute(mapping, form, request, response);
    }

    /**
     * Gets an inquirable impl from the impl service name parameter. Then calls lookup service to retrieve the record
     * from the key/value parameters. Finally gets a list of Rows from the inquirable
     */
    // CU Customization: Allow the KIM module to return custom Person and Role inquiry URL redirects.
    public ActionForward start(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        InquiryForm inquiryForm = (InquiryForm) form;
        if (inquiryForm.getBusinessObjectClassName() == null) {
            LOG.error("Business object name not given.");
            throw new RuntimeException("Business object name not given.");
        }

        Class boClass = Class.forName(inquiryForm.getBusinessObjectClassName());
        ModuleService responsibleModuleService = KRADServiceLocatorWeb.getKualiModuleService()
                .getResponsibleModuleService(boClass);
        if (responsibleModuleService != null && responsibleModuleService.isExternalizable(boClass)) {
            String redirectUrl = responsibleModuleService.getExternalizableBusinessObjectInquiryUrl(boClass,
                    request.getParameterMap());
            ActionForward redirectingActionForward = new RedirectingActionForward(redirectUrl);
            redirectingActionForward.setModule("/");
            return redirectingActionForward;
        } else if (responsibleModuleService instanceof KimModuleService) {
            KimModuleService kimModuleService = (KimModuleService) responsibleModuleService;
            if (kimModuleService.hasCustomInquiryUrl(boClass)) {
                String baseInquiryUrl = kimModuleService.getInquiryUrl(boClass);
                Map<String, String> urlParameters = kimModuleService.getUrlParameters(
                        boClass.getName(), request.getParameterMap());
                String redirectUrl = UrlFactory.parameterizeUrl(baseInquiryUrl, urlParameters);
                ActionForward redirectingActionForward = new RedirectingActionForward(redirectUrl);
                redirectingActionForward.setModule("/");
                return redirectingActionForward;
            }
        }

        return continueWithInquiry(mapping, form, request, response);
    }

    /**
     * Downloads the attachment to the user's browser
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward downloadAttachment(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        InquiryForm inquiryForm = (InquiryForm) form;
        int line = getSelectedLine(request);

        BusinessObject bo = retrieveBOFromInquirable(inquiryForm);
        if (line < 0) {
            if (bo instanceof PersistableAttachment) {
                PersistableAttachment attachment = (PersistableAttachment) bo;
                if (StringUtils.isNotBlank(attachment.getFileName())
                    && attachment.getAttachmentContent() != null) {
                    streamToResponse(attachment.getAttachmentContent(), attachment.getFileName(),
                            attachment.getContentType(), response);
                }
            }
        } else {
            if (bo instanceof PersistableAttachmentList) {
                PersistableAttachmentList<PersistableAttachment> attachmentsBo =
                        (PersistableAttachmentList<PersistableAttachment>) bo;
                if (CollectionUtils.isEmpty(attachmentsBo.getAttachments())) {
                    return null;
                }

                List<? extends PersistableAttachment> attachments = attachmentsBo.getAttachments();
                if (CollectionUtils.isNotEmpty(attachments)
                    && attachments.size() > line) {
                    PersistableAttachment attachment = attachmentsBo.getAttachments().get(line);
                    streamToResponse(attachment.getAttachmentContent(), attachment.getFileName(),
                            attachment.getContentType(), response);
                }
            }
        }
        return null;
    }

    public ActionForward downloadCustomBOAttachment(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String fileName = request.getParameter(KRADConstants.BO_ATTACHMENT_FILE_NAME);
        String contentType = request.getParameter(KRADConstants.BO_ATTACHMENT_FILE_CONTENT_TYPE);
        String fileContentBoField = request.getParameter(KRADConstants.BO_ATTACHMENT_FILE_CONTENT_FIELD);
        //require certain request properties
        if (fileName != null
            && contentType != null
            && fileContentBoField != null) {
            //make sure user has authorization to download attachment
            checkAuthorization(form, findMethodToCall(form, request));

            fileName = StringUtils.replace(fileName, " ", "_");

            InquiryForm inquiryForm = (InquiryForm) form;
            BusinessObject bo = retrieveBOFromInquirable(inquiryForm);
            checkBO(bo);

            Class clazz = bo.getClass();
            Method method = clazz.getMethod("get" + StringUtils.capitalize(fileContentBoField));
            byte[] fileContents = (byte[]) method.invoke(bo);
            streamToResponse(fileContents, fileName, contentType, response);
        } else {
            if (fileName == null) {
                LOG.error("Request Parameter \"" + KRADConstants.BO_ATTACHMENT_FILE_NAME + "\" not provided.");
            }
            if (contentType == null) {
                LOG.error("Request Parameter \"" + KRADConstants.BO_ATTACHMENT_FILE_CONTENT_TYPE +
                        "\" not provided.");
            }
            if (fileContentBoField == null) {
                LOG.error("Request Parameter \"" + KRADConstants.BO_ATTACHMENT_FILE_CONTENT_FIELD +
                        "\" not provided.");
            }
        }
        return null;
    }

    /**
     * Downloads the selected attachment to the user's browser
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward downloadBOAttachment(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Long noteIdentifier = Long.valueOf(request.getParameter(KRADConstants.NOTE_IDENTIFIER));

        Note note = this.getNoteService().getNoteByNoteId(noteIdentifier);
        if (note != null) {
            Attachment attachment = note.getAttachment();
            if (attachment != null) {
                //make sure attachment is setup with backwards reference to note (rather then doing this we could also
                // just call the attachment service (with a new method that took in the note)
                attachment.setNote(note);
                WebUtils.saveMimeInputStreamAsFile(response, attachment.getAttachmentMimeTypeCode(),
                        attachment.getAttachmentContents(), attachment.getAttachmentFileName(),
                        attachment.getAttachmentFileSize().intValue());
            }
            return null;
        }

        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    public ActionForward continueWithInquiry(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        InquiryForm inquiryForm = (InquiryForm) form;

        if (inquiryForm.getBusinessObjectClassName() == null) {
            LOG.error("Business object name not given.");
            throw new RuntimeException("Business object name not given.");
        }

        BusinessObject bo = retrieveBOFromInquirable(inquiryForm);
        checkBO(bo);

        populateSections(mapping, request, inquiryForm, bo);

        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    /**
     * Turns on (or off) the inactive record display for a maintenance collection.
     */
    public ActionForward toggleInactiveRecordDisplay(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        InquiryForm inquiryForm = (InquiryForm) form;
        if (inquiryForm.getBusinessObjectClassName() == null) {
            LOG.error("Business object name not given.");
            throw new RuntimeException("Business object name not given.");
        }

        BusinessObject bo = retrieveBOFromInquirable(inquiryForm);
        checkBO(bo);

        Inquirable kualiInquirable = inquiryForm.getInquirable();
        //////////////////////////////
        String collectionName = extractCollectionName(request, KRADConstants.TOGGLE_INACTIVE_METHOD);
        if (collectionName == null) {
            LOG.error("Unable to get find collection name in request.");
            throw new RuntimeException("Unable to get find collection class in request.");
        }
        String parameterName = (String) request.getAttribute(KRADConstants.METHOD_TO_CALL_ATTRIBUTE);
        boolean showInactive = Boolean.parseBoolean(StringUtils.substringBetween(parameterName,
                KRADConstants.METHOD_TO_CALL_BOPARM_LEFT_DEL, "."));
        kualiInquirable.setShowInactiveRecords(collectionName, showInactive);
        //////////////////////////////

        populateSections(mapping, request, inquiryForm, bo);

        // toggling the display to be visible again, re-open any previously closed inactive records
        if (showInactive) {
            WebUtils.reopenInactiveRecords(inquiryForm.getSections(), inquiryForm.getTabStates(), collectionName);
        }

        return mapping.findForward(RiceConstants.MAPPING_BASIC);
    }

    @Override
    public ActionForward toggleTab(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        InquiryForm inquiryForm = (InquiryForm) form;
        if (inquiryForm.getBusinessObjectClassName() == null) {
            LOG.error("Business object name not given.");
            throw new RuntimeException("Business object name not given.");
        }

        BusinessObject bo = retrieveBOFromInquirable(inquiryForm);
        checkBO(bo);

        populateSections(mapping, request, inquiryForm, bo);

        return super.toggleTab(mapping, form, request, response);
    }

    @Override
    public ActionForward hideAllTabs(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        // KULRICE-2852: Overrides hideAllTabs() so that it also calls the populateSections() method.
        InquiryForm inquiryForm = (InquiryForm) form;
        if (inquiryForm.getBusinessObjectClassName() == null) {
            LOG.error("Business object name not given.");
            throw new RuntimeException("Business object name not given.");
        }

        BusinessObject bo = retrieveBOFromInquirable(inquiryForm);
        checkBO(bo);

        populateSections(mapping, request, inquiryForm, bo);

        return super.hideAllTabs(mapping, form, request, response);
    }

    @Override
    public ActionForward showAllTabs(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        // KULRICE-2852: Overrides showAllTabs() so that it also calls the populateSections() method.
        InquiryForm inquiryForm = (InquiryForm) form;
        if (inquiryForm.getBusinessObjectClassName() == null) {
            LOG.error("Business object name not given.");
            throw new RuntimeException("Business object name not given.");
        }

        BusinessObject bo = retrieveBOFromInquirable(inquiryForm);
        checkBO(bo);

        populateSections(mapping, request, inquiryForm, bo);

        return super.showAllTabs(mapping, form, request, response);
    }

    /**
     * Handles exporting the BusinessObject for this Inquiry to XML if it has a custom XML exporter available.
     */
    public ActionForward export(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        InquiryForm inquiryForm = (InquiryForm) form;
        if (inquiryForm.isCanExport()) {
            BusinessObject bo = retrieveBOFromInquirable(inquiryForm);
            checkBO(bo);
            if (bo != null) {
                BusinessObjectEntry businessObjectEntry = KNSServiceLocator.getBusinessObjectDictionaryService()
                        .getBusinessObjectEntry(inquiryForm.getBusinessObjectClassName());
                Class<? extends Exporter> exporterClass = businessObjectEntry.getExporterClass();
                if (exporterClass != null) {
                    Exporter exporter = exporterClass.newInstance();
                    response.setContentType(KRADConstants.XML_MIME_TYPE);
                    response.setHeader("Content-disposition", "attachment; filename=export.xml");
                    exporter.export(businessObjectEntry.getBusinessObjectClass(), Collections.singletonList(bo),
                            KRADConstants.XML_FORMAT, response.getOutputStream());
                }
            } else {
                //show the empty section with error
                populateSections(mapping, request, inquiryForm, bo);
                return mapping.findForward(RiceConstants.MAPPING_BASIC);
            }
        }

        return null;
    }

    /**
     * Convert a Request into a Map<String,String>. Technically, Request parameters do not neatly translate into a Map
     * of Strings, because a given parameter may legally appear more than once (so a Map of String[] would be more
     * accurate.) This method should be safe for business objects, but may not be reliable for more general uses.
     */
    protected String extractCollectionName(HttpServletRequest request, String methodToCall) {
        // collection name and underlying object type from request parameter
        String parameterName = (String) request.getAttribute(KRADConstants.METHOD_TO_CALL_ATTRIBUTE);
        String collectionName = null;
        if (StringUtils.isNotBlank(parameterName)) {
            collectionName = StringUtils.substringBetween(parameterName, methodToCall + ".", ".(");
        }
        return collectionName;
    }

    protected BusinessObject retrieveBOFromInquirable(InquiryForm inquiryForm) {
        Inquirable kualiInquirable = inquiryForm.getInquirable();
        // retrieve the business object
        BusinessObject bo = kualiInquirable.getBusinessObject(inquiryForm.retrieveInquiryDecryptedPrimaryKeys());
        if (bo == null) {
            LOG.error("No records found in inquiry action.");
            GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, RiceKeyConstants.ERROR_INQUIRY);
        }
        return bo;
    }

    protected void populateSections(ActionMapping mapping, HttpServletRequest request, InquiryForm inquiryForm,
            BusinessObject bo) {
        Inquirable kualiInquirable = inquiryForm.getInquirable();

        if (bo != null) {
            // get list of populated sections for display
            List<Section> sections = kualiInquirable.getSections(bo);
            inquiryForm.setSections(sections);
            kualiInquirable.addAdditionalSections(sections, bo);
        } else {
            inquiryForm.setSections(getEmptySections(kualiInquirable.getTitle()));
        }

        request.setAttribute(KRADConstants.INQUIRABLE_ATTRIBUTE_NAME, kualiInquirable);
    }

    /**
     * Handy method to stream the byte array to response object
     *
     * @param fileContents
     * @param response
     * @throws Exception
     */
    protected void streamToResponse(byte[] fileContents, String fileName, String fileContentType,
            HttpServletResponse response) throws Exception {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream(fileContents.length);
            baos.write(fileContents);
            WebUtils.saveMimeOutputStreamAsFile(response, fileContentType, baos, fileName);
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                    baos = null;
                }
            } catch (IOException ioEx) {
                LOG.error("Error while downloading attachment");
                throw new RuntimeException("IOException occurred while downloading attachment", ioEx);
            }
        }
    }

    /**
     * @return list of sections with one empty section and one row.
     */
    private List<Section> getEmptySections(String title) {
        final Row row = new Row(Collections.emptyList());

        final Section section = new Section(Collections.singletonList(row));
        section.setErrorKey("*");
        section.setSectionTitle(title != null ? title : "");
        section.setNumberOfColumns(0);

        return Collections.singletonList(section);
    }

    /**
     * throws an exception if BO fails the check.
     *
     * @param bo the BusinessObject to check.
     * @throws UnsupportedOperationException if BO is null & no messages have been generated.
     */
    private void checkBO(BusinessObject bo) {
        if (bo == null && GlobalVariables.getMessageMap().hasNoMessages()) {
            throw new UnsupportedOperationException("The record you have inquired on does not exist.");
        }
    }

    protected NoteService getNoteService() {
        if (noteService == null) {
            noteService = KRADServiceLocator.getNoteService();
        }
        return this.noteService;
    }
}
