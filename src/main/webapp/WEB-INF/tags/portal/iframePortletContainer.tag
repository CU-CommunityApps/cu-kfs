<%--
   - The Kuali Financial System, a comprehensive financial management system for higher education.
   - 
   - Copyright 2005-2014 The Kuali Foundation
   - 
   - This program is free software: you can redistribute it and/or modify
   - it under the terms of the GNU Affero General Public License as
   - published by the Free Software Foundation, either version 3 of the
   - License, or (at your option) any later version.
   - 
   - This program is distributed in the hope that it will be useful,
   - but WITHOUT ANY WARRANTY; without even the implied warranty of
   - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   - GNU Affero General Public License for more details.
   - 
   - You should have received a copy of the GNU Affero General Public License
   - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<%@ attribute name="channelTitle" required="true" %>
<%@ attribute name="channelUrl" required="true" %>
<%-- ==== CU Customization: Added Kuali's CONTRIB-101 frame resizing enhancement ==== --%>
<%@ attribute name="frameHeight" required="false" %>

<c:if test="${empty frameHeight}">
  <c:set var="frameHeight" value="500"/>
</c:if>

<iframe src="${channelUrl}" onload='setFocusedIframeDimensions("iframeportlet", ${frameHeight}, true);' name="iframeportlet" id="iframeportlet" hspace="0" vspace="0" style="height: ${frameHeight}px;" title="E-Doc" frameborder="0" height="${frameHeight}" scrolling="auto" width="100%"></iframe>

