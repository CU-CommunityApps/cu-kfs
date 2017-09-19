/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2014 The Kuali Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cornell.kfs.paymentworks.xmlObjects;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.paymentworks.PaymentWorksConstants;

public class PaymentWorksSupplierUploadDTO {

    private String vendorNum;
    private String supplierName;
    private String siteCode;
    private String Address1;
    private String Address2;
    private String City;
    private String State;
    private String Country;
    private String Zipcode;
    private String tin;
    private String contactEmail;

    private boolean sendToPaymentWorks;

    public String getVendorNum() {
        return vendorNum;
    }

    public void setVendorNum(String vendorNum) {
        this.vendorNum = vendorNum;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSiteCode() {
        return siteCode;
    }

    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }

    public String getAddress1() {
        return Address1;
    }

    public void setAddress1(String address1) {
        Address1 = address1;
    }

    public String getAddress2() {
        return Address2;
    }

    public void setAddress2(String address2) {
        Address2 = address2;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        Country = country;
    }

    public String getZipcode() {
        return Zipcode;
    }

    public void setZipcode(String zipcode) {
        Zipcode = zipcode;
    }

    public String getTin() {
        return tin;
    }

    public void setTin(String tin) {
        this.tin = tin;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public boolean isSendToPaymentWorks() {
        return sendToPaymentWorks;
    }

    public void setSendToPaymentWorks(boolean sendToPaymentWorks) {
        this.sendToPaymentWorks = sendToPaymentWorks;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(getVendorNum());
        sb.append(KFSConstants.COMMA);
        sb.append(StringUtils.defaultIfEmpty(getSiteCode(), ""));
        sb.append(KFSConstants.COMMA);
        sb.append(PaymentWorksConstants.DOUBLE_QUOTE);
        sb.append(StringUtils.defaultIfEmpty(getSupplierName(), ""));
        sb.append(PaymentWorksConstants.DOUBLE_QUOTE);
        sb.append(KFSConstants.COMMA);
        sb.append(PaymentWorksConstants.DOUBLE_QUOTE);
        sb.append(StringUtils.defaultIfEmpty(getAddress1(), ""));
        sb.append(PaymentWorksConstants.DOUBLE_QUOTE);
        sb.append(KFSConstants.COMMA);
        sb.append(PaymentWorksConstants.DOUBLE_QUOTE);
        sb.append(StringUtils.defaultIfEmpty(getAddress2(), ""));
        sb.append(PaymentWorksConstants.DOUBLE_QUOTE);
        sb.append(KFSConstants.COMMA);
        sb.append(PaymentWorksConstants.DOUBLE_QUOTE);
        sb.append(StringUtils.defaultIfEmpty(getCity(), ""));
        sb.append(PaymentWorksConstants.DOUBLE_QUOTE);
        sb.append(KFSConstants.COMMA);
        sb.append(PaymentWorksConstants.DOUBLE_QUOTE);
        sb.append(StringUtils.defaultIfEmpty(getState(), ""));
        sb.append(PaymentWorksConstants.DOUBLE_QUOTE);
        sb.append(KFSConstants.COMMA);
        sb.append(PaymentWorksConstants.DOUBLE_QUOTE);
        sb.append(StringUtils.defaultIfEmpty(getCountry(), ""));
        sb.append(PaymentWorksConstants.DOUBLE_QUOTE);
        sb.append(KFSConstants.COMMA);
        sb.append(StringUtils.defaultIfEmpty(getZipcode(), ""));
        sb.append(KFSConstants.COMMA);
        sb.append(StringUtils.defaultIfEmpty(getTin(), ""));
        sb.append(KFSConstants.COMMA);
        sb.append(PaymentWorksConstants.DOUBLE_QUOTE);
        sb.append(StringUtils.defaultIfEmpty(getContactEmail(), ""));
        sb.append(PaymentWorksConstants.DOUBLE_QUOTE);

        return sb.toString();
    }

}
