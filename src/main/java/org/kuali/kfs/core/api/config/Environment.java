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
package org.kuali.kfs.core.api.config;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * ====
 * CU Customization: Reintroduced the "production.environment.code" config property handling.
 *                   Portions of the reintroduced code are identical to the 2024-02-28 financials code.
 * ====
 * 
 * A class representing the "environment" in which the app is currently running.
 */
@Component
public final class Environment {
    // ==== CU Customization: Added a Logger instance. ====
    private static final Logger LOG = LogManager.getLogger();

    public static final String PROPERTY_NAME = "environment";

    private final String lane;
    private final boolean productionEnvironment;
    private final String tenant;

    // ==== CU Customization: added "production.environment.code" back in as an injectable argument. ====
    public Environment(
            @Value("${KFS_INFRA_LANE_NAME:dev}") final String lane,
            @Value("${production.environment.code}") final String productionEnvironmentCode,
            @Value("${KFS_INFRA_TENANT_NAME:No tenant specified}") final String tenant
    ) {
        // ==== CU Customization: Check for a non-blank "production.environment.code" value. ====
        Validate.isTrue(
                StringUtils.isNotBlank(productionEnvironmentCode),
                "productionEnvironmentCode must be supplied"
        );
        Validate.isTrue(StringUtils.isNotBlank(lane), "lane must be supplied");
        this.lane = lane.toLowerCase(Locale.US);
        Validate.isTrue(StringUtils.isNotBlank(tenant), "tenant must be supplied");
        this.tenant = tenant.toLowerCase(Locale.US);

        // Future-proofing by including "prod", in case we can ever afford to buy a vowel
        // ==== CU Customization: Allow the "production.environment.code" setting as an option. ====
        LOG.info("Environment, Allowed Production environment codes/lanes: prd, prod, {}", productionEnvironmentCode);
        LOG.info("Environment, Current environment code/lane: {}", this.lane);
        productionEnvironment = List.of("prd", "prod", productionEnvironmentCode).contains(this.lane);
        LOG.info("Environment, Resulting is-production-environment setting: {}", productionEnvironment);
    }

    /**
     * @return The lane, in lowercase, in which the app is running
     */
    public String getLane() {
        return lane;
    }

    /**
     * @return The tenant, in lowercase, in which the app is running
     */
    public String getTenant() {
        return tenant;
    }

    // ==== CU Customization: Updated Javadoc comment. ====
    /**
     * @return True if {@code lane} is "prd" or "prod" or value of "production.environment.code" prop; otherwise, false.
     */
    public boolean isProductionEnvironment() {
        return productionEnvironment;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Environment)) {
            return false;
        }
        final Environment that = (Environment) o;
        return Objects.equals(lane, that.lane)
               && Objects.equals(tenant, that.tenant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lane, tenant);
    }

    @Override
    public String toString() {
        return "Environment{"
               + "lane='" + lane + '\''
               + ", tenant='" + tenant + '\''
               + '}';
    }

}
