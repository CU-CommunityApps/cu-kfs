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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "bank_acct")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksBankAccountDTO {

    private String id;
    private String bank_name;
    private String bank_acct_num;
    private String validation_file;
    private String ach_email;
    private String routing_num;
    private String acct;
    private String bank_acct_alias;
    private String bank_acct_type;
    private String authorized;
    private String acct_company;
    private String swift_code;
    private String name_on_acct;
    private PaymentWorksBankAddressDTO bank_address;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getBank_name() {
        return bank_name;
    }
    
    public void setBank_name(String bank_name) {
        this.bank_name = bank_name;
    }
    
    public String getBank_acct_num() {
        return bank_acct_num;
    }
    
    public void setBank_acct_num(String bank_acct_num) {
        this.bank_acct_num = bank_acct_num;
    }
    
    public String getValidation_file() {
        return validation_file;
    }
    
    public void setValidation_file(String validation_file) {
        this.validation_file = validation_file;
    }
    
    public String getAch_email() {
        return ach_email;
    }
    
    public void setAch_email(String ach_email) {
        this.ach_email = ach_email;
    }
    
    public String getRouting_num() {
        return routing_num;
    }
    
    public void setRouting_num(String routing_num) {
        this.routing_num = routing_num;
    }
    
    public String getAcct() {
        return acct;
    }
    
    public void setAcct(String acct) {
        this.acct = acct;
    }
    
    public String getBank_acct_alias() {
        return bank_acct_alias;
    }
    
    public void setBank_acct_alias(String bank_acct_alias) {
        this.bank_acct_alias = bank_acct_alias;
    }
    
    public String getBank_acct_type() {
        return bank_acct_type;
    }
    
    public void setBank_acct_type(String bank_acct_type) {
        this.bank_acct_type = bank_acct_type;
    }
    
    public String getAuthorized() {
        return authorized;
    }
    
    public void setAuthorized(String authorized) {
        this.authorized = authorized;
    }
    
    public String getAcct_company() {
        return acct_company;
    }
    
    public void setAcct_company(String acct_company) {
        this.acct_company = acct_company;
    }
    
    public String getSwift_code() {
        return swift_code;
    }
    
    public void setSwift_code(String swift_code) {
        this.swift_code = swift_code;
    }
    
    public String getName_on_acct() {
        return name_on_acct;
    }
    
    public void setName_on_acct(String name_on_acct) {
        this.name_on_acct = name_on_acct;
    }
    
    public PaymentWorksBankAddressDTO getBank_address() {
        return bank_address;
    }
    
    public void setBank_address(PaymentWorksBankAddressDTO bank_address) {
        this.bank_address = bank_address;
    }

}
