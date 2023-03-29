package edu.cornell.kfs.sys.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Helper class to allow for using an XMLStreamReader in the init-resources part of a try-with-resources block.
 */
public final class XMLStreamReaderWrapper implements Closeable {

    private final XMLStreamReader xmlReader;

    public XMLStreamReaderWrapper(XMLStreamReader xmlReader) {
        Objects.requireNonNull(xmlReader, "xmlReader cannot be null");
        this.xmlReader = xmlReader;
    }

    public XMLStreamReader getXMLStreamReader() {
        return xmlReader;
    }

    @Override
    public void close() throws IOException {
        try {
            xmlReader.close();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

}
