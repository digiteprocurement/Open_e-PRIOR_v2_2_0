package eu.europa.ec.cipa.adapter.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	private ByteArrayWrapper document;
	private List<Attachment> attachments = new ArrayList<Attachment>();
	private Party sender = new Party();
	private Party receiver = new Party();
	private String localName;
	private String nameSpace;
	private String documentId;
	private String typeCode;
	private String correlationId;

	public String getNameSpace() {
		return nameSpace;
	}

	public void setNameSpace(String nameSpace) {
		this.nameSpace = nameSpace;
	}

	public String getLocalName() {
		return localName;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}

	public ByteArrayWrapper getDocument() {
		return document;
	}

	public void setDocument(ByteArrayWrapper document) {
		this.document = document;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public Party getSender() {
		return sender;
	}

	public Party getReceiver() {
		return receiver;
	}

	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public String getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	};

    @Override
    public String toString() {
        return new ToStringBuilder(this)
        .append("sender", sender)
        .append("receiver", receiver)
        .append("localName", localName)
        .append("nameSpace", nameSpace)
        .append("documentId", documentId)
        .append("typeCode", typeCode)
        .append("correlationId", correlationId)
        .append("attachments", attachments)
        .toString();

    }
}
