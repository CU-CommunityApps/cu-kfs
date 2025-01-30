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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.diff.DifferenceEvaluators;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.annotation.XmlDocumentFilter;
import edu.cornell.kfs.sys.batch.CuBatchFileUtils;

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




    public static String getXmlFileNameWithoutPathOrClasspathPrefix(final String fileName) {
        String result = CuBatchFileUtils.getFileNameWithoutPath(fileName);
        if (StringUtils.startsWithIgnoreCase(result, ResourcePatternResolver.CLASSPATH_URL_PREFIX)) {
            result = StringUtils.substringAfter(result, ResourcePatternResolver.CLASSPATH_URL_PREFIX);
        }
        return result;
    }

    public static InputStream getXmlInputStream(final String sourceFile) throws IOException {
        if (StringUtils.startsWithIgnoreCase(sourceFile, ResourcePatternResolver.CLASSPATH_URL_PREFIX)) {
            return CuCoreUtilities.getResourceAsStream(sourceFile);
        } else {
            return new FileInputStream(sourceFile);
        }
    }



    public static List<String> filterXml(final String targetDirectory, final Object annotatedItem,
            final String... sourceFiles) throws IOException, XMLStreamException {
        return filterXml(targetDirectory, annotatedItem, List.of(sourceFiles));
    }

    public static List<String> filterXml(final String targetDirectory, final Object annotatedItem,
            final Collection<String> sourceFiles) throws IOException, XMLStreamException {
        Validate.isTrue(StringUtils.endsWith(targetDirectory, CUKFSConstants.SLASH),
                "targetDirectory must contain a trailing slash");
        Validate.notEmpty(sourceFiles, "At least one source file must be specified");

        final Stream.Builder<String> newFilesWithFilteredXml = Stream.builder();

        for (final String sourceFile : sourceFiles) {
            final String sourceFileWithoutPath = getXmlFileNameWithoutPathOrClasspathPrefix(sourceFile);
            final String targetFile = targetDirectory + sourceFileWithoutPath;
            filterXml(sourceFile, targetFile, annotatedItem);
            newFilesWithFilteredXml.add(targetFile);
        }

        return newFilesWithFilteredXml.build().collect(Collectors.toUnmodifiableList());
    }

    public static void filterXml(final String sourceFile, final String targetFile, Object annotatedItem)
            throws IOException, XMLStreamException {
        try (
                final InputStream source = getXmlInputStream(sourceFile);
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
                final CloseShieldInputStream shieldedSource = CloseShieldInputStream.wrap(source);
                final CloseShieldOutputStream shieldedTarget = CloseShieldOutputStream.wrap(target);
                final InputStreamReader reader = new InputStreamReader(shieldedSource, StandardCharsets.UTF_8);
                final OutputStreamWriter writer = new OutputStreamWriter(shieldedTarget, StandardCharsets.UTF_8);
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
            } else if (annotatedItem instanceof XmlDocumentFilter) {
                xmlFilterer = new CuXmlFilterer(xmlReader, xmlWriter, (XmlDocumentFilter) annotatedItem);
            } else {
                throw new IllegalArgumentException(
                        "annotatedItem was not a class or an enum constant or the annotation itself");
            }
            
            xmlFilterer.filterXml();
        } finally {
            CuXMLStreamUtils.closeQuietly(xmlWriter);
            CuXMLStreamUtils.closeQuietly(xmlReader);
        }
    }

}
