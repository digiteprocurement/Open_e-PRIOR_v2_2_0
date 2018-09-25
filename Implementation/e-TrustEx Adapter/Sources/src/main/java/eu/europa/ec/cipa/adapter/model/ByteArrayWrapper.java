package eu.europa.ec.cipa.adapter.model;

import java.io.Serializable;

public class ByteArrayWrapper implements Serializable {

	private static final long serialVersionUID = 1L;
	private byte[] content;

	public ByteArrayWrapper(byte[] content) {
		this.content = content;
	}

	public byte[] getContent() {
		return content;
	}
}
