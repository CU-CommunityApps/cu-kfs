/*
 * Copyright 2007-2008 The Kuali Foundation
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
package org.kuali.rice.core.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.PooledConnection;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.enhydra.jdbc.pool.StandardXAPoolDataSource;
import org.kuali.rice.core.exception.RiceRuntimeException;
import org.kuali.rice.core.util.MaxAgeSoftReference;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * StandardXAPoolDataSource subclass that adds some convienance getters and setters and implements our Lifecycle interface.
 * 
 * @deprecated We will be removing this file from a future release in order to get rid of our dependencies on XAPool.  If you
 * desire to continue using JOTM and XAPool, please configure using org.enhyrdra.jdbc.standard.StandardXADataSource directly
 * instead of using this class.
 */
public class XAPoolDataSource extends StandardXAPoolDataSource implements InitializingBean, DisposableBean {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(XAPoolDataSource.class);

    private static final long serialVersionUID = -3698043954102287887L;
    public static final String DRIVER_CLASS_NAME = "driverClassName";
    public static final String URL = "url";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String MAX_SIZE = "maxSize";
    public static final String MIN_SIZE = "minSize";
    public static final String MAX_WAIT = "maxWait";
    public static final String VALIDATION_QUERY = "validationQuery";

    private RiceXADataSource dataSource = new RiceXADataSource();
    private boolean started = false;

    public XAPoolDataSource() {
        setDataSource(this.dataSource);
        setPreparedStmtCacheSize(0);
        setCheckLevelObject(2);
    }

    public void afterPropertiesSet() throws Exception {
    }

    public void destroy() throws Exception {
        LOG.info("Destroying WorkflowManagedDatasource.");
        shutdown(true);
        this.started = false;
    }

    public boolean isStarted() {
        return this.started;
    }

    public String getDriverClassName() throws SQLException {
        return this.dataSource.getDriverName();
    }

    public long getMaxWait() {
        return super.getDeadLockMaxWait();
    }

    public String getUrl() {
        return this.dataSource.getUrl();
    }

    public String getUsername() {
        return this.dataSource.getUser();
    }

    public String getValidationQuery() {
        return super.getJdbcTestStmt();
    }

    public void setBeanName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public void setDriverClassName(String driverClassName) {
        try {
            this.dataSource.setDriverName(driverClassName);
        } catch (SQLException e) {
            throw new RiceRuntimeException("Problem setting the driver name to: " + driverClassName, e);
        }
    }

    public void setMaxWait(long maxWait) {
        super.setDeadLockMaxWait(maxWait);
    }

    public void setPassword(String password) {
        // passwrd needs to be set in both places or else there will be an error
        this.dataSource.setPassword(password);
        super.setPassword(password);
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.dataSource.setTransactionManager(transactionManager);
    }

    public void setUrl(String url) {
        this.dataSource.setUrl(url);
    }

    public void setUsername(String username) {
        // username needs to be set in both places or else there will be an error
        this.dataSource.setUser(username);
        super.setUser(username);
    }

    public void setValidationQuery(String validationQuery) {
        super.setJdbcTestStmt(validationQuery);
    }

    public void setPreparedStmtCacheSize(int preparedStatementCacheSize) {
    	this.dataSource.setPreparedStatementCacheSize(preparedStatementCacheSize);
    }
    
    // BEGIN CU Modification : add a threadlocal cache to hold the most recently verified connections
    static final ThreadLocal<MaxAgeSoftReference<List<Object>>> VERIFIED_OBJECT_CACHE = new ThreadLocal<MaxAgeSoftReference<List<Object>>>();
    // This is used to ensure that connections *are* tested every so often, since threads can be re-used
    static final int MAX_TIME_BETWEEN_TESTS_IN_MS = 60 * 1000; // 1 minute
    
    // FYI : code here is explicitly *NOT* using code which might use the
    // hashCode or equals methods on the objects, since we want to 
    // run off of identity only, not on the object's concept of equality
    
    /**
     * Checks whether a given object is in the cache.  This should be a PooledConnection, 
     * but we will not make any assumptions here.
     */
    protected boolean isObjectInCache( Object o ) {
    	List<Object> threadsCache = (VERIFIED_OBJECT_CACHE.get() == null)?null:VERIFIED_OBJECT_CACHE.get().get(); // threads = thread's
    	// first time we are run, add the list to the cache
    	if ( threadsCache == null ) {
    		threadsCache = new ArrayList<Object>(3);
    		MaxAgeSoftReference<List<Object>> t = new MaxAgeSoftReference<List<Object>>(System.currentTimeMillis() + MAX_TIME_BETWEEN_TESTS_IN_MS, threadsCache);
    		VERIFIED_OBJECT_CACHE.set(t);
    	}
        if ( LOG.isTraceEnabled() ) {
        	StringBuilder sb = new StringBuilder( "Current Objects in Cache: " );
        	for ( Object obj : threadsCache ) {
        		sb.append( obj.getClass().getSimpleName() ).append( "(").append( System.identityHashCode(obj) ).append( ")   " );
        	}
    		LOG.trace( sb.toString() );
    		LOG.trace( "Object Passed In: " + o.getClass().getSimpleName() + "(" + System.identityHashCode(o) + ")" );
        }
    	
    	for ( Object obj : threadsCache ) {
    		if ( System.identityHashCode(o) == System.identityHashCode(obj) ) {
    			return true;
    		}
    	}
    	return false;
    }

    /**
     * Adds the given PooledConnection to the cache.
     */
    protected void addObjectToCache( Object o ) {
    	List<Object> threadsCache = (VERIFIED_OBJECT_CACHE.get() == null)?null:VERIFIED_OBJECT_CACHE.get().get(); // threads = thread's
    	// first time we are run, add the list to the cache
    	if ( threadsCache == null ) {
    		threadsCache = new ArrayList<Object>(3);
    		MaxAgeSoftReference<List<Object>> t = new MaxAgeSoftReference<List<Object>>(System.currentTimeMillis() + MAX_TIME_BETWEEN_TESTS_IN_MS, threadsCache);
    		VERIFIED_OBJECT_CACHE.set(t);
    	}
    	if ( LOG.isTraceEnabled() ) {
    		LOG.trace( "Adding Object To Cache: " + o.getClass().getSimpleName() + "(" + System.identityHashCode(o) + ")" );
    	}
    	threadsCache.add(o);
    }
    
    /**
     * This method tests if a connection is valid or not. It overrides the
     * method in StandardPoolDataSource to take into account global transactions:
     * if global transaction is in progress - suspend it so that
     * connection testing happens ouside of transaction.
     * If connection testing fails - it will not affect transaction
     * and next good connection can join the transaction
     */
     public boolean testThisObject(Object o) {
        // CU Change : avoid superfluous testing of the given connection within a single thread
        // If the cache has a value *AND* the object is the same object
        // then assume we have already verified this object within the thread and return true.
        if ( isObjectInCache(o) ) {
        	LOG.debug( "Connection matched cache: skipping validation query.");
        	return true;
        } else {
        	LOG.debug( "Connection did not match cache: running validation query.");
        }
        // END CU Change (The remainder of this method - except where noted - is copied from the superclass.)
        Connection ret = null;
        Transaction suspended = null;        
        
        try {
            Transaction tx = transactionManager == null
                                ? null
                                : transactionManager.getTransaction();
            boolean isActive = tx == null
                                ? false
                                : tx.getStatus() == Status.STATUS_ACTIVE;
            if (isActive) {
                suspended = transactionManager.suspend();
            }

            if ( LOG.isDebugEnabled() ) {
            	if ( LOG.isTraceEnabled() ) {
            		// if we are tracing, let's see where this connection is being created from
            		LOG.trace( "in TestThisObject( " + o.getClass().getName() + " )", new Throwable() );
            	} else {
            		LOG.debug( "in TestThisObject( " + o.getClass().getName() + " )" );
            	}
            	LOG.debug( "isActive: " + isActive );
            	if ( transactionManager != null ) {
            		LOG.debug( "transaction manager status: " + transactionManager.getStatus() );
            	}
            	LOG.debug( "transaction: " + tx );
            	if ( tx != null ) {
            		LOG.debug( "transaction status: " + tx.getStatus() );
            	}
            }

            PooledConnection con = (PooledConnection) o;
            ret = con.getConnection();

            Statement s = ret.createStatement();
            s.execute(jdbcTestStmt);
            s.close();
            try {
                ret.close();
            } catch (Exception e) {
            	LOG.error( "testThisObject: Unable to close the connection (return to the pool) after testing.", e);
            }
            // CU Change: add the verified connection to the pool
            addObjectToCache(o);
            // END CU Change
            return true;
        } catch (Exception e) {
            LOG.error( "testThisObject: exception testing statement", e);
            return false;
        } finally {
            if (suspended != null) {
                try {
                    transactionManager.resume(suspended);
                } catch (Exception ex) {
                	LOG.error( "testThisObject: Unable to resume suspended transaction after test.", ex );
                    return false;
                }
            }
        }
    }
}
