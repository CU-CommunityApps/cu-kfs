/**
 * 
 */
package edu.cornell.kfs.coa.service.impl;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.context.SpringContext;

import com.opencsv.CSVReader;

import edu.cornell.kfs.coa.businessobject.AccountReversion;
import edu.cornell.kfs.coa.businessobject.AccountReversionDetail;
import edu.cornell.kfs.coa.dataaccess.AccountReversionImportDao;
import edu.cornell.kfs.coa.service.AccountReversionImportService;

/**
 *
 */
public class AccountReversionImportServiceImpl implements AccountReversionImportService {
	
	private static final Logger LOG = LogManager.getLogger(AccountReversionImportServiceImpl.class);

	
	ParameterService parameterService;
	BusinessObjectService boService;
	AccountReversionImportDao arid; 
	
	/* (non-Javadoc)
	 * @see edu.cornell.kfs.coa.service.AccountReversionImportService#importAccountReversions()
	 */
	public void importAccountReversions(File f) {
	  
		// KFSPTS-2174 : Repurposed this batch job to append the loaded values to the existing table, rather than delete all current values and reload the tables from scratch
	    //		arid.destroyAccountReversionsAndDetails();
		int count = 0;
		String objectCode = parameterService.getParameterValueAsString("KFS-COA", "Reversion", "CASH_REVERSION_OBJECT_CODE");
		
		Integer fiscalYear = Integer.parseInt(parameterService.getParameterValueAsString("KFS-COA", "Reversion", "ACCOUNT_REVERSION_FISCAL_YEAR"));
		
		try {
			CSVReader reader = new CSVReader(new FileReader(f));
			List<String[]> lines = reader.readAll();

			Object[] array = lines.toArray();
			LOG.info("Read: "+ lines.toArray().length+" records");
			for (int i = 0; i < array.length; i++) {
            	String[] line = (String[]) array[i];
                String fromChart = line[0];
                String fromAcct = line[1];

                String toChart = line[2];
                String toAcct = line[3];
                LOG.info("Creating Reversion for: from account: "+ fromAcct +": to account "+ toAcct);
                if (StringUtils.isNotBlank(fromChart) &&StringUtils.isNotBlank(fromAcct) && StringUtils.isNotBlank(toChart) &&StringUtils.isNotBlank(toAcct)) {

                	AccountReversion exists = null;
                	Map<String, String> pks = new HashMap<String, String>();

                	pks.put("UNIV_FISCAL_YR", String.valueOf(fiscalYear));
                	pks.put("FIN_COA_CD", fromChart);
                	pks.put("ACCT_NBR", fromAcct);
                	exists = (AccountReversion)SpringContext.getBean(BusinessObjectService.class).findByPrimaryKey(AccountReversion.class, pks);
                	
                	if(ObjectUtils.isNotNull(exists)) {

                		LOG.info("Account Reversion already exists for this fiscal year ("+fiscalYear+"): from account: "+ fromAcct +": to account "+ toAcct);
                		continue;
                	}

                	// Validate from account; cannot add account reversion if account does not exist
                	Account fromAcctExists = null;
                	fromAcctExists = SpringContext.getBean(AccountService.class).getByPrimaryId(fromChart, fromAcct);
                	if(ObjectUtils.isNull(fromAcctExists)) {
                		LOG.info("From account ("+ fromAcct +") does not exist.");
                		LOG.info("Account Reversion already exists for this fiscal year ("+fiscalYear+"): from account: "+ fromAcct +": to account "+ toAcct);
                		continue;
                	}
                	
                	// Validate to account; cannot add account reversion if account does not exist
                	Account toAcctExists = null;
                	toAcctExists = SpringContext.getBean(AccountService.class).getByPrimaryId(fromChart, fromAcct);
                	if(ObjectUtils.isNull(toAcctExists)) {
                		LOG.info("To account ("+ toAcct +") does not exist.");
                		LOG.info("Account Reversion already exists for this fiscal year ("+fiscalYear+"): from account: "+ fromAcct +": to account "+ toAcct);
                		continue;
                	}
                	
                	AccountReversion accountReversion = new AccountReversion(); 

                	accountReversion.setUniversityFiscalYear(fiscalYear);
                	accountReversion.setChartOfAccountsCode(fromChart);
                	accountReversion.setAccountNumber(fromAcct);

                	accountReversion.setBudgetReversionChartOfAccountsCode(toChart);
                	accountReversion.setBudgetReversionAccountNumber(toAcct);
                	accountReversion.setCashReversionFinancialChartOfAccountsCode(toChart);
                	accountReversion.setCashReversionAccountNumber(toAcct);
                	accountReversion.setCarryForwardByObjectCodeIndicator(false);
                	accountReversion.setActive(true);

                	AccountReversionDetail accountReversionDetail = new AccountReversionDetail();
                	accountReversionDetail.setUniversityFiscalYear(fiscalYear);
                	accountReversionDetail.setChartOfAccountsCode(fromChart);
                	accountReversionDetail.setAccountNumber(fromAcct);
                	accountReversionDetail.setAccountReversionCategoryCode("A1");
                	accountReversionDetail.setAccountReversionCode("CA");
                	accountReversionDetail.setAccountReversionObjectCode(objectCode);
                	accountReversionDetail.setActive(true);

                	accountReversion.addAccountReversionDetail(accountReversionDetail);
                	boService.save(accountReversion);
                	count++;
                }
            
            }
			reader.close();
		
		} catch (Exception e){ e.printStackTrace();}
		finally {
			LOG.info("Wrote: "+ count +" records");
			
		}
	}

	/**
	 * @return the parameterService
	 */
	public ParameterService getParameterService() {
		return parameterService;
	}

	/**
	 * @param parameterService the parameterService to set
	 */
	public void setParameterService(ParameterService parameterService) {
		this.parameterService = parameterService;
	}

	/**
	 * @return the boService
	 */
	public BusinessObjectService getBoService() {
		return boService;
	}

	/**
	 * @param boService the boService to set
	 */
	public void setBoService(BusinessObjectService boService) {
		this.boService = boService;
	}

	/**
	 * @return the arid
	 */
	public AccountReversionImportDao getArid() {
		return arid;
	}

	/**
	 * @param arid the arid to set
	 */
	public void setArid(AccountReversionImportDao arid) {
		this.arid = arid;
	}

	
	
}
