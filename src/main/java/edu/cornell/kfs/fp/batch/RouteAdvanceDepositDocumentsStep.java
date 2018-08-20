package edu.cornell.kfs.fp.batch;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.fp.batch.service.AdvanceDepositService;
/**
 * Portions Modified 04/2016 and Copyright Cornell University
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 Copyright Indiana University
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class RouteAdvanceDepositDocumentsStep extends AbstractStep {

	private static final Logger LOG = LogManager.getLogger(RouteAdvanceDepositDocumentsStep.class);
    private AdvanceDepositService advanceDepositService;

    /**
     * @see org.kuali.kfs.kns.bo.Step#execute(String, Date)
     */
    public boolean execute(String jobName, Date jobRunDate) {
        // put a delay in here to workaround locking exception happening with ach income advance deposit approve and indexing
        try {
            Thread.sleep(300000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return advanceDepositService.routeAdvanceDepositDocuments();
    }

    public void setAdvanceDepositService(AdvanceDepositService advanceDepositService) {
        this.advanceDepositService = advanceDepositService;
    }

}
