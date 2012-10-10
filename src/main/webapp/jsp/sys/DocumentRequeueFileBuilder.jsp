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
	headerTitle="Document Requeue File Builder" docTitle="" renderMultipart="true"
	multipartHack="true"
	transactionalDocument="false" htmlFormAction="documentRequeueFileBuilder" errorKey="foo">
	
	<strong><h2>	
	  Generate Document Requeue File 
	  </h2></strong>
	</br>
	
	<table width="100%" border="0"><tr><td>	
	  <kul:errors keyMatch="*" errorTitle="Errors Found:"/>
	</td></tr></table>  
	</br>
		
	<kul:tabTop tabTitle="Generate Document Requeue File" defaultOpen="true" tabErrorKey="">
      <div class="tab-container" align="center">
          <h3>Generate File</h3>
          <table width="100%" summary="" cellpadding="0" cellspacing="0">
            <tr>
              <th></th>
            </tr>
            
            <tr>
              <td class="infoline"><div align="center">
				<input type="hidden" name="action" value="generate" />
           		<html:image src="${ConfigProperties.externalizable.images.url}buttonsmall_create.gif" styleClass="globalbuttons" property="methodToCall.generate" title="Generate File" alt="Generate File" />
              </td>
            </tr>
         </table>
      </div>
	</kul:tabTop>
	
	<kul:panelFooter />
	
</kul:page>
