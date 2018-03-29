/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2017 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.cornell.kfs.module.purap.businessobject;

import org.kuali.kfs.module.purap.businessobject.PurApAccountingLineBase;
import org.kuali.kfs.module.purap.businessobject.RequisitionItem;

import java.math.BigDecimal;

/**
 * Version of RequisitionAccount specifically for testing.
 *
 * This replacement for RequistionAccount avoids the Spring call in getAccountLinePercent from that class, but can
 * still call super.getAccountLinePercent() to get the initial accountLinePercent from PurApAccountingLineBase which it
 * couldn't do if it extended RequisitionAccount to override getAccountLinePercent().
 */
public class TestableRequisitionAccount extends PurApAccountingLineBase {

    public TestableRequisitionAccount() {
        this.setSequenceNumber(0);
    }

    public RequisitionItem getRequisitionItem() {
        return super.getPurapItem();
    }

    /**
     * @param requisitionItem The requisitionItem to set.
     * @deprecated
     */
    @Deprecated
    public void setRequisitionItem(RequisitionItem requisitionItem) {
        setPurapItem(requisitionItem);
    }


    @Override
    public BigDecimal getAccountLinePercent() {
        BigDecimal accountLinePercent = super.getAccountLinePercent();

        if (accountLinePercent == null || accountLinePercent.compareTo(BigDecimal.ZERO) == 0) {
            accountLinePercent = new BigDecimal(100);
            this.setAccountLinePercent(accountLinePercent);
        }

        return accountLinePercent;
    }

    /*
     * TODO: Temporary hack to avoid Spring call in setAccountNumber() method, without interfering
     * with existing tests that work around this issue. This should be removed once we standardize
     * a better way of avoiding this Spring call during micro-tests.
     */
    public static void setAccountNumberForMicroTest(TestableRequisitionAccount accountingLine, String accountNumber) {
        accountingLine.accountNumber = accountNumber;
    }

}
