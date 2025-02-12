package edu.cornell.kfs.tax.batch.service.impl;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.sys.KFSConstants;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;

import edu.cornell.kfs.tax.batch.xml.TaxOutputDefinitionV2;

public class WrappedCsvWriter implements Closeable {

    private final FileOutputStream outputStream;
    private final OutputStreamWriter streamWriter;
    private final ICSVWriter csvWriter;
    private final String outputFileName;

    public WrappedCsvWriter(final String outputFileName,
            final TaxOutputDefinitionV2 taxOutputDefinition) throws IOException {
        Validate.notBlank(outputFileName, "outputFileName cannot be blank");
        Validate.notNull(taxOutputDefinition, "taxOutputDefinition cannot be null");

        FileOutputStream outputStream = null;
        OutputStreamWriter streamWriter = null;
        ICSVWriter csvWriter = null;
        boolean success = false;

        try {
            outputStream = new FileOutputStream(outputFileName);
            streamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            csvWriter = buildCSVWriter(streamWriter, taxOutputDefinition.getFieldSeparator());
            this.outputStream = outputStream;
            this.streamWriter = streamWriter;
            this.csvWriter = csvWriter;
            this.outputFileName = outputFileName;
            success = true;
        } finally {
            if (!success) {
                IOUtils.closeQuietly(csvWriter, streamWriter, outputStream);
            }
        }
    }

    private static ICSVWriter buildCSVWriter(final Writer fileWriter, final String separator) {
        Validate.isTrue(StringUtils.length(separator) == 1, "separator should have been exactly 1 character long");
        return new CSVWriterBuilder(fileWriter)
                .withSeparator(separator.charAt(0))
                .withLineEnd(KFSConstants.NEWLINE)
                .build();
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(csvWriter, streamWriter, outputStream);
    }

    public ICSVWriter getCsvWriter() {
        return csvWriter;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

}
