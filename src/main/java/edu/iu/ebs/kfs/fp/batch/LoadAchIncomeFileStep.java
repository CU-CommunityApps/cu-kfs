package edu.iu.ebs.kfs.fp.batch;

import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.iu.ebs.kfs.fp.batch.service.AdvanceDepositService;

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
public class LoadAchIncomeFileStep extends AbstractStep {
	private AdvanceDepositService advanceDepositService;
	
	public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        advanceDepositService.cleanTransactionsTable();
        advanceDepositService.loadFile();
		return true;
	}

    public void setAdvanceDepositService(AdvanceDepositService advanceDepositService) {
        this.advanceDepositService = advanceDepositService;
    }

	
}
