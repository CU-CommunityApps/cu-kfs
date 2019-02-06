package edu.cornell.kfs.sys.batch;

import org.kuali.kfs.sys.batch.FlatFileParseTracker;
import org.kuali.kfs.sys.batch.FlatFileParseTrackerImpl;

public class TestDelimitedFlatFileParser extends CuDelimitedFlatFileParser {

    @Override
    protected FlatFileParseTracker buildNewParseTrackerInstanceFromPrototype() {
        return new FlatFileParseTrackerImpl();
    }

}
