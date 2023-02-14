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
package org.kuali.kfs.kew.actions;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.kuali.kfs.kew.actionrequest.ActionRequest;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.exception.InvalidActionTakenException;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kim.impl.identity.principal.Principal;

import java.util.List;

/**
 * CU Customization: Backported the critical portion of the FINP-8777 fix that adds the saveActionTaken() call.
 * This overlay can be removed when we upgrade to the 2022-10-19 financials patch.
 * ============
 * 
 * Simply records an action taken with an annotation.
 */
public class LogDocumentActionAction extends ActionBase {

    private static final Logger LOG = LogManager.getLogger();

    /**
     * @param rh        RouteHeader for the document upon which the action is taken.
     * @param principal User taking the action.
     */
    public LogDocumentActionAction(DocumentRouteHeaderValue rh, Principal principal) {
        super(KewApiConstants.ACTION_TAKEN_LOG_DOCUMENT_ACTION_CD, rh, principal);
    }

    /**
     * @param rh         RouteHeader for the document upon which the action is taken.
     * @param principal  User taking the action.
     * @param annotation User comment on the action taken
     */
    public LogDocumentActionAction(DocumentRouteHeaderValue rh, Principal principal, String annotation) {
        super(KewApiConstants.ACTION_TAKEN_LOG_DOCUMENT_ACTION_CD, rh, principal, annotation);
    }

    @Override
    public String validateActionRules() {
        // log action is always valid so return no error message
        return "";
    }

    @Override
    public String validateActionRules(List<ActionRequest> actionRequests) {
        // log action is always valid so return no error message
        return "";
    }

    /**
     * Records the non-routed document action. - Checks to make sure the document status allows the action. Records
     * the action.
     *
     * @throws InvalidActionTakenException
     */
    @Override
    public void recordAction() throws InvalidActionTakenException {
        ThreadContext.put("docId", getRouteHeader().getDocumentId());

        String errorMessage = validateActionRules();
        if (StringUtils.isNotEmpty(errorMessage)) {
            throw new InvalidActionTakenException(errorMessage);
        }

        LOG.debug("Logging document action");
        saveActionTaken();
        // LogDocumentAction should not contact the PostProcessor which is why we don't call notifyActionTaken
    }
}
