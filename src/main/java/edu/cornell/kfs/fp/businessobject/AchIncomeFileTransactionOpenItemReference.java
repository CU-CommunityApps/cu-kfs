package edu.cornell.kfs.fp.businessobject;

import org.kuali.kfs.core.api.util.type.KualiDecimal;
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
public class AchIncomeFileTransactionOpenItemReference extends TransientBusinessObjectBase {
	private String type;
	private String invoiceNumber;
	private KualiDecimal invoiceAmount;
	private KualiDecimal netAmount;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}

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
