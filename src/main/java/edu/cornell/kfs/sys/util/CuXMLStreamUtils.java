package edu.cornell.kfs.sys.util;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * Utility class for quietly closing StAX readers/writers.
 */
public final class CuXMLStreamUtils {

    private CuXMLStreamUtils() {
        throw new UnsupportedOperationException("Do not call this method!");
    }

    public static void closeQuietly(XMLStreamReader xmlReader) {
        if (xmlReader != null) {
            try {
                xmlReader.close();
            } catch (Exception e) {
                // Ignore.
            }
        }
    }

    public static void closeQuietly(XMLStreamWriter xmlWriter) {
        if (xmlWriter != null) {
            try {
                xmlWriter.close();
            } catch (Exception e) {
                // Ignore.
            }
        }
    }

}
