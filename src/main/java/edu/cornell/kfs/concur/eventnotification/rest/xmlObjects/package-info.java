@XmlSchema(
    namespace = ConcurXmlNamespaces.NOTIFICATION,
    xmlns = { @XmlNs(prefix = "", namespaceURI = ConcurXmlNamespaces.NOTIFICATION) },
    attributeFormDefault = XmlNsForm.QUALIFIED,
    elementFormDefault = XmlNsForm.QUALIFIED
)
package edu.cornell.kfs.concur.eventnotification.rest.xmlObjects;

import jakarta.xml.bind.annotation.XmlNs;
import jakarta.xml.bind.annotation.XmlNsForm;
import jakarta.xml.bind.annotation.XmlSchema;

import edu.cornell.kfs.concur.ConcurConstants.ConcurXmlNamespaces;
