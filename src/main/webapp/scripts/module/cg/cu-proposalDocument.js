/*
 * Overriding the following functions from KualiCo's "proposalDocument.js" file so that the Person Name
 * field references can be replaced with the potentially masked equivalents.
 */

var proposalDirectorIDLookup = function(userIdField) {
  var userIdFieldName = userIdField.name
  var elPrefix = findElPrefix(userIdFieldName)
  var userNameFieldName = elPrefix + '.nameMaskedIfNecessary'
  var universalIdFieldName = findElPrefix(elPrefix) + '.principalId'

  loadDirectorInfo(userIdFieldName, universalIdFieldName, userNameFieldName)
}

var loadDirectorInfo = function(userIdFieldName, universalIdFieldName, userNameFieldName) {
  var userId = dwr.util.getValue(userIdFieldName).trim()

  if (userId === '') {
    clearRecipients(universalIdFieldName)
    clearRecipients(userNameFieldName)
  } else {
    var dwrReply = {
      callback: function (data) {
        if (data != null && typeof data === 'object') {
          setRecipientValue(universalIdFieldName, data.principalId)
          setRecipientValue(userNameFieldName, data.nameMaskedIfNecessary)
        } else {
          clearRecipients(universalIdFieldName)
          setRecipientValue(userNameFieldName, wrapError('director not found'), true)
        }
      },
      errorHandler: function (errorMessage) {
        clearRecipients(universalIdFieldName)
        setRecipientValue(userNameFieldName, wrapError('director not found'), true)
      }
    }
    // eslint-disable-next-line
    PersonService.getPersonByPrincipalName(userId, dwrReply)
  }
}
