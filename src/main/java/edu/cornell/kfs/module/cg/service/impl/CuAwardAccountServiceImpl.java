package edu.cornell.kfs.module.cg.service.impl;

import edu.cornell.kfs.module.cg.dataaccess.AwardAccountDao;
import edu.cornell.kfs.module.cg.service.CuAwardAccountService;


public class CuAwardAccountServiceImpl implements CuAwardAccountService {
	protected AwardAccountDao awardAccountDao;

	/**
	 * @see edu.cornell.kfs.module.cg.service.CuAwardAccountService#isAccountUsedOnAnotherAward(java.lang.String, java.lang.String, java.lang.Long)
	 */
	@Override
	public boolean isAccountUsedOnAnotherAward(String chart, String account, Long proposalNumber) {
		return awardAccountDao.isAccountUsedOnAnotherAward(chart, account, proposalNumber);
	}

	/**
	 * Gets the awardAccountDao.
	 * 
	 * @return awardAccountDao
	 */
	public AwardAccountDao getAwardAccountDao() {
		return awardAccountDao;
	}

	/**
	 * Sets the awardAccountDao.
	 * 
	 * @param awardAccountDao
	 */
	public void setAwardAccountDao(AwardAccountDao awardAccountDao) {
		this.awardAccountDao = awardAccountDao;
	}

}
