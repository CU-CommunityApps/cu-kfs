/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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
package org.kuali.kfs.module.purap.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.KewApiServiceLocator;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.datadictionary.exception.UnknownDocumentTypeException;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.context.SpringContext;

/**
 * Base class for Related View Business Objects.
 */
public abstract class AbstractRelatedView extends PersistableBusinessObjectBase {
    private static final Logger LOG = LogManager.getLogger();

    private Integer accountsPayablePurchasingDocumentLinkIdentifier;
    private Integer purapDocumentIdentifier;
    private String documentNumber;
    private String poNumberMasked;

    //create date from the workflow document header...
    private DateTime createDate;

    protected DocumentHeader documentHeader;

    public Integer getAccountsPayablePurchasingDocumentLinkIdentifier() {
        return accountsPayablePurchasingDocumentLinkIdentifier;
    }

    public void setAccountsPayablePurchasingDocumentLinkIdentifier(
            Integer accountsPayablePurchasingDocumentLinkIdentifier) {
        this.accountsPayablePurchasingDocumentLinkIdentifier = accountsPayablePurchasingDocumentLinkIdentifier;
    }

    public Integer getPurapDocumentIdentifier() {
        return purapDocumentIdentifier;
    }

    public void setPurapDocumentIdentifier(Integer purapDocumentIdentifier) {
        this.purapDocumentIdentifier = purapDocumentIdentifier;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public List<Note> getNotes() {
        List<Note> notes = new ArrayList<>();
        //reverse the order of notes only when anything exists in it..
        NoteService noteService = SpringContext.getBean(NoteService.class);
        // ==== CU Customization: Use the note target property, rather than always assuming that the doc header is the target. ====
        List<Note> tmpNotes = noteService.getByRemoteObjectId(findDocument(this.documentNumber).getNoteTarget().getObjectId());
        notes.clear();
        // reverse the order of notes retrieved so that newest note is in the front
        for (int i = tmpNotes.size()-1; i>=0; i--) {
            Note note = tmpNotes.get(i);
            notes.add(note);
        }
        return notes;
    }

    public String getUrl() {
        String documentTypeName = this.getDocumentTypeName();
        DocumentType docType = KEWServiceLocator.getDocumentTypeService().getDocumentTypeByName(documentTypeName);
        String docHandlerUrl = docType.getResolvedDocumentHandlerUrl();
        int endSubString = docHandlerUrl.lastIndexOf("/");
        String serverName = docHandlerUrl.substring(0, endSubString);
        String handler = docHandlerUrl.substring(endSubString + 1, docHandlerUrl.lastIndexOf("?"));
        return serverName + "/" + handler + "?channelTitle=" + docType.getName() + "&" +
                KRADConstants.DISPATCH_REQUEST_PARAMETER + "=" + KRADConstants.DOC_HANDLER_METHOD + "&" +
                KRADConstants.PARAMETER_DOC_ID + "=" + this.getDocumentNumber() + "&" +
                KRADConstants.PARAMETER_COMMAND + "=" + KewApiConstants.DOCSEARCH_COMMAND;
    }

    public String getDocumentIdentifierString() {
        if (purapDocumentIdentifier != null) {
            return purapDocumentIdentifier.toString();
        } else {
            return documentNumber;
        }
    }


    /**
     * @return the document label according to the label specified in the data dictionary.
     * @throws WorkflowException
     */
    public String getDocumentLabel() {
        return SpringContext.getBean(DataDictionaryService.class).getDocumentLabelByTypeName(getDocumentTypeName());
    }
    
    public abstract String getDocumentTypeName();

    public String getPoNumberMasked() {
        return poNumberMasked;
    }

    public void setPoNumberMasked(String poNumberMasked) {
        this.poNumberMasked = poNumberMasked;
    }

    /**
     * This method calls the workflow helper to allow for customization method to quickly grab status without any
     * fetching of extraneous information which causes problems for large numbers of related documents an api call
     * will be added to core Rice to support this in the next release
     */
    public String getApplicationDocumentStatus() {
        return documentHeader.getApplicationDocumentStatus();
    }

    public DocumentRouteHeaderValue findWorkflowDocument(String documentId){
        return KewApiServiceLocator.getWorkflowDocumentService().getDocument(documentId);
    }

    /**
     * @param documentHeaderId
     * @return document The document in the workflow that matches the document header id.
     */
    protected Document findDocument(String documentHeaderId) {
        Document document = null;

        try {
            document = SpringContext.getBean(DocumentService.class).getByDocumentHeaderId(documentHeaderId);
        } catch (UnknownDocumentTypeException ex) {
            LOG.error("Exception encountered on finding the document: {}", documentHeaderId, ex);
        }

        return document;
    }

    public DateTime getCreateDate() {
        DocumentRouteHeaderValue document = findWorkflowDocument(this.getDocumentNumber());
        return document.getDateCreated();

    }
}
