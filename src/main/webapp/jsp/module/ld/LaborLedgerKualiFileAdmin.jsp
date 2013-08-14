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

<kul:page showDocumentInfo="false"
	headerTitle="Create Disencumbrance" docTitle="Create Disencumbrance"
	transactionalDocument="false" htmlFormAction="laborLedgerBatchFileAdmin">
	<c:out value="${status}"/>
	<br/>
	<br/>
	<li><portal:portalLink displayTitle="true" title="Click here to return to the Batch File lookup" url="kr/lookup.do?methodToCall=start&businessObjectClassName=edu.cornell.kfs.module.ld.businessobject.LaborLedgerBatchFile&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /></li>
</kul:page>
