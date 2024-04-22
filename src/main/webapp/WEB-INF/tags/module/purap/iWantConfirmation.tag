
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<%@ attribute name="documentAttributes" required="true" type="java.util.Map"
              description="The DataDictionary entry containing attributes for this row's fields." %>
<c:set var="tabindexOverrideBase" value="20" />

<kul:tabTop tabTitle="I Want Document Confirm Action" defaultOpen="true" tabErrorKey="*">
    <div class="tab-container">
        <table class="standard" summary="I Want Document Confirm Action" >
            <tr>
                <th class="right" width="100%">
                   SSC should not be creating a REQ/DV when the contract indicator is Yes. Do you wish to continue?
                </th>
               
            </tr>

		</table>
    </div>
</kul:tabTop>
