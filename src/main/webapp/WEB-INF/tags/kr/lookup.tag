<%--

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

--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>
<%@ attribute name="boClassName" required="true" description="The name of the class to look up instances of in the linked lookup." %>
<%@ attribute name="fieldConversions" required="false" description="Pre-set values to populate within the lookup form." %>
<%@ attribute name="lookupParameters" required="false" description="On return from lookup, these parameters describe which attributes of the business object to populate in the lookup parent." %>
<%@ attribute name="hideReturnLink" required="false" description="Whether the return link should be rendered or not in the linked lookup." %>
<%@ attribute name="suppressActions" required="false" description="Whether action buttons should be rendered or not in the linked lookup." %>
<%@ attribute name="tabindexOverride" required="false" description="Overrides the tab index of this lookup icon." %>
<%@ attribute name="extraButtonSource" required="false" description="The image source of an extra button to have displayed in the linked lookup." %>
<%@ attribute name="extraButtonParams" required="false" description="The parameters, such as the method to call, which the extra button will apply when clicked." %>
<%@ attribute name="anchor" required="false" description="If present, on return from lookup, the page will be scrolled to the named anchor specified by this attribute." %>
<%@ attribute name="fieldLabel" required="false" description="The label of the field which this lookup icon is associated with." %>
<%@ attribute name="readOnlyFields" required="false" description="Which fields in the linked lookup should be displayed at read only." %>
<%@ attribute name="referencesToRefresh" required="false" description="On return from the lookup, the references on the parent business object which will be refreshed." %>
<%@ attribute name="autoSearch" required="false" description="Determines whether auto-search will be used in the lookup." %>
<%@ attribute name="searchIconOverride" required="false" description="If present, changes the url of the icon image from the default Kuali magnifying glass to the image given here." %>
<%@ attribute name="baseLookupUrl" required="false" description="The url the lookup will direct itself to." %>
<%@ attribute name="addClass" required="false" description="Additional class(es) to add to the input" %>
<%@ attribute name="newLookup" required="false" description="Determines whether this lookup should use the new framework or not" %>
<%@ attribute name="fieldPropertyName" required="false" description="The property name of the field associated with this lookup" %>
<%@ attribute name="staticLookupFieldData" required="false" description="Static data to populate in the new lookup" %>
<%-- CU Customization: Added a new attribute to assist with handling Person lookups. --%>
<%@ attribute name="alreadyAdjustedForPerson" required="false" description="For Person lookups, indicates whether the parent tag has already made adjustments to potentially mask the relevant Person data." %>

<%-- CU Customization: If necessary, make adjustments to reference the potentially masked Person fields. --%>

<c:if test="${boClassName eq 'org.kuali.kfs.kim.impl.identity.Person' && !alreadyAdjustedForPerson}">

    <c:if test="${!empty fieldConversions}">
        <c:set var="fieldConversions" value="${kfsfunc:convertPersonFieldConversionsForMasking(fieldConversions)}"/>
    </c:if>

    <c:if test="${!empty lookupParameters}">
        <c:set var="lookupParameters" value="${kfsfunc:convertPersonLookupParametersForMasking(lookupParameters)}"/>
    </c:if>

</c:if>

<%-- End CU Customization --%>

<c:choose>
  <c:when test="${!empty tabindexOverride}">
    <c:set var="tabindex" value="${tabindexOverride}"/>
  </c:when>
  <c:otherwise>
    <c:set var="tabindex" value="${KualiForm.nextArbitrarilyHighIndex}"/>
  </c:otherwise>
</c:choose>
<c:choose>
  <c:when test="${!empty searchIconOverride}">
    <c:set var="lookupicon" value="${searchIconOverride}"/>
  </c:when>
  <c:otherwise>
    <c:set var="lookupicon" value="${ConfigProperties.externalizable.images.url}searchicon.png"/>
  </c:otherwise>
</c:choose>

<c:set var="epMethodToCallAttribute" value="methodToCall.performLookup.(!!${boClassName}!!).(((${fieldConversions}))).((`${lookupParameters}`)).((<${hideReturnLink}>)).(([${extraButtonSource}])).((*${extraButtonParams}*)).((^${suppressActions}^)).((&${readOnlyFields}&)).((/${referencesToRefresh}/)).((~${autoSearch}~)).(::::;${baseLookupUrl};::::).anchor${anchor}"/>
${kfunc:registerEditableProperty(KualiForm, epMethodToCallAttribute)}
<c:choose>
  <c:when test="${newLookup}">
    <%--
      If the newLookup flag is set in the datadictionary, we add more properties to the input field to be read by
      javascript that can display a modern lookup on an old document page
    --%>
    <input
      type="image"
      tabindex="${tabindex}"
      src="${lookupicon}"
      border="0"
      class="searchicon ${addClass}"
      valign="middle"
      alt="Search ${fieldLabel}"
      title="Search ${fieldLabel}"
      data-lookup-type="single"
      data-business-object-name="${boClassName}"
      data-field-name="${fieldPropertyName}"
      data-lookup-parameters="${lookupParameters}"
      data-field-conversions="${fieldConversions}"
      data-read-only-fields="${readOnlyFields}"
      data-static-lookup-field-data="${staticLookupFieldData}"
    />
  </c:when>
  <c:otherwise>
    <input
      type="image"
      tabindex="${tabindex}"
      name="${epMethodToCallAttribute}"
      src="${lookupicon}"
      border="0"
      class="searchicon ${addClass}"
      valign="middle"
      alt="Search ${fieldLabel}"
      title="Search ${fieldLabel}"
    />
  </c:otherwise>
</c:choose>
