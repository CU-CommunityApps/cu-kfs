package edu.cornell.kfs.module.bc.service.impl;

import edu.cornell.kfs.module.bc.dataaccess.DeptOrgCrosswalkDao;
import edu.cornell.kfs.module.bc.service.DeptOrgCrosswalkService;

public class DeptOrgCrosswalkServiceImpl implements DeptOrgCrosswalkService{

	private DeptOrgCrosswalkDao deptOrgCrosswalkDao;
	
	public void updatePositionDataExtension(String deptID, String orgCode){
		deptOrgCrosswalkDao.updatePositionDataExtension(deptID, orgCode);
	}

	public void setDeptOrgCrosswalkDao(DeptOrgCrosswalkDao deptOrgCrosswalkDao) {
		this.deptOrgCrosswalkDao = deptOrgCrosswalkDao;
	}


}
