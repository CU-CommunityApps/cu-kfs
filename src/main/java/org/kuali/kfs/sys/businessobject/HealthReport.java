/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
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
package org.kuali.kfs.sys.businessobject;

import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.impl.services.CoreImplServiceLocator;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.springframework.cache.CacheManager;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/* Cornell Customization: backport redis*/
public class HealthReport {
    private ConfigurationService configurationService;

    public String status = "";
    public String message = "";
    public List<Metric> metrics;
    public ServiceIntegrityReport integrityReport;

    public HealthReport() {
        metrics = new ArrayList<>();
    }

    public HealthReport checkHealth() {
        integrityReport = new ServiceIntegrityReport();
        long startTime = System.currentTimeMillis();
        checkVersion();
        checkMemory();
        checkDatabase();
        checkSessions();
        checkCache();
        if (getMessage().isEmpty()) {
            status = "OK";
            message = "System checks out";
        }
        Metric metric = new Metric("Health", "executionTime", (System.currentTimeMillis() - startTime) + "ms");
        metrics.add(metric);
        return this;

    }

    protected void checkVersion() {
        integrityReport.addService("applicationVersion", ServiceStatus.ok(getConfigurationService()
                .getPropertyValueAsString(KFSConstants.APPLICATION_VERSION_KEY)));
    }

    protected void checkCache() {
        try {
            doCheckCache();
            integrityReport.addService("cache", ServiceStatus.ok());
        } catch (Exception e) {
            status = "Failed";
            message += "Failed to assess Cache health.\n";
            integrityReport.addService(
                "cache",
                ServiceStatus.error(String.format("Unable to verify Cache health: %s", e.getMessage()))
            );
        }
    }

    protected void doCheckCache() {
        final List<CacheManager> cacheManagers = CoreImplServiceLocator.getCacheManagerRegistry().getCacheManagers();
        Metric metric = new Metric("Cache", "cacheManagerCount", "" + cacheManagers.size());
        metrics.add(metric);
        final int count = cacheManagers.stream().mapToInt(cm -> cm.getCacheNames().size()).sum();
        metric = new Metric("Cache", "cacheCount", "" + count);
        metrics.add(metric);
    }

    protected void checkSessions() {
        try {
            doCheckSessions();
            integrityReport.addService("session", ServiceStatus.ok());
        } catch (Exception e) {
            status = "Failed";
            message += "Failed to assess Session health.\n";
            integrityReport.addService(
                "session",
                ServiceStatus.error(String.format("Unable to verify Session health: %s", e.getMessage()))
            );
        }
    }

    protected void doCheckSessions() throws JMException {
        String context = ConfigContext.getCurrentContextConfig().getProperty("app.context.name");

        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = new ObjectName("Catalina:type=Manager,context=/" + context + ",host=localhost");
        Object activeSessions = mBeanServer.getAttribute(objectName, "activeSessions");
        Metric metric = new Metric("Session", "active", activeSessions.toString());
        metrics.add(metric);
        Object expiredSessions = mBeanServer.getAttribute(objectName, "expiredSessions");
        metric = new Metric("Session", "expired", expiredSessions.toString());
        metrics.add(metric);
        Object maxActive = mBeanServer.getAttribute(objectName, "maxActive");
        metric = new Metric("Session", "maxActive", maxActive.toString());
        metrics.add(metric);
    }

    protected void checkDatabase() {
        try {
            doCheckDatabase();
        } catch (Exception e) {
            status = "Failed";
            message += "Failed to assess Database health.\n";
            integrityReport.addService(
                "db",
                ServiceStatus.error(String.format("Unable to verify Database health: %s", e.getMessage()))
            );
        }
    }

    protected void doCheckDatabase() throws SQLException {
        DataSource transactionalDS = (DataSource) ConfigContext.getCurrentContextConfig()
                .getObject(KFSConstants.DATASOURCE_OBJ);
        DataSource nonTransactionalDS = (DataSource) ConfigContext.getCurrentContextConfig()
                .getObject(KFSConstants.NON_TRANSACTIONAL_DATASOURCE_OBJ);

        Connection conn = transactionalDS.getConnection();
        boolean closed = conn.isClosed();
        conn.close();
        Metric metric = new Metric("Database", "kfs-xa-closed", Boolean.toString(closed));
        metrics.add(metric);

        ServiceStatus status = closed ? ServiceStatus.error("Transactional database is closed") : ServiceStatus.ok();
        integrityReport.addService("db.transactional", status);

        conn = nonTransactionalDS.getConnection();
        closed = conn.isClosed();
        conn.close();
        metric = new Metric("Database", "kfs-closed", Boolean.toString(closed));
        metrics.add(metric);

        status = closed ? ServiceStatus.error("Non-Transactional database is closed") : ServiceStatus.ok();
        integrityReport.addService("db.non-transactional", status);
    }

    protected void checkMemory() {
        try {
            doCheckMemory();
        } catch (Exception e) {
            status = "Failed";
            message += "Failed to assess Memory health.\n";
            integrityReport.addService(
                "memory",
                ServiceStatus.error(String.format("Unable to verify memory usage: %s", e.getMessage()))
            );
        }

    }

    protected void doCheckMemory() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long percentUsedMemory = freeMemory / totalMemory;

        Metric metric = new Metric("Memory", "free", "" + freeMemory);
        metrics.add(metric);
        metric = new Metric("Memory", "max", "" + runtime.maxMemory());
        metrics.add(metric);
        metric = new Metric("Memory", "total", "" + totalMemory);
        metrics.add(metric);

        ServiceState status = ServiceState.OK;
        if (percentUsedMemory >= 0.66) {
            status = ServiceState.ERROR;
        } else if (percentUsedMemory >= 0.33) {
            status = ServiceState.WARN;
        }
        integrityReport.addService(
            "memory.available",
            new ServiceStatus(status, String.format("Memory Available: %d%%", percentUsedMemory * 100))
        );
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private ConfigurationService getConfigurationService() {
        if (configurationService == null) {
            configurationService = SpringContext.getBean(ConfigurationService.class);
        }
        return configurationService;
    }
}
