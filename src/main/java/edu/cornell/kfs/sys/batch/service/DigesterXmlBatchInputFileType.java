package edu.cornell.kfs.sys.batch.service;

import java.io.ByteArrayInputStream;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.binder.DigesterLoader;
import org.apache.commons.digester3.binder.RulesModule;
import org.apache.commons.digester3.xmlrules.FromXmlRulesModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.URL;
import javax.xml.validation.Schema;

import org.kuali.kfs.sys.batch.XmlBatchInputFileTypeBase;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.kfs.sys.exception.XmlErrorHandler;

// CU customization to continue support for commons-digester after KualiCo has removed it from base code;
// This can be removed once all code has been updated to use jaxb or other framework to replace commons-digester.
public abstract class DigesterXmlBatchInputFileType extends XmlBatchInputFileTypeBase<Object> {
    
    private static final Logger LOG = LogManager.getLogger();
    protected String digestorRulesFileName;
    
    @Override
    public Object parse(byte[] fileByteContent) throws ParseException {
        if (fileByteContent == null) {
            LOG.error("an invalid(null) argument was given");
            throw new IllegalArgumentException("an invalid(null) argument was given");
        }

        // handle zero byte contents, xml parsers don't deal with them well
        if (fileByteContent.length == 0) {
            LOG.error("an invalid argument was given, empty input stream");
            throw new IllegalArgumentException("an invalid argument was given, empty input stream");
        }

        // validate contents against schema
        ByteArrayInputStream validateFileContents = new ByteArrayInputStream(fileByteContent);
        validateContentsAgainstSchema(getSchemaLocation(), validateFileContents);

        // setup digester for parsing the xml file
        Digester digester = buildDigester(getSchemaLocation(), getDigestorRulesFileName());

        Object parsedContents;
        try {
            ByteArrayInputStream parseFileContents = new ByteArrayInputStream(fileByteContent);
            parsedContents = digester.parse(parseFileContents);
        } catch (Exception e) {
            LOG.error("Error parsing xml contents", e);
            throw new ParseException("Error parsing xml contents: " + e.getMessage(), e);
        }

        return parsedContents;
    }
    
    
    /**
     * @return fully-initialized Digester used to process entry XML files
     */
    public Digester buildDigester(String schemaLocation, String digestorRulesFileName) {
        // locate Digester rules
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final URL rulesUrl = classLoader.getResource(digestorRulesFileName);
        if (rulesUrl == null) {
            throw new RuntimeException("unable to locate digester rules file " + digestorRulesFileName);
        }

        // create and init digester
        RulesModule rules = new FromXmlRulesModule() {
            @Override
            protected void loadRules() {
                loadXMLRules(rulesUrl);
            }
        };
        DigesterLoader loader = DigesterLoader.newLoader(rules);
        Digester digester = loader.newDigester();

        digester.setNamespaceAware(false);
        digester.setValidating(true);
        digester.setErrorHandler(new XmlErrorHandler());

        Schema schema = getSchema(schemaLocation);
        digester.setXMLSchema(schema);

        return digester;
    }
    
    public String getDigestorRulesFileName() {
        return digestorRulesFileName;
    }

    public void setDigestorRulesFileName(final String digestorRulesFileName) {
        this.digestorRulesFileName = digestorRulesFileName;
    }

}
