/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * CU Customization: Updated the "loadEmplInfo" function to reference the potentially masked Person Name fields.
 */

var chartCodeSuffix = '.chartOfAccountsCode'
var chartNameSuffix = '.chart.finChartOfAccountDescription'
var accountNumberSuffix = '.accountNumber'
var subAccountNumberSuffix = '.subAccountNumber'
var subAccountNameSuffix = '.subAccount.subAccountName'
var objectCodeSuffix = '.financialObjectCode'
var subObjectCodeSuffix = '.financialSubObjectCode'
var subObjectCodeNameSuffix = '.subObjectCode.financialSubObjectCodeName'
var universityFiscalYearSuffix = '.universityFiscalYear'

// eslint-disable-next-line no-unused-vars
function loadChartInfo (coaCodeFieldName, coaNameFieldName) {
  var coaCode = dwr.util.getValue(coaCodeFieldName)

  if (coaCode === '') {
    clearRecipients(coaNameFieldName, '')
  } else {
    var dwrReply = {
      callback: function (data) {
        if (data != null && typeof data === 'object') {
          setRecipientValue(coaNameFieldName, data.finChartOfAccountDescription)
        } else {
          setRecipientValue(
            coaNameFieldName,
            wrapError('chart not found'),
            true
          )
        }
      },
      errorHandler: function (errorMessage) {
        setRecipientValue(coaNameFieldName, wrapError('chart not found'), true)
      }
    }
    ChartService.getByPrimaryId(coaCode, dwrReply) // eslint-disable-line no-undef
  }
}

// eslint-disable-next-line no-unused-vars
function setReportsToChartCode () {
  // TODO: detect if in lookup or document mode
  // make AJAX call to get reports-to chart
  var coaCode = dwr.util.getValue(
    'document.newMaintainableObject' + chartCodeSuffix
  )

  if (coaCode !== '') {
    var dwrReply = {
      callback: function (data) {
        if (data != null && typeof data === 'object') {
          var reportsToChartDiv = document.getElementById(
            'document.newMaintainableObject.reportsToChartOfAccountsCode.div'
          )
          reportsToChartDiv.innerHTML = data.reportsToChartOfAccountsCode
        } else {
          window.status = 'chart not found.'
        }
      },
      errorHandler: function (errorMessage) {
        window.status = 'Unable to get reports-to chart.'
      }
    }
    ChartService.getByPrimaryId(coaCode, dwrReply) // eslint-disable-line no-undef
  }
}

// eslint-disable-next-line no-unused-vars
function loadAccountInfo (accountCodeFieldName, accountNameFieldName) {
  var elPrefix = findElPrefix(accountCodeFieldName)
  var accountCode = dwr.util.getValue(accountCodeFieldName)
  var coaCodeFieldName = elPrefix + chartCodeSuffix
  var coaNameFieldName = elPrefix + chartNameSuffix

  if (valueChanged(accountCodeFieldName)) {
    setRecipientValue(elPrefix + subAccountNumberSuffix, '')
    setRecipientValue(elPrefix + subAccountNameSuffix, '')
    setRecipientValue(elPrefix + subObjectCodeSuffix, '')
    setRecipientValue(elPrefix + subObjectCodeNameSuffix, '')
  }

  var dwrReply
  var dwrResult = {
    callback: function (param) {
      if (typeof param === 'boolean' && param === true) {
        var coaCode = dwr.util.getValue(coaCodeFieldName)
        // alert("Account Can Cross Chart: coaCode = " + coaCode + ", accountCode = " + accountCode);
        if (accountCode === '') {
          clearRecipients(accountNameFieldName)
        } else if (coaCode === '') {
          setRecipientValue(
            accountNameFieldName,
            wrapError('chart code is empty'),
            true
          )
        } else {
          accountCode = accountCode.toUpperCase()
          dwrReply = {
            callback: function (data) {
              if (data != null && typeof data === 'object') {
                setRecipientValue(accountNameFieldName, data.accountName)
              } else {
                setRecipientValue(
                  accountNameFieldName,
                  wrapError('account not found'),
                  true
                )
              }
            },
            errorHandler: function (errorMessage) {
              setRecipientValue(
                accountNameFieldName,
                wrapError('error looking up account'),
                true
              )
            }
          }

          // eslint-disable-next-line no-undef
          AccountService.getByPrimaryIdWithCaching(
            coaCode,
            accountCode,
            dwrReply
          )
        }
      } else {
        // alert("Account Cant Cross Chart: coaCodeFieldName = " + coaCodeFieldName);
        if (accountCode === '') {
          clearRecipients(accountNameFieldName)
          clearRecipients(coaCodeFieldName)
          clearRecipients(coaNameFieldName)
        } else {
          accountCode = accountCode.toUpperCase()
          dwrReply = {
            callback: function (data) {
              if (data != null && typeof data === 'object') {
                setRecipientValue(accountNameFieldName, data.accountName)
                setRecipientValue(coaCodeFieldName, data.chartOfAccountsCode)
                // alert("coaCode = " + dwr.util.getValue(coaCodeFieldName+".div"));
                setRecipientValue(
                  coaNameFieldName,
                  data.chartOfAccounts.finChartOfAccountDescription
                )
              } else {
                setRecipientValue(
                  accountNameFieldName,
                  wrapError('account not found'),
                  true
                )
                clearRecipients(coaCodeFieldName)
                clearRecipients(coaNameFieldName)
              }
            },
            errorHandler: function (errorMessage) {
              setRecipientValue(
                accountNameFieldName,
                wrapError('error looking up account'),
                true
              )
              clearRecipients(coaCodeFieldName)
              clearRecipients(coaNameFieldName)
            }
          }

          AccountService.getUniqueAccountForAccountNumber(accountCode, dwrReply) // eslint-disable-line no-undef
        }
      }
    },
    errorHandler: function (errorMessage) {
      setRecipientValue(
        accountNameFieldName,
        wrapError('error looking up AccountCanCrossChart parameter'),
        true
      )
    }
  }
  AccountService.accountsCanCrossCharts(dwrResult) // eslint-disable-line no-undef
}

// eslint-disable-next-line no-unused-vars
function loadSubAccountInfo (subAccountCodeFieldName, subAccountNameFieldName) {
  var elPrefix = findElPrefix(subAccountCodeFieldName)
  var coaCode = getElementValue(elPrefix + chartCodeSuffix)
  var accountCode = getElementValue(elPrefix + accountNumberSuffix)
  var subAccountCode = getElementValue(subAccountCodeFieldName)
  // alert("loadSubAccountInfo:\ncoaCode = " + coaCode + "\naccountCode = " + accountCode + "\nsubAccountCode = " + subAccountCode);

  if (subAccountCode === '') {
    clearRecipients(subAccountNameFieldName)
  } else if (coaCode === '') {
    setRecipientValue(
      subAccountNameFieldName,
      wrapError('chart code is empty'),
      true
    )
  } else if (accountCode === '') {
    setRecipientValue(
      subAccountNameFieldName,
      wrapError('account number is empty'),
      true
    )
  } else {
    var dwrReply = {
      callback: function (data) {
        if (data != null && typeof data === 'object') {
          setRecipientValue(subAccountNameFieldName, data.subAccountName)
        } else {
          setRecipientValue(
            subAccountNameFieldName,
            wrapError('sub-account not found'),
            true
          )
        }
      },
      errorHandler: function (errorMessage) {
        setRecipientValue(
          subAccountNameFieldName,
          wrapError('sub-account not found'),
          true
        )
      }
    }
    // eslint-disable-next-line no-undef
    SubAccountService.getByPrimaryId(
      coaCode,
      accountCode,
      subAccountCode,
      dwrReply
    )
  }
}

// eslint-disable-next-line no-unused-vars
function loadObjectInfo (
  fiscalYear,
  objectTypeNameRecipient,
  objectTypeCodeRecipient,
  objectCodeFieldName,
  objectNameFieldName
) {
  var elPrefix = findElPrefix(objectCodeFieldName)
  var coaCode = getElementValue(elPrefix + chartCodeSuffix)
  var objectCode = getElementValue(objectCodeFieldName)
  // alert("loadObjectInfo:\nfiscalYear = " + fiscalYear + "\ncoaCode = " + coaCode + "\nobjectCode = " + objectCode);

  if (valueChanged(objectCodeFieldName)) {
    clearRecipients(elPrefix + subObjectCodeSuffix)
    clearRecipients(elPrefix + subObjectCodeNameSuffix)
  }
  if (objectCode === '') {
    clearRecipients(objectNameFieldName)
  } else if (coaCode === '') {
    setRecipientValue(
      objectNameFieldName,
      wrapError('chart code is empty'),
      true
    )
  } else if (fiscalYear === '') {
    setRecipientValue(
      objectNameFieldName,
      wrapError('fiscal year is missing'),
      true
    )
  } else {
    var dwrReply = {
      callback: function (data) {
        if (data != null && typeof data === 'object') {
          setRecipientValue(objectNameFieldName, data.financialObjectCodeName)
          setRecipientValue(
            objectTypeCodeRecipient,
            data.financialObjectTypeCode
          )
          setRecipientValue(
            objectTypeNameRecipient,
            data.financialObjectType.name
          )
        } else {
          setRecipientValue(
            objectNameFieldName,
            wrapError('object not found'),
            true
          )
          clearRecipients(objectTypeCodeRecipient)
          clearRecipients(objectTypeNameRecipient)
        }
      },
      errorHandler: function (errorMessage) {
        window.status = errorMessage
        setRecipientValue(
          objectNameFieldName,
          wrapError('object not found'),
          true
        )
        clearRecipients(objectTypeCodeRecipient)
        clearRecipients(objectTypeNameRecipient)
      }
    }
    ObjectCodeService.getByPrimaryId(fiscalYear, coaCode, objectCode, dwrReply) // eslint-disable-line no-undef
  }
}

// eslint-disable-next-line no-unused-vars
function loadObjectCodeInfo (objectCodeFieldName, objectNameFieldName) {
  var elPrefix = findElPrefix(objectCodeFieldName)
  var fiscalYear = getElementValue(elPrefix + universityFiscalYearSuffix)
  var coaCode = getElementValue(elPrefix + chartCodeSuffix)
  var objectCode = getElementValue(objectCodeFieldName)
  // alert("loadObjectCodeInfo:\nfiscalYear = " + fiscalYear + "\ncoaCode = " + coaCode + "\nobjectCode = " + objectCode);

  if (valueChanged(objectCodeFieldName)) {
    clearRecipients(objectNameFieldName)
  }
  if (objectCode === '') {
    clearRecipients(objectNameFieldName)
  } else if (coaCode === '') {
    setRecipientValue(
      objectNameFieldName,
      wrapError('chart code is empty'),
      true
    )
  } else if (fiscalYear === '') {
    setRecipientValue(
      objectNameFieldName,
      wrapError('fiscal year is missing'),
      true
    )
  } else {
    var dwrReply = {
      callback: function (data) {
        if (data != null && typeof data === 'object') {
          setRecipientValue(objectNameFieldName, data.financialObjectCodeName)
        } else {
          setRecipientValue(
            objectNameFieldName,
            wrapError('object not found'),
            true
          )
        }
      },
      errorHandler: function (errorMessage) {
        setRecipientValue(
          objectNameFieldName,
          wrapError('object not found'),
          true
        )
      }
    }
    ObjectCodeService.getByPrimaryId(fiscalYear, coaCode, objectCode, dwrReply) // eslint-disable-line no-undef
  }
}

// eslint-disable-next-line no-unused-vars
function loadSubObjectInfo (
  fiscalYear,
  subObjectCodeFieldName,
  subObjectNameFieldName
) {
  var elPrefix = findElPrefix(subObjectCodeFieldName)
  var coaCode = getElementValue(elPrefix + chartCodeSuffix)
  var accountCode = getElementValue(elPrefix + accountNumberSuffix)
  var objectCode = getElementValue(elPrefix + objectCodeSuffix)
  var subObjectCode = getElementValue(subObjectCodeFieldName)
  // alert("loadSubObjectInfo:\nfiscalYear = " + fiscalYear + "\ncoaCode = " + coaCode + "\naccountCode = " + accountCode + "\nobjectCode = " + objectCode + "\nsubObjectCode = " + subObjectCode);

  if (subObjectCode === '') {
    clearRecipients(subObjectNameFieldName)
  } else if (coaCode === '') {
    setRecipientValue(subObjectNameFieldName, wrapError('chart is empty'), true)
  } else if (fiscalYear === '') {
    setRecipientValue(
      subObjectNameFieldName,
      wrapError('fiscal year is missing'),
      true
    )
  } else if (accountCode === '') {
    setRecipientValue(
      subObjectNameFieldName,
      wrapError('account is empty'),
      true
    )
  } else if (objectCode === '') {
    setRecipientValue(
      subObjectNameFieldName,
      wrapError('object code is empty'),
      true
    )
  } else {
    var dwrReply = {
      callback: function (data) {
        if (data != null && typeof data === 'object') {
          setRecipientValue(
            subObjectNameFieldName,
            data.financialSubObjectCodeName
          )
        } else {
          setRecipientValue(
            subObjectNameFieldName,
            wrapError('sub-object not found'),
            true
          )
        }
      },
      errorHandler: function (errorMessage) {
        setRecipientValue(
          subObjectNameFieldName,
          wrapError('sub-object not found'),
          true
        )
      }
    }
    // eslint-disable-next-line no-undef
    SubObjectCodeService.getByPrimaryId(
      fiscalYear,
      coaCode,
      accountCode,
      objectCode,
      subObjectCode,
      dwrReply
    )
  }
}

// eslint-disable-next-line no-unused-vars
function loadProjectInfo (projectCodeFieldName, projectNameFieldName) {
  var projectCode = getElementValue(projectCodeFieldName)

  if (projectCode === '') {
    clearRecipients(projectNameFieldName)
  } else {
    var dwrReply = {
      callback: function (data) {
        if (data != null && typeof data === 'object') {
          setRecipientValue(projectNameFieldName, data.name)
        } else {
          setRecipientValue(
            projectNameFieldName,
            wrapError('project not found'),
            true
          )
        }
      },
      errorHandler: function (errorMessage) {
        setRecipientValue(
          projectNameFieldName,
          wrapError('project not found'),
          true
        )
      }
    }
    ProjectCodeService.getByPrimaryId(projectCode, dwrReply) // eslint-disable-line no-undef
  }
}

// eslint-disable-next-line no-unused-vars
function loadObjectTypeInfo (objectTypeCodeFieldName, objectTypeNameFieldName) {
  var objectTypeCode = getElementValue(objectTypeCodeFieldName)

  if (objectTypeCode === '') {
    clearRecipients(objectTypeNameFieldName)
  } else {
    var dwrReply = {
      callback: function (data) {
        if (data != null && typeof data === 'object') {
          setRecipientValue(objectTypeNameFieldName, data.name)
        } else {
          setRecipientValue(
            objectTypeNameFieldName,
            wrapError('object type not found'),
            true
          )
        }
      },
      errorHandler: function (errorMessage) {
        setRecipientValue(
          objectTypeNameFieldName,
          wrapError('object type not found'),
          true
        )
      }
    }
    ObjectTypeService.getByPrimaryKey(objectTypeCode, dwrReply) // eslint-disable-line no-undef
  }
}

// eslint-disable-next-line no-unused-vars
function loadOriginationInfo (
  originationCodeFieldName,
  originationCodeNameFieldName
) {
  var originationCode = getElementValue(originationCodeFieldName)

  if (originationCode === '') {
    clearRecipients(originationCodeNameFieldName)
  } else {
    var dwrReply = {
      callback: function (data) {
        if (data != null && typeof data === 'object') {
          setRecipientValue(
            originationCodeNameFieldName,
            data.financialSystemDatabaseName
          )
        } else {
          setRecipientValue(
            originationCodeNameFieldName,
            wrapError('origin code not found'),
            true
          )
        }
      },
      errorHandler: function (errorMessage) {
        setRecipientValue(
          originationCodeNameFieldName,
          wrapError('origin code not found'),
          true
        )
      }
    }
    OriginationCodeService.getByPrimaryKey(originationCode, dwrReply) // eslint-disable-line no-undef
  }
}

// eslint-disable-next-line no-unused-vars
function loadEmplInfo (emplIdFieldName, userNameFieldName) {
  var userId = dwr.util.getValue(emplIdFieldName)
  var containerDiv = document.getElementById(userNameFieldName + divSuffix)

  if (userId === '') {
    dwr.util.setValue(containerDiv.id, '')
  } else {
    var dwrReply = {
      callback: function (data) {
        if (data != null && typeof data === 'object') {
          // ==== CU Customization: Use potentially masked Person name instead. ====
          dwr.util.setValue(containerDiv.id, data.nameMaskedIfNecessary, { escapeHtml: true })
          // ==== End CU Customization ====
        } else {
          dwr.util.setValue(containerDiv.id, wrapError('person not found'), {
            escapeHtml: false
          })
        }
      },
      errorHandler: function (errorMessage) {
        dwr.util.setValue(containerDiv.id, wrapError('person not found'), {
          escapeHtml: false
        })
      }
    }
    PersonService.getPersonByEmployeeId(userId, dwrReply) // eslint-disable-line no-undef
  }
}

// eslint-disable-next-line no-unused-vars
function updateAssetLocation (itemId, locationId) {
  $(
    'input#populate-building-item' +
      itemId +
      '-location' +
      locationId +
      '-button'
  ).click()
}

// eslint-disable-next-line no-unused-vars
function updateDeliveryBuilding () {
  $('input#populate-delivery-building-code-button').click()
}

/** searchs for all child nodes and executes the specified function **/
NodeIterator.invoke = function (func) {
  jQuery("[id^='tab-'][id$='-div']").each(function () {
    func(document, this.id.substring(4, this.id.length - 4))
  })
}
