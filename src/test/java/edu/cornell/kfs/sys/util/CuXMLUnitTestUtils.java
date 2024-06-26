package edu.cornell.kfs.sys.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import org.apache.commons.collections4.IterableUtils;

public class CuXMLUnitTestUtils {

    private static final Logger LOG = LogManager.getLogger();

    public static void compareXML(File expectedXmlFile, File actualXmlFile) {
        Diff xmlDiff = DiffBuilder.compare(expectedXmlFile)
                .withTest(actualXmlFile)
                .checkForIdentical()
                .ignoreComments()
                .ignoreWhitespace()
                .build();

        for (Difference dff : xmlDiff.getDifferences()) {
            LOG.info("compareXML, difference: " + dff);
        }

        assertEquals(IterableUtils.size(xmlDiff.getDifferences()), 0);
    }
}
