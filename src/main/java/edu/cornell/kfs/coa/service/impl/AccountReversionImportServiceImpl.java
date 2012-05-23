/**
 * 
 */
package edu.cornell.kfs.coa.service.impl;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.ojb.broker.util.logging.Logger;
import org.kuali.kfs.coa.businessobject.AccountReversion;
import org.kuali.kfs.coa.businessobject.AccountReversionDetail;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.ParameterService;

import au.com.bytecode.opencsv.CSVReader;
import edu.cornell.kfs.coa.dataaccess.AccountReversionImportDao;
import edu.cornell.kfs.coa.dataaccess.impl.AccountReversionImportDaoJdbc;
import edu.cornell.kfs.coa.service.AccountReversionImportService;

/**
 * @author kwk43
 *
 */
public class AccountReversionImportServiceImpl implements AccountReversionImportService {
	
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AccountReversionImportServiceImpl.class);

	
	ParameterService parameterService;
	BusinessObjectService boService;
	AccountReversionImportDao arid; 
	
	/* (non-Javadoc)
	 * @see edu.cornell.kfs.coa.service.AccountReversionImportService#importAccountReversions()
	 */
	public void importAccountReversions(File f) {
		
		
		arid.destroyAccountReversionsAndDetails();
		
		String objectCode = parameterService.getParameterValue("KFS-COA", "Reversion", "CASH_REVERSION_OBJECT_CODE");
		
		try {
			CSVReader reader = new CSVReader(new FileReader(f));
			List<String[]> lines = reader.readAll();

			Object[] array = lines.toArray();
			LOG.info("Read: "+ lines.toArray().length+" records");
			for (int i = 0; i < array.length; i++) {
            //for (String[] line : array) {
            	String[] line = (String[]) array[i];
                String fromChart = line[0];
                String fromAcct = line[1];

                String toChart = line[2];
                String toAcct = line[3];
                LOG.info("Creating Reversion for: from account: "+ fromAcct +": to account "+ toAcct);
                if (StringUtils.isNotBlank(fromChart) &&StringUtils.isNotBlank(fromAcct) && StringUtils.isNotBlank(toChart) &&StringUtils.isNotBlank(toAcct)) {

                	AccountReversion accountReversion = new AccountReversion(); 
                	accountReversion.setUniversityFiscalYear(2012);
                	accountReversion.setChartOfAccountsCode(fromChart);
                	accountReversion.setAccountNumber(fromAcct);

                	accountReversion.setBudgetReversionChartOfAccountsCode(toChart);
                	accountReversion.setBudgetReversionAccountNumber(toAcct);
                	accountReversion.setCashReversionFinancialChartOfAccountsCode(toChart);
                	accountReversion.setCashReversionAccountNumber(toAcct);
                	accountReversion.setCarryForwardByObjectCodeIndicator(false);
                	accountReversion.setActive(true);

                	//List details = new ArrayList();
                	AccountReversionDetail accountReversionDetail = new AccountReversionDetail();
                	accountReversionDetail.setUniversityFiscalYear(2012);
                	accountReversionDetail.setChartOfAccountsCode(fromChart);
                	accountReversionDetail.setAccountNumber(fromAcct);
                	accountReversionDetail.setAccountReversionCategoryCode("A1");
                	accountReversionDetail.setAccountReversionCode("CA");
                	accountReversionDetail.setAccountReversionObjectCode(objectCode);
                	accountReversionDetail.setActive(true);

                	//details.add(accountReversionDetail);

                	accountReversion.addAccountReversionDetail(accountReversionDetail);
                	boService.save(accountReversion);
                }
            
            }
		
		} catch (Exception e){ e.printStackTrace();}
		finally {
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
