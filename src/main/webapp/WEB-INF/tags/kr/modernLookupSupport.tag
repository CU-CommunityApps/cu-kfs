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
<div id="lookupModal"></div>
<script type="text/javascript">
  /**
   * This block of javascript is the glue that brings together maintenance documents and the new lookup framework.
   * Using the data-* properties on the lookup input icons, we can send the appropriate data to the lookup
   * react components providing the same functionality as the old lookups but using a modal and the new pages
   * to create a faster and easier experience for the user
   */

  function documentPathToSelector(path) {
    return '[name="' + path + '"]'
  }

  function getElementWithSelector(selector) {
    const namedElement = $(documentPathToSelector(selector))
    if (namedElement.length > 0) {
      return namedElement
    } else {
      const idElement = document.getElementById(selector);
      if (idElement) {
        return $(idElement)
      } else {
        const namedDiv = $(documentPathToSelector(selector + '.div'));
        if (namedDiv.length > 0) {
          return namedDiv;
        } else {
          const idDiv = document.getElementById(selector + '.div');
          return $(idDiv);
        }
      }
    }
  }

  /**
   * Processes an attribute string for field conversions. Converts a string of data to a map where the keys are
   * the path of the document and the value is the key of the fieldName
   */
  function getParameterMap(paramString) {
    const parameterMap = {}
    if (paramString) {
      const params = paramString.split(',');
      params.forEach(function (param) {
        if (param.length > 0) {
          const paramParts = param.split(':')
          const documentPath = paramParts[0]
          const resultKey = paramParts[1]
          parameterMap[documentPath] = resultKey
        }
      });
    }
    return parameterMap
  }

  /**
   * Takes a map of selectors:formFieldName
   * returns: a map with keys being the formFieldName and values being the element value for the provided selector
   */
  function getDocumentValuesFromParamMap(paramMap) {
    const documentValues = {}
    for (const fieldName in paramMap) {
      const documentPath = paramMap[fieldName]
      const element = getElementWithSelector(documentPath)
      if (element && element.length > 0) {
        if (element.is('input') || element.is('select')) {
          documentValues[fieldName] = element.val()
        } else {
          documentValues[fieldName] = element.text()
        }

      }
    }
    return documentValues
  }

  /**
   * Takes a map of document path keys that are paired with formFieldNames. Using those document path keys we
   * create a selector to populate any relationships that can be inferred from the returned value of the lookup
   */
  function setDocumentValuesFromParamMap(paramMap, values) {
    for (const documentPath in paramMap) {
      const element = getElementWithSelector(documentPath)
      if (element && element.length > 0) {
        const resultKey = paramMap[documentPath]
        let value = values[resultKey]
        if (typeof (value) === 'object') {
          value = value.value
        }
        if (element.is('input') || element.is('select')) {
          element.val(value)
        } else {
          element.text(value)
        }
      }
    }
  }

  function propertiesFromButton(button) {
    const propertyMap = {
      // businessObjectName is the name of the business object we are looking up
      businessObjectName: 'data-business-object-name',

      // lookupParameters are the fields that should be populated as a result of chosen row in a lookup
      lookupParameters: 'data-lookup-parameters',

      // fieldConversions are fields to be pre-populated on a lookup if they are already filled out on the doc
      fieldConversions: 'data-field-conversions',

      // fieldPropertyName is the name of the input field associated with the clicked lookup
      fieldPropertyName: 'data-field-name',

      // readOnlyFields are fields to be read-only on a lookup
      readOnlyFields: 'data-read-only-fields',

      // staticLookupData contains static data to be passed to the lookup search criteria
      staticLookupData: 'data-static-lookup-field-data'
    }

    const buttonProps = {}
    const attributes = button.attributes
    for (const propName in propertyMap) {
      const attributeName = propertyMap[propName]
      if (attributes[attributeName]) {
        buttonProps[propName] = attributes[attributeName].value
      }
    }

    return buttonProps
  }

  const getNewLookupConfiguration = async (buttonProps, businessObjectNameParts, multipleReturn = false) => {
    const businessObjectName = businessObjectNameParts[businessObjectNameParts.length - 1];
    const staticLookupData = getParameterMap(buttonProps.staticLookupData);
    const conversionMap = getParameterMap(buttonProps.fieldConversions);
    const lookupQueryString = await storeFormData(conversionMap, multipleReturn);
    const staticQueryString = Object.keys(staticLookupData).map(key => {
      if (staticLookupData[key] && staticLookupData !== "") {
        return key + '=' + encodeURIComponent(staticLookupData[key]);
      }
      return null;
    }).filter(entry => entry !== null).join('&');
    return [businessObjectName, lookupQueryString, staticQueryString];
  }

  async function newLookupClicked(e) {
    e.preventDefault();
    const buttonProps = propertiesFromButton(e.target);
    const businessObjectNameParts = buttonProps.businessObjectName.split(".");

    if (businessObjectNameParts.length > 0) {
      const [businessObjectName, lookupQueryString, staticQueryString] = await getNewLookupConfiguration(buttonProps,
        businessObjectNameParts);
      // CU Customization: Use JSP substitution to fill in the first section of the URL.
      let lookupUrl = '/${ConfigProperties.app.context.name}/webapp/lookup/' + businessObjectName;
      if (lookupQueryString && lookupQueryString !== '') {
        lookupUrl += '?' + lookupQueryString;
      }
      if (staticQueryString && staticQueryString !== '') {
        lookupUrl += (lookupUrl.contains('?') ? '&' : '?') + staticQueryString;
      }

      location.assign(lookupUrl);
    }
    return false;
  }

  async function multipleReturnLookupClicked(e) {
    e.preventDefault();
    const buttonProps = propertiesFromButton(e.target);
    const businessObjectNameParts = buttonProps.businessObjectName.split(".");

    if (businessObjectNameParts.length > 0) {
      const [businessObjectName, lookupQueryString, staticQueryString] = await getNewLookupConfiguration(buttonProps,
        businessObjectNameParts, true);
      // CU Customization: Use JSP substitution to fill in the first section of the URL.
      let lookupUrl = '/${ConfigProperties.app.context.name}/webapp/lookup/' + businessObjectName;
      if (lookupQueryString && lookupQueryString !== '') {
        lookupUrl += '?' + lookupQueryString;
      }
      if (staticQueryString && staticQueryString !== '') {
        lookupUrl += (lookupUrl.contains('?') ? '&' : '?') + staticQueryString;
      }

      location.assign(lookupUrl);
    }
    return false;
  }

  const storeFormData = async (conversionMap, multipleReturn) => {
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
    let parentId;
    if (window.ReduxShim.store.getState()['pageHistory'].history.length > 0) {
      parentId =
        window.ReduxShim.store.getState()['pageHistory'].history[window.ReduxShim.store.getState()['pageHistory'].history.length - 1].parentId;
    }
    // CU Customization: Backport FINP-12199 change that adds 'formKey' to the param map.
    await window.ReduxShim.store.dispatch(window.ReduxShim.pageHistory.actions.pushHistory({
      parentId,
      title: document.getElementsByTagName('h1')[0].innerText.trim(),
      legacy: true,
      pathname: location.pathname,
      pageConfiguration: {
        formData,
        fieldMap: conversionMap,
        returnRequestParamMap: {
          docFormKey: 'docFormKey',
          docNum: 'docNum',
          formKey: 'formKey',
          methodToCall: 'refresh'
        },
        multipleReturn
      }
    }));
    return Object.keys(conversionMap).map(key => {
      if (formData[conversionMap[key]] && formData[conversionMap[key]] !== "") {
        return key + '=' + encodeURIComponent(formData[conversionMap[key]]);
      }
      return null;
    }).filter(entry => entry !== null).join('&');
  }

  const loadFormData = async (incomingData) => {
    const elements = document.forms[0].elements;

    // Some elements come from the backend on render, and we need them to persist that value
    // CU Customization: Backport FINP-12199 change that adds 'formKey' to the ignoreElements array.
    const ignoreElements = ['docFormKey', 'formKey'];
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
    const newLookups = document.querySelectorAll('[data-lookup-type="single"]')
    newLookups.forEach(function (lookupButton) {
      lookupButton.addEventListener('click', newLookupClicked);
    });

    const multipleReturnLookups = document.querySelectorAll('[data-lookup-type="multiple"]')
    multipleReturnLookups.forEach(function (lookupButton) {
      lookupButton.addEventListener('click', multipleReturnLookupClicked);
    });
  });

  document.addEventListener("ReduxStoreLoaded", async () => {
    const returnedData = window.ReduxShim.store.getState()['pageHistory'].returnedData
    if (returnedData) {
      await loadFormData(returnedData);
      await window.ReduxShim.store.dispatch(window.ReduxShim.pageHistory.actions.clearReturnedData());
      if (returnedData.multiValueLookupResults && returnedData.multiValueLookupResults !== '') {
        const submit = document.querySelector('[value="multivalue-lookup"]');
        if (submit) {
          submit.click();
        }
      }
    }
  })
</script>
