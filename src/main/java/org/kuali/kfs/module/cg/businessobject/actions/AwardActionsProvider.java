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
package org.kuali.kfs.module.cg.businessobject.actions;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.datadictionary.Action;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.service.ContractsGrantsBillingUtilityService;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.actions.BusinessObjectActionsProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * CU Customization: Fixed an issue where the "Invoices" link's URL could end up malformed
 *                   if the KFS application URL does not end with "/fin".
 */
public class AwardActionsProvider extends BusinessObjectActionsProvider {

    private ConfigurationService configurationService;
    private ContractsGrantsBillingUtilityService contractsGrantsBillingUtilityService;

    @Override
    public List<Action> getActionLinks(final BusinessObjectBase businessObject, final Person user) {
        final List<Action> actions = super.getActionLinks(businessObject, user);
        if (contractsGrantsBillingUtilityService.isContractsGrantsBillingEnhancementActive()) {
            actions.add(getInvoicesAction(businessObject));
        }
        return actions;
    }

    private Action getInvoicesAction(final BusinessObjectBase bo) {
        final Award award = (Award) bo;
        final Map<String, String> params = new HashMap<>();
        params.put(KFSPropertyConstants.DOCUMENT_TYPE_NAME, ArConstants.ArDocumentTypeCodes.CONTRACTS_GRANTS_INVOICE);
        params.put(
                KewApiConstants.DOCUMENT_ATTRIBUTE_FIELD_PREFIX + KFSPropertyConstants.PROPOSAL_NUMBER,
                award.getProposalNumber()
        );
        String url = UrlFactory.parameterizeUrl(
                configurationService.getPropertyValueAsString(KRADConstants.WORKFLOW_DOCUMENTSEARCH_URL_KEY),
                params
        );
        // ==== CU Customization: Strip off the application URL, not the base URL. ====
        final String applicationUrl = configurationService.getPropertyValueAsString(KFSConstants.APPLICATION_URL_KEY);
        url = StringUtils.stripStart(url.replace(applicationUrl, ""), "/fin/");

        final Action invoicesAction = new Action("Invoices", "GET", url, "_blank");
        return invoicesAction;
    }

    public void setConfigurationService(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setContractsGrantsBillingUtilityService(final ContractsGrantsBillingUtilityService contractsGrantsBillingUtilityService) {
        this.contractsGrantsBillingUtilityService = contractsGrantsBillingUtilityService;
    }
}
