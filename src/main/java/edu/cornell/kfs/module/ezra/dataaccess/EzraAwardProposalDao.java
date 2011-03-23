/**
 * 
 */
package edu.cornell.kfs.module.ezra.dataaccess;

import java.sql.Date;
import java.util.List;

import edu.cornell.kfs.module.ezra.businessobject.EzraProposalAward;

/**
 * @author kwk43
 *
 */
public interface EzraAwardProposalDao {

	public List<EzraProposalAward> getProposals();
	public List<EzraProposalAward> getAwardsUpdatedSince(Date date);
	
}
