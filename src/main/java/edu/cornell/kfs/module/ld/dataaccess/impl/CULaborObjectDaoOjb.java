package edu.cornell.kfs.module.ld.dataaccess.impl;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.kfs.module.ld.businessobject.LaborObject;
import org.kuali.kfs.module.ld.dataaccess.impl.LaborObjectDaoOjb;

import edu.cornell.kfs.module.ld.dataaccess.CULaborObjectDao;

public class CULaborObjectDaoOjb extends LaborObjectDaoOjb implements CULaborObjectDao {
	
	 public LaborObject getByPrimaryId( int fiscalYear, String chartCode,String objectCode ){
	        Criteria criteria = new Criteria();
	        criteria.addEqualTo("universityFiscalYear", fiscalYear);
	        criteria.addEqualTo("chartOfAccountsCode", chartCode==null?chartCode:chartCode.toUpperCase());
	        criteria.addEqualTo("financialObjectCode", objectCode==null?objectCode:objectCode.toUpperCase());
	        return (LaborObject) getPersistenceBrokerTemplate().getObjectByQuery(QueryFactory.newQuery(LaborObject.class, criteria));
	 }

}
