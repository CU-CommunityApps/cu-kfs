package edu.cornell.kfs.module.ezra.service.impl;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kns.UserSession;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.service.DocumentService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.ObjectUtils;

import edu.cornell.kfs.module.cg.businessobject.AgencyExtension;
import edu.cornell.kfs.module.cg.businessobject.ProposalExtension;
import edu.cornell.kfs.module.ezra.businessobject.EzraProposal;
import edu.cornell.kfs.module.ezra.businessobject.Sponsor;
import edu.cornell.kfs.module.ezra.dataaccess.EzraProposalDao;
import edu.cornell.kfs.module.ezra.dataaccess.SponsorDao;
import edu.cornell.kfs.module.ezra.service.EzraService;
import edu.cornell.kfs.module.ezra.util.EzraUtils;

public class EzraServiceImpl implements EzraService {

	private BusinessObjectService businessObjectService;
	private DocumentService documentService;
	private SponsorDao sponsorDao;
	private EzraProposalDao proposalDao;
	private DateTimeService dateTimeService;



	public boolean updateProposalsSince(Date date) {
		boolean result = false;
		List<EzraProposal> proposals = proposalDao.getProposalsUpdatedSince(date);
		Map fields = new HashMap();
		for (EzraProposal ezraProposal : proposals) {
			String proposalId = ezraProposal.getProjectId();
			fields.clear();
			fields.put("proposalNumber", proposalId);
			Proposal proposal = (Proposal)businessObjectService.findByPrimaryKey(Proposal.class, fields);
			Proposal oldProposal = proposal;
			if (ObjectUtils.isNull(proposal)) {
				proposal = new Proposal();
				proposal.setProposalNumber(Long.valueOf(proposalId));
				proposal.setAgencyNumber(ezraProposal.getSponsorNumber().toString());
				//check to see if this is a real cfda 
				proposal.setCfdaNumber(ezraProposal.getCfdaNumber());
				proposal.setProposalProjectTitle(ezraProposal.getProjectTitle());
				proposal.setGrantNumber(ezraProposal.getSponsorProjectId());
				proposal.setProposalStatusCode(ezraProposal.getStatus());
				proposal.setProposalPurposeCode(ezraProposal.getPurpose());
				proposal.setProposalBeginningDate(ezraProposal.getStartDate());
				proposal.setProposalEndingDate(ezraProposal.getStopDate());
				proposal.setProposalTotalAmount(ezraProposal.getTotalAmt());
				proposal.setFederalPassThroughAgencyNumber(ezraProposal.getFederalPassThroughAgencyNumber().toString());
				proposal.setProposalFederalPassThroughIndicator(ezraProposal.getFederalPassThroughBoolean());
				proposal.setProposalAwardTypeCode("Z");
				proposal.setActive(true);

				ProposalExtension ext = (ProposalExtension)proposal.getExtension();
				if (ext == null) {
					ext = new ProposalExtension();
				}
				ext.setLastUpdated(SpringContext.getBean(DateTimeService.class).getCurrentSqlDate());
				ext.setProposalNumber(proposal.getProposalNumber());
				proposal.setExtension(ext);
			} else {

				if (!StringUtils.equals(proposal.getAgencyNumber(), ezraProposal.getSponsorNumber().toString())) {
					proposal.setAgencyNumber(ezraProposal.getSponsorNumber().toString());
				}
				if (!StringUtils.equals(proposal.getCfdaNumber(), ezraProposal.getCfdaNumber())) {
					//check to see if this is a real cfda 

					proposal.setCfdaNumber(ezraProposal.getCfdaNumber());
				}
				if (!StringUtils.equals(proposal.getProposalProjectTitle(), ezraProposal.getProjectTitle())) {
					proposal.setProposalProjectTitle(ezraProposal.getProjectTitle());
				}
				if (!StringUtils.equals(proposal.getGrantNumber(), ezraProposal.getSponsorProjectId())) {
					proposal.setGrantNumber(ezraProposal.getSponsorProjectId());
				}
				if (!StringUtils.equals(proposal.getProposalStatusCode(), ezraProposal.getStatus())) {
					proposal.setProposalStatusCode(ezraProposal.getStatus());
				}
				if (!StringUtils.equals(proposal.getProposalPurposeCode(), ezraProposal.getPurpose())) {
					proposal.setProposalPurposeCode(ezraProposal.getPurpose());
				}
				if (dateTimeService.dateDiff(proposal.getProposalBeginningDate(), ezraProposal.getStartDate(), true) != 0) {
					proposal.setProposalBeginningDate(ezraProposal.getStartDate());
				}
				if (dateTimeService.dateDiff(proposal.getProposalEndingDate(), ezraProposal.getStopDate(), true) != 0) {
					proposal.setProposalEndingDate(ezraProposal.getStopDate());
				}
				if (proposal.getProposalTotalAmount().compareTo(ezraProposal.getTotalAmt()) != 0) {
					proposal.setProposalTotalAmount(ezraProposal.getTotalAmt());
				}
				if (!StringUtils.equals(proposal.getFederalPassThroughAgencyNumber(), ezraProposal.getFederalPassThroughAgencyNumber().toString())) {
					proposal.setFederalPassThroughAgencyNumber(ezraProposal.getFederalPassThroughAgencyNumber().toString());
				}
				if ( !(proposal.getProposalFederalPassThroughIndicator() ^ ezraProposal.getFederalPassThroughBoolean())) {
					proposal.setProposalFederalPassThroughIndicator(ezraProposal.getFederalPassThroughBoolean());
				}
				proposal.setProposalAwardTypeCode("Z");

				proposal.setActive(true);
				ProposalExtension ext = (ProposalExtension)proposal.getExtension();
				if (ext == null) {
					ext = new ProposalExtension();
				}
				ext.setLastUpdated(SpringContext.getBean(DateTimeService.class).getCurrentSqlDate());
				ext.setProposalNumber(proposal.getProposalNumber());
				proposal.setExtension(ext);
				businessObjectService.save(ext);
			}
			GlobalVariables.setUserSession(new UserSession(KFSConstants.SYSTEM_USER));
			// DocumentService docService = SpringContext.getBean(DocumentService.class);
			MaintenanceDocument proposalDoc  = null;
			try {
				proposalDoc = (MaintenanceDocument) documentService.getNewDocument("PRPL");
			} catch (WorkflowException we) {
				we.printStackTrace();
			}
			proposalDoc.getDocumentHeader().setDocumentDescription("Auto creation of new proposal");
			if (ObjectUtils.isNotNull(oldProposal)) {
				proposalDoc.getOldMaintainableObject().setBusinessObject(oldProposal);
			} else {
				// agencyDoc.setOldMaintainableObject(new KualiMaintainableImpl());
			}
			proposalDoc.getNewMaintainableObject().setBusinessObject(proposal);
			try {
				documentService.saveDocument(proposalDoc);
				proposalDoc.getDocumentHeader().getWorkflowDocument().routeDocument("Automatically created and routed");
			} catch (WorkflowException we) {
				we.printStackTrace();
			}
//			Date lastUpdated = dateTimeService.getCurrentSqlDate();
//			proposal.setLastUpdated(lastUpdated);
//			businessObjectService.save(sponsor);
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
				//need to create a new agency here
				//TODO: refactor into a service call
				agency = new Agency();
				agency.setAgencyNumber(sponsorId.toString());
				agency.setReportingName(sponsor.getSponsorLabel());
				if (sponsor.getSponsorName().length() > 50)
					agency.setFullName(sponsor.getSponsorName().substring(0,49));
				else {
					agency.setFullName(sponsor.getSponsorName());
				}
				if (sponsor.getParentSponsor() != null)
					agency.setReportsToAgencyNumber(sponsor.getParentSponsor().toString());
				String sponsorTypeCode = EzraUtils.getAgencyTypeMap().get(sponsor.getSourceCode().toString());
				agency.setAgencyTypeCode(sponsorTypeCode);
				agency.setActive(true);
				AgencyExtension ext = (AgencyExtension)agency.getExtension();
				if (ext == null) {
					ext = new AgencyExtension();
				}
				ext.setLastUpdated(SpringContext.getBean(DateTimeService.class).getCurrentSqlDate());
				ext.setAgencyNumber(agency.getAgencyNumber());
				agency.setExtension(ext);
				// agency.refreshReferenceObject("extension");
			} else {
				//agencyService.updateAgency(sponsor);
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
				AgencyExtension ext = (AgencyExtension)agency.getExtension();
				if (ext == null) {
					ext = new AgencyExtension();
				}
				ext.setAgencyNumber(agency.getAgencyNumber());
				ext.setLastUpdated(SpringContext.getBean(DateTimeService.class).getCurrentSqlDate());
				agency.setExtension(ext);
				agency.setActive(true);
				// agency.refreshReferenceObject("extension");
				businessObjectService.save(ext);
			}
			GlobalVariables.setUserSession(new UserSession(KFSConstants.SYSTEM_USER));
			// DocumentService docService = SpringContext.getBean(DocumentService.class);
			MaintenanceDocument agencyDoc  = null;
			try {
				agencyDoc = (MaintenanceDocument) documentService.getNewDocument("AGCY");
			} catch (WorkflowException we) {
				we.printStackTrace();
			}
			agencyDoc.getDocumentHeader().setDocumentDescription("Auto creation of new agency");
			if (ObjectUtils.isNotNull(oldAgency)) {
				agencyDoc.getOldMaintainableObject().setBusinessObject(oldAgency);
			} else {
				// agencyDoc.setOldMaintainableObject(new KualiMaintainableImpl());
			}
			agencyDoc.getNewMaintainableObject().setBusinessObject(agency);
			try {
				documentService.saveDocument(agencyDoc);
				agencyDoc.getDocumentHeader().getWorkflowDocument().routeDocument("Automatically created and routed");
			} catch (WorkflowException we) {
				we.printStackTrace();
			}
//			Date lastUpdated = dateTimeService.getCurrentSqlDate();
//			sponsor.setLastUpdated(lastUpdated);
//			businessObjectService.save(sponsor);
		}
		return result;

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
	public EzraProposalDao getProposalDao() {
		return proposalDao;
	}

	/**
	 * @param proposalDao the proposalDao to set
	 */
	public void setProposalDao(EzraProposalDao proposalDao) {
		this.proposalDao = proposalDao;
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
