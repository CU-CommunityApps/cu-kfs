package edu.cornell.kfs.module.cg.service;

public interface CuAwardAccountService {

	/**
	 * Checks if the given Award Account is used on another award
	 * 
	 * @param chart
	 * @param account
	 * @param proposalNumber
	 * @return true if used, false otherwise
	 */
	public boolean isAccountUsedOnAnotherAward(String chart, String account, Long proposalNumber);

}
