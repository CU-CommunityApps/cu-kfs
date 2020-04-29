@XmlSchema(
    namespace = ConcurXmlNamespaces.TRAVEL_REQUEST,
    xmlns = { @XmlNs(prefix = "", namespaceURI = ConcurXmlNamespaces.TRAVEL_REQUEST) },
    attributeFormDefault = XmlNsForm.QUALIFIED,
    elementFormDefault = XmlNsForm.QUALIFIED
)
package edu.cornell.kfs.concur.rest.request.xmlObjects;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;

import edu.cornell.kfs.concur.ConcurConstants.ConcurXmlNamespaces;
