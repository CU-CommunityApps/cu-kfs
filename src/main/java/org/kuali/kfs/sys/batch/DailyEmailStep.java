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

import org.kuali.kfs.kew.mail.service.ActionListEmailService;

import java.util.Date;

/**
 * CU Customization: Backported the version of this class from the 2021-04-01 financials patch.
 * 
 * Batch step implementation for the Daily Email
 */
public class DailyEmailStep extends AbstractStep {

    private ActionListEmailService actionListEmailService;

    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        actionListEmailService.sendDailyReminder();
        return true;
    }

    public void setActionListEmailService(ActionListEmailService actionListEmailService) {
        this.actionListEmailService = actionListEmailService;
    }
}
