/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
package org.kuali.kfs.sys.dashboardnav;

import org.kuali.kfs.core.framework.persistence.jdbc.dao.PlatformAwareDaoBaseJdbc;

/**
 * A class meant to provide information about the database server.
 */
public class DatabaseServerInfoDao extends PlatformAwareDaoBaseJdbc {

    // Cornell customization: code customized to get Oracle database version
    private static final String SERVER_VERSION_QUERY = "SELECT version from V$INSTANCE";

    public String databaseServerVersion() {
        return getJdbcTemplate().queryForObject(SERVER_VERSION_QUERY, String.class);
    }

}
