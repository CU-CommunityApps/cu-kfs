package edu.cornell.kfs.coa.batch.dataaccess.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.springframework.jdbc.core.RowMapper;

import edu.cornell.kfs.coa.batch.businessobject.WorkdayOpenAccountDetailDTO;
import edu.cornell.kfs.coa.batch.dataaccess.WorkdayOpenAccountDao;

public class WorkdayOpenAccountDaoJdbc extends PlatformAwareDaoBaseJdbc implements WorkdayOpenAccountDao {
    private static final Logger LOG = LogManager.getLogger();
    
    protected static final String FIN_COA_CD = "FIN_COA_CD";
    protected static final String ACCOUNT_NBR = "ACCOUNT_NBR";
    protected static final String ACCOUNT_NM = "ACCOUNT_NM";
    protected static final String SUB_FUND_GRP_WAGE_IND = "SUB_FUND_GRP_WAGE_IND";
    protected static final String SUB_FUND_GRP_CD = "SUB_FUND_GRP_CD";
    protected static final String FIN_HGH_ED_FUNC_CD = "FIN_HGH_ED_FUNC_CD";
    protected static final String ACCT_EFFECT_DT = "ACCT_EFFECT_DT";
    protected static final String ACCT_CLOSED_IND = "ACCT_CLOSED_IND";
    protected static final String ACCT_TYP_CD = "ACCT_TYP_CD";
    protected static final String SUB_ACCT_NBR = "SUB_ACCT_NBR";
    protected static final String SUB_ACCT_NM = "SUB_ACCT_NM";
    protected static final String SUB_ACCT_ACTV_CD = "SUB_ACCT_ACTV_CD";
    protected static final String FIN_OBJECT_CD = "FIN_OBJECT_CD";
    protected static final String FIN_SUB_OBJ_CD = "FIN_SUB_OBJ_CD";
    protected static final String FIN_SUB_OBJ_CD_NM = "FIN_SUB_OBJ_CD_NM";
    protected static final String CG_CFDA_NBR = "CG_CFDA_NBR";
    
    protected UniversityDateService universityDateService;

    @Override
    public List<WorkdayOpenAccountDetailDTO> getWorkdayOpenAccountDetails() {
        try {
            RowMapper<WorkdayOpenAccountDetailDTO> rowMapper = (resultSet, rowNumber) -> {
                WorkdayOpenAccountDetailDTO detail = new WorkdayOpenAccountDetailDTO();
                detail.setChart(resultSet.getString(FIN_COA_CD));
                detail.setAccountNumber(resultSet.getString(ACCOUNT_NBR));
                detail.setAccountName(resultSet.getString(ACCOUNT_NM));
                detail.setSubFundGroupWageIndicator(resultSet.getString(SUB_FUND_GRP_WAGE_IND));
                detail.setSubFundGroupCode(resultSet.getString(SUB_FUND_GRP_CD));
                detail.setHigherEdFunctionCode(resultSet.getString(FIN_HGH_ED_FUNC_CD));
                detail.setAccountEffectiveDate(resultSet.getDate(ACCT_EFFECT_DT));
                detail.setAccountClosedIndicator(resultSet.getString(ACCT_CLOSED_IND));
                detail.setAccountTypeCode(resultSet.getString(ACCT_TYP_CD));
                detail.setSubAccountNumber(resultSet.getString(SUB_ACCT_NBR));
                detail.setSubAccountName(resultSet.getString(SUB_ACCT_NM));
                detail.setSubAccountActiveIndicator(resultSet.getString(SUB_ACCT_ACTV_CD));
                detail.setObjectCode(resultSet.getString(FIN_OBJECT_CD));
                detail.setSubObjectCode(resultSet.getString(FIN_SUB_OBJ_CD));
                detail.setSubObjectName(resultSet.getString(FIN_SUB_OBJ_CD_NM));
                detail.setAccountCfdaNumber(resultSet.getString(CG_CFDA_NBR));
                return detail;
            };
            return this.getJdbcTemplate().query(buildFullOpenAccountSql(), rowMapper);
        } catch (Exception e) {
            LOG.error("getWorkdayOpenAccountDetails, had an error getting open account details: ", e);
            throw new RuntimeException(e);
        }
    }
    
    protected String buildFullOpenAccountSql() {
        StringBuilder sb = new StringBuilder();
        sb.append(buildAccountSubQuery());
        sb.append(buildUnionStatement());
        sb.append(buildSubAccountSubQuery());
        sb.append(buildUnionStatement());
        sb.append(buildSubObjectSubQuery());
        sb.append(buildOrderByClause());
        String fullSQL = sb.toString();
        LOG.info("buildFullOpenAccountSql, the open account SQL statement is " + fullSQL);
        return fullSQL;
    }
    
    private String buildAccountSubQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append(buildBaseSelect());
        sb.append(buildEmptySubAccountSelect());
        sb.append(buildEmptySubObjectSelect());
        sb.append(buildBaseFromAndJoin());
        sb.append(buildBaseWhere());
        return sb.toString();
    }
    
    private String buildUnionStatement() {
        return " UNION ";
    }
    
    private String buildSubAccountSubQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append(buildBaseSelect());
        sb.append("CSA.SUB_ACCT_NBR, CSA.SUB_ACCT_NM, CSA.SUB_ACCT_ACTV_CD, ");
        sb.append(buildEmptySubObjectSelect());
        sb.append(buildBaseFromAndJoin());
        sb.append("JOIN KFS.CA_SUB_ACCT_T CSA ON CAT.FIN_COA_CD = CSA.FIN_COA_CD AND CAT.ACCOUNT_NBR = CSA.ACCOUNT_NBR ");
        sb.append(buildBaseWhere());
        sb.append("AND CSA.SUB_ACCT_ACTV_CD = 'Y' ");
        return sb.toString();
    }
    
    private String buildSubObjectSubQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append(buildBaseSelect());
        sb.append(buildEmptySubAccountSelect());
        sb.append("CSO.FIN_OBJECT_CD, CSO.FIN_SUB_OBJ_CD, CSO.FIN_SUB_OBJ_CD_NM, CAT.CG_CFDA_NBR ");
        sb.append(buildBaseFromAndJoin());
        sb.append("JOIN KFS.CA_SUB_OBJECT_CD_T CSO ON CAT.FIN_COA_CD = CSO.FIN_COA_CD AND CAT.ACCOUNT_NBR = CSO.ACCOUNT_NBR ");
        sb.append("JOIN KFS.LD_LABOR_OBJ_T COC ON CAT.FIN_COA_CD = COC.FIN_COA_CD AND CSO.FIN_OBJECT_CD = COC.FIN_OBJECT_CD AND CSO.UNIV_FISCAL_YR = COC.UNIV_FISCAL_YR ");
        sb.append(buildBaseWhere());
        sb.append("AND CSO.UNIV_FISCAL_YR = ").append(universityDateService.getCurrentFiscalYear());
        sb.append(" AND COC.ACTV_IND = 'Y' ");
        return sb.toString();
    }
    
    private String buildBaseSelect() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT CAT.FIN_COA_CD, CAT.ACCOUNT_NBR, CAT.ACCOUNT_NM, CSF.SUB_FUND_GRP_WAGE_IND, ");
        sb.append("CAT.SUB_FUND_GRP_CD, CAT.FIN_HGH_ED_FUNC_CD, CAT.ACCT_EFFECT_DT,  CAT.ACCT_CLOSED_IND, CAT.ACCT_TYP_CD, ");
        return sb.toString();
    }
    
    private String buildEmptySubAccountSelect() {
        return "'' AS SUB_ACCT_NBR, '' AS SUB_ACCT_NM, '' AS SUB_ACCT_ACTV_CD, ";
    }
    
    private String buildEmptySubObjectSelect() {
        return "'' AS FIN_OBJECT_CD, '' AS FIN_SUB_OBJ_CD, '' AS FIN_SUB_OBJ_CD_NM, CAT.CG_CFDA_NBR ";
    }
    
    private String buildBaseFromAndJoin() {
        StringBuilder sb = new StringBuilder();
        sb.append("FROM KFS.CA_ACCOUNT_T CAT ");
        sb.append("JOIN KFS.CA_SUB_FUND_GRP_T CSF ON CAT.SUB_FUND_GRP_CD = CSF.SUB_FUND_GRP_CD ");
        return sb.toString();
    }
    
    private String buildBaseWhere() {
        StringBuilder sb = new StringBuilder();
        sb.append("WHERE CAT.ACCT_CLOSED_IND = 'N' ");
        sb.append("AND CSF.SUB_FUND_GRP_WAGE_IND = 'Y' ");
        sb.append("AND CAT.FIN_COA_CD = 'IT' ");
        return sb.toString();
    }
    
    private String buildOrderByClause() {
        StringBuilder sb = new StringBuilder();
        sb.append(" ORDER BY ").append(FIN_COA_CD).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(ACCOUNT_NBR).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(SUB_ACCT_NBR).append(KFSConstants.COMMA).append(KFSConstants.BLANK_SPACE);
        sb.append(FIN_SUB_OBJ_CD);
        return sb.toString();
    }

    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

}
