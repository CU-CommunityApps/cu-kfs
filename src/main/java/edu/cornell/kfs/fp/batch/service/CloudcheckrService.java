package edu.cornell.kfs.fp.batch.service;

import java.io.IOException;
import java.net.URISyntaxException;

import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAwsResultWrapper;
import edu.cornell.kfs.fp.batch.xml.cloudcheckr.CloudCheckrWrapper;

public interface CloudcheckrService {
    
    public CloudCheckrWrapper getCloudCheckrWrapper(String startDate, String endDate) throws URISyntaxException, IOException;
    
    public DefaultKfsAccountForAwsResultWrapper getDefaultKfsAccountForAwsResultWrapper() throws URISyntaxException, IOException;
}
