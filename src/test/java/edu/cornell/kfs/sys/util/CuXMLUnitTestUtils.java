package edu.cornell.kfs.sys.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.diff.DifferenceEvaluators;

import org.apache.commons.collections4.IterableUtils;

public class CuXMLUnitTestUtils {

    private static final Logger LOG = LogManager.getLogger();

    public static void compareXML(File expectedXmlFile, File actualXmlFile) {
        compareXMLWithEvaluators(expectedXmlFile, actualXmlFile, DifferenceEvaluators.Default);
    }
    
    public static void compareXMLWithEvaluators(File expectedXmlFile, File actualXmlFile, DifferenceEvaluator... evaluators) {
        Diff xmlDiff = DiffBuilder.compare(expectedXmlFile)
                .withTest(actualXmlFile)
                .checkForIdentical()
                .ignoreComments()
                .ignoreWhitespace()
                .withDifferenceEvaluator(
                        DifferenceEvaluators.chain(evaluators))
                .build();

        for (Difference dff : xmlDiff.getDifferences()) {
            LOG.info("compareXMLWithEvaluators, difference: " + dff);
        }

        assertEquals(IterableUtils.size(xmlDiff.getDifferences()), 0);
    }
}
