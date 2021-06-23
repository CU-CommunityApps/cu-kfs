<%--

    The Kuali Financial System, a comprehensive financial management system for higher education.

    Copyright 2005-2021 Kuali, Inc.

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
<%--
    CU Customization: Backported this file from the 2021-04-08 financials patch to bring in the FINP-7470 changes.
    This overlay should be removed when we upgrade to the 2021-04-08 financials patch.
--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<kul:page headerTitle="Stuck Documents" lookup="true"
          transactionalDocument="false" showDocumentInfo="false"
          htmlFormAction="StuckDocuments" docTitle="Stuck Documents">
    <div class="headerarea-small" id="headerarea">
        <h1>Stuck Document Processing</h1>
    </div>
    <html-el:form action="StuckDocuments">
        <html-el:hidden property="methodToCall" value=""/>
        <kul:csrf />
        <div style="margin:2em">
            <p>
                The Stuck Documents Report lists documents that are currently stuck in workflow.

                The Autofix Report lists documents that were processed by the Autofix job.
            </p>
            <div>
                <a href="StuckDocuments.do?methodToCall=report" id="stuck-doc-report-link"
                   title="Stuck Documents Report">
                    Stuck Documents Report
                </a>
            </div>
            <div>
                <a href="StuckDocuments.do?methodToCall=autofixReport" id="autofix-report-link" title="Autofix Report">
                    Autofix Report
                </a>
            </div>
        </div>
    </html-el:form>
</kul:page>
