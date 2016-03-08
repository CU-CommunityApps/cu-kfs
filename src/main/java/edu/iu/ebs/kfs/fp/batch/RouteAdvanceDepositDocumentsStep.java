package edu.iu.ebs.kfs.fp.batch;

import java.util.Date;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.iu.ebs.kfs.fp.batch.service.AdvanceDepositService;
import edu.iu.ebs.kfs.fp.batch.service.impl.AbstractAdvanceDepositServiceBase;
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
    
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(RouteAdvanceDepositDocumentsStep.class);
    private AdvanceDepositService advanceDepositService;

    /**
     * @see org.kuali.kfs.sys.batch.Step#execute(String, Date)
     */
    public boolean execute(String jobName, Date jobRunDate) {
        // TODO: put a temporary delay in here to workaround locking exception happening with ach income advance deposit approve and indexing
        try {
            Thread.sleep(300000);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return advanceDepositService.routeAdvanceDepositDocuments();
    }

    public void setAdvanceDepositService(AdvanceDepositService advanceDepositService) {
        this.advanceDepositService = advanceDepositService;
    }

    
}
