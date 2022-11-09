package edu.cornell.kfs.sys.batch;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.batch.InitiateDirectory;
import org.kuali.kfs.kns.bo.Step;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

import edu.cornell.kfs.sys.CUKFSConstants;

public abstract class CuAbstractStep extends AbstractStep implements Step, BeanNameAware, InitializingBean, InitiateDirectory{
    
    
    /**
	 * This method takes the file provided and modifies the name of that file to include a date and timestamp.
	 * @param origFile File to be renamed
	 * @param fileName Original file's name
	 * @param directory Directory path where the file will reside
	 */
	public void addTimeStampToFileName(File origFile, String fileName, String directory) {
		String fileNameProper = fileName.substring(0, fileName.lastIndexOf('.'));
		String fileExtension = fileName.substring(fileName.lastIndexOf('.'));

		DateFormat df = new SimpleDateFormat(CUKFSConstants.DATE_FORMAT_MMddyyyy_hhmmss, Locale.US);
		
		File newFile = new File(directory+File.separator+fileNameProper+"_"+df.format(new Date())+fileExtension);
		origFile.renameTo(newFile);
		
	}
}
