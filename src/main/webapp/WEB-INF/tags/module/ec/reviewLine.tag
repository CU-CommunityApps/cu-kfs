<%--
 Copyright 2005-2009 The Kuali Foundation
 
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

<%@ tag description="render review buttons for % and totals"%>

<%@ attribute name="emptySpan" required="true"
    description="Empty Spaces before the button" %> 
<%@ attribute name ="percentageMethod2call" required ="true" %>
<%@ attribute name ="totalMethod2call" 		required ="true" %>

		<tr>
			<td colspan="${emptySpan}"></td>		
			<td >	
                            <html:image property="${percentageMethod2call}"
                                        src="${ConfigProperties.externalizable.images.url}tinybutton-recalculate.gif"
                                        title="reviewPercentages" alt="reviewPercentages" 
                                        onclick="excludeSubmitRestriction=true"/>
				
			</td>
			<td>&nbsp;</td>
			<td >	
                            <html:image property="${totalMethod2call}"
                                        src="${ConfigProperties.externalizable.images.url}tinybutton-recalculate.gif"
                                        title="reviewTotals" alt="reviewTotals" 
                                        onclick="excludeSubmitRestriction=true"/>
				
			</td>
				
		<td>&nbsp;</td>
		<td>&nbsp;</td>


		</tr>
