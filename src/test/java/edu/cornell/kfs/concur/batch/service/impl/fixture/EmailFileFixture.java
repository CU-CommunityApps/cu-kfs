package edu.cornell.kfs.concur.batch.service.impl.fixture;

import org.apache.commons.lang.StringUtils;

public enum EmailFileFixture {
    
    EMPTY_FILE("src/test/resources/edu/cornell/kfs/concur/batch/service/impl/fixture/empty.txt", StringUtils.EMPTY),
    SIMPLE_FILE("src/test/resources/edu/cornell/kfs/concur/batch/service/impl/fixture/simpleContents.txt", "This is the first line.\n\tThis is the second line indented.");
    
    public final String fullFilePath;
    public final String fileContents;
    
    private EmailFileFixture(String fullFilePath, String filContents) {
        this.fullFilePath = fullFilePath;
        this.fileContents = filContents;
    }

}
