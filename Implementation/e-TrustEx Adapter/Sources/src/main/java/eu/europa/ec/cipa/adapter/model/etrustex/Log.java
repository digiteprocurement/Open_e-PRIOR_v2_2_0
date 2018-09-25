package eu.europa.ec.cipa.adapter.model.etrustex;

public class Log {

	
	private static final int MAX_VALUE_SIZE = 255;
	private static final int MAX_DESCRIPTION_SIZE = 2000;
	
	public static final String TYPE_INFO  = "INFO";
	public static final String TYPE_ERROR = "ERROR";
	
	public static final String OPERATION_VALIDATE = "VALIDATE";
	public static final String OPERATION_RESOLVE_PARTIES = "RESOLVE_PARTIES";
	public static final String OPERATION_DETACH_ATTACHMENT = "DETACH_ATTACHMENT";
	public static final String OPERATION_SEND_DOCUMENT = "SEND_DOCUMENT";
	public static final String OPERATION_SEND_ATTACHMENT = "SEND_ATTACHMENT";
	public static final String OPERATION_GET_TASKS = "GET_TASKS";
	public static final String OPERATION_SEND_ATT_WRAPPER = "SEND_ATT_WRAPPER";
	public static final String OPERATION_SEND_DOC_WRAPPER = "SEND_DOC_WRAPPER";
	public static final String OPERATION_SEND_DOC_XML_WRAPPER = "SEND_DOC_XML_WRAPPER";
	public static final String OPERATION_SEND_BUNDLE = "SEND_BUNDLE";
	
	public static final String MODULE_ADAPTER = "AS2ADAPTER";
	public static final String MODULE_ADMIN   = "AS2ADMIN";
		
	private Log() {

	}

	public static Log newLog() {
		return new Log();
	}

	private String type = TYPE_INFO;
	private String operation;
	private String description;
	private String value;
	private String correlation;
	private String documentId;
	private String typeCode;
	private String module = MODULE_ADAPTER;

	public Log setType(String type) {
		this.type = type;
		return this;
	}

	public Log setOperation(String operation) {
		this.operation = operation;
		return this;
	}

	public Log setDescription(String description) {
		this.description = description;
		if (this.description !=null && this.description.length()>MAX_DESCRIPTION_SIZE){
			this.description = this.description.substring(0,MAX_DESCRIPTION_SIZE-4)+"...";
		}
		return this;
	}

	public Log setValue(String value) {
		this.value = value;
		if (this.value !=null && this.value.length()>MAX_VALUE_SIZE){
			this.value = this.value.substring(0,MAX_VALUE_SIZE-4)+"...";
		}
		return this;
	}

	public Log setCorrelation(String correlation) {
		this.correlation = correlation;
		return this;
	}

	public Log setDocumentId(String documentId) {
		this.documentId = documentId;
		return this;
	}

	public Log setTypeCode(String typeCode) {
		this.typeCode = typeCode;
		return this;
	}

	public Log setModule(String module) {
		this.module = module;
		return this;
	}

	public String getType() {
		return type;
	}

	public String getOperation() {
		return operation;
	}

	public String getDescription() {
		return description;
	}

	public String getValue() {
		return value;
	}

	public String getCorrelation() {
		return correlation;
	}

	public String getDocumentId() {
		return documentId;
	}

	public String getTypeCode() {
		return typeCode;
	}

	public String getModule() {
		return module;
	}

}
