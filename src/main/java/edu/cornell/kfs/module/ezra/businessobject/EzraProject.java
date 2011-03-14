/**
 * 
 */
package edu.cornell.kfs.module.ezra.businessobject;

import java.sql.Date;
import java.util.LinkedHashMap;

import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kns.bo.PersistableBusinessObjectBase;

/**
 * @author kwk43
 *
 */
public class EzraProject extends PersistableBusinessObjectBase {

	private String projectId;
	private Long projectDirectorId;
	private String projectDepartmentId;
	private Date lastUpdated;
	
	
	/**
	 * @return the projectId
	 */
	public String getProjectId() {
		return projectId;
	}

	/**
	 * @param projectId the projectId to set
	 */
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	
	/**
	 * @return the projectDirectorId
	 */
	public Long getProjectDirectorId() {
		return projectDirectorId;
	}

	/**
	 * @param projectDirectorId the projectDirectorId to set
	 */
	public void setProjectDirectorId(Long projectDirectorId) {
		this.projectDirectorId = projectDirectorId;
	}

	/**
	 * @return the projectDepartmentId
	 */
	public String getProjectDepartmentId() {
		return projectDepartmentId;
	}

	/**
	 * @param projectDepartmentId the projectDepartmentId to set
	 */
	public void setProjectDepartmentId(String projectDepartmentId) {
		this.projectDepartmentId = projectDepartmentId;
	}

	/**
	 * @return the lastUpdated
	 */
	public Date getLastUpdated() {
		return lastUpdated;
	}

	/**
	 * @param lastUpdated the lastUpdated to set
	 */
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	/* (non-Javadoc)
	 * @see org.kuali.rice.kns.bo.BusinessObjectBase#toStringMapper()
	 */
	@Override
	protected LinkedHashMap toStringMapper() {
		LinkedHashMap m = new LinkedHashMap();

        m.put("projectId", projectId);
	    return m;
	}

}
