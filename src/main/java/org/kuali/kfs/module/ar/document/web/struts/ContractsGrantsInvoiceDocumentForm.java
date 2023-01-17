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
package org.kuali.kfs.module.ar.document.web.struts;

import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.service.DocumentHelperService;
import org.kuali.kfs.kns.web.ui.ExtraButton;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.module.ar.ArAuthorizationConstants;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.businessobject.CustomerInvoiceDetail;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.module.ar.document.authorization.ContractsGrantsInvoiceDocumentAuthorizer;
import org.kuali.kfs.module.ar.document.authorization.ContractsGrantsInvoiceDocumentPresentationController;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import java.util.List;
import java.text.MessageFormat;

//CU customization: backport FINP-5292, this file can be removed when we upgrade to the 06/30/2022 version of financials
public class ContractsGrantsInvoiceDocumentForm extends CustomerInvoiceForm {

    @Override
    public List<ExtraButton> getExtraButtons() {
        extraButtons.clear();
        String buttonUrl = getConfigService().getPropertyValueAsString(KFSConstants.EXTERNALIZABLE_IMAGES_URL_KEY);
        ContractsGrantsInvoiceDocument cgInvoiceDocument = (ContractsGrantsInvoiceDocument) getDocument();
        DocumentHelperService docHelperService = SpringContext.getBean(DocumentHelperService.class);
        ContractsGrantsInvoiceDocumentPresentationController presoController =
                (ContractsGrantsInvoiceDocumentPresentationController) docHelperService
                        .getDocumentPresentationController(cgInvoiceDocument);
        ContractsGrantsInvoiceDocumentAuthorizer documentAuthorizer =
                (ContractsGrantsInvoiceDocumentAuthorizer) docHelperService.getDocumentAuthorizer(cgInvoiceDocument);
        final Person user = GlobalVariables.getUserSession().getPerson();

        if (presoController.canErrorCorrect(cgInvoiceDocument)
                && documentAuthorizer.canErrorCorrect(cgInvoiceDocument, user)) {
            extraButtons.add(generateErrorCorrectionButton());
        }
        if (getDocumentActions().containsKey(KRADConstants.KUALI_ACTION_CAN_EDIT)
                && presoController.canProrate(cgInvoiceDocument)) {
            addExtraButton(ArConstants.PRORATE_BUTTON_METHOD, buttonUrl + ArConstants.PRORATE_BUTTON_FILE_NAME,
                    ArConstants.PRORATE_BUTTON_ALT_TEXT);
        }
        //CU customization: backport FINP-5292
        if (editingMode.containsKey(ArAuthorizationConstants.ContractsGrantsInvoiceDocumentEditMode.UPDATE_FINAL_BILL_INDICATOR)) {
            final String label = MessageFormat.format("{0} Final Bill", cgInvoiceDocument.getInvoiceGeneralDetail().isFinalBillIndicator() ? "Clear" : "Set");
            addExtraButton(ArConstants.UPDATE_FINAL_BILL_IND_BUTTON_METHOD, "", label);
        }
        return extraButtons;
    }

    @Override
    protected String getDefaultDocumentTypeName() {
        return ArConstants.ArDocumentTypeCodes.CONTRACTS_GRANTS_INVOICE;
    }

    public ContractsGrantsInvoiceDocument getContractsGrantsInvoiceDocument() {
        return (ContractsGrantsInvoiceDocument) getDocument();
    }

    public KualiDecimal getCurrentTotal() {
        return getContractsGrantsInvoiceDocument().getInvoiceGeneralDetail().getTotalAmountBilledToDate()
                .subtract(getContractsGrantsInvoiceDocument().getInvoiceGeneralDetail().getTotalPreviouslyBilled());
    }

    public boolean isShowTransmissionDateButton() {
        return getEditingMode()
                .containsKey(ArAuthorizationConstants.ContractsGrantsInvoiceDocumentEditMode.MODIFY_TRANSMISSION_DATE);
    }

    public boolean isSendIndicatorSelectable() {
        return getEditingMode()
                .containsKey(ArAuthorizationConstants.ContractsGrantsInvoiceDocumentEditMode.MODIFY_TRANSMISSION_DATE)
                || getDocumentActions().containsKey(KRADConstants.KUALI_ACTION_CAN_EDIT);
    }

    public boolean isShowTransmissionButton() {
        return getEditingMode()
                .containsKey(ArAuthorizationConstants.ContractsGrantsInvoiceDocumentEditMode.MODIFY_TRANSMISSION_DATE);
    }

    // replace parent class method to remove filling defaults from OrganizationAccountingDefault.
    // Accounting lines for CINV are auto created based on CG Inv Object Code,
    // none of the details populated in the superclass method are used.
    @Override
    protected SourceAccountingLine createNewSourceAccountingLine(AccountingDocument financialDocument) {
        return new CustomerInvoiceDetail();
    }
}
