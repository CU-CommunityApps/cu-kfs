package edu.cornell.kfs.module.bc.dataaccess.impl;


import org.kuali.rice.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;

import edu.cornell.kfs.module.bc.dataaccess.DeptOrgCrosswalkDao;

public class DeptOrgCrosswalkDaoJdbc extends PlatformAwareDaoBaseJdbc implements DeptOrgCrosswalkDao{
	public void updatePositionDataExtension(String deptID, String orgCode){
        StringBuilder deleteSQL = new StringBuilder(500);
       // sql.append("select max(effdt),position_nbr from kfs.ps_position_data_tx a where a.position_nbr in (select distinct position_nbr from kfs.ps_position_data where deptid = ?) group by position_nbr");
        deleteSQL.append("delete  from kfs.ps_position_data_tx");
        String deleteSQLStr = deleteSQL.toString();
        getSimpleJdbcTemplate().update(deleteSQLStr);

        StringBuilder insertSQL = new StringBuilder(500);

        insertSQL.append("insert into kfs.ps_position_data_tx (POSITION_NBR,effdt,org_cd,obj_id,ver_nbr) select position_nbr,EFFDT , (CASE WHEN (select KFS_ORG_CD from kfs.cu_ld_bcn_dept_org_t where HR_DEPT = d.deptid ) is NULL then 'XXXX' else (select KFS_ORG_CD from kfs.cu_ld_bcn_dept_org_t where HR_DEPT = d.deptid ) END) org_cd, (select sys_guid() from dual),1 from kfs.ps_position_data d ");
        String insertSQLStr = insertSQL.toString();
        getSimpleJdbcTemplate().update(insertSQLStr);
		
	}
}
