package edu.cornell.kfs.fp.businessobject;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;
/**
 Portions Modified 04/2016 and Copyright Cornell University

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
public class AchIncomeFileTransactionSetTrailer extends TransientBusinessObjectBase {
     
    private String transactionSetControlNumber;
    
    public String getTransactionSetControlNumber() {
        return transactionSetControlNumber;
    }

    public void setTransactionSetControlNumber(String transactionSetControlNumber) {
        this.transactionSetControlNumber = transactionSetControlNumber;
    }

}
