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

<%--
    Cornell-specific channel pertaining to CU's tax reporting module.
--%>
<channel:portalChannelTop channelTitle="Tax Reporting" />
<div class="body">
    <ul class="chan">
        <li><portal:portalLink displayTitle="true" title="Object Code Bucket Mapping" url="kr/lookup.do?methodToCall=start&businessObjectClassName=edu.cornell.kfs.tax.businessobject.ObjectCodeBucketMapping&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /></li>
        <li><portal:portalLink displayTitle="true" title="Transaction Override" url="kr/lookup.do?methodToCall=start&businessObjectClassName=edu.cornell.kfs.tax.businessobject.TransactionOverride&docFormKey=88888888&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true" /></li>
    </ul>
</div>
<channel:portalChannelBottom />
