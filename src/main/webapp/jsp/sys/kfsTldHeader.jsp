<%--

    The Kuali Financial System, a comprehensive financial management system for higher education.

    Copyright 2005-2022 Kuali, Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="e" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://www.kuali.org/struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://struts.apache.org/tags-bean-el" prefix="bean-el" %>
<%@ taglib uri="http://www.kuali.org/struts.apache.org/tags-html-el" prefix="html-el" %>
<%@ taglib uri="http://struts.apache.org/tags-logic-el" prefix="logic-el" %>
<%@ taglib uri="http://www.kuali.org/jsp/jstl/functions" prefix="kfunc" %>

<%@ taglib tagdir="/WEB-INF/tags/kr" prefix="kul" %>
<%@ taglib tagdir="/WEB-INF/tags/kim" prefix="kim" %>
<%@ taglib tagdir="/WEB-INF/tags/kr/dd" prefix="dd" %>
<%@ taglib tagdir="/WEB-INF/tags/ksr" prefix="ksr" %>
<%@ taglib tagdir="/WEB-INF/tags/fp" prefix="fp" %>
<%@ taglib tagdir="/WEB-INF/tags/gl" prefix="gl" %>
<%@ taglib tagdir="/WEB-INF/tags/gl/glcp" prefix="glcp" %>
<%@ taglib tagdir="/WEB-INF/tags/sys" prefix="sys" %>
<%@ taglib tagdir="/WEB-INF/tags/module/cg" prefix="cg" %>
<%@ taglib tagdir="/WEB-INF/tags/module/ld" prefix="ld" %>
<%@ taglib tagdir="/WEB-INF/tags/module/purap" prefix="purap" %>
<%@ taglib tagdir="/WEB-INF/tags/module/cams" prefix="cams" %>
<%@ taglib tagdir="/WEB-INF/tags/module/ar" prefix="ar" %>
<%@ taglib tagdir="/WEB-INF/tags/pdp" prefix="pdp" %>
<%@ taglib tagdir="/WEB-INF/tags/module/ec" prefix="ec" %>

<%-- utility web functions --%>
<%@ taglib uri="/WEB-INF/tlds/kfsfunc.tld" prefix="kfsfunc" %>
<%@ taglib uri="/WEB-INF/tlds/kfssys.tld" prefix="sys-java" %>
