package eu.europa.ec.cipa.adapter.services;

import static eu.europa.ec.cipa.adapter.utils.StaxUtils.isXMLExistsAndUnique;
import static eu.europa.ec.cipa.adapter.utils.StaxUtils.removeXmNode;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import eu.europa.ec.cipa.adapter.dao.db.EtrustexDBDAO;
import eu.europa.ec.cipa.adapter.dao.db.OxalysDBDAO;
import eu.europa.ec.cipa.adapter.dao.ws.AttachmentWSDAO;
import eu.europa.ec.cipa.adapter.dao.ws.CreditNoteWSDAO;
import eu.europa.ec.cipa.adapter.dao.ws.InvoiceWSDAO;
import eu.europa.ec.cipa.adapter.dao.ws.SendServicesEtrustExImpl;
import eu.europa.ec.cipa.adapter.model.Attachment;
import eu.europa.ec.cipa.adapter.model.ByteArrayWrapper;
import eu.europa.ec.cipa.adapter.model.Message;
import eu.europa.ec.cipa.adapter.model.Wrapper;
import eu.europa.ec.cipa.adapter.model.etrustex.Log;
import eu.europa.ec.cipa.adapter.model.etrustex.PartyIdScheme;
import eu.europa.ec.cipa.adapter.model.oxalys.OxalysMessage;
import eu.europa.ec.cipa.adapter.utils.RandomUtil;
import static eu.europa.ec.cipa.adapter.model.etrustex.Log.*; 

public class DocumentService {

	public static final String PROCESS_PARAM_ATT = "attachment";
	public static final String PROCESS_PARAM_ATT_COUNT = "attachmentsCount";
	public static final String PROCESS_PARAM_MESSAGE = "message";
	public static final String PROCESS_PARAM_SEND_DOC_FAILS_COUNT = "sendDocumentRetrialCount";
	public static final String PROCESS_PARAM_SEND_ATT_FAILS_COUNT = "sendAttachmentRetrialCount";
	public static final String PROCESS_PARAM_SEND_ATT_WRAPPER_FAILS_COUNT = "sendAttachmentWraperRetrialCount";
	public static final String PROCESS_PARAM_SEND_DOC_WRAPPER_FAILS_COUNT = "sendDocWraperRetrialCount";
	public static final String PROCESS_PARAM_SEND_DOC_XML_WRAPPER_FAILS_COUNT = "sendDocXmlWraperRetrialCount";
	public static final String PROCESS_PARAM_SEND_BUNDLE_FAILS_COUNT = "sendBundleRetrialCount";
	public static final String PROCESS_PARAM_OXA_ID = "OXA_ID";
	public static final String PROCESS_PARAM_SENDER_ID = "SENDER_ID";
	public static final String PROCESS_PARAM_RECEIVER_ID = "RECEIVER_ID";
	public static final String PROCESS_PARAM_ERROR_MESSAGE = "errorMessage";
	public static final String PROCESS_PARAM_PROCESSING_ERROR_DECISION = "processingErrorDecision";
	public static final String PROCESS_PARAM_SEND_ERROR_DECISION = "sendErrorDecision";
	public static final String PROCESS_PARAM_ATT_ERROR_DECISION = "attErrorDecision";
	public static final String PROCESS_PARAM_ATT_WRAPPER_ERROR_DECISION = "attWrapperErrorDecision";
	public static final String PROCESS_PARAM_SEND_DOC_WRAPPER_ERROR_DECISION = "sendDocWrapperErrorDecision";
	public static final String PROCESS_PARAM_SEND_DOC_XML_WRAPPER_ERROR_DECISION = "docXmlWrapperErrorDecision";
	public static final String PROCESS_PARAMBUNDLE_ERROR_DECISION = "bundleErrorDecision";
	
	public static final String PROCESS_PARAM_DOC_PDF_WRAPPER = "DoctPdfWrapper";
	public static final String PROCESS_PARAM_DOC_XML_WRAPPER = "DocXmlWrapper";

DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	public static enum ProcessingErrorDecisions {

		RETRY("retry"), CANCEL("cancel");

		ProcessingErrorDecisions(String value) {
			this.value = value;
		}

		private String value;

		public String getValue() {
			return value;
		}

	};

	public static enum AttErrorDecisions {

		RETRY("retry"), CANCEL("cancel");

		AttErrorDecisions(String value) {
			this.value = value;
		}

		private String value;

		public String getValue() {
			return value;
		}

	};

	public static enum SendErrorDecisions {

		RESTART("restart"), RETRY("retry"), CANCEL("cancel");

		SendErrorDecisions(String value) {
			this.value = value;
		}

		private String value;

		public String getValue() {
			return value;
		}

	};

	private static final String XML_SEPARATOR = "::";
	private static final String XML_NAMESPACE_EMBEDDED_ATT = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";
	private static final String XML_LOCALNAME_EMBEDDED_ATT = "EmbeddedDocumentBinaryObject";
	private static final String XML_NAMESPACE_INVOICE = "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2";
	public static final String XML_LOCALNAME_INVOICE = "Invoice";
	private static final String XML_CODE_INVOICE = "380";
	private static final String XML_NAMESPACE_CREDIT_NOTE = "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2";
	public static final String XML_LOCALNAME_CREDIT_NOTE = "CreditNote";
	private static final String XML_CODE_CREDIT_NOTE = "81";

	private static final String PARTY_ID_SEPARATOR = ":";

	private static Logger logger = Logger.getLogger(DocumentService.class);

	@Autowired
	private OxalysDBDAO oxalysDBDAO;
	@Autowired
	private EtrustexDBDAO etrustexDBDAO;
	@Autowired
	private InvoiceWSDAO invoiceWSDAO;
	@Autowired
	private CreditNoteWSDAO creditNoteWSDAO;
	@Autowired
	private AttachmentWSDAO attachmentWSDAO;
	@Autowired
	private TaskService taskService;
	@Autowired
	private SendServicesEtrustExImpl sendServicesEtrustExImpl;	
	
	public void validate(DelegateExecution execution) throws Exception {
		String uuid = null;
		String id = null;
		try {
			uuid = RandomUtil.generateUUID();
			String log = "Start validation";
			logger.debug(uuid + ": " + log);
			id = (String) execution.getVariable(PROCESS_PARAM_OXA_ID);
			
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_VALIDATE).setDescription(log).setCorrelation(uuid).setDocumentId(id));
					
			log = "Oxalys Message id:" + id;
			logger.debug(uuid + ": " + log);
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_VALIDATE).setDescription(log).setCorrelation(uuid));

			OxalysMessage msg = oxalysDBDAO.findMessageReceivedById(id);

			Message message = new Message();
			message.setDocument(new ByteArrayWrapper(msg.getContent()));
			message.setCorrelationId(uuid);

			String type = msg.getType();
			log = id + "Oxalys Message type:" + type;
			logger.debug(uuid + ": " + log);
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_VALIDATE).setDescription(log).setCorrelation(uuid));
			// check if it is a known type
			boolean isInvoice = false;
			boolean isCreditnote = false;
			
			isInvoice = (type.indexOf(XML_NAMESPACE_INVOICE + XML_SEPARATOR + XML_LOCALNAME_INVOICE) > -1);
			isCreditnote = (type.indexOf(XML_NAMESPACE_CREDIT_NOTE + XML_SEPARATOR	+ XML_LOCALNAME_CREDIT_NOTE) > -1);
			
			if (type == null
					|| (isInvoice && isCreditnote)) {
				log = "Unknown type";
				RuntimeException re = new RuntimeException(log);
				logger.error(re.getMessage());
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_VALIDATE).setDescription(log).setCorrelation(uuid).setTypeCode(TYPE_ERROR));
				throw re;
			}
			
			// check if there is one and only one invoice (with the expected NS)
			if (isInvoice) {
				logger.debug("processing an Invoice...");
				boolean exists = isXMLExistsAndUnique(XML_LOCALNAME_INVOICE,
						XML_NAMESPACE_INVOICE, msg.getContent());
				if (!exists) {
					log = "Cannot find a unique invoice in the provided message";
					RuntimeException re = new RuntimeException(log);
					logger.error(re.getMessage());
					oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_VALIDATE).setDescription(log).setCorrelation(uuid).setTypeCode(TYPE_ERROR));
					throw re;
				}
				message.setLocalName(XML_LOCALNAME_INVOICE);
				message.setNameSpace(XML_NAMESPACE_INVOICE);
				message.setTypeCode(XML_CODE_INVOICE);
				log = "Message type code";
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_VALIDATE).setDescription(log).setCorrelation(uuid).setTypeCode(XML_CODE_INVOICE));
			} else if (isCreditnote) {
				// check if there is one and only one credit note (with the
				// expected NS)
				logger.debug("processing a CreditNote...");
				boolean exists = isXMLExistsAndUnique(
						XML_LOCALNAME_CREDIT_NOTE, XML_NAMESPACE_CREDIT_NOTE,
						msg.getContent());
				if (!exists) {
					log = "Cannot find a unique credit note in the provided message";
					RuntimeException re = new RuntimeException(log);
					logger.error(re.getMessage());
					oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_VALIDATE).setDescription(log).setCorrelation(uuid).setTypeCode(TYPE_ERROR));
					throw re;
				}
				message.setLocalName(XML_LOCALNAME_CREDIT_NOTE);
				message.setNameSpace(XML_NAMESPACE_CREDIT_NOTE);
				message.setTypeCode(XML_CODE_CREDIT_NOTE);
				log = "Message type code";
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_VALIDATE).setDescription(log).setCorrelation(uuid).setTypeCode(XML_CODE_CREDIT_NOTE));
			}

			execution.setVariable(PROCESS_PARAM_MESSAGE, message);
			log= "Stop validation";
			logger.debug(uuid + ": " + log);
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_VALIDATE).setDescription(log).setCorrelation(uuid));
			
		} catch (Exception e) {
			String error = e.getMessage() + ", details: "
					+ ExceptionUtils.getStackTrace(e);
			execution.setVariable(PROCESS_PARAM_ERROR_MESSAGE, error);
			String log = "Error occured during validation " + error;
			logger.error(log);
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_VALIDATE).setDescription(log).setCorrelation(uuid).setTypeCode(TYPE_ERROR));
			throw new BpmnError("validationError",
					"An error has occured during the validation task, uuid" + uuid + " : "
							+ e.getMessage());
		}

	}

	public void resolveParties(DelegateExecution execution) throws Exception {
		String uuid = null;
		String log  = null;
		String id = null;
		try {
			log  = "Start Resolve parties"; 
			logger.debug(log);
			
			Message message = (Message) execution
					.getVariable(PROCESS_PARAM_MESSAGE);
			
			uuid = message.getCorrelationId();
			
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_RESOLVE_PARTIES).setDescription(log).setCorrelation(uuid));
			
			String senderId = (String) execution
					.getVariable(PROCESS_PARAM_SENDER_ID);
			log="Sender id:" + senderId;
			logger.debug(uuid + ": " + log);
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_RESOLVE_PARTIES).setDescription(log).setCorrelation(uuid));
			
			String receiverId = (String) execution
					.getVariable(PROCESS_PARAM_RECEIVER_ID);
			log = "Receiver id:" + receiverId;
			logger.debug(uuid + ": " + log);
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_RESOLVE_PARTIES).setDescription(log).setCorrelation(uuid));
			

			if (senderId == null){
				log = "Sender ID cannot be null";
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_RESOLVE_PARTIES).setDescription(log).setCorrelation(uuid).setType(TYPE_ERROR));
				throw new RuntimeException(log);
			}
			
			if (receiverId == null){
				log = "Receiver ID cannot be null";
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_RESOLVE_PARTIES).setDescription(log).setCorrelation(uuid).setType(TYPE_ERROR));
				throw new RuntimeException(log);
			}
			
			String[] strArr = senderId.split(PARTY_ID_SEPARATOR);

			if (strArr.length != 2){
				log = "Invalid Sender ID format";
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_RESOLVE_PARTIES).setDescription(log).setCorrelation(uuid).setType(TYPE_ERROR));
				throw new RuntimeException(log);
			}
			
			PartyIdScheme scheme = etrustexDBDAO
					.findPartyIdSchemeByIsoCode(uuid, strArr[0]);

			if (scheme == null){
				log = "Unknown scheme for sender";
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_RESOLVE_PARTIES).setDescription(log).setCorrelation(uuid).setType(TYPE_ERROR));
				throw new RuntimeException(log);
			}
			
			log = "Sender scheme:" + scheme.getSchemeId();
			
			logger.debug(uuid + ": " + log);
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_RESOLVE_PARTIES).setDescription(log).setCorrelation(uuid));

			boolean partyExists = etrustexDBDAO.isPartyExist(uuid, strArr[1],
					scheme.getSchemeId());

			if (!partyExists) {
				log = "Sender does not exist in eTrustex";
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_RESOLVE_PARTIES).setDescription(log).setCorrelation(uuid).setType(TYPE_ERROR));
				throw new RuntimeException(log);
			}
			
			log = "Sender exists in eTrustex";			
			logger.debug(uuid + ": " + log);
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_RESOLVE_PARTIES).setDescription(log).setCorrelation(uuid));

			message.getSender().setIdScheme(scheme.getSchemeId());
			message.getSender().setIdValue(strArr[1]);

			strArr = receiverId.split(PARTY_ID_SEPARATOR);

			if (strArr.length != 2){
				log ="Invalid Receiver ID format";
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_RESOLVE_PARTIES).setDescription(log).setCorrelation(uuid).setType(TYPE_ERROR));
				throw new RuntimeException(log);
			}
			
			scheme = etrustexDBDAO.findPartyIdSchemeByIsoCode(uuid, strArr[0]);

			if (scheme == null){
				log ="Unknown scheme for receiver";
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_RESOLVE_PARTIES).setDescription(log).setCorrelation(uuid).setType(TYPE_ERROR));
				throw new RuntimeException(log);
			}
			
			log = "Receiver scheme:" + scheme.getSchemeId();			
			logger.debug(uuid + ": " + log);
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_RESOLVE_PARTIES).setDescription(log).setCorrelation(uuid));

			partyExists = etrustexDBDAO.isPartyExist(uuid, strArr[1],
					scheme.getSchemeId());

			if (!partyExists) {
				log ="Receiver does not exist in eTrustex";
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_RESOLVE_PARTIES).setDescription(log).setCorrelation(uuid).setType(TYPE_ERROR));
				throw new RuntimeException(log);
			}
						
			log = "Receiver exists in eTrustex";			
			logger.debug(uuid + ": " + log);
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_RESOLVE_PARTIES).setDescription(log).setCorrelation(uuid));

			message.getReceiver().setIdScheme(scheme.getSchemeId());
			message.getReceiver().setIdValue(strArr[1]);

			log ="Stop Resolve parties";			
			logger.debug(uuid + ": " + log);
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_RESOLVE_PARTIES).setDescription(log).setCorrelation(uuid));
			
		} catch (Exception e) {
			String error = "An error has occured during the parties resolution task, uuid " + uuid + ": "
					+ e.getMessage();
			execution.setVariable(PROCESS_PARAM_ERROR_MESSAGE, error);
			logger.error(error + ", details:" + ExceptionUtils.getStackTrace(e));
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_RESOLVE_PARTIES).setDescription(error).setCorrelation(uuid).setType(TYPE_ERROR));
			throw new BpmnError("validationError", error);
		}

	}

	public void detachAttachments(DelegateExecution execution) throws Exception {
		String uuid = null;
		try {
			String log = "Start Detach attachments";
			logger.debug(log);		
			
			Message message = (Message) execution
					.getVariable(PROCESS_PARAM_MESSAGE);
			uuid = message.getCorrelationId();		
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_DETACH_ATTACHMENT).setDescription(log).setCorrelation(uuid));
			
			ByteArrayWrapper wrapper = removeXmNode(uuid, XML_LOCALNAME_EMBEDDED_ATT,
					XML_NAMESPACE_EMBEDDED_ATT, message.getDocument(),
					message.getAttachments());
			message.setDocument(wrapper);

			execution.setVariable(PROCESS_PARAM_ATT_COUNT, new Long(message
					.getAttachments().size()));
			
			log = "Stop Detach Attachments";
			logger.debug(uuid + ": " + log);
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_DETACH_ATTACHMENT).setDescription(log).setCorrelation(uuid));
		} catch (Exception e) {
			String error = "An error has occured during the detach attachments task, uuid " + uuid + ": "
					+ e.getMessage();
			execution.setVariable(PROCESS_PARAM_ERROR_MESSAGE, error);
			logger.error(error + ", details: "
					+ ExceptionUtils.getStackTrace(e));
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_DETACH_ATTACHMENT).setDescription(error).setCorrelation(uuid).setType(TYPE_ERROR));
			throw new BpmnError("validationError", error);
		}
	}

	private void updateAttachment(Message message) {
		// set parent information
		for (Attachment att : message.getAttachments()) {
			att.setParrentId(message.getDocumentId());
			att.setParrentTypeCode(message.getTypeCode());
			att.setId(att.getParrentId() + "_att" + att.getId());

		}
	}

	public void sendDocument(DelegateExecution execution) throws Exception {
		String log = null;
		String uuid = null;
		String id = null;
		
		try {
			log = "Start Send document";
			logger.debug(log);
			Message message = (Message) execution
					.getVariable(PROCESS_PARAM_MESSAGE);
			uuid = message.getCorrelationId();
			id = message.getDocumentId();
			logger.debug(uuid + ": Send document, parameters" + message.toString());

			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_DOCUMENT).setDescription(log).setCorrelation(uuid).setDocumentId(id).setValue(sdf.format(new Date())));
			
			switch (message.getLocalName()) {
			case XML_LOCALNAME_INVOICE:
				invoiceWSDAO.sendDocument(uuid, message, message.getSender(),
						message.getReceiver());
				break;
			case XML_LOCALNAME_CREDIT_NOTE:
				creditNoteWSDAO.sendDocument(uuid, message, message.getSender(),
						message.getReceiver());
				break;
			default:
				log = "Unsupported document type!";
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_DOCUMENT).setDescription(log).setCorrelation(uuid).setType(TYPE_ERROR).setDocumentId(id));
				throw new RuntimeException(log);// should
																			// never
																			// happen
			}
			updateAttachment(message);
			

			log = "Stop Send document";
			logger.debug(uuid + ": " + log);
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_DOCUMENT).setDescription(log).setCorrelation(uuid).setDocumentId(id).setValue(sdf.format(new Date())));
			
		} catch (Exception e) {
			String error = "An error has occured during the document sending task, uuid " + uuid + ": "
					+ e.getMessage();
			execution.setVariable(PROCESS_PARAM_ERROR_MESSAGE, error);
			logger.error(uuid + ": " + error + ", details: "
					+ ExceptionUtils.getStackTrace(e));
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_DOCUMENT).setDescription(error).setCorrelation(uuid).setType(TYPE_ERROR).setDocumentId(id).setValue(sdf.format(new Date())));
			try {

				Object countParam = execution
						.getVariable(PROCESS_PARAM_SEND_DOC_FAILS_COUNT);
				Long count = 0l;
				if (countParam != null) {
					count = (Long) countParam;
				}
				execution.setVariable(PROCESS_PARAM_SEND_DOC_FAILS_COUNT,
						new Long(count + 1));
				log = "Increased retry send document count from "
						+ count + " to " + (count + 1);
				logger.error(uuid + ": " + log);
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_DOCUMENT).setDescription(log).setCorrelation(uuid).setType(TYPE_ERROR).setDocumentId(id).setValue(sdf.format(new Date())));

			} catch (Exception ie) {
				String internalError = "An error has occured in the error managegement of send document ! Details:"
						+ ExceptionUtils.getStackTrace(ie);
				logger.error(uuid + ": " + internalError);
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_DOCUMENT).setDescription(internalError).setCorrelation(uuid).setType(TYPE_ERROR).setDocumentId(id).setValue(sdf.format(new Date())));
				execution.setVariable(PROCESS_PARAM_ERROR_MESSAGE,
						internalError);
			}
			throw new BpmnError("sendDocumentError", error);
		}
	}

	public void sendAttachment(DelegateExecution execution) throws Exception {
		String uuid = null;
		String log = null;
		String id = null;
		try {
			log = "Start Send document";
			logger.debug(log);			
			Message message = (Message) execution
					.getVariable(PROCESS_PARAM_MESSAGE);
			uuid = message.getCorrelationId();
			Attachment att = (Attachment) execution
					.getVariable(PROCESS_PARAM_ATT);
			id = att.getId();
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_ATTACHMENT).setDescription(log).setCorrelation(uuid).setDocumentId(id).setValue(sdf.format(new Date())));
			

			attachmentWSDAO.sendDocument(uuid, att, message.getSender(),
					message.getReceiver());
			
			log = "Stop Send document";
			logger.debug(uuid + ": " + log);
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_ATTACHMENT).setDescription(log).setCorrelation(uuid).setDocumentId(id).setValue(sdf.format(new Date())));
		} catch (Exception e) {
			String error = "An error has occured during the attachment sending task, uuid " + uuid + ":"
					+ e.getMessage();
			execution.setVariable(PROCESS_PARAM_ERROR_MESSAGE, error
					+ ", details: " + ExceptionUtils.getStackTrace(e));
			logger.error(uuid + ": " + error + ", details: "
					+ ExceptionUtils.getStackTrace(e));
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_ATTACHMENT).setDescription(error).setCorrelation(uuid).setType(TYPE_ERROR).setDocumentId(id).setValue(sdf.format(new Date())));
			try {

				Object countParam = execution
						.getVariable(PROCESS_PARAM_SEND_ATT_FAILS_COUNT);
				Long count = 0l;
				if (countParam != null) {
					count = (Long) countParam;
				}
				execution.setVariable(PROCESS_PARAM_SEND_ATT_FAILS_COUNT,
						new Long(count + 1));
				log = "Increased retry send document count from "
						+ count + " to " + (count + 1);				
				logger.error(uuid + ": " + log);
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_ATTACHMENT).setDescription(log).setCorrelation(uuid).setType(TYPE_ERROR).setDocumentId(id).setValue(sdf.format(new Date())));
			} catch (Exception ie) {
				String internalError = "An error has occured in the error managegement of send attachment ! Details:"
						+ ExceptionUtils.getStackTrace(ie);
				logger.error(uuid + ": " + internalError);
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_ATTACHMENT).setDescription(internalError).setCorrelation(uuid).setType(TYPE_ERROR).setDocumentId(id).setValue(sdf.format(new Date())));
				execution.setVariable(PROCESS_PARAM_ERROR_MESSAGE,
						internalError);
			}
			throw new BpmnError("sendAttachmentError", error);
		}

	}

	public List<Task> getPendingTasks() {
		String log = "Start get pending tasks";
		logger.debug(log);
		//oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_GET_TASKS).setDescription(log).setModule(MODULE_ADAPTER));
		
		TaskQuery query = taskService.createTaskQuery().orderByExecutionId()
				.desc().active();
		List<Task> tasks = query.list();
		
		log = "Stop get pending tasks";
		logger.debug(log);
		//oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_GET_TASKS).setDescription(log).setModule(MODULE_ADAPTER));
		
		return tasks;
	}
	
	
	public void sendAttWrapper(DelegateExecution execution) throws Exception {
		String uuid = null;
		String log = null;
		String id = null;
		try {

			log = "Start Send att wrapper";
			logger.debug(log);			
			Message message = (Message) execution
					.getVariable(PROCESS_PARAM_MESSAGE);
			uuid = message.getCorrelationId();
			Attachment att = (Attachment) execution
					.getVariable(PROCESS_PARAM_ATT);
			id = att.getId();
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_ATT_WRAPPER).setDescription(log).setCorrelation(uuid).setDocumentId(id));
			

			sendServicesEtrustExImpl.sendBinary(uuid, att, message.getSender(),
					message.getReceiver());
			
			log = "Stop Send att wrapper";
			logger.debug(uuid + ": " + log);
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_ATT_WRAPPER).setDescription(log).setCorrelation(uuid).setDocumentId(id));
		} catch (Exception e) {
			String error = "An error has occured during the attachment wrapper sending task, uuid " + uuid + ": "
					+ e.getMessage();
			execution.setVariable(PROCESS_PARAM_ERROR_MESSAGE, error
					+ ", details: " + ExceptionUtils.getStackTrace(e));
			logger.error(uuid + ": " + error + ", details: "
					+ ExceptionUtils.getStackTrace(e));
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_ATT_WRAPPER).setDescription(error).setCorrelation(uuid).setType(TYPE_ERROR).setDocumentId(id));
			
			
			try {

				Object countParam = execution
						.getVariable(PROCESS_PARAM_SEND_ATT_WRAPPER_FAILS_COUNT);
				Long count = 0l;
				if (countParam != null) {
					count = (Long) countParam;
				}
				execution.setVariable(PROCESS_PARAM_SEND_ATT_WRAPPER_FAILS_COUNT,
						new Long(count + 1));
				log = "Increased retry send att document wrapper count from "
						+ count + " to " + (count + 1);				
				logger.error(uuid + ": " + log);
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_ATT_WRAPPER).setDescription(log).setCorrelation(uuid).setType(TYPE_ERROR).setDocumentId(id));
			} catch (Exception ie) {
				String internalError = "An error has occured in the error managegement of send attachment wrapper! Details:"
						+ ExceptionUtils.getStackTrace(ie);
				logger.error(uuid + ": " + internalError);
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_ATT_WRAPPER).setDescription(internalError).setCorrelation(uuid).setType(TYPE_ERROR).setDocumentId(id));
				execution.setVariable(PROCESS_PARAM_ERROR_MESSAGE,
						internalError);
			}
			throw new BpmnError("sendAttachmentWrapperError", error);
		}


	}

	public void sendDoctWrapper(DelegateExecution execution) throws Exception {
		String uuid = null;
		String log = null;
		String id = null;
		try {
			
			log = "Start Send doc wrapper";
			logger.debug(log);			
			Message message = (Message) execution
					.getVariable(PROCESS_PARAM_MESSAGE);
			uuid = message.getCorrelationId();
			id = message.getDocumentId();
			
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_DOC_WRAPPER).setDescription(log).setCorrelation(uuid).setDocumentId(id));
			
			Attachment att = sendServicesEtrustExImpl.sendHTTPSOAPMessageView(message);
			
			log = "Receive doc view";
			logger.debug(uuid + ": " + log);			
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_DOC_WRAPPER).setDescription(log).setCorrelation(uuid).setDocumentId(id));
			
			if(att != null){
				sendServicesEtrustExImpl.sendBinary(uuid, att, message.getSender(),
					message.getReceiver());
				
				execution.setVariable(PROCESS_PARAM_DOC_PDF_WRAPPER, sendServicesEtrustExImpl.getHash(att));
			}else{
				log = "Doc view empty";
				logger.debug(uuid + ": " + log);			
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_DOC_WRAPPER).setDescription(log).setCorrelation(uuid).setDocumentId(id));

			}
			
			
			
			log = "Stop Send doc wrapper";
			logger.debug(uuid + ": " + log);
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_DOC_WRAPPER).setDescription(log).setCorrelation(uuid).setDocumentId(id));
		} catch (Exception e) {
			String error = "An error has occured during the doc wrapper sending task, uuid " + uuid + ": "
					+ e.getMessage();
			execution.setVariable(PROCESS_PARAM_ERROR_MESSAGE, error
					+ ", details: " + ExceptionUtils.getStackTrace(e));
			logger.error(uuid + ": " + error + ", details: "
					+ ExceptionUtils.getStackTrace(e));
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_DOC_WRAPPER).setDescription(error).setCorrelation(uuid).setType(TYPE_ERROR).setDocumentId(id));
			
			
			try {

				Object countParam = execution
						.getVariable(PROCESS_PARAM_SEND_DOC_WRAPPER_FAILS_COUNT);
				Long count = 0l;
				if (countParam != null) {
					count = (Long) countParam;
				}
				execution.setVariable(PROCESS_PARAM_SEND_DOC_WRAPPER_FAILS_COUNT,
						new Long(count + 1));
				log = "Increased retry send document wrapper count from "
						+ count + " to " + (count + 1);				
				logger.error(uuid + ": " + log);
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_DOC_WRAPPER).setDescription(log).setCorrelation(uuid).setType(TYPE_ERROR).setDocumentId(id));
			} catch (Exception ie) {
				String internalError = "An error has occured in the error managegement of send doc wrapper! Details:"
						+ ExceptionUtils.getStackTrace(ie);
				logger.error(uuid + ": " + internalError);
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_DOC_WRAPPER).setDescription(internalError).setCorrelation(uuid).setType(TYPE_ERROR).setDocumentId(id));
				execution.setVariable(PROCESS_PARAM_ERROR_MESSAGE,
						internalError);
			}
			throw new BpmnError("sendDocWrapperError", error);
		}


	}
	

	public void sendDoctXmlWrapper(DelegateExecution execution) throws Exception {
		String uuid = null;
		String log = null;
		String id = null;
		try {
			
			log = "Start Send doc xml wrapper";
			logger.debug(log);			
			Message message = (Message) execution
					.getVariable(PROCESS_PARAM_MESSAGE);
			uuid = message.getCorrelationId();
			id = message.getDocumentId();
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_DOC_XML_WRAPPER).setDescription(log).setCorrelation(uuid).setDocumentId(id));
			
			Attachment att = sendServicesEtrustExImpl.generateDoctXmlRequest(message);
			sendServicesEtrustExImpl.sendBinary(uuid, att, message.getSender(),
				message.getReceiver());
			
			execution.setVariable(PROCESS_PARAM_DOC_XML_WRAPPER, sendServicesEtrustExImpl.getHash(att));
			
			log = "Stop Send doc xml wrapper";
			logger.debug(uuid + ": " + log);
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_DOC_XML_WRAPPER).setDescription(log).setCorrelation(uuid).setDocumentId(id));
		} catch (Exception e) {
			String error = "An error has occured during the doc xml wrapper sending task, uuid " + uuid + ": "
					+ e.getMessage();
			execution.setVariable(PROCESS_PARAM_ERROR_MESSAGE, error
					+ ", details: " + ExceptionUtils.getStackTrace(e));
			logger.error(uuid + ": " + error + ", details: "
					+ ExceptionUtils.getStackTrace(e));
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_DOC_XML_WRAPPER).setDescription(error).setCorrelation(uuid).setType(TYPE_ERROR).setDocumentId(id));
			
			
			try {

				Object countParam = execution
						.getVariable(PROCESS_PARAM_SEND_DOC_XML_WRAPPER_FAILS_COUNT);
				Long count = 0l;
				if (countParam != null) {
					count = (Long) countParam;
				}
				execution.setVariable(PROCESS_PARAM_SEND_DOC_XML_WRAPPER_FAILS_COUNT,
						new Long(count + 1));
				log = "Increased retry send document xml wrapper count from "
						+ count + " to " + (count + 1);				
				logger.error(uuid + ": " + log);
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_DOC_XML_WRAPPER).setDescription(log).setCorrelation(uuid).setType(TYPE_ERROR).setDocumentId(id));
			} catch (Exception ie) {
				String internalError = "An error has occured in the error managegement of send doc xml wrapper! Details:"
						+ ExceptionUtils.getStackTrace(ie);
				logger.error(uuid + ": " + internalError);
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_DOC_XML_WRAPPER).setDescription(internalError).setCorrelation(uuid).setType(TYPE_ERROR).setDocumentId(id));
				execution.setVariable(PROCESS_PARAM_ERROR_MESSAGE,
						internalError);
			}
			throw new BpmnError("sendDocXmlWrapperError", error);
		}


	}

	public void sendBundle(DelegateExecution execution) throws Exception {
		String log = null;
		String uuid = null;
		String id = null;
		
		try {
			log = "Start Send bundle";
			
			logger.debug(log);
			Message message = (Message) execution
					.getVariable(PROCESS_PARAM_MESSAGE);
			uuid = message.getCorrelationId();
			id = message.getDocumentId();
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_BUNDLE).setDescription(log).setCorrelation(uuid).setDocumentId(id));
			
			Wrapper wrapperPdf = (Wrapper) execution
					.getVariable(PROCESS_PARAM_DOC_PDF_WRAPPER);
			Wrapper wrapperXml = (Wrapper) execution
					.getVariable(PROCESS_PARAM_DOC_XML_WRAPPER);

			
			sendServicesEtrustExImpl.sendBundleXML(message, wrapperPdf, wrapperXml);

			log = "Stop Send bundle";
			logger.debug(uuid + ": " + log);
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_BUNDLE).setDescription(log).setCorrelation(uuid).setDocumentId(id));
			
		} catch (Exception e) {
			String error = "An error has occured during the bundle sending task, uuid " + uuid + ": "
					+ e.getMessage();
			execution.setVariable(PROCESS_PARAM_ERROR_MESSAGE, error);
			logger.error(uuid + ": " + error + ", details: "
					+ ExceptionUtils.getStackTrace(e));
			oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_BUNDLE).setDescription(error).setCorrelation(uuid).setType(TYPE_ERROR).setDocumentId(id));
			try {

				Object countParam = execution
						.getVariable(PROCESS_PARAM_SEND_BUNDLE_FAILS_COUNT);
				Long count = 0l;
				if (countParam != null) {
					count = (Long) countParam;
				}
				execution.setVariable(PROCESS_PARAM_SEND_BUNDLE_FAILS_COUNT,
						new Long(count + 1));
				log = "Increased retry send bundle count from "
						+ count + " to " + (count + 1);
				logger.error(uuid + ": " + log);
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_BUNDLE).setDescription(log).setCorrelation(uuid).setType(TYPE_ERROR).setDocumentId(id));

			} catch (Exception ie) {
				String internalError = "An error has occured in the error managegement of send Bundle! Details:"
						+ ExceptionUtils.getStackTrace(ie);
				logger.error(uuid + ": " + internalError);
				oxalysDBDAO.log(Log.newLog().setOperation(OPERATION_SEND_BUNDLE).setDescription(internalError).setCorrelation(uuid).setType(TYPE_ERROR).setDocumentId(id));
				execution.setVariable(PROCESS_PARAM_ERROR_MESSAGE,
						internalError);
			}
			throw new BpmnError("sendBundleError", error);
		}
	}
	

}
