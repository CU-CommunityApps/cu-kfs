package edu.cornell.kfs.krad.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigContext;
import org.kuali.kfs.krad.service.MaintainableXMLConversionService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.util.CuXMLStreamUtils;

/**
 * Alternative implementation of MaintainableXMLConversionService that supports
 * more advanced conversion features.
 * 
 * The portions pertaining to rule-map-parsing had to be copied from
 * MaintainableXMLConversionServiceImpl. This is because that class uses
 * a private method to initialize the maps, and it's called in an area where
 * overriding the default conversion behavior would make the parsing code unusable.
 * If MaintainableXMLConversionServiceImpl is modified to have better subclass support
 * in the future, then this class should be modified accordingly to subclass it.
 * 
 * The path to the conversion rules file can be configured explicitly if needed.
 * If no such setup has been performed, then the file path will be obtained
 * from the "maintainable.conversion.rule.file" property instead.
 * 
 * For details on the features of this conversion service implementation,
 * refer to the CynergyMaintenanceXMLConverter class.
 * 
 * NOTE: Unlike other MaintainableXMLConversionService implementations,
 * this class is intended for converting the entire maintenance document
 * XML payload at once, not just a sub-section of the XML content.
 */
public class CuMaintainableXMLConversionServiceImpl implements MaintainableXMLConversionService, InitializingBean {

    private static final Logger LOG = LogManager.getLogger();

    // Copied this constant from the superclass and increased its visibility.
    protected static final String CONVERSION_RULE_FILE_PARAMETER = "maintainable.conversion.rule.file";

    protected Map<String,Map<String,String>> classPropertyRuleMaps;
    protected Map<String,String> dateRuleMap;
    protected String conversionRuleFile;

    public void setConversionRuleFile(String conversionRuleFile) {
        this.conversionRuleFile = conversionRuleFile;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (StringUtils.isBlank(conversionRuleFile)) {
            conversionRuleFile = ConfigContext.getCurrentContextConfig().getProperty(CONVERSION_RULE_FILE_PARAMETER);
        }
        if (StringUtils.isNotBlank(conversionRuleFile)) {
            initializeRuleMaps(conversionRuleFile);
        }
    }

    /**
     * This implementation just returns the same note XML that was passed in.
     * Our customized full-conversion process will handle the note XML conversion instead.
     */
    @Override
    public String transformMaintainableNoteXML(String xml) {
        return xml;
    }

    @Override
    public String transformMaintainableXML(String xml) {
        if (StringUtils.isBlank(xml)) {
            return StringUtils.EMPTY;
        } else if (StringUtils.isBlank(conversionRuleFile)) {
            return xml;
        }
        
        StringReader reader = null;
        StringBuilderWriter writer = null;
        XMLStreamReader xmlReader = null;
        XMLStreamWriter xmlWriter = null;
        
        try {
            reader = new StringReader(xml);
            writer = new StringBuilderWriter(xml.length());
            
            XMLInputFactory inFactory = XMLInputFactory.newInstance();
            inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
            xmlReader = inFactory.createXMLStreamReader(reader);
            
            XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
            xmlWriter = outFactory.createXMLStreamWriter(writer);

            CuMaintenanceXMLConverter xmlConverter = createXMLConverter();
            xmlConverter.initialize(xmlReader, xmlWriter);
            xmlConverter.performConversion();
            xmlWriter.flush();
            
            return writer.toString();
        } catch (XMLStreamException e) {
            handleXMLStreamException(e);
        } finally {
            CuXMLStreamUtils.closeQuietly(xmlWriter);
            CuXMLStreamUtils.closeQuietly(xmlReader);
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(reader);
        }
        
        return xml;
    }

    protected CuMaintenanceXMLConverter createXMLConverter() {
        return new CuMaintenanceXMLConverter(classPropertyRuleMaps, dateRuleMap);
    }

    /*
     * Based upon code from MaintainableXMLConversionServiceImpl.setRuleMaps().
     */
    protected void initializeRuleMaps(String resourcePath) {
        InputStream conversionRuleStream = null;
        
        try {
            conversionRuleStream = getConversionRuleStream(resourcePath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            Document domDocument = documentBuilder.parse(conversionRuleStream);
            XPath xpath = XPathFactory.newInstance().newXPath();
            
            domDocument.getDocumentElement().normalize();
            classPropertyRuleMaps = getClassPropertyRuleMapsFromXML(domDocument, xpath);
            dateRuleMap = getDateRuleMapFromXML(domDocument, xpath);
        } catch (Exception e) {
            throw new RuntimeException("Could not parse XML conversion rule file", e);
        } finally {
            IOUtils.closeQuietly(conversionRuleStream);
        }
    }

    /*
     * Uses code from MaintainableXMLConversionServiceImpl.setRuleMaps(), but has been
     * modified accordingly to read classpath resources properly.
     */
    protected InputStream getConversionRuleStream(String resourcePath) throws IOException {
        Resource resource;
        if (StringUtils.startsWith(resourcePath, ResourcePatternResolver.CLASSPATH_URL_PREFIX)) {
            resource = new ClassPathResource(resourcePath, Thread.currentThread().getContextClassLoader());
        } else {
            resource = new FileSystemResource(resourcePath);
        }
        
        if (resource.exists()) {
            return resource.getInputStream();
        } else if (StringUtils.startsWith(resourcePath, ResourcePatternResolver.CLASSPATH_URL_PREFIX)) {
            return CuCoreUtilities.getResourceAsStream(resourcePath);
        } else {
            return this.getClass().getResourceAsStream(resourcePath);
        }
    }

    /*
     * Uses the rule-map-parsing code from MaintainableXMLConversionServiceImpl.setRuleMaps().
     */
    protected Map<String,Map<String,String>> getClassPropertyRuleMapsFromXML(Document domDocument, XPath xpath) throws XPathExpressionException {
        Map<String,Map<String,String>> ruleMaps = new HashMap<>();
        
        XPathExpression exprClassProperties = xpath.compile(
                "//*[@name='maint_doc_changed_class_properties']/pattern");
        XPathExpression exprClassPropertiesPatterns = xpath.compile("pattern");
        NodeList propertyClassList = (NodeList) exprClassProperties.evaluate(domDocument, XPathConstants.NODESET);
        for (int s = 0; s < propertyClassList.getLength(); s++) {
            String classText = xpath.evaluate("class/text()", propertyClassList.item(s));
            Map<String, String> propertyRuleMap = new HashMap<String, String>();
            NodeList classPropertiesPatterns = (NodeList) exprClassPropertiesPatterns.evaluate(
                    propertyClassList.item(s), XPathConstants.NODESET);
            for (int c = 0; c < classPropertiesPatterns.getLength(); c++) {
                String matchText = xpath.evaluate("match/text()", classPropertiesPatterns.item(c));
                String replaceText = xpath.evaluate("replacement/text()", classPropertiesPatterns.item(c));
                propertyRuleMap.put(matchText, replaceText);
            }
            ruleMaps.put(classText, propertyRuleMap);
        }
        
        return ruleMaps;
    }

    /*
     * Uses a tweaked archaic version of the date-rule-map-parsing code from MaintainableXMLConversionServiceImpl.setRuleMaps().
     */
    protected Map<String,String> getDateRuleMapFromXML(Document domDocument, XPath xpath) throws XPathExpressionException {
        Map<String,String> ruleMap = new HashMap<>();
        
        XPathExpression dateFieldNames = xpath.compile("//*[@name='maint_doc_date_changes']/pattern");
        NodeList dateNamesList = (NodeList) dateFieldNames.evaluate(domDocument, XPathConstants.NODESET);
        for (int s = 0; s < dateNamesList.getLength(); s++) {
            String matchText = xpath.evaluate("match/text()", dateNamesList.item(s));
            String replaceText = xpath.evaluate("replacement/text()", dateNamesList.item(s));
            ruleMap.put(matchText, replaceText);
        }
        
        return ruleMap;
    }

    /*
     * The handling of conversion exceptions has been moved to this separate method for unit testing convenience.
     */
    protected void handleXMLStreamException(XMLStreamException e) {
        LOG.error("Error converting legacy maintainable XML", e);
    }

}
