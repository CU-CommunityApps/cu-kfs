<%--
 Copyright 2008 The Kuali Foundation.
 
 Licensed under the Educational Community License, Version 1.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.opensource.org/licenses/ecl1.php
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<kul:page showDocumentInfo="false"
	headerTitle="Check Reconciliation Report" docTitle="Check Reconciliation Report" renderMultipart="false"
	transactionalDocument="false" htmlFormAction="crCheckReconciliationReport" errorKey="foo" showTabButtons="true">
	
	<html-el:hidden property="methodToCall" />

	<table width="100%" border="0">
	<tr><td>	
		<kul:errors keyMatch="*" errorTitle="Errors Found On Page:"/>
	</td></tr>
	</table>  
	
	</br>

	<kul:tabTop tabTitle="Run Report" defaultOpen="true" tabErrorKey="">

	    <div class="tab-container" align="center">
			<h3>Report Parameters</h3>

	      	<table width="100%" cellpadding="0" cellspacing="0" class="datatable">
			<tr>
				<th class="grid" width="50%" align="right">Start Date:</th>
		      	<td class="grid" width="50%">
					<kul:dateInputNoAttributeEntry property="startDate" maxLength="10" size="10" />
				</td>
			</tr>
			<tr>
				<th class="grid" width="50%" align="right">End Date:</th>
		      	<td class="grid" width="50%">
					<kul:dateInputNoAttributeEntry property="endDate" maxLength="10" size="10" />
				</td>
			</tr>
			<tr>
				<th class="grid" width="50%" align="right">Format:</th>
		      	<td class="grid" width="50%">
					<input type="radio" name="format" value="pdf" checked>PDF
					<input type="radio" name="format" value="excel">Excel
				</td>
			</tr>
			</table>
		</div>

	</kul:tabTop>

    <div id="globalbuttons" class="globalbuttons">
		<html:image src="${ConfigProperties.kr.externalizable.images.url}buttonsmall_submit.gif" styleClass="globalbuttons" property="methodToCall.performReport" title="submit" alt="submit" onclick="excludeSubmitRestriction=true"/>
        <html:image src="${ConfigProperties.kr.externalizable.images.url}buttonsmall_close.gif"  styleClass="globalbuttons" property="methodToCall.returnToIndex" title="close"  alt="close"/>
    </div>

</kul:page>