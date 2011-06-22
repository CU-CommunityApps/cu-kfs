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
import org.kuali.kfs.module.cg.businessobject.AwardAccount;
import org.kuali.kfs.module.cg.businessobject.AwardProjectDirector;
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
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;

import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;
import edu.cornell.kfs.module.ezra.businessobject.Deliverable;
import edu.cornell.kfs.module.ezra.businessobject.EzraProject;
import edu.cornell.kfs.module.ezra.businessobject.EzraProposalAward;
import edu.cornell.kfs.module.ezra.businessobject.Investigator;
import edu.cornell.kfs.module.ezra.businessobject.ProjectInvestigator;
import edu.cornell.kfs.module.ezra.businessobject.Sponsor;
import edu.cornell.kfs.module.ezra.dataaccess.EzraAwardProposalDao;
import edu.cornell.kfs.module.ezra.dataaccess.SponsorDao;
import edu.cornell.kfs.module.ezra.service.EzraService;
import edu.cornell.kfs.module.ezra.util.EzraUtils;

public class EzraServiceImpl implements EzraService {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(EzraServiceImpl.class);

    private BusinessObjectService businessObjectService;
	private DocumentService documentService;
	private SponsorDao sponsorDao;
	private EzraAwardProposalDao ezraAwardProposalDao;
	private DateTimeService dateTimeService;
	private ParameterService parameterService;

	public boolean updateProposals() {
		
		List<EzraProposalAward> proposals = ezraAwardProposalDao.getProposals();
		Map fields = new HashMap();
		for (EzraProposalAward ezraProposal : proposals) {
			String proposalId = ezraProposal.getProjectId();
			fields.clear();
			fields.put("proposalNumber", proposalId);
			Proposal proposal = (Proposal)businessObjectService.findByPrimaryKey(Proposal.class, fields);
			if (ObjectUtils.isNull(proposal)) {
				proposal = createProposal(ezraProposal);
			 
				routeProposalDocument(proposal);
				
				Award award = createAward(proposal, null, ezraProposal);
				routeAwardDocument(award, null);
			}
		}
		return true;
	}
	
	public boolean updateAwardsSince(Date date) {
		boolean result = true;
		List<EzraProposalAward> awards = ezraAwardProposalDao.getAwardsUpdatedSince(date);
		Map fields = new HashMap();
		LOG.info("Retrieved : "+ awards.size()+" Awards to update since "+ date.toString());
		for (EzraProposalAward ezraAward : awards) {
			String proposalId = ezraAward.getProjectId();
			fields.clear();
			fields.put("proposalNumber", proposalId);
			Award award = (Award)businessObjectService.findByPrimaryKey(Award.class, fields);
			if (award == null) {
				LOG.error("Award: "+proposalId +" is null and probably should have already been created");
			} else {
				Proposal proposal = createProposal(ezraAward);
				Award newAward = createAward(proposal, award, ezraAward);
				routeAwardDocument(newAward, award);
				
			}
		}
		return result;
	}
	
	public boolean updateSponsorsSince(Date date) {
		boolean result = true;
		List<Sponsor> sponsors = sponsorDao.getSponsorsUpdatedSince(date);
		Map fields = new HashMap();
		for (Sponsor sponsor : sponsors) {
			Long sponsorId =  sponsor.getSponsorId();
			fields.clear();
			fields.put("agencyNumber", sponsorId.toString());
			Agency agency = (Agency)businessObjectService.findByPrimaryKey(Agency.class, fields);
			Agency oldAgency = agency;
			if (!ObjectUtils.isNull(agency)) {
				updateAgency(agency, sponsor);
				routeAgencyDocument(agency, oldAgency);
			}
			
		}
		return result;

	}
	
	protected Award createAward(Proposal proposal, Award oldAward, EzraProposalAward ezraAward) {
		Award award = new Award(proposal);
		award.setProposal(proposal);
		award.setAwardStatusCode(proposal.getProposalStatusCode());
		award.setAwardBeginningDate(proposal.getProposalBeginningDate());
		award.setAwardEndingDate(proposal.getProposalEndingDate());
		award.setAwardDirectCostAmount(proposal.getProposalDirectCostAmount());
		award.setAwardIndirectCostAmount(KualiDecimal.ZERO);
		award.setGrantDescriptionCode(EzraUtils.getGrantDescriptionMap().get(ezraAward.getAwardDescriptionCode()));
		award.setAwardEntryDate(dateTimeService.getCurrentSqlDate());
		if (ObjectUtils.isNull(oldAward)) {
			List<AwardAccount> accounts = getAwardAccounts(proposal);
			award.setAwardAccounts(accounts);
		} else {
			award.setAwardAccounts(oldAward.getAwardAccounts());
			award.setVersionNumber(oldAward.getVersionNumber());
		}
		
		for (AwardProjectDirector apd : award.getAwardProjectDirectors()) {
			Map primaryKeys = new HashMap();
			primaryKeys.put("principalId", apd.getPrincipalId());
			primaryKeys.put("proposalNumber", apd.getProposalNumber());
			AwardProjectDirector projDir = (AwardProjectDirector) businessObjectService.findByPrimaryKey(AwardProjectDirector.class, primaryKeys);
			if (ObjectUtils.isNotNull(projDir)) {
				apd.setVersionNumber(projDir.getVersionNumber());
			} 
		}
		
		award.refreshReferenceObject("extension");
		
		Map fieldValues = new HashMap();
		fieldValues.put("projectId", award.getProposalNumber());
		fieldValues.put("deliverableType", 'F');
		fieldValues.put("finalIndicator", 'Y');
		List<Deliverable> deliverables = (List<Deliverable>)businessObjectService.findMatching(Deliverable.class, fieldValues);
		
		if (deliverables.size() == 1) {
			Deliverable deliverable = deliverables.get(0);
			if (ObjectUtils.isNotNull(deliverable)) {
				AwardExtendedAttribute aea = (AwardExtendedAttribute)award.getExtension();
				aea.setFinalFiscalReportDate(deliverable.getDueDate());
				aea.setFinalFinancialReportRequired(true);
			}
		}
		if (ezraAward.getCsVolClg() != null && 
				ezraAward.getCsVolCntr() != null && 
				ezraAward.getCsVolDept() != null && 
				ezraAward.getCsVolExt() != null && 
				ezraAward.getCsVolUniv() != null &&
				ezraAward.getCsMandClg() != null && 
				ezraAward.getCsMandCntr() != null && 
				ezraAward.getCsMandDept() != null && 
				ezraAward.getCsMandExt() != null && 
				ezraAward.getCsMandUniv() != null) {
			KualiDecimal volCostShareRequired = ezraAward.getCsVolClg().add(ezraAward.getCsVolCntr().add(ezraAward.getCsVolDept().add(ezraAward.getCsVolExt().add(ezraAward.getCsVolUniv()))));
			KualiDecimal mandCostShareRequired = ezraAward.getCsMandClg().add(ezraAward.getCsMandCntr().add(ezraAward.getCsMandDept().add(ezraAward.getCsMandExt().add(ezraAward.getCsMandUniv()))));
		
			if (volCostShareRequired.isNonZero() || mandCostShareRequired.isNonZero()) {
				AwardExtendedAttribute aea = (AwardExtendedAttribute)award.getExtension();
				aea.setCostShareRequired(true);
			}
		}
		AwardExtendedAttribute aea = (AwardExtendedAttribute)award.getExtension();
		aea.setVersionNumber(aea.getVersionNumber()+1);
		award.refreshReferenceObject("proposal");
		award.refreshNonUpdateableReferences();
		return award;
	}
	
	protected Proposal createProposal(EzraProposalAward ezraProposal) {
		String proposalId = ezraProposal.getProjectId();

		Proposal proposal = new Proposal();
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
		EzraProject project = (EzraProject)businessObjectService.findBySinglePrimaryKey(EzraProject.class, proposal.getProposalNumber());
		
		List<ProposalProjectDirector> ppds = createProjectDirectors(proposal.getProposalNumber(), project);
		proposal.setProposalProjectDirectors(ppds);

		List<ProposalOrganization> propOrgs = createProposalOrganizations(proposal.getProposalNumber(), project);
		proposal.setProposalOrganizations(propOrgs);
		
		//check to see if this is a real cfda 
		if (ezraProposal.getCfdaNumber() != null) {
			proposal.setCfdaNumber(ezraProposal.getCfdaNumber().trim());
		}
		if (project.getProjectTitle() != null) {
			proposal.setProposalProjectTitle(project.getProjectTitle().trim());
		}
		
		if (ObjectUtils.isNotNull(ezraProposal.getSponsorProjectId()) && ezraProposal.getSponsorProjectId().length() > 27) {
			proposal.setGrantNumber(ezraProposal.getSponsorProjectId().substring(0,26));
		} else {
			proposal.setGrantNumber(ezraProposal.getSponsorProjectId());
		}
		proposal.setProposalStatusCode(EzraUtils.getProposalAwardStatusMap().get(ezraProposal.getStatus()));
		proposal.setProposalPurposeCode(EzraUtils.getProposalPurposeMap().get(ezraProposal.getPurpose()));
		proposal.setProposalBeginningDate(ezraProposal.getStartDate());
		proposal.setProposalEndingDate(ezraProposal.getStopDate());
		proposal.setProposalDirectCostAmount(ezraProposal.getTotalAmt());
		proposal.setProposalIndirectCostAmount(KualiDecimal.ZERO);
//		if (ezraProposal.getFederalPassThroughAgencyNumber() != null) {
//			Agency agency = businessObjectService.findBySinglePrimaryKey(Agency.class, ezraProposal.getFederalPassThroughAgencyNumber().toString());
//			if (ObjectUtils.isNull(agency)) {
//				agency = createAgency(ezraProposal.getFederalPassThroughAgencyNumber());
//				routeAgencyDocument(agency, null);
//			}
//			proposal.setFederalPassThroughAgencyNumber(ezraProposal.getFederalPassThroughAgencyNumber().toString());
//		}
//		proposal.setProposalFederalPassThroughIndicator(ezraProposal.getFederalPassThroughBoolean());
		proposal.setProposalAwardTypeCode("Z");
		proposal.setActive(true);
		return proposal;
	}

	public Agency createAgency(Long sponsorId) {
		LOG.info("Creating Agency: "+ sponsorId);
		Sponsor sponsor = businessObjectService.findBySinglePrimaryKey(Sponsor.class, sponsorId);
		Agency agency = new Agency();
		agency.setAgencyNumber(sponsorId.toString());
		agency.setReportingName(sponsor.getSponsorLabel());
		if (ObjectUtils.isNotNull(sponsor.getSponsorName()) && sponsor.getSponsorName().length() > 50)
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
//		AgencyExtension ext = (AgencyExtension)agency.getExtension();
//		if (ext == null) {
//			ext = new AgencyExtension();
//		}
//		ext.setLastUpdated(SpringContext.getBean(DateTimeService.class).getCurrentSqlDate());
//		//ext.setAgencyNumber(agency.getAgencyNumber());
//		agency.setExtension(ext);
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
			
			//Need to create the reports to agency hierarchy.
			Agency rptsToAgency = businessObjectService.findBySinglePrimaryKey(Agency.class, sponsor.getParentSponsor());
			if (rptsToAgency == null) {
				rptsToAgency = createAgency(sponsor.getParentSponsor());
				routeAgencyDocument(rptsToAgency, null);
			}
			agency.setReportsToAgencyNumber(sponsor.getParentSponsor().toString());
		}
		String sponsorTypeCode = EzraUtils.getAgencyTypeMap().get(sponsor.getSourceCode().toString());
		if (!StringUtils.equals(agency.getAgencyTypeCode(), sponsorTypeCode)) {
			agency.setAgencyTypeCode(sponsorTypeCode);
		}
		agency.setActive(true);
//		AgencyExtension ext = (AgencyExtension)agency.getExtension();
//		if (ext == null) {
//			ext = new AgencyExtension();
//		}
//		//ext.setAgencyNumber(agency.getAgencyNumber());
//		ext.setLastUpdated(SpringContext.getBean(DateTimeService.class).getCurrentSqlDate());
//		//ext.setVersionNumber(ext.getVersionNumber()+1);
//		agency.setExtension(ext);
		//		agency.setActive(true);
		// agency.refreshReferenceObject("extension");
		//	businessObjectService.save(ext);

	}
	
	
	
	private void routeAgencyDocument(Agency agency, Agency oldAgency) {
		GlobalVariables.clear();
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
			agencyDoc.getDocumentHeader().setDocumentDescription(agency.getAgencyNumber()+" by auto edit");
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
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void routeAwardDocument(Award award, Award oldAward) {
		GlobalVariables.clear();
		GlobalVariables.setUserSession(new UserSession(KFSConstants.SYSTEM_USER));
		// DocumentService docService = SpringContext.getBean(DocumentService.class);
		MaintenanceDocument awardDoc  = null;
		try {
			awardDoc = (MaintenanceDocument) documentService.getNewDocument("AWRD");
		} catch (WorkflowException we) {
			we.printStackTrace();
		}
		LOG.info("Created a new Award doc. "+ award.getProposalNumber());
		awardDoc.getDocumentHeader().setDocumentDescription("Auto creation of new award : " +award.getProposalNumber()); 
		if (ObjectUtils.isNotNull(oldAward)) {
			awardDoc.getOldMaintainableObject().setBusinessObject(oldAward);
			award.setVersionNumber(oldAward.getVersionNumber());
			awardDoc.getDocumentHeader().setDocumentDescription(award.getProposalNumber()+" by auto edit");

		} 
		awardDoc.getNewMaintainableObject().setBusinessObject(award);;
		try {
			documentService.saveDocument(awardDoc);
			//documentService.routeDocument(awardDoc, "Automatically created and routed", null);
			awardDoc.getDocumentHeader().getWorkflowDocument().routeDocument("Automatically created and routed");
		} catch (WorkflowException we) {
			we.printStackTrace();
		} catch (RuntimeException rte) {
			LOG.error(rte);
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void routeProposalDocument(Proposal proposal) {
		GlobalVariables.clear();
		GlobalVariables.setUserSession(new UserSession(KFSConstants.SYSTEM_USER));
		MaintenanceDocument proposalDoc  = null;
		try {
			proposalDoc = (MaintenanceDocument) documentService.getNewDocument("PRPL");
		} catch (WorkflowException we) {
			we.printStackTrace();
		}
		proposalDoc.getDocumentHeader().setDocumentDescription("Auto creation of new proposal: "+ proposal.getProposalNumber());
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
	
	

	private List<ProposalProjectDirector> createProjectDirectors(Long projectId, EzraProject project) {
		List<ProposalProjectDirector> projDirs = new ArrayList<ProposalProjectDirector>();
		Investigator investigator = (Investigator)businessObjectService.findBySinglePrimaryKey(Investigator.class, project.getProjectDirectorId());
		if (investigator != null) {
			PersonService ps = SpringContext.getBean(PersonService.class);
			if (investigator.getNetId() != null) {
				Person director = ps.getPersonByPrincipalName(investigator.getNetId());
				if (director != null) {
					Map primaryKeys = new HashMap();
					primaryKeys.put("principalId", director.getPrincipalId());
					primaryKeys.put("proposalNumber", projectId);
					ProposalProjectDirector ppd = (ProposalProjectDirector) businessObjectService.findByPrimaryKey(ProposalProjectDirector.class, primaryKeys);
					if (ObjectUtils.isNull(ppd)) {
						ppd = new ProposalProjectDirector();
					} 
					//else {
					//				ppd.setVersionNumber(ppd.getVersionNumber());
					//			}
					ppd.setPrincipalId(director.getPrincipalId());
					ppd.setProposalNumber(new Long(project.getProjectId()));
					ppd.setProposalPrimaryProjectDirectorIndicator(true);
					ppd.setActive(true);
					KIMServiceLocator.getRoleManagementService().assignPrincipalToRole(director.getPrincipalId(), "KFS-SYS", "Contracts & Grants Project Director", new AttributeSet());
					projDirs.add(ppd);
				} else {
					LOG.error("PI: " +investigator.getNetId()+" for award :"+ projectId+" is not in kfs");
				}
			} else {
				LOG.error("PI netId for award :"+ projectId+" is null");

			}
		} else {
			LOG.error("Null PI: "+ project.getProjectDirectorId());
		}
		Map fieldValues = new HashMap();
		fieldValues.put("projectId", projectId.toString());
		fieldValues.put("investigatorRole", "CO");
		List<ProjectInvestigator> pis = (List<ProjectInvestigator>)businessObjectService.findMatching(ProjectInvestigator.class, fieldValues);
		for (ProjectInvestigator pi : pis) {
			Investigator inv = (Investigator)businessObjectService.findBySinglePrimaryKey(Investigator.class, pi.getInvestigatorId());
			if (inv != null) {
				PersonService ps = SpringContext.getBean(PersonService.class);
				if (inv.getNetId() !=  null) {
					Person director = ps.getPersonByPrincipalName(inv.getNetId());
					if (director != null) {
						Map primaryKeys = new HashMap();
						primaryKeys.put("principalId", director.getPrincipalId());
						primaryKeys.put("proposalNumber", projectId);
						ProposalProjectDirector ppd = (ProposalProjectDirector) businessObjectService.findByPrimaryKey(ProposalProjectDirector.class, primaryKeys);
						if (ObjectUtils.isNull(ppd)) {
							ppd = new ProposalProjectDirector();
						}
						ppd.setPrincipalId(director.getPrincipalId());
						ppd.setProposalNumber(new Long(project.getProjectId()));
						ppd.setProposalPrimaryProjectDirectorIndicator(false);
						ppd.setActive(true);
						KIMServiceLocator.getRoleManagementService().assignPrincipalToRole(director.getPrincipalId(), "KFS-SYS", "Contracts & Grants Project Director", new AttributeSet());

						//check to make sure that this project director is not already in the list.
						for (ProposalProjectDirector projDir : projDirs) {
							if (projDir.getPrincipalId().equals(ppd.getPrincipalId()))
								continue;

						}
						projDirs.add(ppd);
					} else {
						LOG.error("Investigator: " +investigator.getNetId()+" is for award :"+ projectId+" is not in kfs");
					}
				} else {
					LOG.error("Invesigator netId for award :"+ projectId+" is null");

				}
			} else {
				LOG.error("Null investigator: "+ pi.getInvestigatorId());
			}
		}
		
		return projDirs;
	}

	private List<ProposalOrganization> createProposalOrganizations(Long projectId, EzraProject project) {
		
		//EzraProject ep = (EzraProject)businessObjectService.findBySinglePrimaryKey(EzraProject.class, projectId);
		String orgCode = project.getProjectDepartmentId();
		
		Map deptFields = new HashMap();
		deptFields.put("organizationCode", orgCode);
		List<Organization> orgs = (List<Organization>)businessObjectService.findMatching(Organization.class, deptFields);
		
		
		LOG.info("Retrieved Orgs :"+ orgs.size() + " for Proposal "+ projectId);
		List<ProposalOrganization> propOrgs = new ArrayList<ProposalOrganization>();
		for (Organization org : orgs) {
			Map primaryKeys = new HashMap();
			primaryKeys.put("chartOfAccountsCode", org.getChartOfAccountsCode());
			primaryKeys.put("organizationCode", org.getOrganizationCode());
			primaryKeys.put("proposalNumber", projectId);
			ProposalOrganization po = (ProposalOrganization) businessObjectService.findByPrimaryKey(ProposalOrganization.class, primaryKeys);
			if (ObjectUtils.isNull(po)) {
				po = new ProposalOrganization();
			} else {
				po.setVersionNumber(po.getVersionNumber()+1);
			}
			po.setChartOfAccountsCode("IT");
			po.setOrganizationCode(org.getOrganizationCode());
			//EzraProject ep = (EzraProject)businessObjectService.findBySinglePrimaryKey(EzraProject.class, projectId);
			if (org.getOrganizationCode().equals(project.getProjectDepartmentId())) {
				po.setProposalPrimaryOrganizationIndicator(true);
			}
			po.setActive(true);
			propOrgs.add(po);
		}
		return propOrgs;
	}
	
	private  List<AwardAccount> getAwardAccounts(Proposal proposal) {
		List<AwardAccount> awardAccounts = new ArrayList<AwardAccount>();

		String principalId = parameterService.getParameterValue("KFS-EZRA", "Award", "DEFAULT_PROJECT_DIRECTOR");
		String[] chartAcct = parameterService.getParameterValue("KFS-EZRA", "Award", "DEFAULT_AWARD_ACCOUNT").split(":");
		String chart = chartAcct[0];
		String acct = chartAcct[1];

		AwardAccount account = new AwardAccount();
		account.setProposalNumber(proposal.getProposalNumber());
		account.setChartOfAccountsCode(chart);
		account.setAccountNumber(acct);
		account.setPrincipalId(principalId);
		
		awardAccounts.add(account);
		
		return awardAccounts;
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
	public EzraAwardProposalDao getEzraAwardProposalDao() {
		return ezraAwardProposalDao;
	}

	/**
	 * @param proposalDao the proposalDao to set
	 */
	public void setEzraAwardProposalDao(EzraAwardProposalDao ezraAwardProposalDao) {
		this.ezraAwardProposalDao = ezraAwardProposalDao;
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

	/**
	 * @return the parameterService
	 */
	public ParameterService getParameterService() {
		return parameterService;
	}

	/**
	 * @param parameterService the parameterService to set
	 */
	public void setParameterService(ParameterService parameterService) {
		this.parameterService = parameterService;
	}

}
