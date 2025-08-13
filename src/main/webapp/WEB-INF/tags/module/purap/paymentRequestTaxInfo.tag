<%--

    The Kuali Financial System, a comprehensive financial management system for higher education.

    Copyright 2005-2024 Kuali, Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<%@ attribute name="documentAttributes" required="true" type="java.util.Map"
              description="The DataDictionary entry containing attributes for this row's fields." %> 
<c:set var="tabindexOverrideBase" value="45" />

<kul:tab tabTitle="Tax Information" defaultOpen="true" tabErrorKey="${PurapConstants.PAYMENT_REQUEST_TAX_TAB_ERRORS}">
    <div class="tab-container" align=center>
    	<c:if test="${taxAreaEditable}">
    		<h3>Tax Area Edits</h3>  
    	</c:if>  	
    	<c:if test="${!taxAreaEditable}">
    		<h3>Tax Information</h3>
    	</c:if>  	

        <table cellpadding="0" cellspacing="0" class="datatable" summary="Tax Info Section">

        	<tr>
            	<th align=right valign=middle class="bord-l-b">
                	<div align="right"><kul:htmlAttributeLabel forceRequired = "true" attributeEntry="${documentAttributes.taxClassificationCode}" /></div>
                </th>
                <td align=left valign=middle class="datacell">
                	<kul:htmlControlAttribute 
                		attributeEntry="${documentAttributes.taxClassificationCode}" property="document.taxClassificationCode" 
                		readOnly="${not taxAreaEditable}" tabindexOverride="${tabindexOverrideBase + 0}"/>
                </td>
                <th align=right valign=middle class="bord-l-b">
                	<div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.taxForeignSourceIndicator}" /></div>
                </th>
                <td align=left valign=middle class="datacell">
                	<kul:htmlControlAttribute 
                		attributeEntry="${documentAttributes.taxForeignSourceIndicator}" property="document.taxForeignSourceIndicator" 
                		readOnly="${not taxAreaEditable}" tabindexOverride="${tabindexOverrideBase + 3}"/>
                </td>
            </tr>
            
            <tr>
                <th align=right valign=middle class="bord-l-b">
                	<div align="right"><kul:htmlAttributeLabel forceRequired = "true" attributeEntry="${documentAttributes.taxFederalPercent}" /></div>
                </th>
                <td align=left valign=middle class="datacell">
                	<kul:htmlControlAttribute
                		attributeEntry="${documentAttributes.taxFederalPercent}"
						property="document.taxFederalPercent"
                		readOnly="${not taxAreaEditable}"
						tabindexOverride="${tabindexOverrideBase + 0}"/>
					&nbsp;                
                    <c:if test="${taxAreaEditable}">
						<!-- These hidden tags contain static values that can be picked up by new lookups to pre-populate fields -->
						<input type="hidden" name="static.federalTaxPercentIncomeTaxTypeCode" value="F" />
						<input type="hidden" name="static.federalTaxPercentActive" value="Y" />
                   		<kul:lookup boClassName="org.kuali.kfs.fp.businessobject.NonresidentTaxPercent"
                    		lookupParameters="document.taxClassificationCode:incomeClassCode,'F':incomeTaxTypeCode,'Y':active,document.taxFederalPercent:incomeTaxPercent"
                        	fieldConversions="incomeTaxTypeCode:static.federalTaxPercentIncomeTaxTypeCode,active:federalTaxPercentActive,incomeTaxPercent:document.taxFederalPercent,incomeClassCode:document.taxClassificationCode"
							fieldPropertyName="document.taxFederalPercent"
							readOnlyFields="active,incomeTaxTypeCode"
							newLookup="true"/> 
                    </c:if>                
                </td>
                <th align=right valign=middle class="bord-l-b">
                	<div align="right"><kul:htmlAttributeLabel  attributeEntry="${documentAttributes.taxExemptTreatyIndicator}" /></div>
                </th>
                <td align=left valign=middle class="datacell">
                	<kul:htmlControlAttribute 
                		attributeEntry="${documentAttributes.taxExemptTreatyIndicator}" property="document.taxExemptTreatyIndicator" 
                		readOnly="${not taxAreaEditable}" tabindexOverride="${tabindexOverrideBase + 3}"/>
                </td>
            </tr>
                        
            <tr>
                <th align=right valign=middle class="bord-l-b">
                	<div align="right"><kul:htmlAttributeLabel forceRequired = "true" attributeEntry="${documentAttributes.taxStatePercent}" /></div>
                </th>
                <td align=left valign=middle class="datacell">
                	<kul:htmlControlAttribute
                		attributeEntry="${documentAttributes.taxStatePercent}"
						property="document.taxStatePercent"
                		readOnly="${not taxAreaEditable}"
						tabindexOverride="${tabindexOverrideBase + 0}"/>
					&nbsp;                
                    <c:if test="${taxAreaEditable}">
						<!-- These hidden tags contain static values that can be picked up by new lookups to pre-populate fields -->
						<input type="hidden" name="static.stateTaxPercentIncomeTaxTypeCode" value="S" />
						<input type="hidden" name="static.stateTaxPercentActive" value="Y" />
                   		<kul:lookup boClassName="org.kuali.kfs.fp.businessobject.NonresidentTaxPercent"
                    		lookupParameters="document.taxClassificationCode:incomeClassCode,staticIncomeTaxTypeCode:incomeTaxTypeCode,'Y':active,document.taxStatePercent:incomeTaxPercent"
                        	fieldConversions="incomeTaxTypeCode:static.stateTaxPercentIncomeTaxTypeCode,active:stateTaxPercentActive.active,incomeTaxPercent:document.taxStatePercent,incomeClassCode:document.taxClassificationCode"
							fieldPropertyName="document.taxStatePercent"
							readOnlyFields="active,incomeTaxTypeCode"
							newLookup="true"/> 
                    </c:if>                
                </td>
                <th align=right valign=middle class="bord-l-b">
                	<div align="right"><kul:htmlAttributeLabel  attributeEntry="${documentAttributes.taxOtherExemptIndicator}" /></div>
                </th>
                <td align=left valign=middle class="datacell">
                	<kul:htmlControlAttribute 
                		attributeEntry="${documentAttributes.taxOtherExemptIndicator}" property="document.taxOtherExemptIndicator" 
                		readOnly="${not taxAreaEditable}" tabindexOverride="${tabindexOverrideBase + 3}"/>
                </td>
            </tr>

            <tr>
                <th align=right valign=middle class="bord-l-b">
                	<div align="right"><kul:htmlAttributeLabel forceRequired = "true" attributeEntry="${documentAttributes.taxCountryCode}" /></div>
                </th>
                <td align=left valign=middle class="datacell">
                	<kul:htmlControlAttribute 
                		attributeEntry="${documentAttributes.taxCountryCode}" property="document.taxCountryCode" 
                		readOnly="${not taxAreaEditable}" tabindexOverride="${tabindexOverrideBase + 0}"/>
                </td>
                <th align=right valign=middle class="bord-l-b">
                	<div align="right"><kul:htmlAttributeLabel  attributeEntry="${documentAttributes.taxGrossUpIndicator}" /></div>
                </th>
                <td align=left valign=middle class="datacell">
                	<kul:htmlControlAttribute 
                		attributeEntry="${documentAttributes.taxGrossUpIndicator}" property="document.taxGrossUpIndicator" 
                		readOnly="${not taxAreaEditable}" tabindexOverride="${tabindexOverrideBase + 3}"/>
                </td>
            </tr>

            <tr>
                <th align=right valign=middle class="bord-l-b">
                	<div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.taxNQIId}" /></div>
                </th>
                <td align=left valign=middle class="datacell">
                	<kul:htmlControlAttribute 
                		attributeEntry="${documentAttributes.taxNQIId}" property="document.taxNQIId" 
                		readOnly="${not taxAreaEditable}" tabindexOverride="${tabindexOverrideBase + 0}"/>
                </td>
                <th align=right valign=middle class="bord-l-b">
                	<div align="right"><kul:htmlAttributeLabel  attributeEntry="${documentAttributes.taxUSAIDPerDiemIndicator}" /></div>
                </th>
                <td align=left valign=middle class="datacell">
                	<kul:htmlControlAttribute 
                		attributeEntry="${documentAttributes.taxUSAIDPerDiemIndicator}" property="document.taxUSAIDPerDiemIndicator" 
                		readOnly="${not taxAreaEditable}" tabindexOverride="${tabindexOverrideBase + 3}"/>
                </td>
            </tr>

            <tr>
                <th align=right valign=middle class="bord-l-b">
                    <div align="right">&nbsp;</div>
                </th>
                <td align=left valign=middle class="datacell">
                    &nbsp;
                </td>                
                <th align=right valign=middle class="bord-l-b">
                	<div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.taxSpecialW4Amount}" /></div>
                </th>
                <td align=left valign=middle class="datacell">
                	<kul:htmlControlAttribute 
                		attributeEntry="${documentAttributes.taxSpecialW4Amount}" property="document.taxSpecialW4Amount" 
                		readOnly="${not taxAreaEditable}" tabindexOverride="${tabindexOverrideBase + 3}"/>
                </td>
            </tr>
            <c:if test="${taxAreaEditable}">
				<tr>
					<td class="infoline" colspan="4">
						<center>
							<html:submit value="Clear All" styleClass="btn btn-default small" property="methodToCall.clearTaxInfo" title="Clear All Info From Nonresident Tax Entries" alt="Clear All Info From Nonresident Tax Entries"/>
						</center>
					</td>
    		    </tr>
			</c:if>
		</table>

    </div>
</kul:tab>
