/*
 * Copyright 2009 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.module.purap.util.cxml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.businessobject.SciQuestPunchoutData;
import org.kuali.kfs.module.purap.businessobject.SciQuestPunchoutDataItem;
import org.kuali.rice.kew.rule.xmlrouting.XPathHelper;
import org.kuali.rice.kew.util.XmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * SciQuestPunchoutDataParser
 *  
 * @author Tom Bradford <tbradford@rsmart.com>
 */
public class PunchoutDataParser {
    private static final String REQ_NAME = "/cXML/Message/PunchOutOrderMessage//Extrinsic[@name='CartName']/text()";
    private static final String ADDRESS_CODE = "/cXML/Message/PunchOutOrderMessage//Extrinsic[@name='ShipToCode']/text()";
    private static final String ITEM = "/cXML/Message/PunchOutOrderMessage//ItemIn/ItemDetail";
    private static final String SPSC = "Classification[@domain='SPSC']/text()";
    private static final String UNSPSC = "Classification[@domain='UNSPSC']/text()";
    private static final String COMMODITY_CODE = "Classification[@domain='CommodityCode']/text()";
    private static final String PRODUCT_SOURCE = "Extrinsic[@name='Product Source']/text()";
    private static final String CONTROLLED = "Classification[@domain='Controlled']/text()";
    private static final String GREEN = "Classification[@domain='Green']/text()";
    private static final String HAZARDOUS = "Classification[@domain='Hazardous']/text()";
    private static final String RADIOACTIVE = "Classification[@domain='Radioactive']/text()";
    private static final String RADIOACTIVE_MINOR = "Classification[@domain='RadioactiveMinor']/text()";
    private static final String SELECT_AGENT = "Classification[@domain='SelectAgent']/text()";
    private static final String TOXIN = "Classification[@domain='Toxin']/text()";
    
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    
    private XPath xpath = XPathHelper.newXPath();    
    private SciQuestPunchoutData punchoutData;
    private Document doc;
    
    public PunchoutDataParser() {
    }

    /**
     * Return the parsed SciQuestPunchoutData instance
     * 
     * @return The SciQuestPunchoutData instance
     */
    public SciQuestPunchoutData getPunchoutData() {
        return punchoutData;
    }
    
    /**
     * Parse from a DOM Document into a SciQuestPunchoutData instance
     * 
     * @param doc The DOM Document to parse
     */
    public void parse(Document doc) {
		this.doc = doc;    	
    	try {
    		process();
    	}
    	catch ( Exception e ) {
    		throw new RuntimeException(e);
    	}
    }
    
    /**
     * Parse from an XML InputStream into a SciQuestPunchoutData instance
     * 
     * @param is The InputStream to parse
     */
    public void parse(InputStream is) {
    	try {
    		this.doc = XmlHelper.trimXml(is);
    		process();
    	}
    	catch ( Exception e ) {
    		throw new RuntimeException(e);
    	}    		
    }

    /**
     * Parse from a String containing XML into a SciQuestPunchoutData instance
     * 
     * @param xml The XML String to parse
     */
    public void parse(String xml) {
    	try {
			parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
		}
    	catch ( UnsupportedEncodingException e ) {
			throw new RuntimeException(e);
		}
    }
    
    private void process() throws Exception {
		punchoutData = new SciQuestPunchoutData();
        parseCartInfo();
        parseItems();    	
    }
    
    private void parseCartInfo() throws Exception {
        punchoutData.setRequisitionName(evalString(REQ_NAME, doc));
        punchoutData.setShipToAddressCode(evalString(ADDRESS_CODE, doc));
    }
    
    private void parseItems() throws Exception {
        NodeList items = (NodeList)xpath.evaluate(ITEM, doc, XPathConstants.NODESET);
        List<SciQuestPunchoutDataItem> itemList = new ArrayList<SciQuestPunchoutDataItem>();
        for ( int i = 0; i < items.getLength(); i++ ) {
            Node item = items.item(i);

            SciQuestPunchoutDataItem punchoutItem = new SciQuestPunchoutDataItem();
            
            punchoutItem.setSpsc(evalString(SPSC, item));
            punchoutItem.setUnspsc(evalString(UNSPSC, item));
            punchoutItem.setCommodityCode(evalString(COMMODITY_CODE, item));            
            punchoutItem.setProductSource(evalString(PRODUCT_SOURCE, item));
            
            punchoutItem.setControlled(evalBoolean(CONTROLLED, item));
            punchoutItem.setGreen(evalBoolean(GREEN, item));
            punchoutItem.setHazardous(evalBoolean(HAZARDOUS, item));
            punchoutItem.setRadioactive(evalBoolean(RADIOACTIVE, item));
            punchoutItem.setRadioactiveMinor(evalBoolean(RADIOACTIVE_MINOR, item));
            punchoutItem.setSelectAgent(evalBoolean(SELECT_AGENT, item));
            punchoutItem.setToxin(evalBoolean(TOXIN, item));
            
            itemList.add(punchoutItem);
        }
        punchoutData.setItems(itemList);          
    }
    
    private String evalString(String expression, Node node) throws Exception {
        return (String)xpath.evaluate(expression, node, XPathConstants.STRING);
    }
    
    private Boolean evalBoolean(String expression, Node node) throws Exception {
        String val = (String)xpath.evaluate(expression, node, XPathConstants.STRING); 
        if ( StringUtils.isNotEmpty(val) ) 
            return TRUE.equalsIgnoreCase(val) ? Boolean.TRUE : Boolean.FALSE;
        else
            return Boolean.FALSE;
    }
}
