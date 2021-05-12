package edu.cornell.kfs.module.purap.dataaccess.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;
import org.springframework.jdbc.core.RowMapper;

import edu.cornell.kfs.module.purap.businessobject.LevelOrganization;
import edu.cornell.kfs.module.purap.dataaccess.LevelOrganizationDao;

public class LevelOrganizationDaoJdbc extends PlatformAwareDaoBaseJdbc implements LevelOrganizationDao {

	private static final Logger LOG = LogManager.getLogger(LevelOrganizationDaoJdbc.class);

    /**
     * Constructs a new LevelOrganizationDaoJdbc
     * 
     */
    public LevelOrganizationDaoJdbc() {
        super();

    }

    /**
     * This overridden method ...
     * 
     * @see edu.cornell.kfs.module.purap.dataaccess.LevelOrganizationDao#getCLevelOrganizations()
     */
    public List<LevelOrganization> getCLevelOrganizations() {
        try {
            // Build the SQL
            StringBuilder sqlBuilder = new StringBuilder(3500);
            sqlBuilder.append("select fin_coa_cd, org_cd, org_nm from ca_org_t where fin_coa_cd in('IT') and ORG_ACTIVE_CD = 'Y' and org_cd <> 'XXXX' and org_typ_cd = 'C' order by fin_coa_cd desc, org_nm");

            String sqlString = sqlBuilder.toString();

            RowMapper<LevelOrganization> mapper = new RowMapper<LevelOrganization>() {
                public LevelOrganization mapRow(ResultSet rs, int rowNum) throws SQLException {
                    LevelOrganization cLevelOrganization = new LevelOrganization();
                    cLevelOrganization.setCode(rs.getString("fin_coa_cd") + "-" + rs.getString("org_cd"));
                    cLevelOrganization.setName(rs.getString("org_nm"));

                    return cLevelOrganization;
                }
            };

            return this.getJdbcTemplate().query(sqlString, mapper);
        } catch (Exception ex) {
            LOG.info("LevelOrganizationDaoJdbc Exception: " + ex.getMessage());
            return null;
        }
    }

    /**
     * This overridden method ...
     * 
     * @see edu.cornell.kfs.module.purap.dataaccess.LevelOrganizationDao#getDLevelOrganizations(java.lang.String)
     */
    public List<LevelOrganization> getDLevelOrganizations(String cLevelOrg) {
        if (StringUtils.isNotEmpty(cLevelOrg) && cLevelOrg.contains("-")) {

            String chart = cLevelOrg.substring(0, cLevelOrg.lastIndexOf("-"));
            String cOrg = cLevelOrg.substring(cLevelOrg.lastIndexOf("-") + 1, cLevelOrg.length());

            try {
                // Build the SQL
                StringBuilder sqlBuilder = new StringBuilder(3500);
                sqlBuilder.append(" select D_Level_Code, D_Level_Name from ( select ");
                sqlBuilder.append("       org_cd,");
                sqlBuilder.append("                  ( select org_cd ");
                sqlBuilder.append("                    from ca_org_t where org_typ_cd='C' and ROWNUM=1 ");
                sqlBuilder.append("              start with org_cd=t2.org_cd and fin_coa_cd= ? ");
                sqlBuilder
                        .append("                   connect by prior rpts_to_org_cd = org_cd and rpts_to_org_cd not in ('UNIV') and fin_coa_cd=?) C_Level_Code, ");
                sqlBuilder.append("               ( select org_nm ");
                sqlBuilder.append("               from ca_org_t where org_typ_cd='C' and ROWNUM=1 ");
                sqlBuilder.append("                start with org_cd=t2.org_cd and fin_coa_cd=? ");
                sqlBuilder
                        .append("                 connect by prior rpts_to_org_cd = org_cd and rpts_to_org_cd not in ('UNIV') and fin_coa_cd=?) C_Level_Name, ");

                sqlBuilder.append("                ( select org_cd from ca_org_t where org_typ_cd='D' and ROWNUM=1 ");
                sqlBuilder.append("              start with org_cd=t2.org_cd and fin_coa_cd=?");
                sqlBuilder
                        .append("             connect by prior rpts_to_org_cd = org_cd and rpts_to_org_cd not in ('UNIV') and fin_coa_cd=?) D_Level_Code,");
                sqlBuilder
                        .append("                       ( select org_nm from ca_org_t where org_typ_cd='D' and ROWNUM=1 ");
                sqlBuilder.append("                start with org_cd=t2.org_cd and fin_coa_cd=?");
                sqlBuilder
                        .append("                connect by prior rpts_to_org_cd = org_cd and rpts_to_org_cd not in ('UNIV') and fin_coa_cd=?) D_Level_Name ");

                sqlBuilder.append("             from  ca_org_t t2 where t2.org_typ_cd = 'D' and ORG_ACTIVE_CD = 'Y' ");
                sqlBuilder.append("             ) t where t.C_Level_Code = ? order by D_Level_Name");

                String sqlString = sqlBuilder.toString();

                // Get the SIP data from the data base, map it to the object and build a result set of objects to be returned to the user.

                RowMapper<LevelOrganization> mapper = new RowMapper<LevelOrganization>() {
                    public LevelOrganization mapRow(ResultSet rs, int rowNum) throws SQLException {
                        LevelOrganization cLevelOrganization = new LevelOrganization();
                        cLevelOrganization.setCode(rs.getString("D_Level_Code"));
                        cLevelOrganization.setName(rs.getString("D_Level_Name"));

                        return cLevelOrganization;
                    }
                };

                return this.getJdbcTemplate().query(sqlString, mapper, chart, chart, chart, chart, chart, chart,
                        chart, chart, cOrg);
            } catch (Exception ex) {
                LOG.info("LevelOrganizationDaoJdbc Exception: " + ex.getMessage());
                return null;
            }
        } else
            return null;
    }

    /**
     * This overridden method ...
     * 
     * @see edu.cornell.kfs.module.purap.dataaccess.LevelOrganizationDao#getCLevelOrganizationForDLevelOrg(java.lang.String)
     */
    public String getCLevelOrganizationForDLevelOrg(String dLevelOrg) {
        String chart = dLevelOrg.substring(0, dLevelOrg.lastIndexOf("-"));
        String dOrg = dLevelOrg.substring(dLevelOrg.lastIndexOf("-") + 1, dLevelOrg.length());

        try {
            // Build the SQL
            StringBuilder sqlBuilder = new StringBuilder(3500);

            sqlBuilder.append("SELECT ");
            sqlBuilder.append(" (SELECT org_cd");
            sqlBuilder.append(" FROM ca_org_t");
            sqlBuilder.append(" WHERE org_typ_cd                  ='C'");
            sqlBuilder.append(" AND ROWNUM                        =1");
            sqlBuilder.append("   START WITH org_cd               =t2.org_cd");
            sqlBuilder.append("  AND fin_coa_cd                    =?");
            sqlBuilder.append("    CONNECT BY prior rpts_to_org_cd = org_cd");
            sqlBuilder.append(" AND rpts_to_org_cd NOT           IN ('UNIV')");
            sqlBuilder.append(" AND fin_coa_cd                    =?");
            sqlBuilder.append(" ) C_Level_Code");

            sqlBuilder.append(" from ca_org_t t2 where t2.org_cd = ?");

            String sqlString = sqlBuilder.toString();

            RowMapper<String> mapper = new RowMapper<String>() {
                public String mapRow(ResultSet rs, int rowNum) throws SQLException {

                    return rs.getString("C_Level_Code");
                }
            };

            List<String> results = this.getJdbcTemplate().query(sqlString, mapper, chart, chart, dOrg);
            return chart + "-" + results.get(0);

        } catch (Exception ex) {
            LOG.info("LevelOrganizationDaoJdbc Exception: " + ex.getMessage());
            return null;
        }
    }

}
