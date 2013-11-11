package edu.cornell.kfs.gl.dataaccess.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.apache.ojb.broker.query.ReportQueryByCriteria;
import org.kuali.kfs.gl.businessobject.Encumbrance;
import org.kuali.kfs.gl.dataaccess.impl.EncumbranceDaoOjb;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.util.TransactionalServiceUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.gl.dataaccess.CuEncumbranceDao;

public class CuEncumbranceDaoOjb extends EncumbranceDaoOjb implements CuEncumbranceDao {
    /**
     * this is for KFSPTS-1786 begin
     */ 
    
    public KualiDecimal getEncumbrances(Map<String, String> input,Collection encumbranceCodes) {
        Criteria criteria = new Criteria();
        KualiDecimal encumbrances=KualiDecimal.ZERO;
       for(String key:input.keySet()){
           criteria.addEqualTo(key, input.get(key));
       }
       criteria.addIn(KFSPropertyConstants.BALANCE_TYPE_CODE, encumbranceCodes);
       ReportQueryByCriteria queryByCriteria =QueryFactory.newReportQuery(Encumbrance.class, 
               new String[]{"sum(accountLineEncumbranceAmount)"}, criteria, false);
    
       Iterator  iterator= getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(queryByCriteria);
     
      if (iterator.hasNext()) {
          Object[] data = (Object[]) TransactionalServiceUtils.retrieveFirstAndExhaustIterator(iterator);
          if (data[0] != null) {
              encumbrances= (KualiDecimal) data[0];
          }
      }
        return encumbrances;
        
    }
    
    /**
     * this is for KFSPTS-1786 end
     */ 
    
    /**
     * Returns an Iterator of all encumbrances that need to be closed for the fiscal year and specified charts
     * 
     * @param fiscalYear a fiscal year to find encumbrances for
     * @param charts charts to find encumbrances for
     * @return an Iterator of encumbrances to close
     * @see org.kuali.kfs.gl.dataaccess.EncumbranceDao#getEncumbrancesToClose(java.lang.Integer, java.util.List)
     */
    @SuppressWarnings("rawtypes")
    public Iterator getEncumbrancesToClose(Integer fiscalYear, Collection<String> charts) {

        Criteria criteria = new Criteria();
        criteria.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, fiscalYear);
        criteria.addIn(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, charts);

        QueryByCriteria query = new QueryByCriteria(Encumbrance.class, criteria);
        query.addOrderByAscending(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE);
        query.addOrderByAscending(KFSPropertyConstants.ACCOUNT_NUMBER);
        query.addOrderByAscending(KFSPropertyConstants.SUB_ACCOUNT_NUMBER);
        query.addOrderByAscending(KFSPropertyConstants.OBJECT_CODE);
        query.addOrderByAscending(KFSPropertyConstants.SUB_OBJECT_CODE);
        query.addOrderByAscending(KFSPropertyConstants.BALANCE_TYPE_CODE);

        return getPersistenceBrokerTemplate().getIteratorByQuery(query);
    }
}
