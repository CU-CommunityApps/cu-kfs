package edu.cornell.kfs.fp.batch.service;

import org.kuali.kfs.sys.batch.FlatFileDataHandler;
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
public interface AdvanceDepositService extends FlatFileDataHandler {

    /**
     * This method will read, parse, validate and load the ach income files.
     */
    void loadFile();

    /**
     * This method will create Advance deposit documents for the ach income transactions
     */
    void createDocuments();

    /**
     * This method retrieves all SAVED advance deposit documents and routes them to the next step in the routing path.
     *
     * @return True if the routing was performed successfully.  A runtime exception will be thrown if any errors occur while routing.
     */
    boolean routeAdvanceDepositDocuments();

    /**
     *  This method remove all the ach Income transaction rows from the transaction load table.
     */
    void cleanTransactionsTable();
}
