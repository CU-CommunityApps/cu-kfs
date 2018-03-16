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

public enum SubObjectCodeFixture {
    SO_109("IT", "1023715", "4020", "109", true);

    public final String chartOfAccountsCode;
    public final String accountNumber;
    public final String objectCode;
    public final String subObjectCode;
    public final boolean active;

    private SubObjectCodeFixture(String chartOfAccountsCode, String accountNumber,String objectCode,String subObjectCode, boolean active) {
        this.chartOfAccountsCode = chartOfAccountsCode;
        this.accountNumber = accountNumber;
        this.objectCode = objectCode;
        this.subObjectCode = subObjectCode;
        this.active = active;
    }
}
