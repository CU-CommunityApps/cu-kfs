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
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<kul:page showDocumentInfo="false"
		headerTitle="I Want Document" docTitle="I Want Document Submitted"
		transactionalDocument="false" htmlFormAction="purapIWant"
		errorKey="*">
	<br>
	<div align="center" style="font-size: 18px">
		Your information was submitted. To see your orders click <a href="${ConfigProperties.procurementgateway.url}" target="_blank"><u>here</u></a>
	</div>
	<br>
	<br>
	<div class="topblurb">

	</div>
</kul:page>