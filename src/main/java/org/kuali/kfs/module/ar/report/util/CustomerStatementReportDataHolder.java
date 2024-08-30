/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.module.ar.report.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * To group and hold the data presented to working reports of extract process
 */
public class CustomerStatementReportDataHolder {

    private Map<String, String> invoice;
    private Map<String, String> customer;
    private Map<String, String> sysinfo;
    private List<CustomerStatementDetailReportDataHolder> details;

    private Map<String, Object> reportData;

    public static final String KEY_OF_INVOICE_ENTRY = "invoice";
    public static final String KEY_OF_CUSTOMER_ENTRY = "customer";
    public static final String KEY_OF_SYSINFO_ENTRY = "sysinfo";
    public static final String KEY_OF_DETAILS_ENTRY = "details";

    public CustomerStatementReportDataHolder() {
        //this(null);

        this.invoice = new HashMap<>();
        this.customer = new HashMap<>();
        this.sysinfo = new HashMap<>();
        this.details = new ArrayList<>();

        this.reportData = new HashMap<>();
    }

    public Map<String, String> getInvoice() {
        return invoice;
    }

    public void setInvoice(Map<String, String> invoice) {
        this.invoice = invoice;
    }

    public Map<String, String> getCustomer() {
        return customer;
    }

    public void setCustomer(Map<String, String> customer) {
        this.customer = customer;
    }

    public Map<String, String> getSysinfo() {
        return sysinfo;
    }

    public void setSysinfo(Map<String, String> sysinfo) {
        this.sysinfo = sysinfo;
    }

    public Map<String, Object> getReportData() {
        reportData.put(KEY_OF_INVOICE_ENTRY, invoice);
        reportData.put(KEY_OF_CUSTOMER_ENTRY, customer);
        reportData.put(KEY_OF_SYSINFO_ENTRY, sysinfo);
        reportData.put(KEY_OF_DETAILS_ENTRY, details);
        return reportData;
    }

    public void setReportData(Map<String, Object> reportData) {
        this.reportData = reportData;
    }

    @Override
    public String toString() {
        return this.getReportData().toString();
    }

    public List<CustomerStatementDetailReportDataHolder> getDetails() {
        return details;
    }

    public void setDetails(List<CustomerStatementDetailReportDataHolder> details) {
        this.details = details;
    }

}
