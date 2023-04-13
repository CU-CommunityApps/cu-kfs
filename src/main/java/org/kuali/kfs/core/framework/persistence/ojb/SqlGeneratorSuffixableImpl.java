/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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
package org.kuali.kfs.core.framework.persistence.ojb;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ojb.broker.accesslayer.sql.SelectStatement;
import org.apache.ojb.broker.accesslayer.sql.SqlGeneratorDefaultImpl;
import org.apache.ojb.broker.metadata.ClassDescriptor;
import org.apache.ojb.broker.platforms.Platform;
import org.apache.ojb.broker.query.Query;
import org.apache.ojb.broker.util.logging.Logger;
import org.apache.ojb.broker.util.logging.LoggerFactory;

/**
 * SqlGeneratorDefaultImpl subclass that replaced the vanilla SqlSelectStatement implementation
 * with a new {@link SuffixedSqlSelectStatement} that is {@link SuffixableQueryByCriteria} - aware.
 * This class needs to be specified as the SqlGenerator implementation in the OJB properties, to replace
 * the SqlGeneratorDefaultImpl.
 * This is a hack to introduce select-for-update functionality into OJB so the same ORM/Criteria abstractions
 * can be retained for select-for-update queries.  Select for update appears to have been added in the OJB
 * source repository, so maybe a forthcoming release will include this functionality and these kludges can be
 * removed.
 *
 * @see SuffixedSqlSelectStatement
 */
@Deprecated
@SuppressWarnings({"PMD.GuardLogStatement", "PMD.ProperLogger"})
public class SqlGeneratorSuffixableImpl extends SqlGeneratorDefaultImpl {

    private Logger logger = LoggerFactory.getLogger(SqlGeneratorSuffixableImpl.class);

    public SqlGeneratorSuffixableImpl(Platform platform) {
        super(platform);
    }

    @Override
    public SelectStatement getPreparedSelectStatement(Query query, ClassDescriptor cld) {
        SelectStatement sql = new SuffixedSqlSelectStatement(getPlatform(), cld, query, logger);
        if (logger.isDebugEnabled()) {
            // CU Customization: Flipped the value of the flag below to keep the log output cleaner.
            boolean masochisticSqlLogging = false;
            if (masochisticSqlLogging) {
                logger.debug("SQL: " + sql.getStatement() + "\n" + query.getCriteria() + "\nFor platform: " +
                        getPlatform().getClass().toString() + "\n" + ExceptionUtils
                        .getStackTrace(new Throwable()));
            } else {
                logger.debug("SQL: " + sql.getStatement());
            }
        }
        return sql;
    }
}
