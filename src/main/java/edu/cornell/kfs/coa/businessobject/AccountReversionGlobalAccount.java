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
import java.util.LinkedHashMap;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.fp.batch.service.impl.ProcurementCardCreateDocumentServiceImpl;

import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetailBase;

/**
 * An organization which is related to a Global Organization Reversion Detail.
 */
public class AccountReversionGlobalAccount extends GlobalBusinessObjectDetailBase {
	private static final Logger LOG = LogManager.getLogger(AccountReversionGlobalAccount.class);
	
	private String documentNumber;
	private String chartOfAccountsCode;
	private String accountNumber;

	private Chart chartOfAccounts;
	private Account account;
	
	/**
	 * Constructs a OrganizationReversionGlobalOrganization
	 */
	public AccountReversionGlobalAccount() {
		super();
	}

	/**
	 * @see org.kuali.kfs.kns.bo.BusinessObjectBase#toStringMapper()
	 */
	protected LinkedHashMap toStringMapperr_RICE20_REFACTORME() {
		LinkedHashMap stringMapper = new LinkedHashMap();
		stringMapper.put(KFSPropertyConstants.DOCUMENT_NUMBER,
				this.documentNumber);
		stringMapper.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE,
				this.chartOfAccountsCode);
		stringMapper.put(KFSPropertyConstants.ACCOUNT_NUMBER,
				this.accountNumber);
		return stringMapper;
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
	 * @param documentNumber
	 *            The documentNumber to set.
	 */
	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	/**
	 * Gets the chartOfAccounts attribute.
	 * 
	 * @return Returns the chartOfAccounts.
	 */
	public Chart getChartOfAccounts() {
		return chartOfAccounts;
	}

	/**
	 * Sets the chartOfAccounts attribute value.
	 * 
	 * @param chartOfAccounts
	 *            The chartOfAccounts to set.
	 * @deprecated
	 */
	public void setChartOfAccounts(Chart chartOfAccounts) {
		this.chartOfAccounts = chartOfAccounts;
	}

	/**
	 * Gets the chartOfAccountsCode attribute.
	 * 
	 * @return Returns the chartOfAccountsCode.
	 */
	public String getChartOfAccountsCode() {
		return chartOfAccountsCode;
	}

	/**
	 * Sets the chartOfAccountsCode attribute value.
	 * 
	 * @param chartOfAccountsCode
	 *            The chartOfAccountsCode to set.
	 */
	public void setChartOfAccountsCode(String chartOfAccountsCode) {
		this.chartOfAccountsCode = chartOfAccountsCode;
	}

	/**
	 * Gets the account attribute.
	 * 
	 * @return Returns the account.
	 */
	public Account getAccount() {
		return account;
	}

	/**
	 * Sets the account attribute value.
	 * 
	 * @param account
	 *            The organization to set.
	 * @deprecated
	 */
	public void setAccount(Account account) {
		this.account = account;
	}

	/**
	 * Gets the accountNumber attribute.
	 * 
	 * @return Returns the accountNumber.
	 */
	public String getAccountNumber() {
		return accountNumber;
	}

	/**
	 * Sets the accountNumber attribute value.
	 * 
	 * @param accountNumber
	 *            The accountNumber to set.
	 */
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	/**
	 * This utility method converts the name of a property into a string
	 * suitable for being part of a locking representation.
	 * 
	 * @param keyName
	 *            the name of the property to convert to a locking
	 *            representation
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
		} catch (IllegalAccessException iae) {
			LOG.info(
					"Illegal access exception while attempting to read property "
							+ keyName, iae);
		} catch (InvocationTargetException ite) {
			LOG.info(
					"Illegal Target Exception while attempting to read property "
							+ keyName, ite);
		} catch (NoSuchMethodException nsme) {
			LOG.info("There is no such method to read property " + keyName
					+ " in this class.", nsme);
		} finally {
			sb.append(keyValue);
		}
		sb.append(KFSConstants.Maintenance.AFTER_VALUE_DELIM);
		return sb.toString();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME
				* result
				+ ((this.getChartOfAccountsCode() == null) ? 0 : this
						.getChartOfAccountsCode().hashCode());
		result = PRIME
				* result
				+ ((this.getDocumentNumber() == null) ? 0 : this
						.getDocumentNumber().hashCode());
		result = PRIME
				* result
				+ ((this.getAccountNumber() == null) ? 0 : this
						.getAccountNumber().hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AccountReversionGlobalAccount other = (AccountReversionGlobalAccount) obj;
		if (this.getChartOfAccountsCode() == null) {
			if (other.getChartOfAccountsCode() != null) {
				return false;
			}
		} else if (!this.getChartOfAccountsCode().equals(
				other.getChartOfAccountsCode())) {
			return false;
		}
		if (this.getDocumentNumber() == null) {
			if (other.getDocumentNumber() != null) {
				return false;
			}
		} else if (!this.getDocumentNumber().equals(other.getDocumentNumber())) {
			return false;
		}
		if (this.getAccountNumber() == null) {
			if (other.getAccountNumber() != null) {
				return false;
			}
		} else if (!this.getAccountNumber().equals(other.getAccountNumber())) {
			return false;
		}
		return true;
	}

}