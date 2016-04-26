package edu.cornell.kfs.fp.businessobject;

import org.kuali.kfs.sys.batch.FlatFileData;
import org.kuali.kfs.sys.batch.FlatFileTransactionInformation;
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

public class AchIncomeFile extends TransientBusinessObjectBase implements FlatFileData {
    private String fileDate;
    private String fileTime;
    private String productionOrTestIndicator;
    private String interchangeControlNumber;
    private List<AchIncomeFileGroup> groups;
    private AchIncomeFileTrailer trailer;
    private FlatFileTransactionInformation fileTransactionInformation;
    private String emailMessageText;

    public AchIncomeFile() {
        this.groups = new ArrayList<AchIncomeFileGroup>();
    }

    public String getFileDate() {
        return fileDate;
    }

    public void setFileDate(String fileDate) {
        this.fileDate = fileDate;
    }

    public String getProductionOrTestIndicator() {
        return productionOrTestIndicator;
    }

    public void setProductionOrTestIndicator(String productionOrTestIndicator) {
        this.productionOrTestIndicator = productionOrTestIndicator;
    }

    public List<AchIncomeFileGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<AchIncomeFileGroup> groups) {
        this.groups = groups;
    }

    public String getFileTime() {
        return fileTime;
    }

    public void setFileTime(String fileTime) {
        this.fileTime = fileTime;
    }

    public FlatFileTransactionInformation getFlatFileTransactionInformation() {
        return this.fileTransactionInformation = new FlatFileTransactionInformation(this.getFileDate());

    }

    public String getEmailMessageText() {
        return emailMessageText;
    }

    public void setEmailMessageText(String emailMessageText) {
        this.emailMessageText = emailMessageText;
    }

    public String getInterchangeControlNumber() {
        return interchangeControlNumber;
    }

    public void setInterchangeControlNumber(String interchangeControlNumber) {
        this.interchangeControlNumber = interchangeControlNumber;
    }

    public AchIncomeFileTrailer getTrailer() {
        return trailer;
    }

    public void setTrailer(AchIncomeFileTrailer trailer) {
        this.trailer = trailer;
    }

}
