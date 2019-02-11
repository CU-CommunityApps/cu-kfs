package edu.cornell.kfs.sys.batch;

import org.kuali.kfs.sys.batch.DelimitedFlatFilePropertySpecification;

public class TestDelimitedFlatFilePropertySpecification extends DelimitedFlatFilePropertySpecification {

    @Override
    protected Class<?> getFormatterClass(Object parsedObject) {
        return formatterClass;
    }

}
