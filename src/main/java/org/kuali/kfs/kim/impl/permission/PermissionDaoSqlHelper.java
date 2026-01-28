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
package org.kuali.kfs.kim.impl.permission;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kim.impl.common.RoleMemberDaoSqlHelper;
import org.kuali.kfs.kim.impl.role.Role;
import org.kuali.kfs.kim.lookup.Constants;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.service.BusinessObjectService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is used to build SQL queries for the Permission object.
 */
// CU customization: fix SQL that is MySql specific to be Oracle compatible:
// - Replace CONCAT with || operator
// - Replace GROUP_CONCAT with LISTAGG 
public class PermissionDaoSqlHelper extends RoleMemberDaoSqlHelper {
    private static final Map<String, String> SORT_MAP = Map.ofEntries(
            Map.entry(Constants.ID, "PERMS.PERM_ID"),
            Map.entry(Constants.NAMESPACE_CODE, "NMSPC_CD"),
            Map.entry(Constants.NAME, "NM"),
            Map.entry(Constants.TEMPLATE_NAME, "TEMPLATE_NAME"),
            Map.entry(Constants.TEMPLATE_NAMESPACE_CODE, "TEMPLATE_NAMESPACE"),
            Map.entry("active", "PERMS.ACTV_IND"),
            Map.entry("detailObjectsToDisplay", "DETAILS"),
            Map.entry("assignedToRolesToDisplay", "GRANTED_TO_ROLES")
    );

    public PermissionDaoSqlHelper(final Map<String, String> fieldValues, final BusinessObjectService businessObjectService) {
        super(fieldValues, businessObjectService);
    }

    public PermissionDaoSqlHelper(
            final Map<String, String> fieldValues,
            final int skip,
            final int limit,
            final String sortField,
            final boolean sortAscending,
            final BusinessObjectService businessObjectService
    ) {
        super(fieldValues, skip, limit, sortField, sortAscending, businessObjectService);
    }

    @Override
    public String buildSql() {
        return buildPermissionSql(true);
    }

    @Override
    public String buildCountSql() {
        return String.format("SELECT COUNT(*) FROM (%s) DUMMY", buildPermissionSql(false));
    }

    @Override
    public String addSort() {
        if (StringUtils.isNotBlank(sortField)) {
            if (SORT_MAP.containsKey(sortField)) {
                return " ORDER BY " + SORT_MAP.get(sortField) + (sortAscending ? " ASC " : " DESC ");
            }
        }
        return "";
    }

    @Override
    public String getCteTableAbbreviation() {
        return "PERM";
    }

    @Override
    public <T extends BusinessObjectBase> T mapResultSetToBusinessObject(final ResultSet resultSet) {
        final Permission permission = new Permission();
        try {
            permission.setId(resultSet.getString("PERM_ID"));
            permission.setObjectId(resultSet.getString("OBJ_ID"));
            permission.setTemplateId(resultSet.getString("PERM_TMPL_ID"));
            permission.setNamespaceCode(resultSet.getString("NMSPC_CD"));
            permission.setName(resultSet.getString("NM"));
            permission.setDescription(resultSet.getString("DESC_TXT"));
            permission.setActive(resultSet.getBoolean("ACTV_IND"));
            final String assignedToRoleIds = resultSet.getString("ROLE_IDS");
            if (StringUtils.isNotEmpty(assignedToRoleIds)) {
                // I hate this.
                permission.setAssignedToRoles((List<Role>) businessObjectService.findMatching(
                        Role.class,
                        Map.of("id", Arrays.asList(assignedToRoleIds.split(",")))
                ));
            }
            permission.refresh();
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
        return (T) permission;
    }

    @Override
    protected Map<String, String> buildCTEs() {
        final Map<String, String> ctes = super.buildCTEs();
        processRolePermCTE(ctes);
        processAttributeValueCTE(ctes);
        processDetailOutputCTE(ctes);
        processAssignedToRolesOutputCTE(ctes);
        return ctes;
    }

    private String buildPermissionSql(final boolean includeSortAndLimit) {
        final Map<String, String> cteMap = buildCTEs();
        if (cteMap.isEmpty()) {
            return String.join(" ",
                    PermissionSql.SELECT,
                    PermissionSql.FROM,
                    includeSortAndLimit ? addSort() : "",
                    includeSortAndLimit ? addLimitAndOffset() : ""
            );
        }
        final String cteString = cteMap.entrySet()
                .stream()
                .map(entry -> entry.getKey() + " AS (" + entry.getValue() + ")")
                .collect(Collectors.joining(", "));
        final List<String> joinClauses = new ArrayList<>();
        joinClauses.add(PermissionSql.JOIN_DETAIL_OUTPUT);
        joinClauses.add(PermissionSql.JOIN_GRANTED_TO_ROLES);
        if (cteMap.containsKey(ROLE_PERM_SQL.ALIAS)) {
            joinClauses.add(PermissionSql.JOIN_ROLE_PERM);
        }
        if (cteMap.containsKey(TEMPLATE_SQL.ALIAS)) {
            joinClauses.add(PermissionSql.JOIN_TEMPLATE);
        }
        if (cteMap.containsKey(ATTRIBUTE_VALUE_SQL.ALIAS)) {
            joinClauses.add(PermissionSql.JOIN_ATTRIBUTE_VALUE);
        }
        final String whereClause = buildWhereClause();
        return String.join(" ", List.of("WITH",
                cteString,
                PermissionSql.SELECT,
                PermissionSql.FROM,
                String.join(" ", joinClauses),
                whereClause,
                PermissionSql.GROUP_BY,
                includeSortAndLimit ? addSort() : "",
                includeSortAndLimit ? addLimitAndOffset() : ""
        ));
    }

    private String buildWhereClause() {
        final List<String> whereClauses = new ArrayList<>();
        final String namespace = fieldValues.get(Constants.NAMESPACE_CODE);
        final String name = fieldValues.get(Constants.NAME);
        final String active = fieldValues.get("active");
        whereClauses.add(addCondition(namespace, "namespace", "NMSPC_CD"));
        whereClauses.add(addCondition(name, "name", "NM"));
        whereClauses.add(addCondition(active, "active", "PERMS.ACTV_IND"));
        whereClauses.removeAll(List.of(""));
        return whereClauses.isEmpty() ? "" : "WHERE " + String.join(" AND ", whereClauses);
    }

    private void processRolePermCTE(final Map<String, String> cteMap) {
        if (!cteMap.containsKey(ROLE_MEMBER_SQL.ALIAS) && !cteMap.containsKey(ROLE_SQL.ALIAS)) {
            return;
        }
        final String joinClause;
        if (cteMap.containsKey(ROLE_MEMBER_SQL.ALIAS)) {
            joinClause = ROLE_PERM_SQL.ROLE_MEMBER_JOIN;
        } else {
            joinClause = ROLE_PERM_SQL.ROLE_JOIN;
        }
        cteMap.put(ROLE_PERM_SQL.ALIAS,
                String.join(" ", List.of(ROLE_PERM_SQL.SELECT, joinClause))
        );
    }

    private void processAttributeValueCTE(final Map<String, String> cteMap) {
        final String attributeValue = fieldValues.get(Constants.ATTRIBUTE_VALUE);
        if (StringUtils.isEmpty(attributeValue)) {
            return;
        }

        final String whereClause = addCondition(attributeValue, "attributeValue", "ATTR_VAL");

        cteMap.put(ATTRIBUTE_VALUE_SQL.ALIAS,
                String.join(" ", List.of(ATTRIBUTE_VALUE_SQL.SELECT, "WHERE", whereClause))
        );
    }

    private void processDetailOutputCTE(final Map<String, String> cteMap) {
        cteMap.put(DETAIL_OUTPUT_SQL.ALIAS, DETAIL_OUTPUT_SQL.SELECT);
    }

    private void processAssignedToRolesOutputCTE(final Map<String, String> cteMap) {
        cteMap.put(GRANTED_TO_ROLES_OUTPUT_SQL.ALIAS, GRANTED_TO_ROLES_OUTPUT_SQL.SELECT);
    }

    private static final class ROLE_PERM_SQL {
        private static final String ALIAS = "ROLE_PERM_CTE";
        private static final String SELECT = "SELECT PERM_ID FROM KRIM_ROLE_PERM_T";
        private static final String ROLE_JOIN = "JOIN ROLE_CTE ON ROLE_CTE.ROLE_ID = KRIM_ROLE_PERM_T.ROLE_ID";
        private static final String ROLE_MEMBER_JOIN =
                "JOIN ROLE_MEMBER_CTE ON ROLE_MEMBER_CTE.ROLE_ID = KRIM_ROLE_PERM_T.ROLE_ID";
    }

    private static final class ATTRIBUTE_VALUE_SQL {
        private static final String ALIAS = "ATTRIBUTE_VALUE_CTE";
        private static final String SELECT = "SELECT PERM_ID FROM KRIM_PERM_ATTR_DATA_T";
    }

    private static final class DETAIL_OUTPUT_SQL {
        private static final String ALIAS = "DETAIL_OUTPUT_CTE";
        private static final String SELECT =
                "SELECT PERM_ID, "
                + "UPPER(NM || ':' || ATTR_VAL) AS DETAIL_VALUE "
                + "FROM KRIM_ATTR_DEFN_T "
                + "JOIN KRIM_PERM_ATTR_DATA_T ON KRIM_ATTR_DEFN_T.KIM_ATTR_DEFN_ID = KRIM_PERM_ATTR_DATA_T.KIM_ATTR_DEFN_ID";
    }

    private static final class GRANTED_TO_ROLES_OUTPUT_SQL {
        private static final String ALIAS = "GRANTED_TO_ROLES_CTE";
        private static final String SELECT =
                "SELECT PERM_ID, "
                + "UPPER(NMSPC_CD || ' ' ||  ROLE_NM) AS GRANTED_TO_ROLE, "
                + "KRIM_ROLE_T.ROLE_ID AS GRANTED_TO_ROLE_ID "
                + "FROM KRIM_ROLE_T "
                + "JOIN KRIM_ROLE_PERM_T ON KRIM_ROLE_T.ROLE_ID = KRIM_ROLE_PERM_T.ROLE_ID "
                + "WHERE KRIM_ROLE_PERM_T.ACTV_IND = 'Y'";
    }

    private static final class PermissionSql {
        private static final String SELECT =
                "SELECT PERMS.PERM_ID,"
                + "     PERMS.OBJ_ID,"
                + "     PERMS.PERM_TMPL_ID,"
                + "     NMSPC_CD,"
                + "     NM,"
                + "     DESC_TXT,"
                + "     PERMS.ACTV_IND,"
                + "     TEMPLATE_NAME,"
                + "     TEMPLATE_NAMESPACE,"
                + "     LISTAGG(DISTINCT DETAIL_VALUE, ', ') WITHIN GROUP (ORDER BY DETAIL_VALUE ASC) AS DETAILS,"
                + "     LISTAGG(DISTINCT GRANTED_TO_ROLE, ' ') WITHIN GROUP (ORDER BY GRANTED_TO_ROLE ASC) AS GRANTED_TO_ROLES,"
                + "     LISTAGG(DISTINCT GRANTED_TO_ROLE_ID, ',') WITHIN GROUP (ORDER BY GRANTED_TO_ROLE_ID ASC) AS ROLE_IDS";
        private static final String FROM = "FROM KRIM_PERM_T PERMS";
        private static final String GROUP_BY = "GROUP BY PERMS.PERM_ID,"
                + "     PERMS.OBJ_ID,"
                + "     PERMS.PERM_TMPL_ID,"
                + "     PERMS.NMSPC_CD,"
                + "     PERMS.NM,"
                + "     DESC_TXT,"
                + "     PERMS.ACTV_IND,"
                + "     TEMPLATE_NAME,"
                + "     TEMPLATE_NAMESPACE";
        private static final String JOIN_ROLE_PERM = "JOIN ROLE_PERM_CTE ON ROLE_PERM_CTE.PERM_ID = PERMS.PERM_ID";
        private static final String JOIN_TEMPLATE =
                "JOIN TEMPLATE_CTE ON TEMPLATE_CTE.PERM_TMPL_ID = PERMS.PERM_TMPL_ID";
        private static final String JOIN_ATTRIBUTE_VALUE =
                "JOIN ATTRIBUTE_VALUE_CTE ON ATTRIBUTE_VALUE_CTE.PERM_ID = PERMS.PERM_ID";
        private static final String JOIN_DETAIL_CRITERIA =
                "JOIN DETAIL_CRITERIA_CTE ON DETAIL_CRITERIA_CTE.PERM_ID = PERMS.PERM_ID";
        private static final String JOIN_DETAIL_OUTPUT =
                "LEFT OUTER JOIN DETAIL_OUTPUT_CTE ON DETAIL_OUTPUT_CTE.PERM_ID = PERMS.PERM_ID";
        private static final String JOIN_GRANTED_TO_ROLES =
                "LEFT OUTER JOIN GRANTED_TO_ROLES_CTE ON GRANTED_TO_ROLES_CTE.PERM_ID = PERMS.PERM_ID";
    }
}
