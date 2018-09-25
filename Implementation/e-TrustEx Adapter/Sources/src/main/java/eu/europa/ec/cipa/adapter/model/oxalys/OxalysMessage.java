package eu.europa.ec.cipa.adapter.model.oxalys;

import java.util.Date;

public class OxalysMessage {

	private String id;
	private String type;
	private String senderId;
	private String receiverId;
	private String messageId;
	private Date receiptionDate;
	private byte[] content;

	public OxalysMessage(String id, String type, String senderId,
			String receiverId, String messageId, Date receiptionDate,
			byte[] content) {
		super();
		this.id = id;
		this.type = type;
		this.senderId = senderId;
		this.receiverId = receiverId;
		this.messageId = messageId;
		this.receiptionDate = receiptionDate;
		this.content = content;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public String getReceiverId() {
		return receiverId;
	}

	public void setReceiverId(String receiverId) {
		this.receiverId = receiverId;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public Date getReceiptionDate() {
		return receiptionDate;
	}

	public void setReceiptionDate(Date receiptionDate) {
		this.receiptionDate = receiptionDate;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

}
