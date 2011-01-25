/**
 * 
 */
package edu.cornell.kfs.module.ezra.dataaccess;

import java.sql.Date;
import java.util.List;

import edu.cornell.kfs.module.ezra.businessobject.Sponsor;

/**
 * @author kwk43
 *
 */
public interface SponsorDao {

	public List<Sponsor> getSponsorsUpdatedSince(Date date);
	
}
