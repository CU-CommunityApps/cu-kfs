<%--
 Copyright 2007-2009 The Kuali Foundation
 
 Licensed under the Educational Community License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.opensource.org/licenses/ecl2.php
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<%@ attribute name="itemAttributes" required="true" type="java.util.Map" description="The DataDictionary entry containing attributes for this row's fields."%>
<%@ attribute name="extraHiddenItemFields" required="false"
              description="A comma seperated list of names to be added to the list of normally hidden fields
              for the existing misc items." %>

<script language="JavaScript" type="text/javascript" src="dwr/interface/CommodityCodeService.js"></script>
<script language="JavaScript" type="text/javascript" src="scripts/vnd/objectInfo.js"></script>
<script language="JavaScript" type="text/javascript" src="dwr/interface/ItemUnitOfMeasureService.js"></script>
<script language="JavaScript" type="text/javascript" src="scripts/module/purap/objectInfo.js"></script>

<c:set var="fullEntryMode" value="${KualiForm.documentActions[Constants.KUALI_ACTION_CAN_EDIT]}" />


<c:set var="hasItems" value="${fn:length(KualiForm.document.items) > 0}" />
<c:set var="hasLineItems" value="${fn:length(KualiForm.document.items) > 0}" />

<c:set var="tabindexOverrideBase" value="50" />

<c:set var="mainColumnCount" value="8"/>
<c:set var="colSpanAction" value="2"/>



	<c:set var="colSpanCatlogNumber" value="1"/>



	<div class="tab-container" align=center>
    
	<table cellpadding="0" cellspacing="0" class="datatable" summary="Items Section">
	
    	<!--  if (fullEntryMode or amendmentEntry) and not lockB2BEntry, then display the addLine -->	
		<c:if test="${fullEntryMode}">
			<tr>
				<td colspan="6" class="subhead">
					<span class="subhead-left">Add Item <a href="${KualiForm.lineItemImportInstructionsUrl}" target="helpWindow"><img src="${ConfigProperties.kr.externalizable.images.url}my_cp_inf.gif" title="Line Item Import Help" alt="Line Item Import Help" hspace="5" border="0" align="middle" /></a>
				</td>
				<td colspan="2" class="subhead" align="right" nowrap="nowrap" style="border-left: none;">
					
				</td>
			</tr>
			
			
			<tr>
				<th  class="neutral" >&nbsp;</th>
				
				<th  class="neutral" ><kul:htmlAttributeLabel attributeEntry="${itemAttributes.itemDescription}"/></th>
				<th  class="neutral" ><kul:htmlAttributeLabel attributeEntry="${itemAttributes.itemQuantity}"/></th>
				<th  class="neutral" ><kul:htmlAttributeLabel attributeEntry="${itemAttributes.itemUnitOfMeasureCode}" useShortLabel="true"/></th>
				<th  class="neutral" ><kul:htmlAttributeLabel attributeEntry="${itemAttributes.itemCatalogNumber}" /></th>
                <th  class="neutral" ><kul:htmlAttributeLabel attributeEntry="${itemAttributes.itemUnitPrice}"/>	</th>			
				
				<th align=left class="neutral" >Action</th>
			</tr>
			
			<tr>
                <td valign="center" class="neutral" >
                <div align="center">
                    <kul:htmlControlAttribute attributeEntry="${itemAttributes.itemLineNumber}" property="newIWantItemLine.itemLineNumber" readOnly="true"/>
                </div>
                </td>
                
                <td valign="center" class="neutral">
                	<div align="center">
				   		<kul:htmlControlAttribute attributeEntry="${itemAttributes.itemDescription}" property="newIWantItemLine.itemDescription" tabindexOverride="${tabindexOverrideBase + 0}" readOnly="${not fullEntryMode}"/>
					</div>
				</td>
				
				<td valign="center" class="neutral" align="center">
				    <kul:htmlControlAttribute attributeEntry="${itemAttributes.itemQuantity}" property="newIWantItemLine.itemQuantity" tabindexOverride="${tabindexOverrideBase + 0}" readOnly="${not fullEntryMode}"/>
			    </td>
                <td valign="center" class="neutral" >
                <div align="center">
                    <c:set var="itemUnitOfMeasureCodeField"  value="newIWantItemLine.itemUnitOfMeasureCode" />
                    <c:set var="itemUnitOfMeasureDescriptionField"  value="newIWantItemLine.itemUnitOfMeasure.itemUnitOfMeasureDescription" />
                    <kul:htmlControlAttribute attributeEntry="${itemAttributes.itemUnitOfMeasureCode}" 
                        property="${itemUnitOfMeasureCodeField}" 
                        readOnly="${not fullEntryMode}"
                        onblur="loadItemUnitOfMeasureInfo( '${itemUnitOfMeasureCodeField}', '${itemUnitOfMeasureDescriptionField}' );${onblur}" tabindexOverride="${tabindexOverrideBase + 0}"/>
                        <c:if test ="${fullEntryMode}">
                   			 <kul:lookup boClassName="edu.cornell.kfs.module.purap.businessobject.IWantDocUnitOfMeasure" 
                        		fieldConversions="itemUnitOfMeasureCode:newIWantItemLine.itemUnitOfMeasureCode"
                        		lookupParameters="'Y':active"/>  
                        </c:if>   
                    <div id="newIWantItemLine.itemUnitOfMeasure.itemUnitOfMeasureDescription.div" class="fineprint">
                        <html:hidden write="true" property="${itemUnitOfMeasureDescriptionField}"/>&nbsp;        
                    </div> 
                    </div>                    
                </td>					    
				<td valign="center" class="neutral" colspan="${colSpanCatlogNumber}">
				<div align="center">
				    <kul:htmlControlAttribute attributeEntry="${itemAttributes.itemCatalogNumber}" property="newIWantItemLine.itemCatalogNumber" tabindexOverride="${tabindexOverrideBase + 0}"  readOnly="${not fullEntryMode}"/>
			    </div>
			    </td>

				<td valign="center" class="neutral">
				    <div align="center">
				        <kul:htmlControlAttribute attributeEntry="${itemAttributes.itemUnitPrice}" property="newIWantItemLine.itemUnitPrice" tabindexOverride="${tabindexOverrideBase + 0}"  readOnly="${not fullEntryMode}"/>
					</div>
				</td>
					
				<td valign="center" class="neutral">
				    <div align="center">
				        <html:image property="methodToCall.addItem" src="${ConfigProperties.externalizable.images.url}tinybutton-save.gif" alt="Insert an Item" title="Add an Item" styleClass="tinybutton" tabindex="${tabindexOverrideBase + 0}"/>
				    </div>
				</td>				
			</tr>
			</c:if>
		
		<!-- End of if (fullEntryMode or amendmentEntry), then display the addLine -->

		<tr>
			<td colspan="${mainColumnCount}" class="subhead">
			    <span class="subhead-left">Current Items</span>
			</td>
		</tr>
		
        <c:if test="${ hasLineItems or hasItems}">
			<tr>
				<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemLineNumber}" />
				
				<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemDescription}" />
				<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemQuantity}" />
				<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemUnitOfMeasureCode}" />
				<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemCatalogNumber}" colspan="${colSpanCatlogNumber}" />
				
				<kul:htmlAttributeHeaderCell attributeEntry="${itemAttributes.itemUnitPrice}" />

				   
                <kul:htmlAttributeHeaderCell literalLabel="Actions" />
                
			</tr>
		</c:if>

		<c:if test="${ !hasLineItems or !hasItems}">
			<tr>
				<th height=30 colspan="${mainColumnCount}" class="neutral">No items added</th>
			</tr>
		</c:if>

		<logic:iterate indexId="ctr" name="KualiForm" property="document.items" id="itemLine">
			
				<tr>
					<td colspan="${mainColumnCount}" class="tab-subhead" style="border-right: none;">
					    Item ${ctr+1}
					</td>
				</tr>

				<!-- table class="datatable" style="width: 100%;" -->

				<tr>
					<td valign="center" class="neutral" align="center">
					<div align="center">
					    <kul:htmlControlAttribute
						    attributeEntry="${itemAttributes.itemLineNumber}"
						    property="document.item[${ctr}].itemLineNumber"
						    readOnly="true"
						    tabindexOverride="${tabindexOverrideBase + 0}"/>
						    </div>
					</td>
					<td valign="center" class="neutral" align="center">
					<div align="center">
						 <kul:htmlControlAttribute
						    attributeEntry="${itemAttributes.itemDescription}"
						    property="document.item[${ctr}].itemDescription"
						     readOnly="${not fullEntryMode}"
						    tabindexOverride="${tabindexOverrideBase + 0}"/>
						    </div>
					</td>	
					<td valign="center" class="neutral" align="center">
					    <kul:htmlControlAttribute
						    attributeEntry="${itemAttributes.itemQuantity}"
						    property="document.item[${ctr}].itemQuantity"
						    readOnly="${not fullEntryMode}"
						    tabindexOverride="${tabindexOverrideBase + 0}"/>
					</td>
                    <td valign="center" class="neutral" align="center">
                        <kul:htmlControlAttribute 
                            attributeEntry="${itemAttributes.itemUnitOfMeasureCode}" 
                            property="document.item[${ctr}].itemUnitOfMeasureCode"
                            onblur="loadItemUnitOfMeasureInfo( 'document.item[${ctr}].itemUnitOfMeasureCode', 'document.item[${ctr}].itemUnitOfMeasure.itemUnitOfMeasureDescription' );${onblur}"
                             readOnly="${not fullEntryMode}"
                            tabindexOverride="${tabindexOverrideBase + 0}"/>
                        <c:if test="false">   
                            <kul:lookup boClassName="org.kuali.kfs.sys.businessobject.UnitOfMeasure" 
                                fieldConversions="itemUnitOfMeasureCode:document.item[${ctr}].itemUnitOfMeasureCode"
                                lookupParameters="'Y':active"/>    
                        </c:if>
                        <div id="document.item[${ctr}].itemUnitOfMeasure.itemUnitOfMeasureDescription.div" class="fineprint">
                            <html:hidden write="true" property="document.item[${ctr}].itemUnitOfMeasure.itemUnitOfMeasureDescription"/>&nbsp;  
                        </div>                        
                    </td>				    
					<td valign="center" class="neutral" colspan="${colSpanCatlogNumber}" align="center">					
					    <kul:htmlControlAttribute
						    attributeEntry="${itemAttributes.itemCatalogNumber}"
						    property="document.item[${ctr}].itemCatalogNumber"
						    readOnly="${not fullEntryMode}" 
						    tabindexOverride="${tabindexOverrideBase + 0}"/>
				    </td>				
                    	    
									
					<td valign="center" class="neutral">
					    <div align="center">
					        <kul:htmlControlAttribute
						        attributeEntry="${itemAttributes.itemUnitPrice}"
						        property="document.item[${ctr}].itemUnitPrice"
						         readOnly="${not fullEntryMode}"
						        tabindexOverride="${tabindexOverrideBase + 0}"/>
						</div>
					</td>
								
					
					<td valign="center" class="neutral" >
					    <div align="center">				
					    	<c:choose>		    	
					    	<c:when test="${fullEntryMode}">
					        	<html:image
						        	property="methodToCall.deleteItem.line${ctr}"
						        	src="${ConfigProperties.kr.externalizable.images.url}tinybutton-delete1.gif"
						        	alt="Delete Item ${ctr+1}" title="Delete Item ${ctr+1}"
						        	styleClass="tinybutton" /><br><br>
						    </c:when>
						    						    
						    <c:otherwise>							    	
					    		<div align="center">&nbsp;</div>
						    </c:otherwise>
						    </c:choose>						    
						</div>
					</td>
				
				</tr>
				

		</logic:iterate>
		
		<!-- BEGIN TOTAL SECTION -->
		<tr>
			<th height=30 colspan="${mainColumnCount}" class="neutral">&nbsp;</th>
		</tr>

		<tr>
			<td colspan="${mainColumnCount}" class="subhead">
                <span class="subhead-left">Totals</span>
                <span class="subhead-right">&nbsp;</span>
            </td>
		</tr>

		<c:set var="colSpanTotalLabel" value="${colSpanItemType+colSpanDescription}"/>
		<c:set var="colSpanTotalAmount" value="${colSpanExtendedPrice}"/>		


		<tr>
			<th align=right colspan="6" scope="row" class="neutral">
			    <div align="right">
			        <kul:htmlAttributeLabel attributeEntry="${DataDictionary.RequisitionDocument.attributes.totalDollarAmount}" />
			    </div>
			</th>
			<td valign=middle class="neutral" colspan="2" >
			    <div align="right"><b>
                    <kul:htmlControlAttribute
                        attributeEntry="${DataDictionary.RequisitionDocument.totalDollarAmount}"
                        property="document.totalDollarAmount"
                        readOnly="true" />&nbsp; </b>
                </div>
			</td>
			
		</tr>

		<!-- END TOTAL SECTION -->

	</table>

	</div>

