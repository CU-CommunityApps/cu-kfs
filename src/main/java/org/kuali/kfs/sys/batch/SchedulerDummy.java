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
package org.kuali.kfs.sys.batch;

import org.quartz.impl.StdScheduler;

/*
 * CU Customization: Added extra method overrides to fix certain areas of base code
 * that still depend upon the scheduler when Quartz is disabled.
 */
public class SchedulerDummy extends StdScheduler {
    
    public static final String INSTANCE_ID = "KFSCustomScheduler";

    public SchedulerDummy() {
        super(null);
    }

    // CU Customization: Override getSchedulerInstanceId() method to make it usable.
    @Override
    public String getSchedulerInstanceId() {
        return INSTANCE_ID;
    }

}
