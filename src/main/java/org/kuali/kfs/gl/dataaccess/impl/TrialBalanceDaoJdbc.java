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
package org.kuali.kfs.gl.dataaccess.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.kns.dao.jdbc.PlatformAwareDaoBaseJdbc;

import org.kuali.kfs.gl.dataaccess.TrialBalanceDao;

/**
 * A class to do the database queries needed to calculate Balance By Consolidation Balance Inquiry Screen
 */
public class TrialBalanceDaoJdbc extends PlatformAwareDaoBaseJdbc implements TrialBalanceDao {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(TrialBalanceDaoJdbc.class);

    public List findBalanceByFields(String selectedFiscalYear, String chartCode) {
        List<Map<String, Object>> results = null;

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT COA_CD, OBJ_CD, A1.FIN_OBJ_CD_NM , A2.FIN_OBJTYP_DBCR_CD, YTD FROM (SELECT A0.FIN_OBJECT_CD AS OBJ_CD, A0.FIN_COA_CD AS COA_CD, SUM(A0.FIN_BEG_BAL_LN_AMT + A0.ACLN_ANNL_BAL_AMT) YTD ");
        queryBuilder.append(" FROM GL_BALANCE_T A0 WHERE A0.FIN_BALANCE_TYP_CD = '" + KFSConstants.BALANCE_TYPE_ACTUAL + "'");
        queryBuilder.append(" AND A0.UNIV_FISCAL_YR = '" + selectedFiscalYear + "'");
        if (!StringUtils.isBlank(chartCode)) {
            queryBuilder.append(" AND A0.FIN_COA_CD = '" + chartCode + "'");
        }
        queryBuilder.append(" GROUP BY A0.FIN_COA_CD, A0.FIN_OBJECT_CD HAVING SUM(A0.FIN_BEG_BAL_LN_AMT + A0.ACLN_ANNL_BAL_AMT) <> 0 ");
        queryBuilder.append(" ORDER BY A0.FIN_COA_CD, A0.FIN_OBJECT_CD) , CA_OBJECT_CODE_T A1, CA_OBJ_TYPE_T A2 WHERE A1.FIN_COA_CD = COA_CD AND A2.FIN_OBJ_TYP_CD=A1.FIN_OBJ_TYP_CD AND ");
        queryBuilder.append(" A1.UNIV_FISCAL_YR = '" + selectedFiscalYear + "'");
        queryBuilder.append(" AND A1.FIN_OBJECT_CD = OBJ_CD");

        results = getSimpleJdbcTemplate().queryForList(queryBuilder.toString());
        return results;
    }


}
