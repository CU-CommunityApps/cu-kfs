/**
 * 
 */
package edu.cornell.kfs.module.ezra.businessobject;

import java.sql.Date;
import java.util.LinkedHashMap;

import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

/**
 * @author kwk43
 *
 */
public class Deliverable extends PersistableBusinessObjectBase {

	private static final long serialVersionUID = 1L;
	private String projectId;
	private String deliverableType;
	private String finalIndicator;
	private Date dueDate;
	
	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getDeliverableType() {
		return deliverableType;
	}

	public void setDeliverableType(String deliverableType) {
		this.deliverableType = deliverableType;
	}

	public String getFinalIndicator() {
		return finalIndicator;
	}

	public void setFinalIndicator(String finalIndicator) {
		this.finalIndicator = finalIndicator;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	@SuppressWarnings("rawtypes")
	protected LinkedHashMap toStringMapper() {
		return null;
	}
}
