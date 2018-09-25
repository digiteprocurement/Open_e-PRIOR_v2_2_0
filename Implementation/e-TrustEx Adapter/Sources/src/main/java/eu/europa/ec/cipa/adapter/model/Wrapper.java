package eu.europa.ec.cipa.adapter.model;

import java.io.Serializable;


public class Wrapper implements Serializable {

	private static final long serialVersionUID = 1L;
	private String DocumentSize;
	private String DocumentHash;
	
	public String getDocumentSize() {
		return DocumentSize;
	}
	public void setDocumentSize(String documentSize) {
		DocumentSize = documentSize;
	}
	public String getDocumentHash() {
		return DocumentHash;
	}
	public void setDocumentHash(String documentHash) {
		DocumentHash = documentHash;
	}	
	

}
