<%--
 Copyright 2005-2007 The Kuali Foundation
 
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
<%@ include file="/kr/WEB-INF/jsp/tldHeader.jsp"%>

<%@ attribute name="editingMode" required="true" description="used to decide editability of overview fields" type="java.util.Map"%>
<%@ attribute name="readOnly" required="true" %>

<c:set var="docHeaderAttributes" value="${DataDictionary.DocumentHeader.attributes}" />
<c:set var="iWantDocAttributes" value="${DataDictionary.IWantDocument.attributes}" />

<kul:tabTop tabTitle="Document Overview" defaultOpen="true" tabErrorKey="${Constants.DOCUMENT_ERRORS}" >
	<div class="tab-container" align=center>
		  <!-- DOC OVERVIEW TABLE -->
		  <html:hidden property="document.documentHeader.documentNumber" />
		  <h3>Document Overview</h3>
		  <table cellpadding="0" cellspacing="0"  title="view/edit document overview information" summary="view/edit document overview information">
		    <tr>
				<td colspan="2" class="neutral" style="color: blue; font-family:Verdana, Verdana, serif; font-size: 12px;font-style: italic">
					<div>
						<b>NOTE:</b> Each statement should tie the purchase to the account being charged; what is being purchased, why it is being purchased, how it will be used.
						<ul>
							<li><b>Not</b> an adequate Business Purpose = "Lab Supplies"</li>
							<li>Adequate Business Purpose = "Electronic components for sample analyzer used in Prof. Smith's XYZ project (or OSP 12345)."</li>
						</ul>
					</div>
				</td>
			</tr>
		    <tr>
		    <th align="right" valign="middle" class="neutral">
		      <kul:htmlAttributeLabel 
		         
		          attributeEntry="${docHeaderAttributes.documentDescription}"
		          
		          />
		       </th>
		      <td align="left" valign="middle" class="neutral">
		      	<kul:htmlControlAttribute property="document.documentHeader.documentDescription" attributeEntry="${docHeaderAttributes.documentDescription}" readOnly="true"/>
		      </td>
		      </tr>
		      <tr>
		      <th align="right" valign="middle" class="neutral">
		      <kul:htmlAttributeLabel
                  
                  attributeEntry="${iWantDocAttributes.explanation}"
                 
                  />
                  </th>
		      <td align="left" valign="middle" class="neutral">
                  <kul:htmlControlAttribute
                      property="document.documentHeader.explanation"
                      attributeEntry="${iWantDocAttributes.explanation}"
                      readOnly="${readOnly}"
                      readOnlyAlternateDisplay="${fn:replace(fn:escapeXml(KualiForm.document.documentHeader.explanation), Constants.NEWLINE, '<br/>')}"
                      />
              </td>
		    </tr>
		    <tr>
		    
		    
		    <th align="right" valign="middle" class="neutral">
				 &nbsp;
		      	
		      </th>		  
              <td align="left" valign="middle" class="neutral">
              	 &nbsp;
              </td>
		    
            </tr>
          </table>
          <jsp:doBody/>            
        </div>
</kul:tabTop>
