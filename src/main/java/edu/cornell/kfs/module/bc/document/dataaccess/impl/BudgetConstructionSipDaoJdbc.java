package edu.cornell.kfs.module.bc.document.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.kuali.kfs.module.bc.document.dataaccess.impl.BudgetConstructionDaoJdbcBase;
import org.kuali.kfs.pdp.batch.service.impl.ExtractPaymentServiceImpl;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import edu.cornell.kfs.module.bc.document.dataaccess.BudgetConstructionSipDao;

public class BudgetConstructionSipDaoJdbc extends BudgetConstructionDaoJdbcBase implements BudgetConstructionSipDao {
	
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(BudgetConstructionSipDaoJdbc.class);
	
	public BudgetConstructionSipDaoJdbc() {
		
	}

    public Collection<SIPExportData> getSIPExtractByPersonUnivId(String univId, boolean bExecutivesOnly) {
       
        try {
        	// Build the SQL
	        StringBuilder sqlBuilder = new StringBuilder(3500);
	        sqlBuilder.append("select ");
	        sqlBuilder.append("( select org_nm ");
	        sqlBuilder.append("from ca_org_t where org_typ_cd='C' and ROWNUM=1 ");
	        sqlBuilder.append("start with org_cd=substr(t2.pos_deptid,4) and fin_coa_cd=substr(t2.pos_deptid,1,2) ");
	        sqlBuilder.append("connect by prior rpts_to_org_cd = org_cd and rpts_to_org_cd not in ('UNIV') and fin_coa_cd=substr(t2.pos_deptid,1,2)) \"C_Level_Name\", ");
	        sqlBuilder.append("t8.DEPTID as HR_DEPTID,");
	        sqlBuilder.append("t2.pos_deptid,");
	        sqlBuilder.append("( select org_nm from ca_org_t where org_typ_cd='D' and ROWNUM=1 ");
	        sqlBuilder.append("start with org_cd=substr(t2.pos_deptid,4) and fin_coa_cd=substr(t2.pos_deptid,1,2) ");
	        sqlBuilder.append("connect by prior rpts_to_org_cd = org_cd and rpts_to_org_cd not in ('UNIV') and fin_coa_cd=substr(t2.pos_deptid,1,2)) \"D_Level_Name\", ");
	        sqlBuilder.append("t2.POSITION_NBR, t2.POS_DESCR, t3.EMPLID, t4.PERSON_NM, t5.SIP_ELIG_FLAG \"SIP_Eligibility\", t5.empl_typ \"SIP_Employee_Type\", ");
	        sqlBuilder.append("t5.EMPL_RCD, t2.JOBCODE, t7.JOB_CD_DESC_SHRT, t7.JOB_FAMILY, t5.CU_PLANNED_FTE, t2.POS_GRADE_DFLT, t6.CU_STATE_CERT, ");
	        sqlBuilder.append("t7.COMP_FREQ, t5.ANNL_RT, t5.COMP_RT, t5.JOB_STD_HRS, t6.WRK_MNTHS, t7.JOB_FUNC, t7.JOB_FUNC_DESC, ");
	        sqlBuilder.append("'0' \"Increase_To_Minimum\", '0' \"Equity\", '0' \"Merit\", '' \"Note\", '0' \"Deferred\", t5.CU_ABBR_FLAG, ");
	        sqlBuilder.append("t3.APPT_TOT_INTND_AMT, t3.APPT_RQST_FTE_QTY, t2.IU_POSITION_TYPE ");
	        sqlBuilder.append("from ( select distinct org_cd from ca_org_t start with org_cd in (select distinct(SEL_ORG_CD) from ld_bcn_ctrl_list_t where person_unvl_id = ?) connect by prior org_cd = rpts_to_org_cd ) t1 ");
	        sqlBuilder.append("inner join LD_BCN_POS_T t2 on t1.org_cd=substr(t2.pos_deptid,4) ");
	        sqlBuilder.append("inner join (select Univ_Fiscal_yr, position_nbr, emplid, sum(APPT_RQST_CSF_AMT) AS APPT_RQST_CSF_AMT, sum(APPT_RQST_FTE_QTY) AS APPT_RQST_FTE_QTY, sum(APPT_TOT_INTND_AMT) AS APPT_TOT_INTND_AMT from LD_PNDBC_APPTFND_T group by Univ_Fiscal_yr, position_nbr, emplid) t3 on t3.position_nbr=t2.position_nbr ");
	        sqlBuilder.append("left join LD_BCN_INTINCBNT_T t4 on t4.EMPLID=t3.emplid ");
	        sqlBuilder.append("left join CU_PS_JOB_DATA t5 on t5.pos_nbr=t2.position_nbr and t5.emplid=t3.emplid ");
	        sqlBuilder.append("left join CU_PS_POSITION_EXTRA t6 on t6.pos_nbr=t2.position_nbr ");
	        sqlBuilder.append("left join CU_PS_JOB_CD t7 on t7.job_cd=t6.job_cd, ");
	        sqlBuilder.append("ps_position_data t8 ");
	        sqlBuilder.append("where  ");
	        sqlBuilder.append("t3.univ_fiscal_yr=t2.univ_fiscal_yr and t3.emplid<>'VACANT' and t5.SIP_ELIG_FLAG='Y' and ");
	        sqlBuilder.append("t8.position_nbr=t2.position_nbr and t8.effdt=(select max(t9.effdt) from ps_position_data t9 where t9.position_nbr=t8.position_nbr) ");
	        // If the user selected executives only
	        if (bExecutivesOnly)
	        	sqlBuilder.append(" and t5.empl_typ in ('Z')  ");
	        else
	        	sqlBuilder.append(" and t5.empl_typ not in ('Z')  ");
	        
	        sqlBuilder.append("order by  ");
	        sqlBuilder.append("\"C_Level_Name\",  ");
	        sqlBuilder.append("t2.pos_deptid,  ");
	        sqlBuilder.append("t2.POSITION_NBR  ");
	        String sqlString = sqlBuilder.toString();

            // Get the SIP data from the data base, map it to the object and build a result set of objects to be returned to the user.
        
	        ParameterizedRowMapper<SIPExportData> mapper = new ParameterizedRowMapper<SIPExportData>() {
	            public SIPExportData mapRow(ResultSet rs, int rowNum) throws SQLException {
	            	SIPExportData sipExportData = new SIPExportData();
	            	sipExportData.setC_Level_Name(rs.getString("C_Level_Name"));
	            	sipExportData.setHR_DEPTID(rs.getString("HR_DEPTID"));
	            	sipExportData.setPOS_DEPTID(rs.getString("POS_DEPTID"));
	            	sipExportData.setD_Level_Name(rs.getString("D_Level_Name"));
	            	sipExportData.setPOSITION_NBR(rs.getString("POSITION_NBR"));
	            	sipExportData.setPOS_DESCR(rs.getString("POS_DESCR"));
	            	sipExportData.setEMPLID(rs.getString("EMPLID"));
	            	sipExportData.setPERSON_NM(rs.getString("PERSON_NM"));
	            	sipExportData.setSIP_Eligibility(rs.getString("SIP_Eligibility"));
	            	sipExportData.setSIP_Employee_Type(rs.getString("SIP_Employee_Type"));
	            	sipExportData.setEMPL_RCD(rs.getString("EMPL_RCD"));
	            	sipExportData.setJOBCODE(rs.getString("JOBCODE"));
	            	sipExportData.setJOB_CD_DESC_SHRT(rs.getString("JOB_CD_DESC_SHRT"));
	            	sipExportData.setJOB_FAMILY(rs.getString("JOB_FAMILY"));
	            	sipExportData.setCU_PLANNED_FTE(rs.getString("CU_PLANNED_FTE"));
	            	sipExportData.setPOS_GRADE_DFLT(rs.getString("POS_GRADE_DFLT"));
	            	sipExportData.setCU_STATE_CERT(rs.getString("CU_STATE_CERT"));
	            	sipExportData.setCOMP_FREQ(rs.getString("COMP_FREQ"));
	            	sipExportData.setANNL_RT(rs.getString("ANNL_RT"));
	            	sipExportData.setCOMP_RT(rs.getString("COMP_RT"));
	            	sipExportData.setJOB_STD_HRS(rs.getString("JOB_STD_HRS"));
	            	sipExportData.setWRK_MNTHS(rs.getString("WRK_MNTHS"));
	            	sipExportData.setJOB_FUNC(rs.getString("JOB_FUNC"));
	            	sipExportData.setJOB_FUNC_DESC(rs.getString("JOB_FUNC_DESC"));
	            	sipExportData.setIncrease_To_Minimum(rs.getString("Increase_To_Minimum"));
	            	sipExportData.setEquity(rs.getString("Equity"));
	            	sipExportData.setMerit(rs.getString("Merit"));
	            	sipExportData.setNote(rs.getString("Note"));
	            	sipExportData.setDeferred(rs.getString("Deferred"));
	            	sipExportData.setCU_ABBR_FLAG(rs.getString("CU_ABBR_FLAG"));
	            	sipExportData.setAPPT_TOT_INTND_AMT(rs.getString("APPT_TOT_INTND_AMT"));
	            	sipExportData.setAPPT_RQST_FTE_QTY(rs.getString("APPT_RQST_FTE_QTY"));
	            	sipExportData.setIU_POSITION_TYPE(rs.getString("IU_POSITION_TYPE"));

	                return sipExportData;
	            }
	        };
	        
	        return this.getSimpleJdbcTemplate().query(sqlString, mapper, univId);
        }
        
        catch (Exception ex) {
        	LOG.info("BudgetConstructionSipDaoJdbc Exception: " + ex.getMessage());
        	return null;
        }
    }
    
    public class SIPExportData
    {
        private String C_Level_Name;
        private String HR_DEPTID;
        private String POS_DEPTID;
        private String D_Level_Name;
        private String POSITION_NBR;
        private String POS_DESCR;
        private String EMPLID;
        private String PERSON_NM;
        private String SIP_Eligibility;
        private String SIP_Employee_Type;
        private String EMPL_RCD;
        private String JOBCODE;
        private String JOB_CD_DESC_SHRT;
        private String JOB_FAMILY;
        private String CU_PLANNED_FTE;
        private String POS_GRADE_DFLT;
        private String CU_STATE_CERT;
        private String ANNL_RT;
        private String COMP_RT;
        private String JOB_STD_HRS;
        private String COMP_FREQ;
        private String WRK_MNTHS;
        private String JOB_FUNC;
        private String JOB_FUNC_DESC;
        private String Increase_To_Minimum;
        private String Equity;
        private String Merit;
        private String Note;
        private String Deferred;
        private String CU_ABBR_FLAG;
        private String APPT_TOT_INTND_AMT;
        private String APPT_RQST_FTE_QTY;
        private String IU_POSITION_TYPE;
        
        /**
         * Default constructor.
         */
        public SIPExportData() {
            super();
        }

        /**
         * Gets the C_Level_Name
         * 
         * @return Returns the C_Level_Name
         */
        public String getC_Level_Name() {
            return C_Level_Name;
        }
        
        /**
         * Gets the DEPTID
         * 
         * @return Returns the DEPTID
         */
		public String getHR_DEPTID() {
			return HR_DEPTID;
		}

        /**
         * Gets the POS_DEPTID
         * 
         * @return Returns the POS_DEPTID
         */
        public String getPOS_DEPTID() {
            return POS_DEPTID;
        }

        /**
         * Gets the D_Level_Name
         * 
         * @return Returns the D_Level_Name
         */
        public String getD_Level_Name() {
            return D_Level_Name;
        }

        /**
         * Gets the POSITION_NBR
         * 
         * @return Returns the POSITION_NBR
         */
        public String getPOSITION_NBR() {
            return POSITION_NBR;
        }

        /**
         * Gets the POS_DESCR
         * 
         * @return Returns the POS_DESCR
         */
        public String getPOS_DESCR() {
            return POS_DESCR;
        }

        /**
         * Gets the EMPLID
         * 
         * @return Returns the EMPLID
         */
        public String getEMPLID() {
            return EMPLID;
        }

        /**
         * Gets the PERSON_NM
         * 
         * @return Returns the PERSON_NM
         */
        public String getPERSON_NM() {
            return PERSON_NM;
        }

        /**
         * Gets the SIP_Eligibility
         * 
         * @return Returns the SIP_Eligibility
         */
        public String getSIP_Eligibility() {
            return SIP_Eligibility;
        }

        /**
         * Gets the SIP_Employee_Type
         * 
         * @return Returns the SIP_Employee_Type
         */
        public String getSIP_Employee_Type() {
            return SIP_Employee_Type;
        }

        /**
         * Gets the EMPL_RCD
         * 
         * @return Returns the EMPL_RCD
         */
        public String getEMPL_RCD() {
            return EMPL_RCD;
        }

        /**
         * Gets the JOBCODE
         * 
         * @return Returns the JOBCODE
         */
        public String getJOBCODE() {
            return JOBCODE;
        }

        /**
         * Gets the JOB_CD_DESC_SHRT
         * 
         * @return Returns the JOB_CD_DESC_SHRT
         */
        public String getJOB_CD_DESC_SHRT() {
            return JOB_CD_DESC_SHRT;
        }

        /**
         * Gets the JOB_FAMILY
         * 
         * @return Returns the JOB_FAMILY
         */
        public String getJOB_FAMILY() {
            return JOB_FAMILY;
        }

        /**
         * Gets the CU_PLANNED_FTE
         * 
         * @return Returns the CU_PLANNED_FTE
         */
        public String getCU_PLANNED_FTE() {
            return CU_PLANNED_FTE;
        }

        /**
         * Gets the POS_GRADE_DFLT
         * 
         * @return Returns the POS_GRADE_DFLT
         */
        public String getPOS_GRADE_DFLT() {
            return POS_GRADE_DFLT;
        }

        /**
         * Gets the CU_STATE_CERT
         * 
         * @return Returns the CU_STATE_CERT
         */
        public String getCU_STATE_CERT() {
            return CU_STATE_CERT;
        }

        /**
         * Gets the ANNL_RT
         * 
         * @return Returns the ANNL_RT
         */
        public String getANNL_RT() {
            return ANNL_RT;
        }

        /**
         * Gets the COMP_RT
         * 
         * @return Returns the COMP_RT
         */
        public String getCOMP_RT() {
            return COMP_RT;
        }

        /**
         * Gets the JOB_STD_HRS
         * 
         * @return Returns the JOB_STD_HRS
         */
        public String getJOB_STD_HRS() {
            return JOB_STD_HRS;
        }

        /**
         * Gets the COMP_FREQ
         * 
         * @return Returns the COMP_FREQ
         */
        public String getCOMP_FREQ() {
            return COMP_FREQ;
        }

        /**
         * Gets the WRK_MNTHS
         * 
         * @return Returns the WRK_MNTHS
         */
        public String getWRK_MNTHS() {
            return WRK_MNTHS;
        }

        /**
         * Gets the JOB_FUNC
         * 
         * @return Returns the JOB_FUNC
         */
        public String getJOB_FUNC() {
            return JOB_FUNC;
        }

        /**
         * Gets the JOB_FUNC_DESC
         * 
         * @return Returns the JOB_FUNC_DESC
         */
        public String getJOB_FUNC_DESC() {
            return JOB_FUNC_DESC;
        }

        /**
         * Gets the Increase_To_Minimum
         * 
         * @return Returns the Increase_To_Minimum
         */
        public String getIncrease_To_Minimum() {
            return Increase_To_Minimum;
        }

        /**
         * Gets the Equity
         * 
         * @return Returns the Equity
         */
        public String getEquity() {
            return Equity;
        }

        /**
         * Gets the Merit
         * 
         * @return Returns the Merit
         */
        public String getMerit() {
            return Merit;
        }

        /**
         * Gets the Note
         * 
         * @return Returns the Note
         */
        public String getNote() {
            return Note;
        }

        /**
         * Gets the Deferred
         * 
         * @return Returns the Deferred
         */
        public String getDeferred() {
            return Deferred;
        }

        /**
         * Gets the CU_ABBR_FLAG
         * 
         * @return Returns the CU_ABBR_FLAG
         */
        public String getCU_ABBR_FLAG() {
            return CU_ABBR_FLAG;
        }
        
        /**
         * Gets the APPT_TOT_INTND_AMT
         * 
         * @return Returns the APPT_TOT_INTND_AMT
         */
		public String getAPPT_TOT_INTND_AMT() {
			return APPT_TOT_INTND_AMT;
		}

        /**
         * Gets the APPT_RQST_FTE_QTY
         * 
         * @return Returns the APPT_RQST_FTE_QTY
         */
		public String getAPPT_RQST_FTE_QTY() {
			return APPT_RQST_FTE_QTY;
		}

        /**
         * Gets the IU_POSITION_TYPE
         * 
         * @return Returns the IU_POSITION_TYPE
         */
        public String getIU_POSITION_TYPE() {
            return IU_POSITION_TYPE;
        }
        
        //SETS
        /**
         * sets the C_Level_Name
         * 
         * @return Returns void
         */
        public void setC_Level_Name(String C_Level_Name) {
        	this.C_Level_Name = C_Level_Name;
        }
        
        /**
         * sets the HR_dEPTID
         * 
         * @return Returns void
         */
		public void setHR_DEPTID(String HR_dEPTID) {
			HR_DEPTID = HR_dEPTID;
		}
		
        /**
         * sets the POS_DEPTID
         * 
         * @return Returns void
         */
        public void setPOS_DEPTID(String POS_DEPTID) {
            this.POS_DEPTID = POS_DEPTID;
        }

        /**
         * sets the D_Level_Name
         * 
         * @return Returns void
         */
        public void setD_Level_Name(String D_Level_Name) {
            this.D_Level_Name = D_Level_Name;
        }

        /**
         * sets the POSITION_NBR
         * 
         * @return Returns void
         */
        public void setPOSITION_NBR(String POSITION_NBR) {
            this.POSITION_NBR = POSITION_NBR;
        }

        /**
         * sets the POS_DESCR
         * 
         * @return Returns void
         */
        public void setPOS_DESCR(String POS_DESCR) {
            this.POS_DESCR = POS_DESCR;
        }

        /**
         * sets the EMPLID
         * 
         * @return Returns void
         */
        public void setEMPLID(String EMPLID) {
            this.EMPLID = EMPLID;
        }

        /**
         * sets the PERSON_NM
         * 
         * @return Returns void
         */
        public void setPERSON_NM(String PERSON_NM) {
            this.PERSON_NM = PERSON_NM;
        }

        /**
         * sets the SIP_Eligibility
         * 
         * @return Returns void
         */
        public void setSIP_Eligibility(String SIP_Eligibility) {
            this.SIP_Eligibility = SIP_Eligibility;
        }

        /**
         * sets the SIP_Employee_Type
         * 
         * @return Returns void
         */
        public void setSIP_Employee_Type(String SIP_Employee_Type) {
            this.SIP_Employee_Type = SIP_Employee_Type;
        }

        /**
         * sets the EMPL_RCD
         * 
         * @return Returns void
         */
        public void setEMPL_RCD(String EMPL_RCD) {
            this.EMPL_RCD = EMPL_RCD;
        }

        /**
         * sets the JOBCODE
         * 
         * @return Returns void
         */
        public void setJOBCODE(String JOBCODE) {
            this.JOBCODE = JOBCODE;
        }

        /**
         * sets the JOB_CD_DESC_SHRT
         * 
         * @return Returns void
         */
        public void setJOB_CD_DESC_SHRT(String JOB_CD_DESC_SHRT) {
            this.JOB_CD_DESC_SHRT = JOB_CD_DESC_SHRT;
        }

        /**
         * sets the JOB_FAMILY
         * 
         * @return Returns void
         */
        public void setJOB_FAMILY(String JOB_FAMILY) {
            this.JOB_FAMILY = JOB_FAMILY;
        }

        /**
         * sets the POS_FTE
         * 
         * @return Returns void
         */
        public void setCU_PLANNED_FTE(String CU_PLANNED_FTE) {
            this.CU_PLANNED_FTE = CU_PLANNED_FTE;
        }

        /**
         * sets the POS_GRADE_DFLT
         * 
         * @return Returns void
         */
        public void setPOS_GRADE_DFLT(String POS_GRADE_DFLT) {
            this.POS_GRADE_DFLT = POS_GRADE_DFLT;
        }

        /**
         * sets the CU_STATE_CERT
         * 
         * @return Returns void
         */
        public void setCU_STATE_CERT(String CU_STATE_CERT) {
            this.CU_STATE_CERT = CU_STATE_CERT;
        }

        /**
         * sets the ANNL_RT
         * 
         * @return Returns void
         */
        public void setANNL_RT(String ANNL_RT) {
            this.ANNL_RT = ANNL_RT;
        }

        /**
         * sets the COMP_RT
         * 
         * @return Returns void
         */
        public void setCOMP_RT(String COMP_RT) {
            this.COMP_RT = COMP_RT;
        }

        /**
         * sets the POS_STD_HRS_DFLT
         * 
         * @return Returns void
         */
        public void setJOB_STD_HRS(String JOB_STD_HRS) {
            this.JOB_STD_HRS = JOB_STD_HRS;
        }

        /**
         * sets the COMP_FREQ
         * 
         * @return Returns void
         */
        public void setCOMP_FREQ(String COMP_FREQ) {
            this.COMP_FREQ = COMP_FREQ;
        }

        /**
         * sets the WRK_MNTHS
         * 
         * @return Returns void
         */
        public void setWRK_MNTHS(String WRK_MNTHS) {
            this.WRK_MNTHS = WRK_MNTHS;
        }

        /**
         * sets the JOB_FUNC
         * 
         * @return Returns void
         */
        public void setJOB_FUNC(String JOB_FUNC) {
            this.JOB_FUNC = JOB_FUNC;
        }

        /**
         * sets the JOB_FUNC_DESC
         * 
         * @return Returns void
         */
        public void setJOB_FUNC_DESC(String JOB_FUNC_DESC) {
            this.JOB_FUNC_DESC = JOB_FUNC_DESC;
        }

        /**
         * sets the Increase_To_Minimum
         * 
         * @return Returns void
         */
        public void setIncrease_To_Minimum(String Increase_To_Minimum) {
            this.Increase_To_Minimum = Increase_To_Minimum;
        }

        /**
         * sets the Equity
         * 
         * @return Returns void
         */
        public void setEquity(String Equity) {
            this.Equity = Equity;
        }

        /**
         * sets the Merit
         * 
         * @return Returns void
         */
        public void setMerit(String Merit) {
            this.Merit = Merit;
        }

        /**
         * sets the Note
         * 
         * @return Returns void
         */
        public void setNote(String Note) {
            this.Note = Note;
        }

        /**
         * sets the Deferred
         * 
         * @return Returns void
         */
        public void setDeferred(String Deferred) {
            this.Deferred = Deferred;
        }

        /**
         * sets the CU_ABBR_FLAG
         * 
         * @return Returns void
         */
        public void setCU_ABBR_FLAG(String CU_ABBR_FLAG) {
            this.CU_ABBR_FLAG = CU_ABBR_FLAG;
        }

        /**
         * sets the APPT_TOT_INTND_AMT
         * 
         * @return Returns void
         */

		public void setAPPT_TOT_INTND_AMT(String aPPT_TOT_INTND_AMT) {
			APPT_TOT_INTND_AMT = aPPT_TOT_INTND_AMT;
		}
	
        /**
         * sets the APPT_RQST_FTE_QTY
         * 
         * @return Returns void
         */
		public void setAPPT_RQST_FTE_QTY(String aPPT_RQST_FTE_QTY) {
			APPT_RQST_FTE_QTY = aPPT_RQST_FTE_QTY;
		}
        

        /**
         * sets the IU_POSITION_TYPE
         * 
         * @return Returns void
         */
        public void setIU_POSITION_TYPE(String IU_POSITION_TYPE) {
            this.IU_POSITION_TYPE = IU_POSITION_TYPE;
        }
    }
}
