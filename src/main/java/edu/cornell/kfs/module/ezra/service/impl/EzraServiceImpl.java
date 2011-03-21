package edu.cornell.kfs.module.ezra.service.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.kfs.module.cg.businessobject.ProposalOrganization;
import org.kuali.kfs.module.cg.businessobject.ProposalProjectDirector;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kim.bo.types.dto.AttributeSet;
import org.kuali.rice.kim.service.KIMServiceLocator;
import org.kuali.rice.kim.service.PersonService;
import org.kuali.rice.kns.UserSession;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.maintenance.Maintainable;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.service.DocumentService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.ObjectUtils;

import edu.cornell.kfs.module.cg.businessobject.AgencyExtension;
import edu.cornell.kfs.module.ezra.businessobject.EzraProject;
import edu.cornell.kfs.module.ezra.businessobject.EzraProposalAward;
import edu.cornell.kfs.module.ezra.businessobject.Investigator;
import edu.cornell.kfs.module.ezra.businessobject.ProjectInvestigator;
import edu.cornell.kfs.module.ezra.businessobject.Sponsor;
import edu.cornell.kfs.module.ezra.dataaccess.EzraProposalDao;
import edu.cornell.kfs.module.ezra.dataaccess.SponsorDao;
import edu.cornell.kfs.module.ezra.service.EzraService;
import edu.cornell.kfs.module.ezra.util.EzraUtils;

public class EzraServiceImpl implements EzraService {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(EzraServiceImpl.class);

    private BusinessObjectService businessObjectService;
	private DocumentService documentService;
	private SponsorDao sponsorDao;
	private EzraProposalDao ezraProposalDao;
	private DateTimeService dateTimeService;

	public boolean updateProposals() {
		boolean result = false;
		List<EzraProposalAward> proposals = ezraProposalDao.getProposals();
		Map fields = new HashMap();
		for (EzraProposalAward ezraProposal : proposals) {
			String proposalId = ezraProposal.getProjectId();
			fields.clear();
			fields.put("proposalNumber", proposalId);
			Proposal proposal = (Proposal)businessObjectService.findByPrimaryKey(Proposal.class, fields);
			if (ObjectUtils.isNull(proposal)) {
				proposal = new Proposal();
				proposal.setProposalNumber(Long.valueOf(proposalId));
				LOG.info("Creating Proposal: "+proposalId);
				if (ezraProposal.getSponsorNumber() != null) {
					Agency agency = businessObjectService.findBySinglePrimaryKey(Agency.class, ezraProposal.getSponsorNumber().toString());
					if (ObjectUtils.isNull(agency)) {
						agency = createAgency(ezraProposal.getSponsorNumber());
						routeAgencyDocument(agency, null);
					}
					proposal.setAgencyNumber(ezraProposal.getSponsorNumber().toString());
				}
				Map projInvFields = new HashMap();
				projInvFields.put("projectId", ezraProposal.getProjectId());
				projInvFields.put("awardProposalId", ezraProposal.getAwardProposalId());
				List<ProjectInvestigator> projInvs = (List<ProjectInvestigator>)businessObjectService.findMatching(ProjectInvestigator.class, projInvFields);
				List<ProposalProjectDirector> ppds = createProjectDirectors(projInvs, proposal.getProposalNumber());
				proposal.setProposalProjectDirectors(ppds);

				Map deptFields = new HashMap();
				deptFields.put("organizationCode", ezraProposal.getDepartmentId());
				List<Organization> orgs = (List<Organization>)businessObjectService.findMatching(Organization.class, deptFields);
				List<ProposalOrganization> propOrgs = createProposalOrganizations(orgs, proposal.getProposalNumber());
				proposal.setProposalOrganizations(propOrgs);
				
				//check to see if this is a real cfda 
				proposal.setCfdaNumber(ezraProposal.getCfdaNumber());
				proposal.setProposalProjectTitle(ezraProposal.getProjectTitle());
				proposal.setGrantNumber(ezraProposal.getSponsorProjectId());
				proposal.setProposalStatusCode(EzraUtils.getProposalAwardStatusMap().get(ezraProposal.getStatus()));
				proposal.setProposalPurposeCode(EzraUtils.getProposalPurposeMap().get(ezraProposal.getPurpose()));
				proposal.setProposalBeginningDate(ezraProposal.getStartDate());
				proposal.setProposalEndingDate(ezraProposal.getStopDate());
				proposal.setProposalTotalAmount(ezraProposal.getTotalAmt());
				if (ezraProposal.getFederalPassThroughAgencyNumber() != null)
					proposal.setFederalPassThroughAgencyNumber(ezraProposal.getFederalPassThroughAgencyNumber().toString());
				proposal.setProposalFederalPassThroughIndicator(ezraProposal.getFederalPassThroughBoolean());
				proposal.setProposalAwardTypeCode("Z");
				proposal.setActive(true);

				
				
				
//				ProposalExtension ext = (ProposalExtension)proposal.getExtension();
//				if (ext == null) {
//					ext = new ProposalExtension();
//				}
//				ext.setLastUpdated(SpringContext.getBean(DateTimeService.class).getCurrentSqlDate());
//				ext.setProposalNumber(proposal.getProposalNumber());
//				proposal.setExtension(ext);
			 
				routeProposalDocument(proposal);
//				
//				Award award = new Award(proposal);
//				award.setAwardStatusCode(proposal.getProposalStatusCode());
//				award.setAwardBeginningDate(proposal.getProposalBeginningDate());
//				award.setAwardEndingDate(proposal.getProposalEndingDate());
//				award.setAwardTotalAmount(proposal.getProposalTotalAmount());
//				award.setAwardEntryDate(dateTimeService.getCurrentSqlDate());
//				//award.setGran
			}
		}
		return result;
	}

	public boolean updateSponsorsSince(Date date) {
		boolean result = false;
		List<Sponsor> sponsors = sponsorDao.getSponsorsUpdatedSince(date);
		Map fields = new HashMap();
		for (Sponsor sponsor : sponsors) {
			Long sponsorId =  sponsor.getSponsorId();
			fields.clear();
			fields.put("agencyNumber", sponsorId.toString());
			Agency agency = (Agency)businessObjectService.findByPrimaryKey(Agency.class, fields);
			Agency oldAgency = agency;
			if (ObjectUtils.isNull(agency)) {
				agency = createAgency(sponsorId);

			} else {
				updateAgency(agency, sponsor);
			}
			routeAgencyDocument(agency, oldAgency);
		}
		return result;

	}

	public Agency createAgency(Long sponsorId) {
		LOG.info("Creating Agency: "+ sponsorId);
		Sponsor sponsor = businessObjectService.findBySinglePrimaryKey(Sponsor.class, sponsorId);
		Agency agency = new Agency();
		agency.setAgencyNumber(sponsorId.toString());
		agency.setReportingName(sponsor.getSponsorLabel());
		if (sponsor.getSponsorName().length() > 50)
			agency.setFullName(sponsor.getSponsorName().substring(0,49));
		else {
			agency.setFullName(sponsor.getSponsorName());
		}
		if (sponsor.getParentSponsor() != null) {
			//Need to create the reports to agency hierarchy.
			Agency rptsToAgency = businessObjectService.findBySinglePrimaryKey(Agency.class, sponsor.getParentSponsor());
			if (rptsToAgency == null) {
				rptsToAgency = createAgency(sponsor.getParentSponsor());
				routeAgencyDocument(rptsToAgency, null);
			}
			agency.setReportsToAgencyNumber(sponsor.getParentSponsor().toString());
		}
		String sponsorTypeCode = EzraUtils.getAgencyTypeMap().get(sponsor.getSourceCode().toString());
		agency.setAgencyTypeCode(sponsorTypeCode);
		agency.setActive(true);
		AgencyExtension ext = (AgencyExtension)agency.getExtension();
		if (ext == null) {
			ext = new AgencyExtension();
		}
		ext.setLastUpdated(SpringContext.getBean(DateTimeService.class).getCurrentSqlDate());
		//ext.setAgencyNumber(agency.getAgencyNumber());
		agency.setExtension(ext);
		return agency;

	}

	public void updateAgency(Agency agency, Sponsor sponsor) {
		if (!StringUtils.equals(agency.getReportingName(), sponsor.getSponsorLabel())) {
			agency.setReportingName(sponsor.getSponsorLabel());
		}
		if (!StringUtils.equals(agency.getFullName(), sponsor.getSponsorName())) {
			if (sponsor.getSponsorName().length() > 50)
				agency.setFullName(sponsor.getSponsorName().substring(0,49));
			else {
				agency.setFullName(sponsor.getSponsorName());
			}
		}

		if (sponsor.getParentSponsor() != null && !StringUtils.equals(agency.getReportsToAgencyNumber(), sponsor.getParentSponsor().toString())) {
			agency.setReportsToAgencyNumber(sponsor.getParentSponsor().toString());
		}
		String sponsorTypeCode = EzraUtils.getAgencyTypeMap().get(sponsor.getSourceCode().toString());
		if (!StringUtils.equals(agency.getAgencyTypeCode(), sponsorTypeCode)) {
			agency.setAgencyTypeCode(sponsorTypeCode);
		}
		agency.setActive(true);
		AgencyExtension ext = (AgencyExtension)agency.getExtension();
		if (ext == null) {
			ext = new AgencyExtension();
		}
		//ext.setAgencyNumber(agency.getAgencyNumber());
		ext.setLastUpdated(SpringContext.getBean(DateTimeService.class).getCurrentSqlDate());
		agency.setExtension(ext);
		//		agency.setActive(true);
		// agency.refreshReferenceObject("extension");
		//	businessObjectService.save(ext);

	}

	private void routeProposalDocument(Proposal proposal) {
		GlobalVariables.setUserSession(new UserSession(KFSConstants.SYSTEM_USER));
		MaintenanceDocument proposalDoc  = null;
		try {
			proposalDoc = (MaintenanceDocument) documentService.getNewDocument("PRPL");
		} catch (WorkflowException we) {
			we.printStackTrace();
		}
		proposalDoc.getDocumentHeader().setDocumentDescription("Auto creation of new proposal");
		proposalDoc.getNewMaintainableObject().setBusinessObject(proposal);
		try {
			documentService.saveDocument(proposalDoc);
			proposalDoc.getDocumentHeader().getWorkflowDocument().routeDocument("Automatically created and routed");
		} catch (WorkflowException we) {
			we.printStackTrace();
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void routeAgencyDocument(Agency agency, Agency oldAgency) {
		GlobalVariables.setUserSession(new UserSession(KFSConstants.SYSTEM_USER));
		// DocumentService docService = SpringContext.getBean(DocumentService.class);
		MaintenanceDocument agencyDoc  = null;
		try {
			agencyDoc = (MaintenanceDocument) documentService.getNewDocument("AGCY");
		} catch (WorkflowException we) {
			we.printStackTrace();
		}
		agencyDoc.getDocumentHeader().setDocumentDescription("Auto creation of new agency: "+ agency.getAgencyNumber());
		if (ObjectUtils.isNotNull(oldAgency)) {
			agencyDoc.getOldMaintainableObject().setBusinessObject(oldAgency);
		} 
		Maintainable agencyMaintainable = agencyDoc.getNewMaintainableObject();
		agencyMaintainable.setBusinessObject(agency);
		agencyDoc.setNewMaintainableObject(agencyMaintainable);
		try {
			documentService.saveDocument(agencyDoc);
			agencyDoc.getDocumentHeader().getWorkflowDocument().routeDocument("Automatically created and routed");
		} catch (WorkflowException we) {
			we.printStackTrace();
		}
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private List<ProposalProjectDirector> createProjectDirectors(List<ProjectInvestigator> projInvs, Long projectId) {
		List<ProposalProjectDirector> projDirs = new ArrayList<ProposalProjectDirector>();
		for (ProjectInvestigator projectInvestigator : projInvs) {
			ProposalProjectDirector ppd = new ProposalProjectDirector();
			Investigator investigator = businessObjectService.findBySinglePrimaryKey(Investigator.class, projectInvestigator.getInvestigatorId());
			if (investigator != null) {
				PersonService ps = SpringContext.getBean(PersonService.class);
				Person director = ps.getPersonByPrincipalName(investigator.getNetId());
				ppd.setPrincipalId(director.getPrincipalId());
				ppd.setProposalNumber(new Long(projectInvestigator.getProjectId()));
				//Map fieldValues = new HashMap();
				//fieldValues.put("projectId", projectInvestigator.getProjectId());
				EzraProject ep = (EzraProject) businessObjectService.findBySinglePrimaryKey(EzraProject.class, projectId);
				if (director.getPrincipalName().equals(ep.getProjectDirectorId())) {
					ppd.setProposalPrimaryProjectDirectorIndicator(true);
				}
				ppd.setActive(true);
				KIMServiceLocator.getRoleManagementService().assignPrincipalToRole(director.getPrincipalId(), "KFS-SYS", "Contracts & Grants Project Director", new AttributeSet());
				projDirs.add(ppd);
			} else {
				LOG.info("Null investigator: "+ projectInvestigator.getInvestigatorId());
			}
		}
		return projDirs;
	}

	private List<ProposalOrganization> createProposalOrganizations(List<Organization> orgs, Long projectId) {
		List<ProposalOrganization> propOrgs = new ArrayList<ProposalOrganization>();
		for (Organization org : orgs) {
			ProposalOrganization po = new ProposalOrganization();
			po.setChartOfAccountsCode("IT");
			po.setOrganizationCode(org.getOrganizationCode());
			EzraProject ep = (EzraProject)businessObjectService.findBySinglePrimaryKey(EzraProject.class, projectId);
			if (org.getOrganizationCode().equals(ep.getProjectDepartmentId())) {
				po.setProposalPrimaryOrganizationIndicator(true);
			}
			po.setActive(true);
			propOrgs.add(po);
		}
		return propOrgs;
	}
	/**
	 * @return the businessObjectService
	 */
	public BusinessObjectService getBusinessObjectService() {
		return businessObjectService;
	}

	/**
	 * @param businessObjectService the businessObjectService to set
	 */
	public void setBusinessObjectService(BusinessObjectService businessObjectService) {
		this.businessObjectService = businessObjectService;
	}

	/**
	 * @return the documentService
	 */
	public DocumentService getDocumentService() {
		return documentService;
	}

	/**
	 * @param documentService the documentService to set
	 */
	public void setDocumentService(DocumentService documentService) {
		this.documentService = documentService;
	}

	/**
	 * @return the sponsorDao
	 */
	public SponsorDao getSponsorDao() {
		return sponsorDao;
	}

	/**
	 * @param sponsorDao the sponsorDao to set
	 */
	public void setSponsorDao(SponsorDao sponsorDao) {
		this.sponsorDao = sponsorDao;
	}


	/**
	 * @return the proposalDao
	 */
	public EzraProposalDao getEzraProposalDao() {
		return ezraProposalDao;
	}

	/**
	 * @param proposalDao the proposalDao to set
	 */
	public void setEzraProposalDao(EzraProposalDao ezraProposalDao) {
		this.ezraProposalDao = ezraProposalDao;
	}

	/**
	 * @return the dateTimeService
	 */
	public DateTimeService getDateTimeService() {
		return dateTimeService;
	}

	/**
	 * @param dateTimeService the dateTimeService to set
	 */
	public void setDateTimeService(DateTimeService dateTimeService) {
		this.dateTimeService = dateTimeService;
	}



}
