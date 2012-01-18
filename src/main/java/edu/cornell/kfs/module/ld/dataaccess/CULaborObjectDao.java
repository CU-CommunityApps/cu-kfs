package edu.cornell.kfs.module.ld.dataaccess;

import org.kuali.kfs.module.ld.businessobject.LaborObject;
import org.kuali.kfs.module.ld.dataaccess.LaborObjectDao;

public interface CULaborObjectDao  extends LaborObjectDao{

	 public LaborObject getByPrimaryId( int fiscalYear, String chartCode,String objectCode );
}
