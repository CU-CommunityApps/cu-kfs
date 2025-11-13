package edu.cornell.kfs.fp.batch.service;

import java.util.List;

import edu.cornell.kfs.fp.batch.TravelMealCardLoadDataFileResults;
import edu.cornell.kfs.fp.businessobject.TravelMealCardFileLineEntry;

public interface TravelMealCardFileFeedService {

    public TravelMealCardLoadDataFileResults loadTmCardDataFromBatchFile(String fileName);
    
    public void sendNotificationFileNotReceived();
    
    public void sendFileProcessingResultsNotification(TravelMealCardLoadDataFileResults loadResults);
    
   /*
    * NOTE: Visibility of the method is public ONLY for unit testing purposes.
    *       It is not intended to be called outside of the actual implementation class.
    */
    public List<TravelMealCardFileLineEntry> readTmCardFileContents(String fileName);

}
