/**
 * 
 */
package edu.cornell.kfs.module.ezra.dataaccess;

import java.sql.Date;
import java.util.List;

import edu.cornell.kfs.module.ezra.businessobject.EzraProposal;

/**
 * @author kwk43
 *
 */
public interface EzraProposalDao {

	public List<EzraProposal> getProposalsUpdatedSince(Date date);
	
}
