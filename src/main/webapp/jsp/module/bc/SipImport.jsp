<%--
 Copyright 2005-2008 The Kuali Foundation
 
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

<c:set var="sipImportAttributes"	value="${DataDictionary.SipImport.attributes}" />

<html:xhtml/>

<kul:page showDocumentInfo="false"
	htmlFormAction="budgetSipImport" renderMultipart="true"
	docTitle=""
    transactionalDocument="false" showTabButtons="true">
	
	<script type="text/javascript">
	
	        var excludeSubmitRestriction = true;
	
	</script>    
	    <br>
    <html:hidden property="returnAnchor" />
    <html:hidden property="returnFormKey" />
    <html:hidden property="backLocation" />
    <html:hidden name="KualiForm" property="universityFiscalYear" />
    
    <strong><h2>SIP Import </h2> </strong>
    <kul:tabTop tabTitle="Sip Import" defaultOpen="true">
		<div class="tab-container" align=center>
			<table bgcolor="#C0C0C0" cellpadding="30" >
				<tr>
					<td width="60%"><h3>SIP Import</h3></td>
					<td width="30%"><h3></h3></td>
					<td width="10%"><h3>Action</h3></td>
				</tr>
				
				<tr >
					<td class="infoline" > 
						<b><kul:htmlAttributeLabel attributeEntry="${sipImportAttributes.fileName}" /></b>
						<html:file property="file" />
					</td>
					<td class="infoline">
						Allow Executives to be imported? &nbsp;<html:checkbox property="allowExecutivesToBeImported" title="Allows executives to be imported in the file you specified on the left."> </html:checkbox>
					</td>
					<td class="infoline">
						<div align="center">
							<html:image property="methodToCall.performImport" onclick="this.form.encoding='multipart/form-data'; return true;" src="kr/static/images/buttonsmall_submit.gif"  title="Import" alt="Import" styleClass="tinybutton" />
						</div>
					</td>
				</tr>
			</table>
			<table bgcolor="#C0C0C0" cellpadding="0" cellspacing="0" >
				<tr align="center" >
					<td class="infoline" width="50%"></td>
					<td class="infoline" colspan="2"> 
						<html:image property="methodToCall.close" src="kr/static/images/buttonsmall_close.gif"  title="Close Window" alt="Close Window" styleClass="tinybutton" />
					</td>
				</tr>
			</table>
		</div>
		
	</kul:tabTop>
	<kul:panelFooter />
</kul:page>
