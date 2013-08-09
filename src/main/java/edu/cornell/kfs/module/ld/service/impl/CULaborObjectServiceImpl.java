package edu.cornell.kfs.module.ld.service.impl;

import edu.cornell.kfs.module.ld.dataaccess.CULaborObjectDao;
import edu.cornell.kfs.module.ld.service.CULaborObjectService;

import org.kuali.kfs.module.ld.businessobject.LaborObject;
import org.kuali.kfs.module.ld.service.impl.LaborObjectServiceImpl;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class CULaborObjectServiceImpl extends LaborObjectServiceImpl implements CULaborObjectService {
    private CULaborObjectDao cuLaborObjectDao;


	public LaborObject getByPrimaryKey(int fiscalYear, String chartCode,String objectCode ){

		return cuLaborObjectDao.getByPrimaryId(fiscalYear,chartCode,objectCode);
	}


	
	public void setCuLaborObjectDao(CULaborObjectDao cuLaborObjectDao) {
		this.cuLaborObjectDao = cuLaborObjectDao;
	}

	
}

