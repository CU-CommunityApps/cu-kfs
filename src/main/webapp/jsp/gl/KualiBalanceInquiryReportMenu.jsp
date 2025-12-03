<%--

    The Kuali Financial System, a comprehensive financial management system for higher education.

    Copyright 2005-2024 Kuali, Inc.

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
<%@ include file="/jsp/sys/kfsTldHeader.jsp" %>
<kul:page showDocumentInfo="false"
          headerTitle="Balance Inquiry Report Menu" docTitle="Balance Inquiry Report Menu"
          transactionalDocument="false"
          htmlFormAction="${KFSConstants.MAPPING_BALANCE_INQUIRY_REPORT_MENU}">

    <html:hidden property="backLocation" write="false"/>
    <html:hidden property="docFormKey" write="false"/>
    <html:hidden property="balanceInquiryReportMenuCallerDocFormKey" write="false"/>
    <html:hidden property="chartOfAccountsCode" write="false"/>
    <html:hidden property="universityFiscalYear" write="false"/>
    <html:hidden property="accountNumber" write="false"/>
    <html:hidden property="subAccountNumber" write="false"/>
    <html:hidden property="financialObjectCode" write="false"/>
    <html:hidden property="financialSubObjectCode" write="false"/>
    <html:hidden property="objectTypeCode" write="false"/>
    <html:hidden property="debitCreditCode" write="false"/>
    <html:hidden property="referenceOriginCode" write="false"/>
    <html:hidden property="referenceTypeCode" write="false"/>
    <html:hidden property="referenceNumber" write="false"/>
    <html:hidden property="projectCode" write="false"/>

    <div class="main-panel">
        <div class="tab-container">
            <table class="standard" style="width:350px; margin: 30px auto;">
                <tr>
                    <th>Available Balances</th>
                    <td>
                        <button
                            name="availableBalances"
                            tabindex="${KualiForm.nextArbitrarilyHighIndex}"
                            data-businessObjectName="AccountBalance"
                            data-parameters="${KualiForm.availableBalancesBalanceInquiryLookupParameters}"
                            data-goToNewLookup="true"
                            type="button"
                            class="btn btn-default"
                        >
                            Search
                        </button>
                    </td>
                </tr>
                <tr>
                    <th>Balances by Consolidation</th>
                    <td>
                        <gl:balanceInquiryLookup
                                boClassName="org.kuali.kfs.gl.businessobject.AccountBalanceByConsolidation"
                                actionPath="${KFSConstants.GL_ACCOUNT_BALANCE_BY_CONSOLIDATION_LOOKUP_ACTION}"
                                lookupParameters="${KualiForm.balancesByConsolidationBalanceInquiryLookupParameters}"
                                hideReturnLink="true"/>
                    </td>
                </tr>
                <tr>
                    <th>Cash Balances</th>
                    <td>
                        <gl:balanceInquiryLookup
                                boClassName="org.kuali.kfs.gl.businessobject.CashBalance"
                                actionPath="${KFSConstants.GL_MODIFIED_INQUIRY_ACTION}"
                                lookupParameters="${KualiForm.cashBalancesBalanceInquiryLookupParameters}"
                                hideReturnLink="true"/>
                    </td>
                </tr>
                <tr>
                    <th>General Ledger Balance</th>
                    <td>
                        <gl:balanceInquiryLookup
                                boClassName="org.kuali.kfs.gl.businessobject.Balance"
                                actionPath="${KFSConstants.GL_BALANCE_INQUIRY_ACTION}"
                                lookupParameters="${KualiForm.generalLedgerBalanceBalanceInquiryLookupParameters}"
                                hideReturnLink="true"/>
                    </td>
                </tr>
                <tr>
                    <th>General Ledger Entry</th>
                    <td>
                        <gl:balanceInquiryLookup
                                boClassName="org.kuali.kfs.gl.businessobject.Entry"
                                actionPath="${KFSConstants.GL_MODIFIED_INQUIRY_ACTION}"
                                lookupParameters="${KualiForm.generalLedgerEntryBalanceInquiryLookupParameters}"
                                hideReturnLink="true"/>
                    </td>
                </tr>
                <tr>
                    <th>General Ledger Pending Entry</th>
                    <td>
                        <gl:balanceInquiryLookup
                                boClassName="org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry"
                                actionPath="${KFSConstants.GL_MODIFIED_INQUIRY_ACTION}"
                                lookupParameters="${KualiForm.generalLedgerPendingEntryBalanceInquiryLookupParameters}"
                                hideReturnLink="true"/>
                    </td>
                </tr>
                <tr>
                    <th>Open Encumbrances</th>
                    <td>
                        <gl:balanceInquiryLookup
                                boClassName="org.kuali.kfs.gl.businessobject.Encumbrance"
                                actionPath="${KFSConstants.GL_MODIFIED_INQUIRY_ACTION}"
                                lookupParameters="${KualiForm.openEncumbrancesBalanceInquiryLookupParameters}"
                                hideReturnLink="true"/>
                    </td>
                </tr>
                <tr>
                    <td class="center" colspan="2">
                        <html:submit
                                styleClass="btn btn-default"
                                property="methodToCall.cancel"
                                alt="cancel"
                                title="cancel"
                                value="Cancel"/>
                    </td>
                </tr>
            </table>
        </div>
    </div>
    <script>
        const navigateToLookup = async (event) => {
          event.preventDefault();
          const businessObjectName = event.target.getAttribute('data-businessObjectName');
          const lookupParameters = event.target.getAttribute('data-parameters');
          //Cornell customization: change the lookup url to start with /kfs/ rather than /fin/
          let lookupUrl = '/kfs/webapp/lookup/' + businessObjectName;
          const formFromUrl = location.search.substring(1);
          const formParams = new URLSearchParams(formFromUrl);
          const fieldMap = lookupParameters.split(',');
          const form = {};
          for(const item of fieldMap) {
            const fields = item.split(':');
            const field = fields[0];
            if (formParams.has(field)) {
              // Special case for AccountBalance
              if (businessObjectName === 'AccountBalance') {
                if (field === 'financialObjectCode') {
                  form['objectCode'] = formParams.get(field);
                } else if (field !== 'objectTypeCode') {
                  // We don't include objectTypeCode for available balances
                  form[field] = formParams.get(field);
                }
              } else {
                form[field] = formParams.get(field);
              }
            }
          }

          const elements = document.forms[0].elements;
          let formData = {};
          for (let i = 0; i < elements.length; i++) {
            const element = elements[i];
            const key = element.id !== '' ? element.id : element.name;
            switch (element.tagName.toLowerCase()) {
              case 'input':
                switch (element.type.toLowerCase()) {
                  case 'hidden':
                  case 'text':
                    formData[key] = element.value;
                    break;
                  case 'radio':
                  case 'checkbox':
                    formData[key] = element.checked;
                    break;
                  default:
                    break;
                }
                break;
              case 'textarea':
                formData[key] = element.value;
                break;
              case 'select':
                formData[key] = element.options[element.selectedIndex].text;
                break;
              default:
                break;
            }
          }

          await window.ReduxShim.store.dispatch(window.ReduxShim.pageHistory.actions.pushHistory({
            title: document.getElementsByTagName('h1')[0].innerText.trim(),
            legacy: true,
            pathname: location.pathname,
            pageConfiguration: {
              formData: formData,
              returnRequestParamMap: Object.fromEntries(formParams),
              hideReturn: true
            }
          }));
          lookupUrl += '?' + new URLSearchParams(form).toString();
          location.assign(lookupUrl);
        }

        const loadFormData = async (incomingData) => {
          const elements = document.forms[0].elements;

          // Some elements come from the backend on render, and we need them to persist that value
          const ignoreElements = ['docFormKey'];
          for (let i = 0; i < elements.length; i++) {
            const element = elements[i];
            const key = element.id !== '' ? element.id : element.name;
            if (ignoreElements.includes(key)) {
              continue;
            }
            const value = incomingData[key];
            switch (element.tagName.toLowerCase()) {
              case 'input':
                switch (element.type.toLowerCase()) {
                  case 'hidden':
                  case 'text':
                    element.value = value;
                    break;
                  case 'radio':
                  case 'checkbox':
                    element.checked = value;
                    break;
                  default:
                    break;
                }
                break;
              case 'textarea':
                element.value = value;
                break;
              case 'select':
                const option = Array.from(element.options).find(item => item.text === value)
                if (option) {
                  option.selected = true;
                }
                break;
              default:
                break;
            }
          }
        }

        document.addEventListener("DOMContentLoaded", function () {
          const newLookups = document.querySelectorAll('[data-goToNewLookup="true"]')
          newLookups.forEach(function (lookupButton) {
            lookupButton.addEventListener('click', navigateToLookup);
          });
        });

        document.addEventListener("ReduxStoreLoaded", async () => {
          const returnedData = window.ReduxShim.store.getState()['pageHistory'].returnedData
          if (returnedData) {
            await loadFormData(returnedData);
            await window.ReduxShim.store.dispatch(window.ReduxShim.pageHistory.actions.clearReturnedData());
          }
        })
    </script>
</kul:page>
