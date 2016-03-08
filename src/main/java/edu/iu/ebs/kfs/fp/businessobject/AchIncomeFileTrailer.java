package edu.iu.ebs.kfs.fp.businessobject;

import java.util.LinkedHashMap;

import org.kuali.kfs.module.ar.ArPropertyConstants;
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
public class AchIncomeFileTrailer extends TransientBusinessObjectBase  {
    private String interchangeControlNumber;
    private int totalGroups;
    
    
    public String getInterchangeControlNumber() {
        return interchangeControlNumber;
    }

    public void setInterchangeControlNumber(String interchangeControlNumber) {
        this.interchangeControlNumber = interchangeControlNumber;
    }
    
    public int getTotalGroups() {
        return totalGroups;
    }

    public void setTotalGroups(int totalGroups) {
        this.totalGroups = totalGroups;
    }

    
    protected LinkedHashMap toStringMapper_RICE20_REFACTORME() {
        LinkedHashMap pkMap = new LinkedHashMap();
        pkMap.put("InterchangeControlNumber", interchangeControlNumber);
        return pkMap;
    }
}
