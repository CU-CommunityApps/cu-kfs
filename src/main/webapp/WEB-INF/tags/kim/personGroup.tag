<%--

    The Kuali Financial System, a comprehensive financial management system for higher education.

    Copyright 2005-2020 Kuali, Inc.

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
<%@ include file="/kr/WEB-INF/jsp/tldHeader.jsp"%>

<c:set var="docGroupAttributes" value="${DataDictionary.PersonDocumentGroup.attributes}" />

<%-- CU Customization: Updated this tag to suppress the group membership actions for read-only cases. --%>
<kul:subtab lookedUpCollectionName="group" width="${tableWidth}" subTabTitle="Groups" noShowHideButton="false">
   <table class="standard side-margins">
        <tr>
            <th><div align="left">&nbsp;</div></th> 
            <kim:cell inquiry="${inquiry}" isLabel="true" textAlign="center" attributeEntry="${docGroupAttributes.groupId}" noColon="true" />
            <kim:cell inquiry="${inquiry}" isLabel="true" textAlign="center" attributeEntry="${docGroupAttributes.namespaceCode}" noColon="true" />
            <kim:cell inquiry="${inquiry}" isLabel="true" textAlign="center" attributeEntry="${docGroupAttributes.groupName}" noColon="true" />
            <kim:cell inquiry="${inquiry}" isLabel="true" textAlign="center" attributeEntry="${docGroupAttributes.kimTypeId}" noColon="true" />
            <kim:cell inquiry="${inquiry}" isLabel="true" textAlign="center" attributeEntry="${docGroupAttributes.activeFromDate}" noColon="true" />
            <kim:cell inquiry="${inquiry}" isLabel="true" textAlign="center" attributeEntry="${docGroupAttributes.activeToDate}" noColon="true" />
            <c:if test="${not inquiry}">    
                <kul:htmlAttributeHeaderCell literalLabel="Actions" scope="col"/>
            </c:if>
        </tr>     
        <c:if test="${not inquiry and not readOnly}">               
            <tr>
                <th class="infoline">
                    <c:out value="Add:" />
                </th>
                <td align="left" valign="middle" class="infoline" >
                    <div align="center">
                        <kul:htmlControlAttribute property="newGroup.groupId" attributeEntry="${docGroupAttributes.groupId}" readOnly="${readOnly}"/>
                        <kul:lookup boClassName="org.kuali.kfs.kim.impl.group.GroupBo" fieldConversions="id:newGroup.groupId,kimTypeId:newGroup.kimTypeId,name:newGroup.groupName,namespaceCode:newGroup.namespaceCode" anchor="${tabKey}" />
                        <%--<html:hidden property="newGroup.groupName" />--%>
                        <html:hidden property="newGroup.kimTypeId" />
                        <html:hidden property="newGroup.kimGroupType.name" />
                        <%--<html:hidden property="newGroup.namespaceCode" />--%>               
                    </div>
                </td>
                <td align="left" valign="middle" class="infoline" >
                    <div align="center">
                        <kul:htmlControlAttribute property="newGroup.namespaceCode" attributeEntry="${docGroupAttributes.namespaceCode}" readOnly="${readOnly}"/>
                        <kul:lookup boClassName="org.kuali.kfs.kim.impl.group.GroupBo"
                                    fieldConversions="id:newGroup.groupId,kimTypeId:newGroup.kimTypeId,name:newGroup.groupName,namespaceCode:newGroup.namespaceCode"
                                    lookupParameters="newGroup.groupId:id,newGroup.groupName:name,newGroup.namespaceCode:namespaceCode"
                                    anchor="${tabKey}" 
                        />
                    </div>
                </td>
                <td align="left" valign="middle" class="infoline" >
                    <div align="center">
                        <kul:htmlControlAttribute property="newGroup.groupName" attributeEntry="${docGroupAttributes.groupName}" readOnly="${readOnly}"/>
                        <kul:lookup boClassName="org.kuali.kfs.kim.impl.group.GroupBo"
                                    fieldConversions="id:newGroup.groupId,kimTypeId:newGroup.kimTypeId,name:newGroup.groupName,namespaceCode:newGroup.namespaceCode"
                                    lookupParameters="newGroup.groupId:id,newGroup.groupName:name,newGroup.namespaceCode:namespaceCode"
                                    anchor="${tabKey}"
                    />
                    </div>
                </td>
                <td align="left" valign="middle" class="infoline" >
                    <div align="center">
                        <kul:htmlControlAttribute property="newGroup.kimGroupType.name" attributeEntry="${docGroupAttributes['kimGroupType.name']}" readOnly="${readOnly}"/>
                    </div>
                </td>
                <td align="left" valign="middle">
                    <div align="center"> <kul:htmlControlAttribute property="newGroup.activeFromDate"  attributeEntry="${docGroupAttributes.activeFromDate}"  datePicker="true" readOnly="${readOnly}"/>
                    </div>
                </td>
                <td align="left" valign="middle">
                    <div align="center"> <kul:htmlControlAttribute property="newGroup.activeToDate"  attributeEntry="${docGroupAttributes.activeToDate}"  datePicker="true" readOnly="${readOnly}"/>
                    </div>
                </td>
                <td class="infoline">
                    <div align=center>
                        <html:submit property="methodToCall.addGroup.anchor${tabKey}"
                            value="Add" styleClass="btn btn-green"/>
                    </div>
                </td>
            </tr>
        </c:if>
        <c:forEach var="group" items="${KualiForm.document.groups}" varStatus="status">
            <%-- CU Customization: Added flag for tracking group editability. --%>
            <c:set var="readOnlyGroup" scope="request" value="${!group.editable || readOnly}" />
            <tr>
                <th class="infoline">
                    <c:out value="${status.index+1}" />
                </th>
                <kim:cell inquiry="${inquiry}" valign="middle" cellClass="infoline" textAlign="center" property="document.groups[${status.index}].groupId"  attributeEntry="${docGroupAttributes.groupId}"  readOnly="true" />
                <kim:cell inquiry="${inquiry}" valign="middle" cellClass="infoline" textAlign="center" property="document.groups[${status.index}].namespaceCode"  attributeEntry="${docGroupAttributes.namespaceCode}" readOnly="true" />
                <kim:cell inquiry="${inquiry}" valign="middle" cellClass="infoline" textAlign="center" property="document.groups[${status.index}].groupName"  attributeEntry="${docGroupAttributes.groupName}" readOnly="true" />
                <kim:cell inquiry="${inquiry}" valign="middle" cellClass="infoline" textAlign="center" property="document.groups[${status.index}].kimGroupType.name"  attributeEntry="${docGroupAttributes['kimGroupType.name']}" readOnly="true" />
                <kim:cell inquiry="${inquiry}" valign="middle" cellClass="infoline" textAlign="center" property="document.groups[${status.index}].activeFromDate"  attributeEntry="${docGroupAttributes.activeFromDate}" datePicker="true" readOnly="${readOnly}" />
                <kim:cell inquiry="${inquiry}" valign="middle" cellClass="infoline" textAlign="center" property="document.groups[${status.index}].activeToDate"  attributeEntry="${docGroupAttributes.activeToDate}" datePicker="true" readOnly="${readOnly}" />

                <%-- CU Customization: Updated condition to take new readOnlyGroup flag into account. --%>
                <c:if test="${not inquiry && not readOnlyGroup}">                        
                    <td>
                        <div align=center>&nbsp;            
                            <html:submit property='methodToCall.deleteGroup.line${status.index}.anchor${currentTabIndex}'
                            value="Inactivate" styleClass='btn'/>
                        </div>
                    </td>
                </c:if>    
            </tr>
        </c:forEach>                    
    </table>
</kul:subtab>
