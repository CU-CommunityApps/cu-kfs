/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
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
package org.kuali.kfs.module.purap.document.authorization;

import org.kuali.kfs.kim.api.KimConstants;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapAuthorizationConstants;
import org.kuali.kfs.module.purap.businessobject.SensitiveData;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.kfs.module.purap.identity.PurapKimAttributes;
import org.kuali.kfs.module.purap.service.SensitiveDataService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.authorization.AccountingDocumentAuthorizerBase;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PurchasingAccountsPayableTransactionalDocumentAuthorizerBase extends AccountingDocumentAuthorizerBase {

    @Override
    protected void addRoleQualification(final Object businessObject, final Map<String, String> attributes) {
        super.addRoleQualification(businessObject, attributes);
        attributes.put(PurapKimAttributes.DOCUMENT_SENSITIVE, "false");
        final PurchasingAccountsPayableDocument purapDoc = (PurchasingAccountsPayableDocument) businessObject;
        if (purapDoc.getAccountsPayablePurchasingDocumentLinkIdentifier() != null) {
            final List<SensitiveData> sensitiveDataList = SpringContext.getBean(SensitiveDataService.class)
                    .getSensitiveDatasAssignedByRelatedDocId(
                            purapDoc.getAccountsPayablePurchasingDocumentLinkIdentifier());
            if (ObjectUtils.isNotNull(sensitiveDataList) && !sensitiveDataList.isEmpty()) {
                final StringBuffer sensitiveDataCodes = new StringBuffer();
                for (final SensitiveData sensitiveData : sensitiveDataList) {
                    if (ObjectUtils.isNotNull(sensitiveData)) {
                        sensitiveDataCodes.append(sensitiveData.getSensitiveDataCode()).append(";");
                    }
                }
                if (sensitiveDataCodes.length() > 0) {
                    attributes.put(PurapKimAttributes.DOCUMENT_SENSITIVE, "true");
                    attributes.put(PurapKimAttributes.SENSITIVE_DATA_CODE, sensitiveDataCodes.toString().substring(0,
                            sensitiveDataCodes.length() - 1));
                    attributes.put(PurapKimAttributes.ACCOUNTS_PAYABLE_PURCHASING_DOCUMENT_LINK_IDENTIFIER,
                            purapDoc.getAccountsPayablePurchasingDocumentLinkIdentifier().toString());
                }
                // KFSUPGRADE-346
            } else if (purapDoc.isSensitive()) {
                attributes.put(PurapKimAttributes.DOCUMENT_SENSITIVE, "true");
            }
        }
    }

    /**
     * Overridden to allow vendor edit if doc can be edited (to allow for the POA to override and restrict vendor edit).
     */
    @Override
    public Set<String> getDocumentActions(
            final Document document, final Person user,
            final Set<String> documentActionsFromPresentationController) {

        final Set<String> documentActionsToReturn = super.getDocumentActions(document, user,
                documentActionsFromPresentationController);

        if (documentActionsToReturn.contains(KRADConstants.KUALI_ACTION_CAN_EDIT)) {
            documentActionsToReturn.add(PurapAuthorizationConstants.CAN_EDIT_VENDOR);
        }

        return documentActionsToReturn;
    }

    @Override
    public boolean canEditDocumentOverview(final Document document, final Person user) {
        return isAuthorizedByTemplate(document, KFSConstants.CoreModuleNamespaces.KFS,
            KimConstants.PermissionTemplateNames.EDIT_DOCUMENT, user.getPrincipalId());
    }

}
