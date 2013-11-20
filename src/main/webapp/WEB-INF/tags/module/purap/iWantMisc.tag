<%--
 Copyright 2006-2009 The Kuali Foundation
 
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

<%@ attribute name="documentAttributes" required="true" type="java.util.Map"
              description="The DataDictionary entry containing attributes for this row's fields." %>
           

<c:set var="fullEntryMode" value="${KualiForm.documentActions[Constants.KUALI_ACTION_CAN_EDIT]}" />
<c:set var="currentUserCampusCode" value="${UserSession.person.campusCode}" />
<c:set var="tabindexOverrideBase" value="30" />

<kul:tab tabTitle="Additional Info" defaultOpen="true" tabErrorKey="${KFSConstants.I_WANT_DOC_MISC_ERRORS}">
    <div class="tab-container" align=center>

            <table cellpadding="0" cellspacing="0" class="datatable" summary="Instructions">
            
            <tr>
                <td colspan="2" class="subhead">Services</td>
            </tr>
            
            <tr>
            <td colspan="2" class="neutral">&nbsp;</td>
            </tr>
            
            <tr>
            
            <th align=right valign=middle class="neutral" width="30%">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.servicePerformedOnCampus}" /></div>
                </th>
                <td align=left valign=middle class="neutral" width="70%">
                    <kul:htmlControlAttribute 
                        attributeEntry="${documentAttributes.servicePerformedOnCampus}" 
                        property="document.servicePerformedOnCampus" readOnly="${not fullEntryMode}" tabindexOverride="${tabindexOverrideBase + 0}"/>&nbsp;
                </td> 
                        
           </tr>
           
            <tr>
            <td colspan="2" class="neutral">&nbsp;</td>
            </tr>
           
           <tr>
                <td colspan="2" class="subhead">Miscellaneous</td>
            </tr>
           <tr>    
                <th align=right valign=middle class="neutral" width="30%">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${documentAttributes.commentsAndSpecialInstructions}"/></div>
                </th>
                <td align=left valign=middle class="neutral" width="70%">
                    <kul:htmlControlAttribute 
                    	attributeEntry="${documentAttributes.commentsAndSpecialInstructions}" property="document.commentsAndSpecialInstructions" 
                    	readOnly="${not (fullEntryMode)}" tabindexOverride="${tabindexOverrideBase + 5}"/>
                    <div id="example" class="fineprint">
            			(i.e. date order is needed)
            		</div>
                </td>
            </tr>
			
        </table>


    </div>
</kul:tab>