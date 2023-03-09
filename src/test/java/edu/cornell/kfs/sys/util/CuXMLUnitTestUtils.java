package edu.cornell.kfs.sys.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;

public class CuXMLUnitTestUtils {
    
    private static final Logger LOG = LogManager.getLogger();
    
    public static void compareXML(File actualXmlFile, File expectedXmlFile) throws SAXException, IOException {
        FileInputStream actualFileInputStream = new FileInputStream(actualXmlFile);
        FileInputStream expectedFIleInputStream = new FileInputStream(expectedXmlFile);

        BufferedReader actualBufferedReader = new BufferedReader(new InputStreamReader(actualFileInputStream));
        BufferedReader expectedBufferedReader = new BufferedReader(new InputStreamReader(expectedFIleInputStream));

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);

        Diff xmlDiff = new Diff(expectedBufferedReader, actualBufferedReader);
        DetailedDiff detailXmlDiff = new DetailedDiff(xmlDiff);
        List<Difference> differences = detailXmlDiff.getAllDifferences();
        for (Difference difference : differences) {
            LOG.info("compareXML, difference: " + difference);
        }
        assertEquals(0, differences.size());
    }
}
