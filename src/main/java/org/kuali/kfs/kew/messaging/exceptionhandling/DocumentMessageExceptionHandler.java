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
package org.kuali.kfs.kew.messaging.exceptionhandling;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kew.api.WorkflowRuntimeException;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.ksb.messaging.PersistedMessage;
import org.kuali.kfs.ksb.service.KSBServiceLocator;
import org.kuali.kfs.ksb.messaging.exceptionhandling.DefaultMessageExceptionHandler;
import org.kuali.kfs.ksb.messaging.exceptionhandling.MessageExceptionHandler;

/**
 * ====
 * CU Customization:
 * Copied this class from an intermediate portion of the FINP-7647 changes from the 2021-09-30 release,
 * and updated it as needed for compatibility with older KFS releases.
 * This overlay should be removed once we upgrade to the 2021-09-30 release or later.
 * ====
 * 
 * A {@link MessageExceptionHandler} which handles putting documents into exception routing.
 */
public class DocumentMessageExceptionHandler extends DefaultMessageExceptionHandler {

    @Override
    protected void placeInException(Throwable throwable, PersistedMessage message) throws Exception {
        KEWServiceLocator.getExceptionRoutingService().placeInExceptionRouting(throwable, message, getDocumentId(message));
    }

    @Override
    public void handleExceptionLastDitchEffort(Throwable throwable, PersistedMessage message, Object service) throws Exception {
        KEWServiceLocator.getExceptionRoutingService().placeInExceptionRoutingLastDitchEffort(throwable, message, getDocumentId(message));
    }

    @Override
    protected void scheduleExecution(Throwable throwable, PersistedMessage message) throws Exception {
        String description = "DocumentId: " + getDocumentId(message);
        KSBServiceLocator.getExceptionRoutingService().scheduleExecution(throwable, message, description);
    }

    protected String getDocumentId(PersistedMessage message) {
        if (StringUtils.isNotEmpty(message.getValue1())) {
            return message.getValue1();
        }
        throw new WorkflowRuntimeException("Unable to put this message in exception routing service name " + message.getServiceName());
    }
}
