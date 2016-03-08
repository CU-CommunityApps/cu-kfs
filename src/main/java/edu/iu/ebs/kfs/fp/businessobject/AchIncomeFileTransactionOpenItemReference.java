package edu.iu.ebs.kfs.fp.businessobject;

import org.kuali.rice.core.api.util.type.KualiDecimal;

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
public class AchIncomeFileTransactionOpenItemReference extends
		AbstractAchIncomeFileDataElement {
    public KualiDecimal invoiceAmount;
	public KualiDecimal netAmount;

	public KualiDecimal getInvoiceAmount() {
		return invoiceAmount;
	}

	public void setInvoiceAmount(KualiDecimal invoiceAmount) {
		this.invoiceAmount = invoiceAmount;
	}

	public KualiDecimal getNetAmount() {
		return netAmount;
	}

	public void setNetAmount(KualiDecimal netAmount) {
		this.netAmount = netAmount;
	}
}
