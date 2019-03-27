package edu.cornell.kfs.rass.batch.service;

import java.util.List;

import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapper;

public interface RassService {

	List<RassXmlDocumentWrapper> readXML();

	boolean updateKFS(List<RassXmlDocumentWrapper> rassXmlDocumentWrappers);

}
