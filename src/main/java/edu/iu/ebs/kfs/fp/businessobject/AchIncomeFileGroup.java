package edu.iu.ebs.kfs.fp.businessobject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.kuali.rice.krad.bo.TransientBusinessObjectBase;
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

public class AchIncomeFileGroup extends TransientBusinessObjectBase {
    private String groupControlNumber;
    private String groupFunctionIdentifierCode;
    private List<AchIncomeFileTransactionSet> transactionSet;
    private AchIncomeFileGroupTrailer groupTrailer;
    

    public AchIncomeFileGroup() {
        this.transactionSet = new ArrayList<AchIncomeFileTransactionSet>();
    }
    
    
  
    public String getGroupControlNumber() {
        return groupControlNumber;
    }



    public void setGroupControlNumber(String groupControlNumber) {
        this.groupControlNumber = groupControlNumber;
    }



    public String getGroupFunctionIdentifierCode() {
        return groupFunctionIdentifierCode;
    }

    public void setGroupFunctionIdentifierCode(String groupFunctionIdentifierCode) {
        this.groupFunctionIdentifierCode = groupFunctionIdentifierCode;
    }

 
    public AchIncomeFileGroupTrailer getGroupTrailer() {
        return groupTrailer;
    }



    public void setGroupTrailer(AchIncomeFileGroupTrailer groupTrailer) {
        this.groupTrailer = groupTrailer;
    }
   
    public List<AchIncomeFileTransactionSet> getTransactionSet() {
        return transactionSet;
    }



    public void setTransactionSet(List<AchIncomeFileTransactionSet> transactionSet) {
        this.transactionSet = transactionSet;
    }



    
    protected LinkedHashMap toStringMapper_RICE20_REFACTORME() {
        // TODO Auto-generated method stub
        return null;
    }
}
