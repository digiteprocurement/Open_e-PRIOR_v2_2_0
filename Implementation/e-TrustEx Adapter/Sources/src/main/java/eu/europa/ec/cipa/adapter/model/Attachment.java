package eu.europa.ec.cipa.adapter.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Attachment implements Serializable {

	private static final long serialVersionUID = 1L;

	private String mimeType;
	private byte[] content;
	private String id;
	private String parrentId;
	private String parrentTypeCode;

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParrentId() {
		return parrentId;
	}

	public void setParrentId(String parrentId) {
		this.parrentId = parrentId;
	}

	public String getParrentTypeCode() {
		return parrentTypeCode;
	}

	public void setParrentTypeCode(String parrentTypeCode) {
		this.parrentTypeCode = parrentTypeCode;
	}
    @Override
    public String toString() {
        return new ToStringBuilder(this)
        .append("mimeType", mimeType)
        .append("id", id)
        .append("parrentId", parrentId)
        .append("parrentTypeCode", parrentTypeCode)

        .toString();

    }
}
