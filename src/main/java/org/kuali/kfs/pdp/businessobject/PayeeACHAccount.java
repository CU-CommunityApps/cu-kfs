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
package org.kuali.kfs.pdp.businessobject;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.kuali.kfs.core.api.criteria.PredicateFactory;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.identity.entity.Entity;
import org.kuali.kfs.kim.impl.identity.principal.Principal;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.datadictionary.AttributeSecurity;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.PdpConstants.PayeeIdTypeCodes;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.FinancialSystemUserService;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;

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

    private ACHBank bankRouting;
    private ACHTransactionType transactionType;
    private ACHPayee achPayee;

    public PayeeACHAccount() {

    }

    /**
     * Gets the achAccountGeneratedIdentifier attribute.
     *
     * @return Returns the achAccountGeneratedIdentifier
     */
    public KualiInteger getAchAccountGeneratedIdentifier() {
        return achAccountGeneratedIdentifier;
    }

    /**
     * Sets the achAccountGeneratedIdentifier attribute.
     *
     * @param achAccountGeneratedIdentifier The achAccountGeneratedIdentifier to set.
     */
    public void setAchAccountGeneratedIdentifier(KualiInteger achAccountGeneratedIdentifier) {
        this.achAccountGeneratedIdentifier = achAccountGeneratedIdentifier;
    }

    /**
     * Gets the bankRoutingNumber attribute.
     *
     * @return Returns the bankRoutingNumber
     */
    public String getBankRoutingNumber() {
        return bankRoutingNumber;
    }

    /**
     * Sets the bankRoutingNumber attribute.
     *
     * @param bankRoutingNumber The bankRoutingNumber to set.
     */
    public void setBankRoutingNumber(String bankRoutingNumber) {
        this.bankRoutingNumber = bankRoutingNumber;
    }

    /**
     * Gets the bankAccountNumber attribute.
     *
     * @return Returns the bankAccountNumber
     */
    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    /**
     * Sets the bankAccountNumber attribute.
     *
     * @param bankAccountNumber The bankAccountNumber to set.
     */
    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    /**
     * Gets the payee's name from KIM or Vendor data, if the payee type is Employee, Entity or Vendor; otherwise returns the stored
     * field value.
     *
     * @return Returns the payee name
     */
    public String getPayeeName() {
        // for Employee, retrieves from Person table by employee ID
        if (StringUtils.equalsIgnoreCase(payeeIdentifierTypeCode, PayeeIdTypeCodes.EMPLOYEE)) {
            if (ObjectUtils.isNotNull(payeeIdNumber)) {
                String name = SpringContext.getBean(FinancialSystemUserService.class).getPersonNameByEmployeeId(payeeIdNumber);

                // Person person = SpringContext.getBean(PersonService.class).getPersonByEmployeeId(payeeIdNumber);
                if (ObjectUtils.isNotNull(name)) {
                    return name;
                }
            }
        } else if (StringUtils.equalsIgnoreCase(payeeIdentifierTypeCode, PayeeIdTypeCodes.ENTITY)) {
            // for Entity, retrieve from Entity table by entity ID
            if (ObjectUtils.isNotNull(payeeIdNumber)) {
                Entity entity = KimApiServiceLocator.getIdentityService().getEntity(payeeIdNumber);
                if (ObjectUtils.isNotNull(entity) && ObjectUtils.isNotNull(entity.getDefaultName())) {
                    return entity.getDefaultName().getCompositeName();
                }
            }
        } else if (StringUtils.equalsIgnoreCase(payeeIdentifierTypeCode, PayeeIdTypeCodes.VENDOR_ID)) {
            // for Vendor, retrieves from Vendor table by vendor number
            VendorDetail vendor = SpringContext.getBean(VendorService.class).getVendorDetail(payeeIdNumber);
            if (ObjectUtils.isNotNull(vendor)) {
                return vendor.getVendorName();
            }
        }

        // otherwise return field value
        return payeeName;
    }

    /**
     * Sets the payeeName attribute.
     *
     * @param payeeName The payeeName to set.
     */
    public void setPayeeName(String payeeName) {
        this.payeeName = payeeName;
    }

    // CU Customization: Added getter and setter for principal name.

    /**
     * Gets the payee's principal name from KIM if payee type is Employee or Entity; otherwise returns null.
     * If the payee is an entity with multiple principals, then this method will return all the principal names
     * in a single String, with ", " as the separator.
     * 
     * @return The payee's principal name if an Employee or Entity payee, null otherwise.
     */
    public String getPayeePrincipalName() {
        String principalName = null;
        
        if (StringUtils.equalsIgnoreCase(payeeIdentifierTypeCode, PayeeIdTypeCodes.EMPLOYEE)) {
            // For employee, find a person with the given employee ID.
            if (ObjectUtils.isNotNull(payeeIdNumber)) {
                Person person = KimApiServiceLocator.getPersonService().getPersonByEmployeeId(payeeIdNumber);
                if (ObjectUtils.isNotNull(person) && StringUtils.isNotBlank(person.getEntityId())) {
                    // If a valid KIM-backed person was found, then return the person's principal name.
                    principalName = person.getPrincipalName();
                }
            }
        } else if (StringUtils.equalsIgnoreCase(payeeIdentifierTypeCode, PayeeIdTypeCodes.ENTITY)) {
            // For an entity, find all principals with the given entity ID.
            if (ObjectUtils.isNotNull(payeeIdNumber)) {
                QueryByCriteria criteria = QueryByCriteria.Builder.fromPredicates(
                        PredicateFactory.equal(KIMPropertyConstants.Person.ENTITY_ID, payeeIdNumber));
                List<Principal> principals = KimApiServiceLocator.getIdentityService().findPrincipals(criteria)
                        .getResults();
                if (CollectionUtils.isNotEmpty(principals)) {
                    // It is possible for KIM entities to have multiple principals, so return a list of all of their principal names.
                    if (principals.size() > 1) {
                        StringBuilder allNames = new StringBuilder();
                        for (Principal principal : principals) {
                            allNames.append(principal.getPrincipalName()).append(", ");
                        }
                        // Remove the trailing ", " separator from the final result.
                        principalName = allNames.substring(0, allNames.length() - 2);
                    } else {
                        // Shortcut for when entity has only one principal, which is the vast majority of cases.
                        principalName = principals.get(0).getPrincipalName();
                    }
                }
            }
        }
        
        return principalName;
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
     * Gets the payee's email address from KIM data if the payee type is Employee or Entity; otherwise, returns the stored field
     * value.
     *
     * @return Returns the payeeEmailAddress
     */
    public String getPayeeEmailAddress() {
//        // for Employee, retrieve from Person table by employee ID
//        if (StringUtils.equalsIgnoreCase(payeeIdentifierTypeCode, PayeeIdTypeCodes.EMPLOYEE)) {
//            Person person = SpringContext.getBean(PersonService.class).getPersonByEmployeeId(payeeIdNumber);
//            if (ObjectUtils.isNotNull(person)) {
//                return person.getEmailAddress();
//            }
//        }
//        // for Entity, retrieve from Entity table by entity ID then from Person table
//        else if (StringUtils.equalsIgnoreCase(payeeIdentifierTypeCode, PayeeIdTypeCodes.ENTITY)) {
//            if (ObjectUtils.isNotNull(payeeIdNumber)) {
//                EntityDefault entity = KimApiServiceLocator.getIdentityService().getEntityDefault(payeeIdNumber);
//                if (ObjectUtils.isNotNull(entity)) {
//                    List<Principal> principals = entity.getPrincipals();
//                    if (principals.size() > 0 && ObjectUtils.isNotNull(principals.get(0))) {
//                        String principalId = principals.get(0).getPrincipalId();
//                        Person person = SpringContext.getBean(PersonService.class).getPerson(principalId);
//                        if (ObjectUtils.isNotNull(person)) {
//                            return person.getEmailAddress();
//                        }
//                    }
//                }
//            }
//        }

        // otherwise returns the field value
        return payeeEmailAddress;
    }

    /**
     * Sets the payeeEmailAddress attribute if the payee is not Employee or Entity.
     *
     * @param payeeEmailAddress The payeeEmailAddress to set.
     */
    public void setPayeeEmailAddress(String payeeEmailAddress) {
        this.payeeEmailAddress = payeeEmailAddress;
    }

    /**
     * Gets the payeeIdentifierTypeCode attribute.
     *
     * @return Returns the payeeIdentifierTypeCode
     */
    public String getPayeeIdentifierTypeCode() {
        return payeeIdentifierTypeCode;
    }

    /**
     * Sets the payeeIdentifierTypeCode attribute.
     *
     * @param payeeIdentifierTypeCode The payeeIdentifierTypeCode to set.
     */
    public void setPayeeIdentifierTypeCode(String payeeIdentifierTypeCode) {
        this.payeeIdentifierTypeCode = payeeIdentifierTypeCode;
    }

    /**
     * Gets the achTransactionType attribute.
     *
     * @return Returns the achTransactionType.
     */
    public String getAchTransactionType() {
        return achTransactionType;
    }

    /**
     * Sets the achTransactionType attribute value.
     *
     * @param achTransactionType The achTransactionType to set.
     */
    public void setAchTransactionType(String achTransactionType) {
        this.achTransactionType = achTransactionType;
    }

    /**
     * Gets the transactionType attribute.
     *
     * @return Returns the transactionType.
     */
    public ACHTransactionType getTransactionType() {
        return transactionType;
    }

    /**
     * Sets the transactionType attribute value.
     *
     * @param transactionType The transactionType to set.
     */
    public void setTransactionType(ACHTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    /**
     * Gets the active attribute.
     *
     * @return Returns the active
     */
    @Override
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active attribute.
     *
     * @param active The active to set.
     */
    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isAutoInactivationIndicator() {
        return autoInactivationIndicator;
    }

    public void setAutoInactivationIndicator(boolean autoInactivationIndicator) {
        this.autoInactivationIndicator = autoInactivationIndicator;
    }

    /**
     * Gets the bankAccountTypeCode attribute.
     *
     * @return Returns the bankAccountTypeCode.
     */
    public String getBankAccountTypeCode() {
        return bankAccountTypeCode;
    }

    /**
     * Sets the bankAccountTypeCode attribute value.
     *
     * @param bankAccountTypeCode The bankAccountTypeCode to set.
     */
    public void setBankAccountTypeCode(String bankAccountTypeCode) {
        this.bankAccountTypeCode = bankAccountTypeCode;
    }

    /**
     * Gets the bankRouting attribute.
     *
     * @return Returns the bankRouting.
     */
    public ACHBank getBankRouting() {
        return bankRouting;
    }

    /**
     * Sets the bankRouting attribute value.
     *
     * @param bankRouting The bankRouting to set.
     * @deprecated
     */
    @Deprecated
    public void setBankRouting(ACHBank bankRouting) {
        this.bankRouting = bankRouting;
    }

    /**
     * Gets the payeeIdNumber attribute.
     *
     * @return Returns the payeeIdNumber.
     */
    public String getPayeeIdNumber() {
        return payeeIdNumber;
    }

    /**
     * Sets the payeeIdNumber attribute value.
     *
     * @param payeeIdNumber The payeeIdNumber to set.
     */
    public void setPayeeIdNumber(String payeeIdNumber) {
        this.payeeIdNumber = payeeIdNumber;
    }

    public String getStandardEntryClass() {
        return standardEntryClass;
    }

    public void setStandardEntryClass(final String standardEntryClass) {
        this.standardEntryClass = standardEntryClass;
    }

    /**
     * Gets the achPayee attribute.
     *
     * @return Returns the achPayee.
     */
    public ACHPayee getAchPayee() {
        return achPayee;
    }

    /**
     * Sets the achPayee attribute value.
     *
     * @param achPayee The achPayee to set.
     */
    public void setAchPayee(ACHPayee achPayee) {
        this.achPayee = achPayee;
    }

    /**
     * KFSCNTRB-1682: Some of the fields contain confidential information
     */
    @Override
    public String toString() {
        final class PayeeACHAccountToStringBuilder extends ReflectionToStringBuilder {
            private PayeeACHAccountToStringBuilder(Object object) {
                super(object);
            }

            @Override
            public boolean accept(Field field) {
                if (BusinessObject.class.isAssignableFrom(field.getType())) {
                    return false;
                }

                AttributeSecurity attributeSecurity = getDataDictionaryService()
                        .getAttributeSecurity(PayeeACHAccount.class.getName(), field.getName());
                if (ObjectUtils.isNotNull(attributeSecurity)
                    && (attributeSecurity.isHide() || attributeSecurity.isMask()
                        || attributeSecurity.isPartialMask())) {
                    return false;
                }

                return super.accept(field);
            }
        }

        ReflectionToStringBuilder toStringBuilder = new PayeeACHAccountToStringBuilder(this);
        return toStringBuilder.toString();
    }
}
