package edu.cornell.kfs.fp.businessobject;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

import java.util.ArrayList;
import java.util.List;
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
public class AchIncomeFileTransactionSet extends TransientBusinessObjectBase {
    private String transactionSetControlNumber;
    private AchIncomeFileTransactionSetTrailer transactionSetTrailer;
    private List<AchIncomeFileTransaction> transactionGuts;

    public AchIncomeFileTransactionSet() {
        this.transactionGuts = new ArrayList<>();
    }
    
    public String getTransactionSetControlNumber() {
        return transactionSetControlNumber;
    }

    public void setTransactionSetControlNumber(String transactionSetControlNumber) {
        this.transactionSetControlNumber = transactionSetControlNumber;
    }
    
    public AchIncomeFileTransactionSetTrailer getTransactionSetTrailer() {
        return transactionSetTrailer;
    }

    public void setTransactionSetTrailer(AchIncomeFileTransactionSetTrailer transactionSetTrailer) {
        this.transactionSetTrailer = transactionSetTrailer;
    }
    
    public List<AchIncomeFileTransaction> getTransactionGuts() {
        return transactionGuts;
    }

    public void setTransactionGuts(List<AchIncomeFileTransaction> transactionGuts) {
        this.transactionGuts = transactionGuts;
    }

}
