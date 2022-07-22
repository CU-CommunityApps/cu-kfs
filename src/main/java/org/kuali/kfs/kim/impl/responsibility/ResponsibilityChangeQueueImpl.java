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
package org.kuali.kfs.kew.impl.responsibility;

import org.apache.commons.collections4.CollectionUtils;
import org.kuali.kfs.kew.api.responsibility.ResponsibilityChangeQueue;
import org.kuali.kfs.kew.service.KEWServiceLocator;

import java.util.Set;

/**
 * Reference implementation of the {@code ResponsibilityChangeQueue}.
 */
public class ResponsibilityChangeQueueImpl implements ResponsibilityChangeQueue {

    @Override
    public void responsibilitiesChanged(final Set<String> responsibilityIds) {
        if (CollectionUtils.isNotEmpty(responsibilityIds)) {
            KEWServiceLocator.getActionRequestService().updateActionRequestsForResponsibilityChange(responsibilityIds);
        }
    }
}
