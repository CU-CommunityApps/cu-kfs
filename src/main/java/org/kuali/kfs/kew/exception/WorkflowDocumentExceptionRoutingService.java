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
package org.kuali.kfs.kew.exception;

import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.ksb.messaging.PersistedMessage;

/**
 * ====
 * CU Customization:
 * Class added in support of the FINP-7647 backport from the 2021-09-30 release.
 * This overlay should be removed once we upgrade to the 2021-09-30 release or later.
 * ====
 */
public interface WorkflowDocumentExceptionRoutingService {

    void placeInExceptionRouting(String errorMessage, PersistedMessage persistedMessage, String documentId) throws
            Exception;

    DocumentRouteHeaderValue placeInExceptionRouting(Throwable throwable, PersistedMessage persistedMessage,
            String documentId) throws Exception;

    void placeInExceptionRoutingLastDitchEffort(Throwable throwable, PersistedMessage persistedMessage,
            String documentId) throws Exception;
}