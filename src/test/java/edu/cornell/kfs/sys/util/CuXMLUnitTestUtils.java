package edu.cornell.kfs.sys.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.diff.DifferenceEvaluators;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;

public class CuXMLUnitTestUtils {

    private static final Logger LOG = LogManager.getLogger();

    public static void compareXML(File expectedXmlFile, File actualXmlFile) {
        compareXMLWithEvaluators(expectedXmlFile, actualXmlFile, DifferenceEvaluators.Default);
    }
    
    public static void compareXML(Object expectedXml, Object actualXml) {
        compareXMLWithEvaluators(expectedXml, actualXml, DifferenceEvaluators.Default);
    }
    
    public static void compareXMLWithEvaluators(File expectedXmlFile, File actualXmlFile, DifferenceEvaluator... evaluators) {
        compareXMLWithEvaluators((Object) expectedXmlFile, (Object) actualXmlFile, evaluators);
    }
    
    public static void compareXMLWithEvaluators(Object expectedXml, Object actualXml, DifferenceEvaluator... evaluators) {
        Diff xmlDiff = DiffBuilder.compare(expectedXml)
                .withTest(actualXml)
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



    public static void filterXml(final String sourceFile, final String targetFile, Object annotatedItem)
            throws IOException, XMLStreamException {
        final FailableFunction<String, InputStream, IOException> sourceBuilder;
        if (StringUtils.startsWithIgnoreCase(sourceFile, TestSpringContextExtension.CLASSPATH_PREFIX)) {
            sourceBuilder = CuCoreUtilities::getResourceAsStream;
        } else {
            sourceBuilder = FileInputStream::new;
        }

        try (
                final InputStream source = sourceBuilder.apply(sourceFile);
                final OutputStream target = new FileOutputStream(targetFile);
        ) {
            filterXml(source, target, annotatedItem);
        }
    }

    public static void filterXml(final InputStream source, final OutputStream target,
            final Object annotatedItem) throws IOException, XMLStreamException {
        XMLStreamReader xmlReader = null;
        XMLStreamWriter xmlWriter = null;

        try (
                final InputStreamReader reader = new InputStreamReader(source, StandardCharsets.UTF_8);
                final OutputStreamWriter writer = new OutputStreamWriter(target, StandardCharsets.UTF_8);
        ) {
            final XMLInputFactory inFactory = XMLInputFactory.newInstance();
            inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
            xmlReader = inFactory.createXMLStreamReader(reader);
            
            final XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
            xmlWriter = outFactory.createXMLStreamWriter(writer);
            
            final CuXmlFilterer xmlFilterer;
            if (annotatedItem instanceof Class) {
                xmlFilterer = new CuXmlFilterer(xmlReader, xmlWriter, (Class<?>) annotatedItem);
            } else if (annotatedItem instanceof Enum) {
                xmlFilterer = new CuXmlFilterer(xmlReader, xmlWriter, (Enum<?>) annotatedItem);
            } else {
                throw new IllegalArgumentException("annotatedItem was not a class or an enum constant");
            }
            
            xmlFilterer.filterXml();
        } finally {
            CuXMLStreamUtils.closeQuietly(xmlWriter);
            CuXMLStreamUtils.closeQuietly(xmlReader);
        }
    }

}
