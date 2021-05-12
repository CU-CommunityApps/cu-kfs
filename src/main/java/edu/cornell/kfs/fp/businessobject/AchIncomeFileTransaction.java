package edu.cornell.kfs.fp.businessobject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import edu.cornell.kfs.fp.CuFPConstants;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
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
public class AchIncomeFileTransaction extends TransientBusinessObjectBase {

    private KualiDecimal transactionAmount;
    private String creditDebitIndicator;
    private Date effectiveDate;
    private String companyId;
    private String paymentMethodCode;
    private AchIncomeFileTransactionTrace trace;
    private List<AchIncomeFileTransactionReference> references;
    private List<AchIncomeFileTransactionDateTime> dateTimes;
    private List<AchIncomeFileTransactionPayerOrPayeeName> payerOrPayees;
    private List<AchIncomeFileTransactionNote> notes;
    private List<AchIncomeFileTransactionOpenItemReference> openItemReferences;
    private AchIncomeFileTransactionPremiumPayersAdminsContact premiumAdminsContact;
    private AchIncomeFileTransactionPremiumReceiverName premiumReceiverName;

    public AchIncomeFileTransaction() {
        this.references = new ArrayList<>();
        this.dateTimes = new ArrayList<>();
        this.payerOrPayees = new ArrayList<>();
        this.notes = new ArrayList<>();
        this.openItemReferences = new ArrayList<>();
    }

    public KualiDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(KualiDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getCreditDebitIndicator() {
        return creditDebitIndicator;
    }

    public void setCreditDebitIndicator(String creditDebitIndicator) {
        this.creditDebitIndicator = creditDebitIndicator;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public AchIncomeFileTransactionTrace getTrace() {
        return trace;
    }

    public void setTrace(AchIncomeFileTransactionTrace trace) {
        this.trace = trace;
    }

    public List<AchIncomeFileTransactionReference> getReferences() {
        return references;
    }

    public void setReferences(List<AchIncomeFileTransactionReference> references) {
        this.references = references;
    }

    public List<AchIncomeFileTransactionDateTime> getDateTimes() {
        return dateTimes;
    }

    public void setDateTimes(List<AchIncomeFileTransactionDateTime> dateTimes) {
        this.dateTimes = dateTimes;
    }

    public List<AchIncomeFileTransactionPayerOrPayeeName> getPayerOrPayees() {
        return payerOrPayees;
    }

    public void setPayerOrPayees(
            List<AchIncomeFileTransactionPayerOrPayeeName> payerOrPayees) {
        this.payerOrPayees = payerOrPayees;
    }

    public List<AchIncomeFileTransactionNote> getNotes() {
        return notes;
    }

    public void setNotes(List<AchIncomeFileTransactionNote> notes) {
        this.notes = notes;
    }

    public List<AchIncomeFileTransactionOpenItemReference> getOpenItemReferences() {
        return openItemReferences;
    }

    public void setOpenItemReferences(
            List<AchIncomeFileTransactionOpenItemReference> openItemReferences) {
        this.openItemReferences = openItemReferences;
    }

    public AchIncomeFileTransactionPremiumPayersAdminsContact getPremiumAdminsContact() {
        return premiumAdminsContact;
    }

    public void setPremiumAdminsContact(AchIncomeFileTransactionPremiumPayersAdminsContact premiumAdminsContact) {
        this.premiumAdminsContact = premiumAdminsContact;
    }

    public AchIncomeFileTransactionPremiumReceiverName getPremiumReceiverName() {
        return premiumReceiverName;
    }

    public void setPremiumReceiverName(AchIncomeFileTransactionPremiumReceiverName premiumReceiverName) {
        this.premiumReceiverName = premiumReceiverName;
    }

    public String getPaymentMethodCode() {
        return paymentMethodCode;
    }

    public void setPaymentMethodCode(String paymentMethodCode) {
        this.paymentMethodCode = paymentMethodCode;
    }

    public String getPayerName() {
        String payerName = null;

        for (AchIncomeFileTransactionPayerOrPayeeName payerOrPayeeName : getPayerOrPayees()) {
            if (StringUtils.equals(CuFPConstants.AchIncomeFileTransactionPayerOrPayeeName.PAYER_TYPE_PR, payerOrPayeeName.getType())) {
                payerName = payerOrPayeeName.getName();
                break;
            }
        }

        if (StringUtils.isBlank(payerName)) {
            if (getPremiumReceiverName() != null) {
                payerName = getPremiumReceiverName().getName();
            } else {
                payerName = CuFPConstants.AchIncomeFileTransaction.PAYER_NOT_IDENTIFIED;
            }
        }

        return payerName;
    }

}
