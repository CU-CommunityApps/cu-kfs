<?xml version="1.0" encoding="UTF-8"?>
<!--

    The Kuali Financial System, a comprehensive financial management system for higher education.

    Copyright 2005-2023 Kuali, Inc.

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

-->
<!DOCTYPE stylesheet [
<!ENTITY tab "&#x9;" ><!-- horizontal tab -->
<!ENTITY n "&#xa;" ><!-- LF -->
]>
<!--
  ====
  CU Customization: Modified this stylesheet to generate Cornell-specific messages.
  ====

  Default notification email style sheet
 -->
<!-- if this stylesheet hurts your eyes, it will hurt even worse if you try to use <xsl:text> -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:strip-space elements="*"/>

    <!-- "Muenchian" method of grouping: http://www.jenitennison.com/xslt/grouping/muenchian.html -->
    <!-- this is a map of document type names to nodesets -->
    <xsl:key name="doctypes-by-name" match="summarizedActionItem" use="docName"/>

    <xsl:template match="immediateReminder">
        <xsl:variable name="docHandlerUrl" select="actionItem/actionItem/docHandlerURL"/>
        <xsl:variable name="actionRequested">
          <xsl:choose>
            <xsl:when test="actionItem/actionItem/actionRequestCd = 'A'">: Approval Needed</xsl:when>
            <xsl:when test="actionItem/actionItem/actionRequestCd = 'K'">: Acknowledgment Needed</xsl:when>
            <xsl:when test="actionItem/actionItem/actionRequestCd = 'F'">: FYI</xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="instructions">
          <xsl:choose>
            <xsl:when test="actionItem/actionItem/actionRequestCd = 'A'"> and approve it</xsl:when>
            <xsl:when test="actionItem/actionItem/actionRequestCd = 'K'"> and acknowledge it</xsl:when>
            <xsl:when test="actionItem/actionItem/actionRequestCd = 'F'"/>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="paramSeparator">
            <xsl:choose>
                <xsl:when test="contains($docHandlerUrl, '?')">&amp;</xsl:when>
                <xsl:otherwise>?</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <email>
            <subject>KFS Workflow: <xsl:if test="actionItem/doc/docRouteStatus = 'D'">Disapproved </xsl:if>Document <xsl:value-of select="actionItem/actionItem/documentId"/>
<xsl:value-of select="actionItem/customSubject"/>
<xsl:value-of select="$actionRequested"/>
</subject>
            <body>An item in your KFS Inbox needs your attention:

From: <xsl:value-of select="actionItem/docInitiatorDisplayName"/>
Title: <xsl:value-of select="actionItem/actionItem/docTitle"/>
Type: <xsl:value-of select="actionItem/documentType/label"/>
Id: <xsl:value-of select="actionItem/actionItem/documentId"/>


Go here to view this item<xsl:value-of select="$instructions"/>: <xsl:value-of select="$docHandlerUrl"/>
<xsl:value-of select="$paramSeparator"/>docId=<xsl:value-of select="actionItem/actionItem/documentId"/>&amp;command=displayActionListView

Or go to your KFS Inbox to view all items that need your attention: <xsl:value-of select="@actionListUrl"/>

<xsl:if test="@env != 'kfs-prod'">
==== Debug ====
Action Item sent to <xsl:value-of select="actionItem/actionItemPrincipalName"/>
<xsl:if test="string(actionItem/actionItem/delegationType)">
 for delegation type <xsl:value-of select="actionItem/actionItem/delegationType"/>
</xsl:if>
in environment <xsl:value-of select="@env"/>
===============
</xsl:if>

<xsl:value-of select="actionItem/customBody"/>
            </body>
        </email>
    </xsl:template>

    <xsl:template match="dailyReminder">
        <email>
            <subject>KFS Reminder</subject>
            <body>Your KFS Inbox has <xsl:value-of select="count(summarizedActionItem)"/> items that need your attention:
<!-- "Muenchian" method of grouping: http://www.jenitennison.com/xslt/grouping/muenchian.html
     this clever little expression ensures that we only match the FIRST node
     for which there is a name-to-nodeset mapping.  More specifically, we want
     to ensure that we only match ONCE, but the FIRST node is the best node
     to match ONCE (or at least it's as good as any other; depends on whether
     we want to preserve relative ordering, etc.) -->
<xsl:for-each select="summarizedActionItem[count(. | key('doctypes-by-name', docName)[1]) = 1]">
    <!-- the xsl:sort modifies the for-each selection order (I think) -->
    <!-- <xsl:sort select="name" /> -->
    <!-- sort by count -->
    <!-- the order of identical values will be arbitrary in the Java map-based implementation;
         switching this to desceding here because 1) it seems more useful for a user and
         2) I want the unit test to pass, and given that it only uses two doc types, reversing
         the order will make it match the literal output of the Java version ;) -->
    <xsl:sort data-type="number" select="count(key('doctypes-by-name', docName))" order="descending"/>
<xsl:text>&tab;</xsl:text><xsl:value-of select="count(key('doctypes-by-name', docName))"/><xsl:text>&tab;</xsl:text><xsl:value-of select="docName"/><xsl:text>&n;</xsl:text>
</xsl:for-each>

To respond to each of these items:
&tab;Go to <xsl:value-of select="@actionListUrl"/>, and then click on its numeric ID in the first column of the List.



To change how these email notifications are sent (immediately, weekly or none):
&tab;Go to <xsl:value-of select="@preferencesUrl"/>



For additional help, please visit: https://www.dfa.cornell.edu/fis/gethelp

<xsl:if test="@env != 'kfs-prod'">
==== Debug ====
Daily Reminder sent to <xsl:value-of select="user/principalName"/>
in environment <xsl:value-of select="@env"/>
===============
</xsl:if>

</body>
        </email>
    </xsl:template>

    <xsl:template match="weeklyReminder">
        <email>
            <subject>KFS Reminder</subject>
            <body>Your KFS Inbox has <xsl:value-of select="count(summarizedActionItem)"/> items that need your attention:
<!-- "Muenchian" method of grouping: http://www.jenitennison.com/xslt/grouping/muenchian.html
     this clever little expression ensures that we only match the FIRST node
     for which there is a name-to-nodeset mapping.  More specifically, we want
     to ensure that we only match ONCE, but the FIRST node is the best node
     to match ONCE (or at least it's as good as any other; depends on whether
     we want to preserve relative ordering, etc.) -->
<xsl:for-each select="summarizedActionItem[count(. | key('doctypes-by-name', docName)[1]) = 1]">
    <!-- the xsl:sort modifies the for-each selection order (I think) -->
    <!-- <xsl:sort select="name" /> -->
    <!-- sort by count -->
    <!-- the order of identical values will be arbitrary in the Java map-based implementation;
         switching this to desceding here because 1) it seems more useful for a user and
         2) I want the unit test to pass, and given that it only uses two doc types, reversing
         the order will make it match the literal output of the Java version ;) -->
    <xsl:sort data-type="number" select="count(key('doctypes-by-name', docName))" order="descending"/>
<xsl:text>&tab;</xsl:text><xsl:value-of select="count(key('doctypes-by-name', docName))"/><xsl:text>&tab;</xsl:text><xsl:value-of select="docName"/><xsl:text>&n;</xsl:text>
</xsl:for-each>

To respond to each of these items:
&tab;Go to <xsl:value-of select="@actionListUrl"/>, and then click on its numeric ID in the first column of the List.



To change how these email notifications are sent (immediately, daily or none):
&tab;Go to <xsl:value-of select="@preferencesUrl"/>



For additional help, please visit: https://www.dfa.cornell.edu/fis/gethelp

<xsl:if test="@env != 'kfs-prod'">
==== Debug ====
Weekly Reminder sent to <xsl:value-of select="user/principalName"/>
in environment <xsl:value-of select="@env"/>
===============
</xsl:if>

</body>
        </email>
    </xsl:template>
</xsl:stylesheet>
