package edu.cornell.kfs.module.ld.service;

import org.kuali.kfs.module.ld.businessobject.LaborObject;
import org.kuali.kfs.module.ld.service.LaborObjectService;

public interface CULaborObjectService extends LaborObjectService{
	
	   public LaborObject getByPrimaryKey(int fiscalYear, String chartCode,String objectCode );

}
