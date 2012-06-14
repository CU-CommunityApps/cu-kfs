package edu.cornell.kfs.module.bc.document.dataaccess.impl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.kuali.kfs.module.bc.businessobject.BudgetConstructionPayRateHolding;
import org.kuali.kfs.module.bc.businessobject.PendingBudgetConstructionAppointmentFunding;
import org.kuali.kfs.module.bc.document.dataaccess.impl.BudgetConstructionDaoJdbcBase;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import edu.cornell.kfs.module.bc.document.dataaccess.BudgetConstructionSipDao;
import edu.cornell.kfs.module.bc.document.dataaccess.SipImportDao;
import edu.cornell.kfs.module.bc.util.ExportUtil;

public class SipImportDaoJdbc extends BudgetConstructionDaoJdbcBase implements SipImportDao {
	
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(BudgetConstructionSipDaoJdbc.class);
	public void BudgetConstructionSipDaoJdbc() { }

    public double getTotalPerCentDistWithRequestAmtGreaterThanZero(String positionNumber, String emplId) {
       
        try {
	        StringBuilder sqlBuilder = new StringBuilder(200);
	        sqlBuilder.append("select sum(APPT_RQST_FTE_QTY)+sum(APPT_RQCSF_FTE_QTY) as TotalPerCentDistribution from LD_PNDBC_APPTFND_T where position_nbr=? and emplid=? and ((APPT_RQST_AMT <> 0) OR (APPT_RQST_CSF_AMT <> 0))");
	        String sqlString = sqlBuilder.toString();
	        
	        BigDecimal bdResult =  this.getSimpleJdbcTemplate().queryForObject(sqlString, BigDecimal.class, positionNumber, emplId);
	        if (ObjectUtils.isNotNull(bdResult))
	        	return bdResult.doubleValue();
	        else
	        	// This means that the APPT_RQST_AMT AND the APPT_RQST_CSF_AMT are both zero and as such the SQL returns a NULL.  Rather
	        	//   than return 1 though to indicate that there is no error, we'll return -1 and handle it in the calling method.  This
	        	//   is done so that in the future we can distinguish between getting a valid 1 back and getting NULL from the SQL.
	        	return -1.00;
        }
        
        catch (Exception ex) {
        	LOG.info("SipImportDaoJdbc Exception: " + ex.getMessage());
        	return -2.00;
        }
    }

    public double getTotalPerCentDistribution(String positionNumber, String emplId) {
        
        try {
	        StringBuilder sqlBuilder = new StringBuilder(200);
	        sqlBuilder.append("select sum(APPT_RQST_FTE_QTY) as TotalPerCentDistribution from LD_PNDBC_APPTFND_T where position_nbr=? and emplid=?");
	        String sqlString = sqlBuilder.toString();
	        
	        BigDecimal bdResult =  this.getSimpleJdbcTemplate().queryForObject(sqlString, BigDecimal.class, positionNumber, emplId);
	        if (ObjectUtils.isNotNull(bdResult))
	        	return bdResult.doubleValue();
	        else
	        	return -1.00;
        }
        
        catch (Exception ex) {
        	LOG.info("SipImportDaoJdbc Exception: " + ex.getMessage());
        	return -2.00;
        }
    }
    
    public double getTotalRequestedAmount(String positionNumber, String emplId) {
        
        try {
	        StringBuilder sqlBuilder = new StringBuilder(200);
	        sqlBuilder.append("select sum(APPT_RQST_AMT) as TotalRequestedAmount from LD_PNDBC_APPTFND_T where position_nbr=? and emplid=?");
	        String sqlString = sqlBuilder.toString();
	        
	        BigDecimal bdResult =  this.getSimpleJdbcTemplate().queryForObject(sqlString, BigDecimal.class, positionNumber, emplId);
	        if (ObjectUtils.isNotNull(bdResult))
	        	return bdResult.doubleValue();
	        else
	        	return -1.00;
        }
        
        catch (Exception ex) {
        	LOG.info("SipImportDaoJdbc Exception: " + ex.getMessage());
        	return -2.00;
        }
    }

	public int numberOfRecordsInCuPsJobDataTable(String positionNumber, String emplId) {
		// The CU_PS_JOB_DATA table is used extensively to validate a number of properties of the SIP record submitted by the units
		//  If this record does not exist for some reason then this method will return false and the appropriate error message will be
		//  generated.  One scenario that can cause a record to not be in this table is as follows: If the job code changes in the 
		//  PeopleSoft HR system, the job that generates the psBudgetFeed file (which is subsequently processed by the psBudgetFeedLoad
		//  batch job) will not contain the record and therefore will invalidate a number of the checks being performed by the validation
		//  methods in SipImportServiceImpl.java
		//  
        try {
	        StringBuilder sqlBuilder = new StringBuilder(200);
	        sqlBuilder.append("select count(*) from cu_ps_job_data where POS_NBR=? and emplid=?");
	        String sqlString = sqlBuilder.toString();
	        
	        BigDecimal bdResult =  this.getSimpleJdbcTemplate().queryForObject(sqlString, BigDecimal.class, positionNumber, emplId);
	        if (ObjectUtils.isNotNull(bdResult))
	        	return bdResult.intValue();
	        else
	        	return -1;
        }        
        catch (Exception ex) {
        	LOG.info("SipImportDaoJdbc Exception: " + ex.getMessage());
        	return -2;
        }
	}
	
    /**
     * @see edu.cornell.kfs.module.bc.document.dataaccess.SipImportDao#hasLeaveAmountWithoutRequestAmount(java.lang.String,
     * java.lang.String)
     */
    public boolean hasLeaveAmountWithoutRequestAmount(String positionNumber, String emplid) {
        boolean result = false;

        try {
            int count = getSimpleJdbcTemplate()
                    .queryForInt(
                            "select count(*) from LD_PNDBC_APPTFND_T where position_nbr=? and emplid=? and APPT_RQST_AMT = 0 and APPT_RQST_CSF_AMT <> 0",
                            "00" + positionNumber, emplid);

            if (count > 0) {
                result = true;
            }

        } catch (Exception ex) {
            LOG.info("SipImportDaoJdbc Exception: " + ex.getMessage());
        }

        return result;
    }

}
