package edu.cornell.kfs.sys.batch.service.impl;

import edu.cornell.kfs.fp.batch.ProcurementCardSummaryFlatInputFileType;
import edu.cornell.kfs.fp.businessobject.ProcurementCardSummary;
import edu.cornell.kfs.sys.batch.KualiDeveloperFlatInputFileType;
import edu.cornell.kfs.sys.batch.service.KualiDeveloperFeedService;

import edu.cornell.kfs.sys.businessobject.KualiDeveloper;
import edu.cornell.kfs.sys.businessobject.KualiDeveloperEntry;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Transactional
public class KualiDeveloperFeedServiceImpl implements KualiDeveloperFeedService {

	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(KualiDeveloperFeedServiceImpl.class);

	protected BatchInputFileService batchInputFileService;
	protected KualiDeveloperFlatInputFileType kualiDeveloperFlatInputFileType;
	protected BusinessObjectService businessObjectService;
	protected DateTimeService dateTimeService;
	
	public boolean loadKualiDeveloperDataFromBatchFile(String fileName) {
		
		LOG.info("\n Processing .done file: " + fileName + "\n");

	
		LOG.info("\n Reading Kuali Developer file entries. \n");
		List<KualiDeveloper> kualiDeveloperList = readKualiDeveloperFileContents(fileName);

		if (kualiDeveloperList == null) {
			LOG.info("No entries in the input file \n");
			return true;
		}

		LOG.info("\n Generating entries from the valid ProcurementCardSummary list. \n");
		List<KualiDeveloperEntry> entriesToLoad = new ArrayList<KualiDeveloperEntry>();
		generateKualiDeveloperEntryToLoadToDB(kualiDeveloperList, entriesToLoad);

		
		LOG.info("\n Loading data into the database: CU_FP_PCARD_SUMMARY_T table. \n");
		loadDataInDB(entriesToLoad);
		return true;
	}	
	
	private void generateKualiDeveloperEntryToLoadToDB(List<KualiDeveloper> kualiDeveloperList, List<KualiDeveloperEntry> entriesToLoad) {
		for (KualiDeveloper kualiDeveloper : kualiDeveloperList) {
			entriesToLoad.add(generateKualiDeveloperEntry(kualiDeveloper));
	    }
	}

	/**
	 * Generate a collection of KualiDeveloperEntry from the given KualiDeveloper entry.
	 * 
	 * @param kualiDeveloper (KualiDeveloper)
	 * @return entry (PKualiDeveloperEntry)
	 */
	protected KualiDeveloperEntry generateKualiDeveloperEntry(KualiDeveloper kualiDeveloper) {
		String employeeId = kualiDeveloper.getEmployeeId();
		String firstName = kualiDeveloper.getFirstName();
		String lastName = kualiDeveloper.getLastName();
		String positionName = kualiDeveloper.getPositionName();
		String socialSecurityNumber = kualiDeveloper.getSocialSecurityNumber();

        KualiDeveloperEntry entry = new KualiDeveloperEntry();
		
		entry.setEmployeeId(employeeId);
		entry.setFirstName(firstName);
		entry.setLastName(lastName);
		entry.setPositionName(positionName);
		entry.setSocialSecurityNumber(socialSecurityNumber);
		
		return entry;
	}

	/**
	 * Reads the incoming Kuali Developer extract with the given file name and builds a list of KualiDeveloperEntry object.
	 * 
	 * @param fileName the name of the Kuali Developer extract to be read in
	 * @return a list of KualiDeveloperEntry objects created from the entries in the Kuali Developer extract
	 */
	protected List<KualiDeveloper> readKualiDeveloperFileContents(String fileName) {

		try {

			FileInputStream fileContents = null;
			fileContents = new FileInputStream(fileName);

			List<KualiDeveloper> kualiDeveloperEntries = null;

			byte[] fileByteContent = IOUtils.toByteArray(fileContents);
			kualiDeveloperEntries = (List<KualiDeveloper>) batchInputFileService.parse(kualiDeveloperFlatInputFileType, fileByteContent);

			if (kualiDeveloperEntries == null || kualiDeveloperEntries.isEmpty()) {
				LOG.warn("No entries in the input file " + fileName);
				return null;
			}

			return kualiDeveloperEntries;

		}
		catch (FileNotFoundException e) {
			LOG.error("File to parse not found " + fileName, e);
			throw new RuntimeException("Cannot find the file requested to be parsed " + fileName + " " + e.getMessage(), e);
		}
		catch (IOException e) {
			LOG.error("Error while getting file bytes:  " + e.getMessage(), e);
			throw new RuntimeException("Error encountered while attempting to get file bytes: " + e.getMessage(), e);
		}
	}
	
	/**
	 * Load entries in the CU_KUALI_DEVELOPER table.
	 * 
	 * @param entriesToLoad
	 */
	protected void loadDataInDB(List<KualiDeveloperEntry> entriesToLoad) {
	
		// wipe out everything first
		businessObjectService.deleteMatching(KualiDeveloperEntry.class, new HashMap<String, String>());

		// do the add records now; 
		for (KualiDeveloperEntry entry : entriesToLoad) {
			KualiDeveloperEntry retrievedEntry = (KualiDeveloperEntry) businessObjectService
						.retrieve(entry);
		//	entriesToLoad
		//	.addAll(generateCalculatedSalaryFoundationTrackerCollection(psPositionJobExtractEntry));

			
        if (ObjectUtils.isNotNull(retrievedEntry)) {
                entry.setVersionNumber(retrievedEntry
                        .getVersionNumber());
            }
            businessObjectService.save(entry);
        }
    }

	public BatchInputFileService getBatchInputFileService() {
		return batchInputFileService;
	}

	public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
		this.batchInputFileService = batchInputFileService;
	}

	public KualiDeveloperFlatInputFileType getKualiDeveloperFlatInputFileType() {
		return kualiDeveloperFlatInputFileType;
	}

	public void setKualiDeveloperFlatInputFileType(KualiDeveloperFlatInputFileType kualiDeveloperFlatInputFileType) {
		this.kualiDeveloperFlatInputFileType = kualiDeveloperFlatInputFileType;
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
