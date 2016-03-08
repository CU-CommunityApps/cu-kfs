package edu.iu.ebs.kfs.fp.batch.service;

import org.kuali.kfs.sys.batch.FlatFileDataHandler;
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
public interface AdvanceDepositService extends FlatFileDataHandler {
    
    
    /** 
     * This method will read ,parse, validate and load the ach income files. 
     */
	public boolean  loadFile();

	/**
	 * This method will create Advance deposit documents for the ach income transactions
	 */
	public void createDocuments();
	
	public boolean routeAdvanceDepositDocuments();
	
	/**
	 *  This method remove all the ach Income transaction rows from the transaction load table.
	 */
	public void cleanTransactionsTable();
}
