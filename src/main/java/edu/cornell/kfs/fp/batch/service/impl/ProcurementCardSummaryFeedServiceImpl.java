package edu.cornell.kfs.fp.batch.service.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.fp.batch.ProcurementCardSummaryFlatInputFileType;
import edu.cornell.kfs.fp.batch.service.ProcurementCardSummaryFeedService;
import edu.cornell.kfs.fp.businessobject.ProcurementCardSummary;
import edu.cornell.kfs.fp.businessobject.ProcurementCardSummaryEntry;


@Transactional
public class ProcurementCardSummaryFeedServiceImpl implements ProcurementCardSummaryFeedService {


	private static final Logger LOG = LogManager.getLogger(ProcurementCardSummaryFeedServiceImpl.class);

	
	protected BatchInputFileService batchInputFileService;
	protected ProcurementCardSummaryFlatInputFileType procurementCardSummaryFlatInputFileType;
	protected BusinessObjectService businessObjectService;
	protected DateTimeService dateTimeService;
	
	/**
	 * @see edu.cornell.kfs.fp.batch.service.ProcurementCardSummaryFeedService#loadPCardDataFromBatchFile(java.lang.String, java.lang.String)
	 */
	
	
	public boolean loadPCardDataFromBatchFile(String fileName) {
		
		LOG.info("\n Processing .done file: " + fileName + "\n");

	
		LOG.info("\n Reading PCard Summary extract entries. \n");
		// read the data from the PCard Summary extract
		List<ProcurementCardSummary> pcardSummaryList = readPCardFileContents(fileName);

		// if there are no entries in the PCard Summary extract file then there is nothing to process
		if (pcardSummaryList == null) {
			LOG.info("No entries in the input file \n");
			return true;
		}

		LOG.info("\n Generating entries from the valid ProcurementCardSummary list. \n");
		List<ProcurementCardSummaryEntry> entriesToLoad = new ArrayList<ProcurementCardSummaryEntry>();
		generatePCardEntryToLoadToDB(pcardSummaryList, entriesToLoad); 
		
		LOG.info("\n Loading data into the database: CU_FP_PCARD_SUMMARY_T table. entriesToLoad={} \n", entriesToLoad.size());
		// load entries into CU_FP_PCARD_SUMMARY_T table
		loadDataInDB(entriesToLoad);
		return true;
	}	
	
	
	
	



	private void generatePCardEntryToLoadToDB(
			List<ProcurementCardSummary> pcardSummaryList,
			List<ProcurementCardSummaryEntry> entriesToLoad) {
		for (ProcurementCardSummary pcardSummary : pcardSummaryList) {

			// entries for the pcard summary 
			entriesToLoad.add(generatePCardSummaryEntry(pcardSummary));
	    }
	}


	/**
	 * Generate a collection of ProcurementCardSummaryEntry from the given
	 * ProcurementCardSummary entry.
	 * 
	 * @param pcardSummary (ProcurementCardSummary) 
	 * @return entry (ProcurementCardSummaryEntry)
	 */
	protected ProcurementCardSummaryEntry generatePCardSummaryEntry(
			ProcurementCardSummary pcardSummary) {
		
		// assign values:
		String cardHolderAccountNumber = pcardSummary.getCardHolderAccountNumber();
		String cardHolderName = pcardSummary.getCardHolderName();
		String emplid = pcardSummary.getEmplid();
		String netid = pcardSummary.getNetid();
		String accountStatus = pcardSummary.getAccountStatus();
		KualiDecimal summaryAmount = generateKualiDecimal(pcardSummary.getSummaryAmount());
		Date cycleStartDate = convertDate(pcardSummary.getCycleStartDate(), "cycle start date", cardHolderAccountNumber, emplid);
		Date loadDate = convertDate(pcardSummary.getLoadDate(), "load date", cardHolderAccountNumber, emplid);
		
		
		ProcurementCardSummaryEntry entry = new ProcurementCardSummaryEntry();
		
		entry.setCardHolderAccountNumber(cardHolderAccountNumber);
		entry.setCardHolderName(cardHolderName);
		entry.setEmplid(emplid);
		entry.setNetid(netid);
		entry.setAccountStatus(accountStatus);
		entry.setSummaryAmount(summaryAmount);
		entry.setCycleStartDate(cycleStartDate);
		entry.setLoadDate(loadDate);
		
		return entry;
	}
	



	/**
	 * Reads the incoming PCard Summary extract with the given file name and builds a list of
	 * ProcurementCardSummaryEntry object.
	 * 
	 * @param fileName
	 *            the name of the PCard Summary extract to be read in
	 * @return a list of ProcurementCardSummaryEntry objects created from the
	 *         entries in the PCard Summary extract
	 */
	protected List<ProcurementCardSummary> readPCardFileContents(String fileName) {

		try {

			FileInputStream fileContents = null;
			// read file contents
			fileContents = new FileInputStream(fileName);

			List<ProcurementCardSummary> pcardSummaryEntries = null;

			byte[] fileByteContent = IOUtils.toByteArray(fileContents);
			pcardSummaryEntries = (List<ProcurementCardSummary>) batchInputFileService
					.parse(procurementCardSummaryFlatInputFileType, fileByteContent);

			// if no entries, log and return
			if (pcardSummaryEntries == null
					|| pcardSummaryEntries.isEmpty()) {
				LOG.warn("No entries in the PCard Summary input file "
						+ fileName);
				return null;
			}

			return pcardSummaryEntries;

		} catch (FileNotFoundException e) {
			LOG.error("File to parse not found " + fileName, e);
			throw new RuntimeException(
					"Cannot find the file requested to be parsed " + fileName
							+ " " + e.getMessage(), e);
		} catch (IOException e) {
			LOG.error("Error while getting file bytes:  " + e.getMessage(), e);
			throw new RuntimeException(
					"Error encountered while attempting to get file bytes: "
							+ e.getMessage(), e);
		}
	}
	
	/**
	 * Load entries in the CU_FP_PCARD_SUMMARY_T table.
	 * 
	 * @param entriesToLoad
	 */
	protected void loadDataInDB(List<ProcurementCardSummaryEntry> entriesToLoad) {
	
		// wipe out everything first
		businessObjectService.deleteMatching(
				ProcurementCardSummaryEntry.class,
				new HashMap<String, String>());

		// do the add records now; 
		for (ProcurementCardSummaryEntry entry : entriesToLoad) {
			ProcurementCardSummaryEntry retrievedEntry = (ProcurementCardSummaryEntry) businessObjectService
						.retrieve(entry);
		//	entriesToLoad
		//	.addAll(generateCalculatedSalaryFoundationTrackerCollection(psPositionJobExtractEntry));

			
			if (ObjectUtils.isNotNull(retrievedEntry)) {
					entry.setVersionNumber(retrievedEntry
							.getVersionNumber());
				}
			try {
				businessObjectService.save(entry);
			} catch (RuntimeException e) {
			    logClassLoaderDebugInfo(entry, retrievedEntry);
			    throw e;
			}
		}
	}
	
    private void logClassLoaderDebugInfo(ProcurementCardSummaryEntry entry, ProcurementCardSummaryEntry retrievedEntry) {
        Class repoClass = org.apache.ojb.broker.metadata.ClassDescriptor.class;
        LOG.info("Repository Class Loader: " + repoClass.getClassLoader());

        Class pcardSummaryEntryClass = edu.cornell.kfs.fp.businessobject.ProcurementCardSummaryEntry.class;
        LOG.info("ProcurementCardSummaryEntry Class Loader: " + pcardSummaryEntryClass.getClassLoader());
        
        LOG.info("ProcurementCardSummaryEntry Class Loader: entry attributes = {}", entry.toString());
        
        LOG.info("ProcurementCardSummaryEntry Class Loader: retrievedEntry attributes = {}", retrievedEntry.toString());
    }
	
	
	/**
	 * Generates a KualiDecimal from an input String by inserting first the
	 * decimal point before the last two digits.
	 * 
	 * @param input
	 * @return a KualiDecimal value for the input
	 */
	protected KualiDecimal generateKualiDecimal(String input) {
        if (StringUtils.isNotEmpty(input)) {
		String result = input;
		//result = result.substring(0, result.length() - 2) + "."
		//		+ result.substring(result.length() - 2, result.length());

		return new KualiDecimal(result);
        } else {
            return KualiDecimal.ZERO;
        }
	}
	
	
	private Date convertDate(String inputDate, String dateDescription,
			String cardHolderAccountNumber, String emplid) {
		Date outputDate = null;

		try {
			if (inputDate != null
					&& StringUtils.isNotBlank(inputDate)) {
				// the date comes in YYYYMMDD format so we change it to MM/DD/YYYY
				
				String outputDateString = inputDate.substring(4, 6) 
	            		+ "/" + inputDate.substring(6) 
	            		+ "/" + inputDate.substring(0, 4);
				outputDate = dateTimeService.convertToSqlDate(outputDateString);
				
				} else {
					LOG.error("Invalid " + dateDescription 
							+ " for account number:"
							+ cardHolderAccountNumber
							+ " and emplid:"
							+ emplid 
							+ "\n");
				}
		} catch (ParseException e) {
			throw new RuntimeException("Invalid date: " + e.getMessage(), e);
		}
		return outputDate;
	}
	
	
	
		
		
//======================================================	
	
	public BatchInputFileService getBatchInputFileService() {
		return batchInputFileService;
	}
	public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
		this.batchInputFileService = batchInputFileService;
	}
	public ProcurementCardSummaryFlatInputFileType getProcurementCardSummaryFlatInputFileType() {
		return procurementCardSummaryFlatInputFileType;
	}
	public void setProcurementCardSummaryFlatInputFileType(
			ProcurementCardSummaryFlatInputFileType procurementCardSummaryFlatInputFileType) {
		this.procurementCardSummaryFlatInputFileType = procurementCardSummaryFlatInputFileType;
	}
	public BusinessObjectService getBusinessObjectService() {
		return businessObjectService;
	}
	public void setBusinessObjectService(BusinessObjectService businessObjectService) {
		this.businessObjectService = businessObjectService;
	}
	public DateTimeService getDateTimeService() {
		return dateTimeService;
	}
	public void setDateTimeService(DateTimeService dateTimeService) {
		this.dateTimeService = dateTimeService;
	}

	
}
