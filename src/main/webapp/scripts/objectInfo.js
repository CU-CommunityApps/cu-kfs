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
 * CU Customization: Updated the loadUserInfo() function to reference the potentially masked Person Name fields.
 */

function loadUserInfo(userIdFieldName, universalIdFieldName, userNameFieldName) {
  var userId = dwr.util.getValue(userIdFieldName);

  if (userId == "") {
    clearRecipients(universalIdFieldName, "");
    clearRecipients(userNameFieldName, "");
  } else {
    var dwrReply = {
      callback: function (data) {
        if (data != null && typeof data == 'object') {
          if (universalIdFieldName != null && universalIdFieldName != "") {
            setRecipientValue(universalIdFieldName, data.principalId);
          }
          // ==== CU Customization: Use the potentially masked name instead. ====
          if (userNameFieldName != null && userNameFieldName != "") {
            setRecipientValue(userNameFieldName, data.nameMaskedIfNecessary);
          } else {
            // guess the DIV name
            divName = userIdFieldName.replace(".principalName", ".nameMaskedIfNecessary.div");
            dwr.util.setValue(divName, data.nameMaskedIfNecessary);
          }
          // ==== End CU Customization ====
        } else {
          if (universalIdFieldName != null && universalIdFieldName != "") {
            setRecipientValue(universalIdFieldName, "");
          }
          if (userNameFieldName != null && userNameFieldName != "") {
            setRecipientValue(userNameFieldName, wrapError("person not found"), true);
          } else {
            // guess the DIV name
            // ==== CU Customization: Use the potentially masked name instead. ====
            divName = userIdFieldName.replace(".principalName", ".nameMaskedIfNecessary.div");
            // ==== End CU Customization ====
            dwr.util.setValue(divName, wrapError("person not found"), {escapeHtml: false});
          }
        }
      },
      errorHandler: function (errorMessage) {
        window.status = errorMessage;
        if (universalIdFieldName != null && universalIdFieldName != "") {
          setRecipientValue(universalIdFieldName, "");
        }
        if (userNameFieldName != null && userNameFieldName != "") {
          setRecipientValue(userNameFieldName, wrapError("person not found"), true);
        } else {
          // guess the DIV name
          // ==== CU Customization: Use the potentially masked name instead. ====
          divName = userIdFieldName.replace(".principalName", ".nameMaskedIfNecessary.div");
          // ==== End CU Customization ====
          dwr.util.setValue(divName, wrapError("person not found"), {escapeHtml: false});
        }
      }
    };
    PersonService.getPersonByPrincipalName(userId, dwrReply);
  }
}

/**
 * This method is only used for docType.  docType now has the ability to update
 * attributes when onBlur is called on the docType text box.  In order to
 * stop the page from continually reloading we need to have a before and
 * after of the docTypeName.  Because hidden vars that are not listed in the
 * datadictionary are removed we have to use the current method.  This mothod
 * stores the current docTypeFullName on Page load.  the call is in page.tag.
 */
function storeCurrentDocTypeNameOnLoad() {
  var oldDocTypeField;
  var docTypeName = document.getElementById("documentTypeName");
  if (document.createElement) {
    oldDocTypeField = document.createElement("input");
    oldDocTypeField.setAttribute("type", "hidden");
    oldDocTypeField.setAttribute("name", "oldDocTypeFieldName");
    oldDocTypeField.setAttribute("value", docTypeName.value);
    document.forms[0].appendChild(oldDocTypeField);
  }
}

/**
 * This method performs an ajax call to the docTypeService to 1. check for a valid
 * docTypeName.  If the name is valid then the page is reposted with the new
 * docTypeName.  This allows for the populating of the attributes on the doc search.
 *
 */
function validateDocTypeAndRefresh(docTypeNameField) {
  if (!document.forms[0].oldDocTypeFieldName) {
    return;
  }

  var docTypeName = dwr.util.getValue(docTypeNameField);
  var oldDocTypeName = document.forms[0].oldDocTypeFieldName.value;

  if (docTypeName != null && oldDocTypeName != docTypeName) {

    var dwrReply = {
      callback: function (data) {
        if (data != null && typeof data == 'object') {
          newField = document.createElement("input");
          newField.type = "hidden";
          newField.name = "documentTypeName";
          newField.value = data.name;


          var frm = document.forms[0];
          frm.methodToCall = 'post';
          frm.refreshCaller = 'docTypeLookupable';

          frm.submit();
        }
      },
      errorHandler: function (errorMessage) {
        window.status = errorMessage;
      }
    };
    DocumentTypeService.findByNameCaseInsensitive(docTypeName, dwrReply);
  }
}
