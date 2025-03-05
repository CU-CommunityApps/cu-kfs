package edu.cornell.kfs.pdp.batch.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.kuali.kfs.pdp.batch.service.impl.ExtractPaymentServiceImpl;
import org.kuali.kfs.pdp.batch.service.impl.Iso20022FormatExtractor;

public class CuExtractPaymentServiceImpl extends ExtractPaymentServiceImpl {

    protected Iso20022FormatExtractor iso20022FormatExtractor;

    public CuExtractPaymentServiceImpl() {
        super(null);
    }
    
    public CuExtractPaymentServiceImpl(
            final Iso20022FormatExtractor iso20022FormatExtractor
    ) {
    	super(iso20022FormatExtractor);
    	this.iso20022FormatExtractor = iso20022FormatExtractor;
    }

    /**
     * MOD: Overridden to make filename unique by adding milliseconds to filename
     **/
    @Override
    protected String getOutputFile(final String fileprefix, final Date runDate) {
        // add a step to check for directory paths
        prepareDirectories(getRequiredDirectoryNames());

        String filename = directoryName + "/" + fileprefix + "_";
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US);
        filename = filename + sdf.format(runDate);
        filename = filename + ".xml";

        return filename;
    }

}
