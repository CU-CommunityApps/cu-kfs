/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
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
package org.kuali.kfs.pdp.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.PostalCode;
import org.kuali.kfs.sys.businessobject.State;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;

public class ACHBank extends PersistableBusinessObjectBase implements MutableInactivatable {

    protected String bankRoutingNumber;
    protected String bankOfficeCode;
    protected String bankServiceNumber;
    protected String bankTypeCode;
    protected String bankNewRoutingNumber;
    protected String bankName;
    protected String bankStreetAddress;
    protected String bankCityName;
    protected String bankStateCode;
    protected String bankCountryCode;
    protected String bankZipCode;
    protected String bankPhoneAreaCode;
    protected String bankPhonePrefixNumber;
    protected String bankPhoneSuffixNumber;
    protected String bankInstitutionStatusCode;
    protected String bankDataViewCode;
    protected boolean active;

    protected State bankState;
    protected PostalCode postalCode;

    public ACHBank() {
        // we need country code to have a value for OJB foreign keys to other location types but it isn't exposed on
        // docs so it never gets set. setting a default value on the column in the db did not do what we want b/c
        // the ojb insert explicitly specifies every column it knows about. this will work for now.
        bankCountryCode = KFSConstants.COUNTRY_CODE_UNITED_STATES;
    }

    /**
     * This constructor takes a line of data from FedACH Directory file and populates the ACHBank object
     *
     * @param fileData line of data from the FedACH Directory file
     */
    public ACHBank(final String fileData) {
        this();
        // 074914274O0710003011020207000000000UNITED COMMERCE BANK 211 SOUTH COLLEGE AVENUE BLOOMINGTON IN474040000812336226511
        // Routing Number 9 1-9 The institution's routing number
        // Office Code 1 10 Main office or branch O=main B=branch
        // Servicing FRB Number 9 11-19 Servicing Fed's main office routing number
        // Record Type Code 1 20 The code indicating the ABA number to be used to route or send ACH items to the RFI
        // 0 = Institution is a Federal Reserve Bank
        // 1 = Send items to customer routing number
        // 2 = Send items to customer using new routing number field
        // Change Date 6 21-26 Date of last change to CRF information (MMDDYY)
        // New Routing Number 9 27-35 Institution's new routing number resulting from a merger or renumber
        // Customer Name 36 36-71 Commonly used abbreviated name
        // Address 36 72-107 Delivery address
        // City 20 108-127 City name in the delivery address
        // State Code 2 128-129 State code of the state in the delivery address
        // Zipcode 5 130-134 Zip code in the delivery address
        // Zipcode Extension 4 135-138 Zip code extension in the delivery address
        // Telephone Area Code 3 139-141 Area code of the CRF contact telephone number
        // Telephone Prefix Number 3 142-144 Prefix of the CRF contact telephone number
        // Telephone Suffix Number 4 145-148 Suffix of the CRF contact telephone number
        // Institution Status Code 1 149 Code is based on the customers receiver code
        // 1=Receives Gov/Comm
        // Data View Code 1 150 1=Current view
        // Filler 5 151-155 Spaces

        setBankRoutingNumber(getField(fileData, 1, 9));
        setBankOfficeCode(getField(fileData, 10, 1));
        setBankServiceNumber(getField(fileData, 11, 9));
        setBankTypeCode(getField(fileData, 20, 1));
        setBankNewRoutingNumber(getField(fileData, 27, 9));
        setBankName(getField(fileData, 36, 36));
        setBankStreetAddress(getField(fileData, 72, 36));
        setBankCityName(getField(fileData, 108, 20));
        setBankStateCode(getField(fileData, 128, 2));
        setBankZipCode(getField(fileData, 130, 5));
        setBankPhoneAreaCode(getField(fileData, 139, 3));
        setBankPhonePrefixNumber(getField(fileData, 142, 3));
        setBankPhoneSuffixNumber(getField(fileData, 145, 4));
        setBankInstitutionStatusCode(getField(fileData, 149, 1));
        setBankDataViewCode(getField(fileData, 150, 1));
        setActive(true);
        setBankCountryCode(KFSConstants.COUNTRY_CODE_UNITED_STATES);
    }

    protected String getField(final String data, final int startChar, final int length) {
        return data.substring(startChar - 1, startChar + length - 1).trim();
    }

    public String getBankRoutingNumber() {
        return bankRoutingNumber;
    }

    public void setBankRoutingNumber(final String bankRoutingNumber) {
        this.bankRoutingNumber = bankRoutingNumber;
    }

    public String getBankOfficeCode() {
        return bankOfficeCode;
    }

    public void setBankOfficeCode(final String bankOfficeCode) {
        this.bankOfficeCode = bankOfficeCode;
    }

    public String getBankServiceNumber() {
        return bankServiceNumber;
    }

    public void setBankServiceNumber(final String bankServiceNumber) {
        this.bankServiceNumber = bankServiceNumber;
    }

    public String getBankTypeCode() {
        return bankTypeCode;
    }

    public void setBankTypeCode(final String bankTypeCode) {
        this.bankTypeCode = bankTypeCode;
    }

    public String getBankNewRoutingNumber() {
        return bankNewRoutingNumber;
    }

    public void setBankNewRoutingNumber(final String bankNewRoutingNumber) {
        this.bankNewRoutingNumber = bankNewRoutingNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(final String bankName) {
        this.bankName = bankName;
    }

    public String getBankStreetAddress() {
        return bankStreetAddress;
    }

    public void setBankStreetAddress(final String bankStreetAddress) {
        this.bankStreetAddress = bankStreetAddress;
    }

    public String getBankCityName() {
        return bankCityName;
    }

    public void setBankCityName(final String bankCityName) {
        this.bankCityName = bankCityName;
    }

    public String getBankStateCode() {
        return bankStateCode;
    }

    public void setBankStateCode(final String bankStateCode) {
        this.bankStateCode = bankStateCode;
    }

    public String getBankZipCode() {
        return bankZipCode;
    }

    public void setBankZipCode(final String bankZipCode) {
        this.bankZipCode = bankZipCode;
    }

    public String getBankPhoneAreaCode() {
        return bankPhoneAreaCode;
    }

    public void setBankPhoneAreaCode(final String bankPhoneAreaCode) {
        this.bankPhoneAreaCode = bankPhoneAreaCode;
    }

    public String getBankPhonePrefixNumber() {
        return bankPhonePrefixNumber;
    }

    public void setBankPhonePrefixNumber(final String bankPhonePrefixNumber) {
        this.bankPhonePrefixNumber = bankPhonePrefixNumber;
    }

    public String getBankPhoneSuffixNumber() {
        return bankPhoneSuffixNumber;
    }

    public void setBankPhoneSuffixNumber(final String bankPhoneSuffixNumber) {
        this.bankPhoneSuffixNumber = bankPhoneSuffixNumber;
    }

    public String getBankInstitutionStatusCode() {
        return bankInstitutionStatusCode;
    }

    public void setBankInstitutionStatusCode(final String bankInstitutionStatusCode) {
        this.bankInstitutionStatusCode = bankInstitutionStatusCode;
    }

    public String getBankDataViewCode() {
        return bankDataViewCode;
    }

    public void setBankDataViewCode(final String bankDataViewCode) {
        this.bankDataViewCode = bankDataViewCode;
    }

    public State getBankState() {
        return bankState;
    }

    public PostalCode getPostalCode() {
        return postalCode;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(final boolean active) {
        this.active = active;
    }

    public String getBankCountryCode() {
        return bankCountryCode;
    }

    public void setBankCountryCode(String bankCountryCode) {
        this.bankCountryCode = bankCountryCode;
    }
}
