package edu.cornell.kfs.gl.dataaccess.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.ojb.broker.query.Criteria;
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
    @SuppressWarnings("rawtypes")
    public KualiDecimal getEncumbrances(Map<String, String> input,Collection encumbranceCodes) {
        Criteria criteria = new Criteria();
        KualiDecimal encumbrances = KualiDecimal.ZERO;
        for (String key:input.keySet()) {
            criteria.addEqualTo(key, input.get(key));
        }
        criteria.addIn(KFSPropertyConstants.BALANCE_TYPE_CODE, encumbranceCodes);
        ReportQueryByCriteria queryByCriteria = QueryFactory.newReportQuery(Encumbrance.class,
                new String[]{"sum(accountLineEncumbranceAmount)"}, criteria, false);

        Iterator  iterator = getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(queryByCriteria);

        if (iterator.hasNext()) {
            @SuppressWarnings("unchecked")
            Object[] data = (Object[]) TransactionalServiceUtils.retrieveFirstAndExhaustIterator(iterator);
            if (data[0] != null) {
                encumbrances = (KualiDecimal) data[0];
            }
        }
        return encumbrances;

    }

    /**
     * this is for KFSPTS-1786 end
     */ 

}
