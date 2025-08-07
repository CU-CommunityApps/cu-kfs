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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.datadictionary.AttributeSecurity;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.PdpConstants.PayeeIdTypeCodes;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.FinancialSystemUserService;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;

import java.lang.reflect.Field;

public class PayeeACHAccount extends PersistableBusinessObjectBase implements MutableInactivatable {

    private KualiInteger achAccountGeneratedIdentifier;
    private String bankRoutingNumber;
    private String bankAccountNumber;
    private String payeeIdNumber;
    private String payeeName;
    private String payeeEmailAddress;
    private String payeeIdentifierTypeCode;
    private String achTransactionType;
    private String bankAccountTypeCode;
    private String standardEntryClass;
    private boolean active;
    private boolean autoInactivationIndicator;
    private boolean bypassFeedUpdateIndicator;

    private ACHBank bankRouting;
    private ACHTransactionType transactionType;
    private ACHPayee achPayee;

    public KualiInteger getAchAccountGeneratedIdentifier() {
        return achAccountGeneratedIdentifier;
    }

    public void setAchAccountGeneratedIdentifier(final KualiInteger achAccountGeneratedIdentifier) {
        this.achAccountGeneratedIdentifier = achAccountGeneratedIdentifier;
    }

    public String getBankRoutingNumber() {
        return bankRoutingNumber;
    }

    public void setBankRoutingNumber(final String bankRoutingNumber) {
        this.bankRoutingNumber = bankRoutingNumber;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(final String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    /**
     * Gets the payee's name from KIM or Vendor data, if the payee type is Employee, Entity or Vendor; otherwise
     * returns the stored field value.
     *
     * @return Returns the payee name
     */
    public String getPayeeName() {
        if (StringUtils.equalsIgnoreCase(payeeIdentifierTypeCode, PayeeIdTypeCodes.EMPLOYEE)) {
            // for Employee, retrieves from Person table by employee ID
            if (ObjectUtils.isNotNull(payeeIdNumber)) {
                final String name = SpringContext.getBean(FinancialSystemUserService.class)
                        .getPersonNameByEmployeeId(payeeIdNumber);
                if (ObjectUtils.isNotNull(name)) {
                    return name;
                }
            }
        } else if (StringUtils.equalsIgnoreCase(payeeIdentifierTypeCode, PayeeIdTypeCodes.ENTITY)) {
            // for Entity, retrieve from Person table by entity ID
            if (ObjectUtils.isNotNull(payeeIdNumber)) {
                final Person person = SpringContext.getBean(PersonService.class).getPersonByEntityId(payeeIdNumber);
                if (ObjectUtils.isNotNull(person)) {
                    return person.getName();
                }
            }
        } else if (StringUtils.equalsIgnoreCase(payeeIdentifierTypeCode, PayeeIdTypeCodes.VENDOR_ID)) {
            // for Vendor, retrieves from Vendor table by vendor number
            final VendorDetail vendor = SpringContext.getBean(VendorService.class).getVendorDetail(payeeIdNumber);
            if (ObjectUtils.isNotNull(vendor)) {
                return vendor.getVendorName();
            }
        }

        // otherwise return field value
        return payeeName;
    }

    public void setPayeeName(final String payeeName) {
        this.payeeName = payeeName;
    }

    // CU Customization: Added getter and setter for principal name.

    /**
     * Gets the payee's principal name from KIM if payee type is Employee or Entity; otherwise returns null.
     * 
     * @return The payee's principal name if an Employee or Entity payee, null otherwise.
     */
    public String getPayeePrincipalName() {
        if (StringUtils.equalsIgnoreCase(payeeIdentifierTypeCode, PayeeIdTypeCodes.EMPLOYEE)) {
            // For Employee, find a person with the given employee ID.
            if (ObjectUtils.isNotNull(payeeIdNumber)) {
                final Person person = SpringContext.getBean(PersonService.class).getPersonByEmployeeId(payeeIdNumber);
                if (ObjectUtils.isNotNull(person)) {
                    return person.getPrincipalName();
                }
            }
        } else if (StringUtils.equalsIgnoreCase(payeeIdentifierTypeCode, PayeeIdTypeCodes.ENTITY)) {
            // for Entity, retrieve from Person table by entity ID.
            if (ObjectUtils.isNotNull(payeeIdNumber)) {
                final Person person = SpringContext.getBean(PersonService.class).getPersonByEntityId(payeeIdNumber);
                if (ObjectUtils.isNotNull(person)) {
                    return person.getPrincipalName();
                }
            }
        }
        
        return null;
    }

    /**
     * No-op setter for payee principal name, which is derived at runtime instead.
     * 
     * @param payeePrincipalName The principal name to set; not actually used.
     */
    public void setPayeePrincipalName(String payeePrincipalName) {
        // Do nothing.
    }

    /**
     * Getter for payee person that always returns null; it is only intended to aid with
     * generating a lookup icon for the "payeePrincipalName" property.
     * 
     * @return null.
     */
    public Person getPayeePerson() {
        return null;
    }

    /**
     * No-op setter for payee person; it is only intended to aid with
     * generating a lookup icon for the "payeePrincipalName" property.
     * 
     * @param payeePerson The payee person to set; not actually used.
     */
    public void setPayeePerson(Person payeePerson) {
        // Do nothing.
    }

    // End CU Customization.

    /**
     * Gets the payee's email address from KIM data if the payee type is Employee or Entity; otherwise, returns the
     * stored field value.
     *
     * @return Returns the payeeEmailAddress
     */
    public String getPayeeEmailAddress() {
        // CU Customization: Always return the email address defined on the Payee ACH Account;
        // do not derive it from the Person table when the payee is an Employee or Entity.
        return payeeEmailAddress;
    }

    public void setPayeeEmailAddress(final String payeeEmailAddress) {
        this.payeeEmailAddress = payeeEmailAddress;
    }

    public String getPayeeIdentifierTypeCode() {
        return payeeIdentifierTypeCode;
    }

    public void setPayeeIdentifierTypeCode(final String payeeIdentifierTypeCode) {
        this.payeeIdentifierTypeCode = payeeIdentifierTypeCode;
    }

    public String getAchTransactionType() {
        return achTransactionType;
    }

    public void setAchTransactionType(final String achTransactionType) {
        this.achTransactionType = achTransactionType;
    }

    public ACHTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(final ACHTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(final boolean active) {
        this.active = active;
    }

    public boolean isAutoInactivationIndicator() {
        return autoInactivationIndicator;
    }

    public void setAutoInactivationIndicator(final boolean autoInactivationIndicator) {
        this.autoInactivationIndicator = autoInactivationIndicator;
    }

    public String getBankAccountTypeCode() {
        return bankAccountTypeCode;
    }

    public void setBankAccountTypeCode(final String bankAccountTypeCode) {
        this.bankAccountTypeCode = bankAccountTypeCode;
    }

    public ACHBank getBankRouting() {
        return bankRouting;
    }

    @Deprecated
    public void setBankRouting(final ACHBank bankRouting) {
        this.bankRouting = bankRouting;
    }

    public String getPayeeIdNumber() {
        return payeeIdNumber;
    }

    public void setPayeeIdNumber(final String payeeIdNumber) {
        this.payeeIdNumber = payeeIdNumber;
    }

    public String getStandardEntryClass() {
        return standardEntryClass;
    }

    public void setStandardEntryClass(final String standardEntryClass) {
        this.standardEntryClass = standardEntryClass;
    }

    public ACHPayee getAchPayee() {
        return achPayee;
    }

    public void setAchPayee(final ACHPayee achPayee) {
        this.achPayee = achPayee;
    }

    public boolean isBypassFeedUpdateIndicator() {
        return bypassFeedUpdateIndicator;
    }

    public void setBypassFeedUpdateIndicator(final boolean bypassFeedUpdateIndicator) {
        this.bypassFeedUpdateIndicator = bypassFeedUpdateIndicator;
    }

    /**
     * Overridden because some fields contain confidential information
     */
    @Override
    public String toString() {
        final class PayeeACHAccountToStringBuilder extends ReflectionToStringBuilder {
            private PayeeACHAccountToStringBuilder(final Object object) {
                super(object);
            }

            @Override
            public boolean accept(final Field field) {
                if (BusinessObject.class.isAssignableFrom(field.getType())) {
                    return false;
                }

                final AttributeSecurity attributeSecurity = getDataDictionaryService()
                        .getAttributeSecurity(PayeeACHAccount.class.getName(), field.getName());
                if (ObjectUtils.isNotNull(attributeSecurity)
                    && (attributeSecurity.isHide() || attributeSecurity.isMask()
                        || attributeSecurity.isPartialMask())) {
                    return false;
                }

                return super.accept(field);
            }
        }

        final ReflectionToStringBuilder toStringBuilder = new PayeeACHAccountToStringBuilder(this);
        return toStringBuilder.toString();
    }
}
