package edu.cornell.kfs.fp.businessobject;

import org.kuali.rice.krad.bo.TransientBusinessObjectBase;

import java.util.ArrayList;
import java.util.List;
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

public class AchIncomeFileGroup extends TransientBusinessObjectBase {
    private String groupControlNumber;
    private String groupFunctionIdentifierCode;
    private List<AchIncomeFileTransactionSet> transactionSets;
    private AchIncomeFileGroupTrailer groupTrailer;

    public AchIncomeFileGroup() {
        this.transactionSets = new ArrayList<>();
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

    public List<AchIncomeFileTransactionSet> getTransactionSets() {
        return transactionSets;
    }

    public void setTransactionSets(List<AchIncomeFileTransactionSet> transactionSets) {
        this.transactionSets = transactionSets;
    }
}
