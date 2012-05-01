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

}
