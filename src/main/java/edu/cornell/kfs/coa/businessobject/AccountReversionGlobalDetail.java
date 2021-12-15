/*
 * Copyright 2007 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.coa.businessobject;

import java.lang.reflect.InvocationTargetException;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;

import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetailBase;

/**
 * This is a representation of an Account Reversion Detail, made specifically for Global Account Reversions. However, as
 * AccountReversionDetail lists Account as a primary key and Global Account Reversions deal with several
 * Accounts, that class could not be re-used for Globals.
 */
public class AccountReversionGlobalDetail extends GlobalBusinessObjectDetailBase {
	private static final Logger LOG = LogManager.getLogger(AccountReversionGlobalDetail.class);

    private String documentNumber;
    private String accountReversionCategoryCode;
    private String accountReversionObjectCode;
    private String accountReversionCode;

    private ReversionCategory reversionCategory;
    private AccountReversionGlobal parentGlobalAccountReversion;
    private ObjectCode accountReversionObject;

    /**
     * Constructs an OrganizationReversionGlobalDocumentDetail.
     */
    public AccountReversionGlobalDetail() {
    }

    /**
     * Gets the documentNumber attribute.
     * 
     * @return Returns the documentNumber.
     */
    public String getDocumentNumber() {
        return documentNumber;
    }

    /**
     * Sets the documentNumber attribute value.
     * 
     * @param documentNumber The documentNumber to set.
     */
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }


    /**
	 * @return the accountReversionCategoryCode
	 */
	public String getAccountReversionCategoryCode() {
		return accountReversionCategoryCode;
	}

	/**
	 * @param accountReversionCategoryCode the accountReversionCategoryCode to set
	 */
	public void setAccountReversionCategoryCode(String accountReversionCategoryCode) {
		this.accountReversionCategoryCode = accountReversionCategoryCode;
	}

	/**
	 * @return the accountReversionObjectCode
	 */
	public String getAccountReversionObjectCode() {
		return accountReversionObjectCode;
	}

	/**
	 * @param accountReversionObjectCode the accountReversionObjectCode to set
	 */
	public void setAccountReversionObjectCode(String accountReversionObjectCode) {
		this.accountReversionObjectCode = accountReversionObjectCode;
	}

	/**
	 * @return the accountReversionCode
	 */
	public String getAccountReversionCode() {
		return accountReversionCode;
	}

	/**
	 * @param accountReversionCode the accountReversionCode to set
	 */
	public void setAccountReversionCode(String accountReversionCode) {
		this.accountReversionCode = accountReversionCode;
	}

	/**
	 * @return the reversionCategory
	 */
	public ReversionCategory getReversionCategory() {
		return reversionCategory;
	}

	/**
	 * @param reversionCategory the reversionCategory to set
	 */
	public void setReversionCategory(ReversionCategory reversionCategory) {
		this.reversionCategory = reversionCategory;
	}

	/**
	 * @return the parentGlobalAccountReversion
	 */
	public AccountReversionGlobal getParentGlobalAccountReversion() {
		return parentGlobalAccountReversion;
	}

	/**
	 * @param parentGlobalAccountReversion the parentGlobalAccountReversion to set
	 */
	public void setParentGlobalAccountReversion(AccountReversionGlobal parentGlobalAccountReversion) {
		this.parentGlobalAccountReversion = parentGlobalAccountReversion;
	}

	/**
	 * @return the reversionObject
	 */
	public ObjectCode getAccountReversionObject() {
		return accountReversionObject;
	}

	/**
	 * @param reversionObject the reversionObject to set
	 */
	public void setAccountReversionObject(ObjectCode accountReversionObject) {
		this.accountReversionObject = accountReversionObject;
	}

	/**
     * This utility method converts the name of a property into a string suitable for being part of a locking representation.
     * 
     * @param keyName the name of the property to convert to a locking representation
     * @return a part of a locking representation
     */
    private String convertKeyToLockingRepresentation(String keyName) {
        StringBuffer sb = new StringBuffer();
        sb.append(keyName);
        sb.append(KFSConstants.Maintenance.AFTER_FIELDNAME_DELIM);
        String keyValue = "";
        try {
            Object keyValueObj = PropertyUtils.getProperty(this, keyName);
            if (keyValueObj != null) {
                keyValue = keyValueObj.toString();
            }
        }
        catch (IllegalAccessException iae) {
            LOG.info("Illegal access exception while attempting to read property " + keyName, iae);
        }
        catch (InvocationTargetException ite) {
            LOG.info("Illegal Target Exception while attempting to read property " + keyName, ite);
        }
        catch (NoSuchMethodException nsme) {
            LOG.info("There is no such method to read property " + keyName + " in this class.", nsme);
        }
        finally {
            sb.append(keyValue);
        }
        sb.append(KFSConstants.Maintenance.AFTER_VALUE_DELIM);
        return sb.toString();
    }

    /**
     * This returns a string of object code names associated with the object code in this org rev change detail.
     * 
     * @return String of distinct object code names
     */
    public String getObjectCodeNames() {
        String objectCodeNames = "";
        if (!StringUtils.isBlank(this.getAccountReversionObjectCode())) {
            if (this.getParentGlobalAccountReversion().getUniversityFiscalYear() != null && this.getParentGlobalAccountReversion().getAccountReversionGlobalAccounts() != null && this.getParentGlobalAccountReversion().getAccountReversionGlobalAccounts().size() > 0) {
                // find distinct chart of account codes
                SortedSet<String> chartCodes = new TreeSet<String>();
                for (AccountReversionGlobalAccount acct : this.getParentGlobalAccountReversion().getAccountReversionGlobalAccounts()) {
                    chartCodes.add(acct.getChartOfAccountsCode());
                }
                String[] chartCodesArray = new String[chartCodes.size()];
                int i = 0;
                for (String chartCode : chartCodes) {
                    chartCodesArray[i] = chartCode;
                    i++;
                }
                objectCodeNames = (String) SpringContext.getBean(ObjectCodeService.class).getObjectCodeNamesByCharts(this.getParentGlobalAccountReversion().getUniversityFiscalYear(), chartCodesArray, this.getAccountReversionObjectCode());
            }
        }
        return objectCodeNames;
    }
}
