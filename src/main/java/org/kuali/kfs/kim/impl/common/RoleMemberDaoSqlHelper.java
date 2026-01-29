/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.kim.impl.common;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kim.lookup.Constants;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.service.BusinessObjectService;

import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for building SQL queries for role member BOs.
 */
//CU customization: fix SQL that is MySql specific to be Oracle compatible:
// - Replace: 
//     LIMIT x OFFSET y
//   With:
//     OFFSET y ROWS FETCH NEXT x ROWS ONLY
// - Replace references to MySQL's NOW() function with references to Oracle SYSDATE
public abstract class RoleMemberDaoSqlHelper {
    protected final Map<String, Object> parameters = new HashMap<>();
    protected final Map<String, String> fieldValues;
    private int skip;
    private int limit;
    protected String sortField;
    protected boolean sortAscending;
    protected BusinessObjectService businessObjectService;

    protected RoleMemberDaoSqlHelper(final Map<String, String> fieldValues, final BusinessObjectService businessObjectService) {
        this.fieldValues = fieldValues;
        this.businessObjectService = businessObjectService;
    }

    protected RoleMemberDaoSqlHelper(
            final Map<String, String> fieldValues,
            final int skip,
            final int limit,
            final String sortField,
            final boolean sortAscending,
            final BusinessObjectService businessObjectService
    ) {
        this.fieldValues = fieldValues;
        this.skip = skip;
        this.limit = limit;
        this.sortField = sortField;
        this.sortAscending = sortAscending;
        this.businessObjectService = businessObjectService;
    }

    public abstract String buildSql();

    public abstract String buildCountSql();

    public abstract String addSort();

    public abstract String getCteTableAbbreviation();

    public abstract <T extends BusinessObjectBase> T mapResultSetToBusinessObject(ResultSet resultSet);

    protected Map<String, String> buildCTEs() {
        final Map<String, String> cteMap = new LinkedHashMap<>();
        processPersonCTE(cteMap);
        processGroupCTE(cteMap);
        processTemplateCTE(cteMap);
        processRoleCTE(cteMap);
        processRoleMemberCTE(cteMap);
        return cteMap;
    }

    protected String addCondition(
            final String fieldValue,
            final String fieldValueKey,
            final String sqlField
    ) {
        return addCondition(fieldValue, fieldValueKey, sqlField, false);
    }

    protected String addCondition(
            final String fieldValue,
            final String fieldValueKey,
            final String sqlField,
            final boolean forceExactMatch
    ) {
        if (StringUtils.isBlank(fieldValue)) {
            return "";
        }
        final String replacedFieldValue = fieldValue.replace("*", "%");
        parameters.put(fieldValueKey, replacedFieldValue);
        if (replacedFieldValue.contains("%") && !forceExactMatch) {
            return "UPPER(" + sqlField + ") LIKE UPPER(:" + fieldValueKey + ") ";
        }
        return "UPPER(" + sqlField + ") = UPPER(:" + fieldValueKey + ") ";
    }

    private void processPersonCTE(final Map<String, String> cteMap) {
        final String principalName = fieldValues.get(Constants.ASSIGNED_TO_PRINCIPAL_NAME);
        if (StringUtils.isBlank(principalName)) {
            return;
        }
        final List<String> whereClauses = new ArrayList<>();
        whereClauses.add(addCondition(principalName, "principalName", "PRNCPL_NM"));
        whereClauses.removeAll(List.of(""));
        cteMap.put(PERSON_SQL.ALIAS,
                String.join(" ", List.of(PERSON_SQL.SELECT, "WHERE", String.join(" AND ", whereClauses)))
        );
    }

    private void processGroupCTE(final Map<String, String> cteMap) {
        final String groupName = fieldValues.get(Constants.ASSIGNED_TO_GROUP_NAME);
        final String groupNamespace = fieldValues.get(Constants.ASSIGNED_TO_GROUP_NAMESPACE_CODE);
        if (StringUtils.isBlank(groupName) && StringUtils.isBlank(groupNamespace)) {
            return;
        }
        final List<String> whereClauses = new ArrayList<>();
        whereClauses.add(addCondition(groupName, "groupName", "GRP_NM"));
        whereClauses.add(addCondition(groupNamespace, "groupNamespace", "NMSPC_CD"));
        whereClauses.removeAll(List.of(""));
        cteMap.put(GROUP_SQL.ALIAS,
                String.join(" ", List.of(GROUP_SQL.SELECT, "WHERE", String.join(" AND ", whereClauses)))
        );
    }

    private void processTemplateCTE(final Map<String, String> cteMap) {
        final String templateName = fieldValues.get(Constants.TEMPLATE_NAME);
        final String templateNamespace = fieldValues.get(Constants.TEMPLATE_NAMESPACE_CODE);
        final String templateId = fieldValues.get(Constants.TEMPLATE_ID);

        final List<String> whereClauses = new ArrayList<>();
        if (StringUtils.isNotEmpty(templateName)) {
            whereClauses.add(addCondition(templateName, "templateName", "NM"));
        }
        if (StringUtils.isNotEmpty(templateNamespace)) {
            whereClauses.add(addCondition(templateNamespace, "templateNamespace", "NMSPC_CD"));
        }
        if (StringUtils.isNotEmpty(templateId)) {
            whereClauses.add(addCondition(templateId, "templateId",
                    getCteTableAbbreviation() + "_TMPL_ID", true));
        }
        whereClauses.removeAll(List.of(""));
        cteMap.put(TEMPLATE_SQL.ALIAS,
                String.join(
                        " ",
                        List.of(MessageFormat.format(TEMPLATE_SQL.SELECT, getCteTableAbbreviation()),
                                whereClauses.isEmpty() ? "" : "WHERE",
                                String.join(" AND ", whereClauses)
                        )
                )
        );
    }

    private void processRoleCTE(final Map<String, String> cteMap) {
        final String roleName = fieldValues.get(Constants.ASSIGNED_TO_ROLE_NAME);
        final String roleNamespace = fieldValues.get(Constants.ASSIGNED_TO_ROLE_NAMESPACE_CODE);
        if (StringUtils.isBlank(roleName) && StringUtils.isBlank(roleNamespace)) {
            return;
        }
        final List<String> whereClauses = new ArrayList<>();
        whereClauses.add(addCondition(roleName, "roleName", "ROLE_NM"));
        whereClauses.add(addCondition(roleNamespace, "roleNamespace", "NMSPC_CD"));
        whereClauses.removeAll(List.of(""));
        cteMap.put(ROLE_SQL.ALIAS,
                String.join(" ", List.of(ROLE_SQL.SELECT, "WHERE", String.join(" AND ", whereClauses)))
        );
    }

    private void processRoleMemberCTE(final Map<String, String> cteMap) {
        if (!cteMap.containsKey(PERSON_SQL.ALIAS) && !cteMap.containsKey(GROUP_SQL.ALIAS)) {
            return;
        }
        final List<String> joinClauses = new ArrayList<>();
        if (cteMap.containsKey(PERSON_SQL.ALIAS)) {
            joinClauses.add(ROLE_MEMBER_SQL.PERSON_JOIN);
        }
        if (cteMap.containsKey(GROUP_SQL.ALIAS)) {
            joinClauses.add(ROLE_MEMBER_SQL.GROUP_JOIN);
        }
        if (cteMap.containsKey(ROLE_SQL.ALIAS)) {
            joinClauses.add(ROLE_MEMBER_SQL.ROLE_JOIN);
        }
        cteMap.put(ROLE_MEMBER_SQL.ALIAS,
                String.join(" ", List.of(ROLE_MEMBER_SQL.SELECT, String.join(" ", joinClauses), ROLE_MEMBER_SQL.WHERE))
        );
    }

    // CU customization
    protected String addLimitAndOffset() {
        return "OFFSET " + skip + " ROWS FETCH NEXT " + limit + " ROWS ONLY";
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    protected static final class PERSON_SQL {
        protected static final String ALIAS = "PERSON_CTE";
        protected static final String SELECT = "SELECT PRNCPL_ID FROM KRIM_PERSON_T";
    }

    protected static final class GROUP_SQL {
        protected static final String ALIAS = "GROUP_CTE";
        protected static final String SELECT = "SELECT GRP_ID FROM KRIM_GRP_T";
    }

    protected static final class TEMPLATE_SQL {
        public static final String ALIAS = "TEMPLATE_CTE";
        protected static final String SELECT = "SELECT {0}_TMPL_ID, "
                                               + "NM AS TEMPLATE_NAME, "
                                               + "NMSPC_CD TEMPLATE_NAMESPACE "
                                               + "FROM KRIM_{0}_TMPL_T";
    }

    protected static final class ROLE_SQL {
        public static final String ALIAS = "ROLE_CTE";
        protected static final String SELECT = "SELECT ROLE_ID FROM KRIM_ROLE_T";
    }

    protected static final class ROLE_MEMBER_SQL {
        public static final String ALIAS = "ROLE_MEMBER_CTE";
        protected static final String SELECT = "SELECT KRIM_ROLE_MBR_T.ROLE_ID FROM KRIM_ROLE_MBR_T";
        protected static final String PERSON_JOIN = "JOIN PERSON_CTE ON PERSON_CTE.PRNCPL_ID = KRIM_ROLE_MBR_T.MBR_ID";
        protected static final String GROUP_JOIN = "JOIN GROUP_CTE ON GROUP_CTE.GRP_ID = KRIM_ROLE_MBR_T.MBR_ID";
        protected static final String ROLE_JOIN = "JOIN ROLE_CTE ON ROLE_CTE.ROLE_ID = KRIM_ROLE_MBR_T.ROLE_ID";
        protected static final String WHERE = "WHERE (ACTV_FRM_DT IS NULL OR ACTV_FRM_DT <= SYSDATE) "
                                              + "AND (ACTV_TO_DT IS NULL OR ACTV_TO_DT >= SYSDATE)";
    }
}
