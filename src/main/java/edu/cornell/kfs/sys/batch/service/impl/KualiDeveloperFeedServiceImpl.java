package edu.cornell.kfs.sys.batch.service.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.sys.batch.KualiDeveloperFlatInputFileType;
import edu.cornell.kfs.sys.batch.service.KualiDeveloperFeedService;
import edu.cornell.kfs.sys.businessobject.KualiDeveloper;

@Transactional
public class KualiDeveloperFeedServiceImpl implements KualiDeveloperFeedService {

	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(KualiDeveloperFeedServiceImpl.class);

	protected BatchInputFileService batchInputFileService;
	protected KualiDeveloperFlatInputFileType kualiDeveloperFlatInputFileType;
	protected BusinessObjectService businessObjectService;
	
	public boolean loadKualiDeveloperDataFromBatchFile(String fileName) {

		LOG.info("\n Reading Kuali Developer file entries. \n");
		List<KualiDeveloper> kualiDeveloperList = readKualiDeveloperFileContents(fileName);

		if (kualiDeveloperList == null) {
			LOG.info("No entries in the input file \n");
			return true;
		}

		LOG.info("\n Loading data into the database: CU_KUALI_DEVELOPER_T table. \n");
		loadDataInDB(kualiDeveloperList);
		return true;
	}	

	protected List<KualiDeveloper> readKualiDeveloperFileContents(String fileName) {

		try {

			FileInputStream fileContents = null;
			fileContents = new FileInputStream(fileName);

			List<KualiDeveloper> kualiDevelopers = null;

			byte[] fileByteContent = IOUtils.toByteArray(fileContents);
			kualiDevelopers = (List<KualiDeveloper>) batchInputFileService.parse(kualiDeveloperFlatInputFileType, fileByteContent);

			if (kualiDevelopers == null || kualiDevelopers.isEmpty()) {
				LOG.warn("No entries in the input file " + fileName);
				return null;
			}

			return kualiDevelopers;
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

	protected void loadDataInDB(List<KualiDeveloper> kualiDevelopersToLoad) {
	    for (KualiDeveloper kualiDeveloper : kualiDevelopersToLoad) {
			KualiDeveloper retrievedEntry = (KualiDeveloper) businessObjectService.retrieve(kualiDeveloper);
            if (ObjectUtils.isNotNull(retrievedEntry)) {
                kualiDeveloper.setVersionNumber(retrievedEntry.getVersionNumber());
            }
            businessObjectService.save(kualiDeveloper);
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

}
