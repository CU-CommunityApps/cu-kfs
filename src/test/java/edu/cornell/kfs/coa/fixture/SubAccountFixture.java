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
package edu.cornell.kfs.coa.fixture;

public enum SubAccountFixture {
    SA_70170("IT", "R583805", "70170", true),
    SA_NONCA("IT", "R589966", "NONCA", true),
    SA_97601("IT", "1023715", "97601", true),
    SA_SHAN("CS", "J801000", "SHAN", true);

    public final String chartOfAccountsCode;
    public final String accountNumber;
    public final String subAccountNumber;
    public final boolean active;

    private SubAccountFixture(String chartOfAccountsCode, String accountNumber, String subAccountNumber, boolean active) {
        this.chartOfAccountsCode = chartOfAccountsCode;
        this.accountNumber = accountNumber;
        this.subAccountNumber = subAccountNumber;
        this.active = active;
    }
}
