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
<%@ include file="/kr/WEB-INF/jsp/tldHeader.jsp"%>

<%--<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.5.0/jquery.min.js"></script>  --%>
 
<kul:tab tabTitle="Route Log" defaultOpen="false">
<div class="tab-container" align=center>
	<c:if test="${ConfigProperties.test.mode ne 'true'}">
	<div id="routelogload">Loading....</div> 
	  <script type="text/javascript">
      function showRouteLog() {
        document.getElementById('routeLogIFrame').src='${ConfigProperties.workflow.url}/RouteLog.do?routeHeaderId=${KualiForm.workflowDocument.routeHeaderId}'; 
        document.getElementById('routelogload').style.display = 'none';
      };

      function doTheUberClick() {
          if (document.getElementById('routelogload').style.display =="block") {
            showRouteLog();
          }
        };

     	function readyToDisplayRouteLog()  {
      		
          	 if (document.getElementsByName('tabStates(RouteLog)')[0].value == "OPEN") {
              showRouteLog();
             }
        };
           
      var theElement = document.getElementById('tab-RouteLog-imageToggle');
	  theElement.setAttribute('onclick','doTheUberClick()');
      window.onload = readyToDisplayRouteLog;

        
      </script>
	  
      <iframe onload="setRouteLogIframeDimensions();" name="routeLogIFrame" id="routeLogIFrame" height="500" width="95%" hspace='0' vspace='0' frameborder='0' title='Workflow Route Log for document id: ${KualiForm.workflowDocument.routeHeaderId}'></iframe> 
	</c:if>
</div>
</kul:tab>