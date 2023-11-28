/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
package org.kuali.kfs.kns.datadictionary;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.kuali.kfs.kns.document.authorization.DocumentAuthorizer;
import org.kuali.kfs.kns.document.authorization.TransactionalDocumentAuthorizerBase;
import org.kuali.kfs.kns.document.authorization.TransactionalDocumentPresentationControllerBase;
import org.kuali.kfs.kns.rule.PromptBeforeValidation;
import org.kuali.kfs.kns.web.derivedvaluesetter.DerivedValuesSetter;
import org.kuali.kfs.krad.datadictionary.ReferenceDefinition;
import org.kuali.kfs.krad.document.DocumentPresentationController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Deprecated
public class TransactionalDocumentEntry extends DocumentEntry {

    protected Class<? extends PromptBeforeValidation> promptBeforeValidationClass;
    protected Class<? extends DerivedValuesSetter> derivedValuesSetterClass;
    protected List<String> webScriptFiles = new ArrayList<>(3);
    protected List<HeaderNavigation> headerNavigationList = new ArrayList<>();

    protected boolean sessionDocument;

    public TransactionalDocumentEntry() {
        super();

        documentAuthorizerClass = TransactionalDocumentAuthorizerBase.class;
        documentPresentationControllerClass = TransactionalDocumentPresentationControllerBase.class;
    }

    @Override
    public List<HeaderNavigation> getHeaderNavigationList() {
        return headerNavigationList;
    }

    @Override
    public List<String> getWebScriptFiles() {
        return webScriptFiles;
    }

    @Override
    public Class<? extends PromptBeforeValidation> getPromptBeforeValidationClass() {
        return promptBeforeValidationClass;
    }

    /**
     * The promptBeforeValidationClass element is the full class name of the java class which determines whether the
     * user should be asked any questions prior to running validation.
     */
    @Override
    public void setPromptBeforeValidationClass(final Class<? extends PromptBeforeValidation> preRulesCheckClass) {
        promptBeforeValidationClass = preRulesCheckClass;
    }

    /**
     * The webScriptFile element defines the name of javascript files that are necessary for processing the document.
     * The specified javascript files will be included in the generated html.
     */
    @Override
    public void setWebScriptFiles(final List<String> webScriptFiles) {
        this.webScriptFiles = webScriptFiles;
    }

    /**
     * The headerNavigation element defines a set of additional tabs which will appear on the document.
     */
    @Override
    public void setHeaderNavigationList(final List<HeaderNavigation> headerNavigationList) {
        this.headerNavigationList = headerNavigationList;
    }

    @Override
    public boolean isSessionDocument() {
        return sessionDocument;
    }

    @Override
    public void setSessionDocument(final boolean sessionDocument) {
        this.sessionDocument = sessionDocument;
    }

    @Override
    public Class<? extends DerivedValuesSetter> getDerivedValuesSetterClass() {
        return derivedValuesSetterClass;
    }

    @Override
    public void setDerivedValuesSetterClass(final Class<? extends DerivedValuesSetter> derivedValuesSetter) {
        derivedValuesSetterClass = derivedValuesSetter;
    }

    /**
     * @return a document authorizer class for the document.
     */
    @Override
    public Class<? extends DocumentAuthorizer> getDocumentAuthorizerClass() {
        return (Class<? extends DocumentAuthorizer>) super.getDocumentAuthorizerClass();
    }

    /**
     * @return the document presentation controller class for the document.
     */
    @Override
    public Class<? extends DocumentPresentationController> getDocumentPresentationControllerClass() {
        return super.getDocumentPresentationControllerClass();
    }

    @Override
    public void completeValidation() {
        super.completeValidation();
        for (final ReferenceDefinition reference : defaultExistenceChecks) {
            reference.completeValidation(documentClass, null);
        }
    }

    /*
     * CU Customization (KFSPTS-23970):
     * Added temporary override of the afterPropertiesSet() method to forcibly remove "../" prefixes
     * from any of the "webScriptFiles" entries. This provides a simpler short-term workaround
     * for the FINP-7386 issue (which was fixed in the 2021-02-25 financials patch).
     * 
     * TODO: Remove this entire class overlay when upgrading to the 2021-02-25 financials patch or later.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (CollectionUtils.isNotEmpty(webScriptFiles)) {
            webScriptFiles = webScriptFiles.stream()
                    .map(scriptFile -> StringUtils.removeStart(scriptFile, "../"))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        super.afterPropertiesSet();
    }
    /*
     * End CU Customization
     */

    @Override
    public String toString() {
        return "TransactionalDocumentEntry for documentType " + getDocumentTypeName();
    }
}
