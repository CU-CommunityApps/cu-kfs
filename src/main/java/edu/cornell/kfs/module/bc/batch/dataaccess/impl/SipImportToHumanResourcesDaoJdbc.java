package edu.cornell.kfs.module.bc.batch.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.kuali.kfs.module.bc.document.dataaccess.impl.BudgetConstructionDaoJdbcBase;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import edu.cornell.kfs.module.bc.document.dataaccess.SipImportToHumanResourcesDao;
import edu.cornell.kfs.module.bc.util.CUBudgetParameterFinder;
import edu.cornell.kfs.module.bc.util.ExportUtil;

public class SipImportToHumanResourcesDaoJdbc extends BudgetConstructionDaoJdbcBase implements SipImportToHumanResourcesDao {
	
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SipImportToHumanResourcesDaoJdbc.class);
	private ExportUtil exportUtil = new ExportUtil();
	
	public SipImportToHumanResourcesDaoJdbc() {
		
	}

    public Collection<SipImportDataForHr> getSipImportDataForHr() {
       
        try {
        	// Build the SQL
	        String sqlString = "select POSITION_NBR, EmplID, COMP_RT, NEW_COMP_RT, COMP_FREQ from CU_LD_BCN_SIP_T";

            // Get the SIP data from the data base, map it to the object and build a result set of objects to be returned to the user.
        
	        ParameterizedRowMapper<SipImportDataForHr> mapper = new ParameterizedRowMapper<SipImportDataForHr>() {
	            public SipImportDataForHr mapRow(ResultSet rs, int rowNum) throws SQLException {
	            	SipImportDataForHr sipImportDataForHr = new SipImportDataForHr();
	            	
	            	/**
	            	 * Notes regarding how this class should be populated (based on the document 
	            	 *  "HRIF0080 HR SIP Salary Improvement Award Input.doc" in JIRA KITI-3126):
	            	 * 
	            	 * 1. The dataset must be tab separated
	            	 * 2. Position ID must contain 8 characters.  If the leading 00’s are missing, they will be added
	            	 * 3. Both compensation rates (pre and post SIP) must have a decimal place with two digits following the 
	            	 * 	  compensation rate (even for annual salaries)
	            	 * 4. Action Code in dataset should always return ‘PAY’
	            	 * 5. Action Reason in dataset should always return ‘SIP’
	            	 * 6. Effective Date should be in YYYY-MM-DD format and should always return the first day of the fiscal year
	            	 * 7. Step default should always return ‘1’ in all cases.
	            	 */
	            	
	            	sipImportDataForHr.setPositionID(exportUtil.removeNulls(rs.getString("POSITION_NBR"), false));
	            	sipImportDataForHr.setEmplID(exportUtil.removeNulls(rs.getString("EmplID"), false));
	            	sipImportDataForHr.setPreSIPCompRate(exportUtil.removeNulls(rs.getString("COMP_RT"), true));
	            	sipImportDataForHr.setPostSIPCompRate(exportUtil.removeNulls(rs.getString("NEW_COMP_RT"), true));
	            	sipImportDataForHr.setActionCode("PAY");
	            	sipImportDataForHr.setActionReason("SIP");
	            	sipImportDataForHr.setSIPEffectiveDate(CUBudgetParameterFinder.getBaseFiscalYear() + "-07-01");
	            	sipImportDataForHr.setCompensationFrequency(exportUtil.removeNulls(rs.getString("COMP_FREQ"), false));
	            	sipImportDataForHr.setUAWPostSIPStep("1");

	                return sipImportDataForHr;
	            }
	        };
	        
	        return this.getSimpleJdbcTemplate().query(sqlString, mapper);
        }
        
        catch (Exception ex) {
        	LOG.info("SipImportToHumanResourcesDaoJdbc Exception: " + ex.getMessage());
        	return null;
        }
    }
    
    public class SipImportDataForHr
    {
    	private String PositionID;
    	private String EmplID;
    	private String PreSIPCompRate;
    	private String PostSIPCompRate;
    	private String ActionCode;
    	private String ActionReason;
    	private String SIPEffectiveDate;
    	private String CompensationFrequency;
    	private String UAWPostSIPStep;
    	        
        /**
         * Default constructor.
         */
        public SipImportDataForHr() {
            super();
        }

		/**
		 * @return the positionID
		 */
		public String getPositionID() {
			return PositionID;
		}

		/**
		 * @param positionID the positionID to set
		 */
		public void setPositionID(String positionID) {
			PositionID = positionID;
		}

		/**
		 * @return the emplID
		 */
		public String getEmplID() {
			return EmplID;
		}

		/**
		 * @param emplID the emplID to set
		 */
		public void setEmplID(String emplID) {
			EmplID = emplID;
		}

		/**
		 * @return the preSIPCompRate
		 */
		public String getPreSIPCompRate() {
			return PreSIPCompRate;
		}

		/**
		 * @param preSIPCompRate the preSIPCompRate to set
		 */
		public void setPreSIPCompRate(String preSIPCompRate) {
			PreSIPCompRate = preSIPCompRate;
		}

		/**
		 * @return the postSIPCompRate
		 */
		public String getPostSIPCompRate() {
			return PostSIPCompRate;
		}

		/**
		 * @param postSIPCompRate the postSIPCompRate to set
		 */
		public void setPostSIPCompRate(String postSIPCompRate) {
			PostSIPCompRate = postSIPCompRate;
		}

		/**
		 * @return the actionCode
		 */
		public String getActionCode() {
			return ActionCode;
		}

		/**
		 * @param actionCode the actionCode to set
		 */
		public void setActionCode(String actionCode) {
			ActionCode = actionCode;
		}

		/**
		 * @return the actionReason
		 */
		public String getActionReason() {
			return ActionReason;
		}

		/**
		 * @param actionReason the actionReason to set
		 */
		public void setActionReason(String actionReason) {
			ActionReason = actionReason;
		}

		/**
		 * @return the sIPEffectiveDate
		 */
		public String getSIPEffectiveDate() {
			return SIPEffectiveDate;
		}

		/**
		 * @param sIPEffectiveDate the sIPEffectiveDate to set
		 */
		public void setSIPEffectiveDate(String sIPEffectiveDate) {
			SIPEffectiveDate = sIPEffectiveDate;
		}

		/**
		 * @return the compensationFrequency
		 */
		public String getCompensationFrequency() {
			return CompensationFrequency;
		}

		/**
		 * @param compensationFrequency the compensationFrequency to set
		 */
		public void setCompensationFrequency(String compensationFrequency) {
			CompensationFrequency = compensationFrequency;
		}

		/**
		 * @return the uAWPostSIPStep
		 */
		public String getUAWPostSIPStep() {
			return UAWPostSIPStep;
		}

		/**
		 * @param uAWPostSIPStep the uAWPostSIPStep to set
		 */
		public void setUAWPostSIPStep(String uAWPostSIPStep) {
			UAWPostSIPStep = uAWPostSIPStep;
		}


    }
}
