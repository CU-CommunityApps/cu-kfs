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

import liquibase.repackaged.org.apache.commons.collections4.IterableUtils;

public class CuXMLUnitTestUtils {

    private static final Logger LOG = LogManager.getLogger();

    public static void compareXML(File expectedXmlFile, File actualXmlFile) {
        compareXMLWithEvaluatorors(expectedXmlFile, actualXmlFile, DifferenceEvaluators.Default);
    }
    
    public static void compareXMLWithEvaluatorors(File expectedXmlFile, File actualXmlFile, DifferenceEvaluator... evaluators) {
        Diff xmlDiff = DiffBuilder.compare(expectedXmlFile)
                .withTest(actualXmlFile)
                .checkForIdentical()
                .ignoreComments()
                .ignoreWhitespace()
                .withDifferenceEvaluator(
                        DifferenceEvaluators.chain(
                                DifferenceEvaluators.Default,
                                new KualiDecimalXmlDifferenceEvaluator()))
                .build();

        for (Difference dff : xmlDiff.getDifferences()) {
            LOG.info("compareXMLWithKualiDecimalEvaluator, difference: " + dff);
        }

        assertEquals(IterableUtils.size(xmlDiff.getDifferences()), 0);
    }
}
