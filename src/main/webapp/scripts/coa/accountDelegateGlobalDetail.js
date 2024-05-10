/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
 * CU Customization: Updated functions to reference the potentially masked Person Name field instead.
 */

// eslint-disable-next-line no-unused-vars
function principalNameLookup (userIdField) {
  var userIdFieldName = userIdField.name
  var elPrefix = findElPrefix(userIdFieldName)
  // ==== CU Customization: Use the potentially masked Person name instead. ====
  var userNameFieldName = elPrefix + '.nameMaskedIfNecessary'
  var universalIdFieldName = findElPrefix(elPrefix) + '.principalId'

  loadPrincipalInfo(userIdFieldName, universalIdFieldName, userNameFieldName)
}

function loadPrincipalInfo (
  userIdFieldName,
  universalIdFieldName,
  userNameFieldName
) {
  var userId = dwr.util.getValue(userIdFieldName).trim()

  if (userId === '') {
    clearRecipients(universalIdFieldName)
    clearRecipients(userNameFieldName)
  } else {
    var dwrReply = {
      callback: function (data) {
        if (data != null && typeof data === 'object') {
          setRecipientValue(universalIdFieldName, data.principalId)
          // ==== CU Customization: Use the potentially masked Person name instead. ====
          setRecipientValue(userNameFieldName, data.nameMaskedIfNecessary)
        } else {
          clearRecipients(universalIdFieldName)
          setRecipientValue(
            userNameFieldName,
            wrapError('User Name not found'),
            true
          )
        }
      },
      errorHandler: function (errorMessage) {
        clearRecipients(universalIdFieldName)
        setRecipientValue(
          userNameFieldName,
          wrapError('user Name not found'),
          true
        )
      }
    }
    PersonService.getPersonByPrincipalName(userId, dwrReply) // eslint-disable-line
  }
}
