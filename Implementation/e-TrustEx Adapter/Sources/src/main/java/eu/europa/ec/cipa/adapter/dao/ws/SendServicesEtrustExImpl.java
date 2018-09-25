package eu.europa.ec.cipa.adapter.dao.ws;
import org.apache.log4j.Logger;
import eu.europa.ec.cipa.adapter.dao.exception.DAOException;
import eu.europa.ec.cipa.adapter.model.Attachment;
import eu.europa.ec.cipa.adapter.model.Message;
import eu.europa.ec.cipa.adapter.model.Party;
import eu.europa.ec.cipa.adapter.model.Wrapper;
import eu.europa.ec.cipa.adapter.services.DocumentService;
import eu.europa.ec.cipa.adapter.utils.MimeCodeUtil;

import org.apache.http.client.ClientProtocolException;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.soap.MimeHeaders;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SendServicesEtrustExImpl extends AbstractWSDAO {

    private static final Logger logger = Logger.getLogger(SendServicesEtrustExImpl.class);
    private final String CHUNK_SIZE = "CHUNK_SIZE";
    private final String ALREADY_EXIST_ERROR = "ALREADY_EXIST_ERROR";
    private final String URL_STORE_BINARY = "URL_STORE_BINARY_WEBSERIVCE";
    private final String USR_STORE_BINARY = "STORE_BINARY_WEBSERVICE_USR";
    private final String PWD_STORE_BINARY = "STORE_BINARY_WEBSERVICE_PWD";
    private final String URL_BUNDLE = "URL_BUNDLE_WEBSERIVCE";
    private final String USR_BUNDLE = "BUNDLE_WEBSERVICE_USR";
    private final String PWD_BUNDLE = "BUNDLE_WEBSERVICE_PWD";
    private final String URL_CREATE_PARTY = "URL_CREATE_PARTY_WEBSERVICE";
    private final String USR_CREATE_PARTY = "CREATE_PARTY_WEBSERVICE_USR";
    private final String PWD_CREATE_PARTY = "CREATE_PARTY_WEBSERVICE_PWD";
    private final String SENDER_CREATE_PARTY = "CREATE_PARTY_WEBSERVICE_SENDERID";
    private final String URL_CREATE_ICA = "URL_CREATE_ICA_WEBSERVICE";
    private final String USR_CREATE_ICA = "CREATE_ICA_WEBSERVICE_USR";
    private final String PWD_CREATE_ICA = "CREATE_ICA_WEBSERVICE_PWD";
    private final String SENDER_CREATE_ICA = "CREATE_ICA_WEBSERVICE_SENDERID";
    private final String EANALLSUPPLIERS = "EANALLSUPPLIERS";
    private final String URL_VIEW = "URL_VIEW_WEBSERIVCE";
    private final String VIEW_USR = "VIEW_WEBSERVICE_USR";
    private final String VIEW_PWD = "VIEW_WEBSERVICE_PWD";
    private final String ID_HUMAN_READIBLE = "_human_readable";
    private final String ID_XML = "_xml";
    private final String MIME_TYPE_PDF = "application/pdf";
    private final String MIME_TYPE_XML = "text/xml";
    
    private final String FILE_WRAPPER = "";

    String chunkSize = null;
    String alreadyExistError = null;
    String eanALL = null;

    String urlStoreBinary = null;
    String usernameStoreBinary = null;
    String passwordStoreBinary = null;

    String urlView = null;
    String usernameView = null;
    String passwordView = null;
    
    String urlParty = null;
    String usernameParty = null;
    String passwordParty = null;
    String senderIdParty = null;

    String urlICA = null;
    String usernameICA = null;
    String passwordICA = null;
    String senderIdICA = null;
    
    String urlBundle = null;
    String usernameBundle = null;
    String passwordBundle = null;
    
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    DateFormat dfTime = new SimpleDateFormat("HH:mm:ss.mmm");
    
    MimeCodeUtil mimeCodeUtil = null;

    private final String REQUEST_CONTENT_TYPE_PROPS_BINARY = "multipart/related; type=\"text/xml\"; start=\"<root>\"; boundary=\"uuid:b7a481a7-274a-42ed-8b84-9bb2280fb2e7\";";
    private final String REQUEST_CONTENT_TYPE_PROPS = "text/xml; charset=UTF-8;";

    private static String SOAP_DOC_WRAPPER = "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' xmlns:ec='ec:services:wsdl:DocumentWrapper-2' "
            + "xmlns:ec1='ec:schema:xsd:CommonAggregateComponents-2' xmlns:stan='http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader' "
            + "xmlns:urn='urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2' "
            + "xmlns:urn1='urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2'>"
            + "<soapenv:Header><ec:Header><ec1:BusinessHeader><stan:Sender><stan:Identifier  schemeID=\"__CALLER_SCHEMEID__\">__CALLER_ID__</stan:Identifier></stan:Sender></ec1:BusinessHeader></ec:Header>"
            + "</soapenv:Header><soapenv:Body><ec:StoreDocumentWrapperRequest><ec:DocumentWrapper><urn1:ID>__ID__</urn1:ID><urn1:IssueDate>__ISSUE_DATE__</urn1:IssueDate>"
            + "<urn1:DocumentTypeCode>__DOCUMENT_TYPE_CODE__</urn1:DocumentTypeCode><urn1:DocumentType>__DOCUMENT_TYPE__</urn1:DocumentType><urn:SenderParty/><ec1:ResourceInformationReference><ec1:DocumentSize>__DOCUMENT_SIZE__</ec1:DocumentSize><ec1:LargeAttachment>"
            + "<ec1:StreamBase64Binary mimeCode='__MIME_CODE__'>"
            + "<inc:Include href=\"cid:429872898333\" xmlns:inc=\"http://www.w3.org/2004/08/xop/include\"/>"
            + "</ec1:StreamBase64Binary></ec1:LargeAttachment></ec1:ResourceInformationReference>"
            + "</ec:DocumentWrapper></ec:StoreDocumentWrapperRequest></soapenv:Body></soapenv:Envelope>\n";
    
    final String payloadHeadersWrapper = "--uuid:b7a481a7-274a-42ed-8b84-9bb2280fb2e7\n"
            + "Content-Type: application/xop+xml; charset=UTF-8; type=\"text/xml\"\n"
            + "Content-Transfer-Encoding: binary\n"
            + "Content-ID: <root>\n\n";

    final String binaryHeaders = "\n--uuid:b7a481a7-274a-42ed-8b84-9bb2280fb2e7\n"
            + "Content-Type: application/octet-stream; name=__FILENAME__ \n"
            + "Content-Transfer-Encoding: binary\n"
            + "Content-ID: <429872898333>\n\n";

    private final String binaryEndPart = "\r\n--uuid:b7a481a7-274a-42ed-8b84-9bb2280fb2e7--\r\n";

    private static String SOAP_BUNDLE_REQUEST = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ec=\"ec:services:wsdl:DocumentBundle-2\" xmlns:ec1=\"ec:schema:xsd:CommonAggregateComponents-2\" xmlns:urn=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\" xmlns:stan=\"http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader\" xmlns:urn1=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\">"
    		+ "<soapenv:Header><ec:Header><ec1:BusinessHeader><stan:Sender><stan:Identifier schemeID=\"__SENDER_SCHEMEID__\">__SENDER_ID__</stan:Identifier></stan:Sender>"
    		+ "<stan:Receiver><stan:Identifier schemeID=\"__RECEIVER_SCHEMEID__\">__RECEIVER_ID__</stan:Identifier></stan:Receiver></ec1:BusinessHeader></ec:Header></soapenv:Header>"
    		+ "<soapenv:Body><ec:SubmitDocumentBundleRequest><ec:DocumentBundle><urn1:ProfileID>__PROFILE_ID__</urn1:ProfileID><urn1:ID>__ID__</urn1:ID><urn1:IssueDate>__ISSUE_DATE__</urn1:IssueDate>"
    		+ "<urn1:IssueTime>__ISSUE_TIME__</urn1:IssueTime>"
    		+ "<urn:SenderParty><urn1:EndpointID schemeID=\"__SENDER_SCHEMEID__\">__SENDER_ID__</urn1:EndpointID></urn:SenderParty>"
    		+ "<urn:ReceiverParty><urn1:EndpointID schemeID=\"__RECEIVER_SCHEMEID__\">__RECEIVER_ID__</urn1:EndpointID></urn:ReceiverParty>"            
    		+ "__DOCUMENT_WRAPPER__</ec:DocumentBundle></ec:SubmitDocumentBundleRequest></soapenv:Body></soapenv:Envelope>";

    private static String SOAP_BUNDLE_REQUEST_WRAPPER = "<ec1:DocumentWrapperReference><urn1:ID>__WRAPPER_ID__</urn1:ID><urn1:DocumentTypeCode>BINARY</urn1:DocumentTypeCode>"
    		+ "<ec1:ResourceInformationReference><urn1:Name>__FILE_NAME__</urn1:Name><ec1:DocumentSize>__FILE_SIZE__</ec1:DocumentSize><ec1:DocumentHashMethod>SHA-512</ec1:DocumentHashMethod>"
    		+ "<urn1:DocumentHash>__FILE_HASH__</urn1:DocumentHash></ec1:ResourceInformationReference></ec1:DocumentWrapperReference>";
    
    private static String SOAP_CREATE_PARTY_REQUEST = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ec=\"ec:services:wsdl:CreateParty-2\" xmlns:ec1=\"ec:schema:xsd:CommonAggregateComponents-2\" xmlns:stan=\"http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader\" xmlns:ec2=\"ec:schema:xsd:CreateParty-2\" xmlns:urn=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\" xmlns:urn1=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\"><soapenv:Header><ec:Header><ec1:BusinessHeader>"
            + "<stan:Sender><stan:Identifier>__SENDER_ID__</stan:Identifier></stan:Sender>"
            + "</ec1:BusinessHeader></ec:Header></soapenv:Header><soapenv:Body><ec:CreatePartyRequest><ec2:CreateParty><urn:Party>"
            + "<urn1:EndpointID>__PARTY_ID__</urn1:EndpointID>"
            + "</urn:Party></ec2:CreateParty></ec:CreatePartyRequest></soapenv:Body></soapenv:Envelope>";

    private static String SOAP_CREATE_ICA_REQUEST = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ec=\"ec:services:wsdl:CreateInterchangeAgreement-2\" xmlns:ec1=\"ec:schema:xsd:CommonAggregateComponents-2\" xmlns:stan=\"http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader\" xmlns:ec2=\"ec:schema:xsd:CreateInterchangeAgreement-2\" xmlns:urn=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\" xmlns:urn1=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\"><soapenv:Header><ec:Header><ec1:BusinessHeader>"
            + "<stan:Sender><stan:Identifier>__SENDER_ID__</stan:Identifier></stan:Sender>"
            + "</ec1:BusinessHeader></ec:Header></soapenv:Header><soapenv:Body><ec:CreateInterchangeAgreementRequest><ec2:CreateInterchangeAgreement><ec1:InterchangeAgreement>"
            + "<urn:SenderParty><urn1:EndpointID>__PARTY_SENDER_ID__</urn1:EndpointID></urn:SenderParty>"
            + "<urn:ReceiverParty><urn1:EndpointID schemeID=\"__PARTY_RECEIVER_SCHEMEID__\">__PARTY_RECEIVER_ID__</urn1:EndpointID></urn:ReceiverParty>"
            + "<urn1:DocumentTypeCode>BDL</urn1:DocumentTypeCode></ec1:InterchangeAgreement></ec2:CreateInterchangeAgreement></ec:CreateInterchangeAgreementRequest></soapenv:Body></soapenv:Envelope>";
    
    private static String SOAP_VIEW_REQUEST = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ec=\"ec:services:wsdl:ViewRequest-2\" xmlns:ec1=\"ec:schema:xsd:CommonAggregateComponents-2\" xmlns:stan=\"http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader\" xmlns:urn=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\" xmlns:urn1=\"urn:oasis:names:specification:ubl:schema:xsd:SignatureAggregateComponents-2\" xmlns:urn2=\"urn:oasis:names:specification:ubl:schema:xsd:SignatureBasicComponents-2\" xmlns:xd=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:urn3=\"urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2\" xmlns:urn4=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\">"
    		+ "<soapenv:Header><ec:Header><ec1:BusinessHeader>"
    		+ "<stan:Sender><stan:Identifier schemeID=\"__SENDER_SCHEMEID__\">__SENDER_ID__</stan:Identifier></stan:Sender>"
    		+ "<stan:Receiver><stan:Identifier schemeID=\"__RECEIVER_SCHEMEID__\">__RECEIVER_ID__</stan:Identifier></stan:Receiver>"
    		+ "</ec1:BusinessHeader></ec:Header></soapenv:Header><soapenv:Body><ec:SubmitViewRequestRequest><ec:ViewRequest>"
    		+ "<urn4:SenderParty><urn:EndpointID schemeID=\"__SENDER_SCHEMEID__\">__SENDER_ID__</urn:EndpointID></urn4:SenderParty>"
    		+ "<urn4:ReceiverParty><urn:EndpointID schemeID=\"__RECEIVER_SCHEMEID__\">__RECEIVER_ID__</urn:EndpointID></urn4:ReceiverParty>"
    		+ "<ec1:DocumentReferenceRequest><urn:ID>__ID__</urn:ID><urn:DocumentTypeCode>__DOC_TYPE__</urn:DocumentTypeCode></ec1:DocumentReferenceRequest>"
    		+ "</ec:ViewRequest></ec:SubmitViewRequestRequest></soapenv:Body></soapenv:Envelope>";

    public void init() {

        if (chunkSize == null) {
        	chunkSize = getOxalisDBDAO()
    				.getMetadata(CHUNK_SIZE);
        }
        if (alreadyExistError == null) {
        	alreadyExistError = getOxalisDBDAO()
    				.getMetadata(ALREADY_EXIST_ERROR);
        }
        if (eanALL == null) {
        	eanALL = getOxalisDBDAO()
    				.getMetadata(EANALLSUPPLIERS);
        }
        
        if (mimeCodeUtil == null) {
        	mimeCodeUtil = new MimeCodeUtil();
        }
        
    }

    public void initStoreBinary() {

        init();

    	urlStoreBinary = getOxalisDBDAO()
				.getMetadata(URL_STORE_BINARY);

    	usernameStoreBinary = getOxalisDBDAO()
				.getMetadata(USR_STORE_BINARY);

    	passwordStoreBinary = getOxalisDBDAO()
				.getMetadata(PWD_STORE_BINARY);

    }

    public void initView() {

        init();

    	urlView = getOxalisDBDAO()
				.getMetadata(URL_VIEW);

    	usernameView = getOxalisDBDAO()
				.getMetadata(VIEW_USR);

    	passwordView = getOxalisDBDAO()
				.getMetadata(VIEW_PWD);

    }
    
    public void initBundle(){
        init();

        urlBundle = getOxalisDBDAO()
				.getMetadata(URL_BUNDLE);

        usernameBundle = getOxalisDBDAO()
				.getMetadata(USR_BUNDLE);

        passwordBundle = getOxalisDBDAO()
				.getMetadata(PWD_BUNDLE);
    	
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public void initRights() {

        init();

        if (urlParty == null) {
        	urlParty = getOxalisDBDAO()
    				.getMetadata(URL_CREATE_PARTY);
        }
        if (usernameParty == null) {
        	usernameParty = getOxalisDBDAO()
    				.getMetadata(USR_CREATE_PARTY);
        }
        if (passwordParty == null) {
        	passwordParty = getOxalisDBDAO()
    				.getMetadata(PWD_CREATE_PARTY);
        }
        if (senderIdParty == null) {
        	senderIdParty = getOxalisDBDAO()
    				.getMetadata(SENDER_CREATE_PARTY);
        }
        if (urlICA == null) {
        	urlICA = getOxalisDBDAO()
    				.getMetadata(URL_CREATE_ICA);
        }
        if (usernameICA == null) {
        	usernameICA = getOxalisDBDAO()
    				.getMetadata(USR_CREATE_ICA);
        }
        if (passwordICA == null) {
        	passwordICA = getOxalisDBDAO()
    				.getMetadata(PWD_CREATE_ICA);
        }
        if (senderIdICA == null) {
        	senderIdICA = getOxalisDBDAO()
    				.getMetadata(SENDER_CREATE_ICA);
        }
    }

    private String generateStoreDocumentWrapperRequest(String uuid, Attachment att, Party sender, Party receiver)  throws DAOException {
    	try {
	    	
			XMLGregorianCalendar xgc = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(new GregorianCalendar());// 1900+aDate.getYear(),
																		// aDate.getMonth(),
																		// aDate.getDate())
	
			xgc.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
			xgc.setTime(DatatypeConstants.FIELD_UNDEFINED,
					DatatypeConstants.FIELD_UNDEFINED,
					DatatypeConstants.FIELD_UNDEFINED);
			
	        
	        String issueDate = df.format(xgc.toGregorianCalendar().getTime());
	        
	        
	        String dynamicSoap = SOAP_DOC_WRAPPER.replaceAll("__ID__", att.getId() + FILE_WRAPPER);
	        dynamicSoap = dynamicSoap.replaceAll("__ISSUE_DATE__", issueDate);
	        dynamicSoap = dynamicSoap.replaceAll("__MIME_CODE__", att.getMimeType());
	
	        dynamicSoap = dynamicSoap.replaceAll("__CALLER_ID__", sender.getIdValue());
			if (sender.getIdScheme() != null) {
				dynamicSoap = dynamicSoap.replaceAll("__CALLER_SCHEMEID__", sender.getIdScheme());			
			}else{
				dynamicSoap = dynamicSoap.replaceAll("__CALLER_SCHEMEID__", "");
			}
	
	        dynamicSoap = dynamicSoap.replaceAll("__DOCUMENT_TYPE_CODE__", "BINARY");
	        //TODO do we need something there?
	        dynamicSoap = dynamicSoap.replaceAll("__DOCUMENT_TYPE__", "");
	
	        if (att.getContent() != null) {
	            dynamicSoap = dynamicSoap.replaceAll("__DOCUMENT_SIZE__", "" + att.getContent().length);
	        }
	
	        dynamicSoap = dynamicSoap.replaceAll("__FILENAME__", att.getId() + FILE_WRAPPER);
	        if (logger.isDebugEnabled()) {
	            logger.debug(uuid + ":---send document wrapper full--start-" + att.getId());
	            logger.debug(uuid + ": " + dynamicSoap);
	            logger.debug(uuid + ":---send document wrapper full--end-" + att.getId());
	        }
	        
	        return dynamicSoap;
	        
		} catch (Exception e) {
			e.printStackTrace();
			throw new DAOException(e);
		}
    	

    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendBinary(String uuid, Attachment att, Party sender, Party receiver) throws DAOException {
    	
    	//EventLogDtoBuilder eventLogDtoBuilder = eventLogService.getEsubmissionEventLogBuilder(null,
		//		EventLogConst.SEND_BINARY_ID, EventLogConst.SEND_BINARY_OPERATION,
		//		EventLogConst.SEND_BINARY_DESCRIPTION, tenderDto);
		
		//eventLogDtoBuilder.withAdditionalInfo("EO ecasID: " + tenderDto.getTbUserId());
        //eventLogDtoBuilder.addAdditionalInfo(tenderBinary.getTbnFilename());
        //eventLogDtoBuilder.withBinaryId(tenderBinary.getTbnTbnId());
        
        initStoreBinary();
        // stream for writing decrypted data
        OutputStream outputStream = null;
        InputStream inputStream = null;
        
        try {
            String soapMsg = generateStoreDocumentWrapperRequest(uuid, att, sender, receiver);

            HttpURLConnection conn = null;

            conn = (HttpURLConnection) new URL(urlStoreBinary).openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setChunkedStreamingMode(Integer.decode(chunkSize));
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", REQUEST_CONTENT_TYPE_PROPS_BINARY);
            String encoded = new String(new Base64().encode((usernameStoreBinary + ":" + passwordStoreBinary).getBytes()));
            conn.setRequestProperty("Authorization", "Basic " + encoded);

            outputStream = conn.getOutputStream();

            outputStream.write(payloadHeadersWrapper.getBytes());
            outputStream.write(soapMsg.getBytes("UTF-8"));
            String updatedBinaryHeaders = binaryHeaders.replaceAll("__FILENAME__", att.getId() + FILE_WRAPPER);
            outputStream.write(updatedBinaryHeaders.getBytes());
            
            byte[] buffer = new byte[1024 * 100];
            int count = 0;
            inputStream = new ByteArrayInputStream(att.getContent());
            
            while ((count = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, count);
            }
            

            outputStream.write(binaryEndPart.getBytes());
            outputStream.flush();

            logger.info(uuid + ":-----sendBinary header infos:------" + att.getId());
            for (String key : conn.getHeaderFields().keySet()) {
                if (key != null && !key.equals("")) {
                	logger.info(uuid + ": " + key + ":" + conn.getHeaderField(key));
                }
            }
            
            // check HTTP Response

            if (HttpURLConnection.HTTP_OK == conn.getResponseCode()) {
            	//eventLogDtoBuilder.withResult(EventLogConst.SUCCESS);
                //eventLogDtoBuilder.withResultInfo("SENT !");
            	
            } else {
                MimeHeaders heads = new MimeHeaders();
                for (String key : conn.getHeaderFields().keySet()) {
                    if (key != null && !key.equals("")) {
                        heads.setHeader(key, conn.getHeaderField(key));
                    }
                }

                InputStream err = conn.getErrorStream();
                StringBuilder sb = new StringBuilder();
                while ((count = err.read(buffer)) > 0) {
                    sb.append(new String(buffer, "UTF-8"));
                }

                String errString = sb.toString();
                try {
                	errString = errString.substring(0, errString.lastIndexOf(":Envelope>") + ":Envelope>".length());
                } catch(Exception ex) {
                	errString = sb.toString();
                }
                if (logger.isDebugEnabled()) {
                    logger.debug(uuid + ": SendServicesEtrustExImpl - sendBinary Wrapper HTTP ErrorStream "  + att.getId() + " " + errString);
                }
                
                if (errString.indexOf(alreadyExistError) >= 0) {                    
                    //eventLogDtoBuilder.withResultInfo("ALREADY SENT !");
                    if (logger.isDebugEnabled()) {
                        logger.debug(uuid + ": SendServicesEtrustExImpl - sendBinary Wrapper the file is already sent !" + att.getId());
                    }
                } else {
                    //eventLogDtoBuilder.logAsError(errString);

                    //eventLogService.saveEventLog(eventLogDtoBuilder);  
                    throw new DAOException(errString);
                }
            }

            //eventLogService.saveEventLog(eventLogDtoBuilder);
            if (logger.isDebugEnabled()) {
                logger.debug(uuid + ": SendServicesEtrustExImpl - sendBinary Wrapper " + att.getId() + FILE_WRAPPER + " Done ! ");
            }
        } catch (Exception e) {
            logger.error(uuid +": SendServicesEtrustExImpl - sendBinary Wrapper  exception " + att.getId() + e.getMessage(), e);
            //eventLogDtoBuilder.logAsError(e.getMessage());
            //eventLogService.saveEventLog(eventLogDtoBuilder);  
            throw new DAOException("An error occurred while sending a binary file wrapper to EtrustEx"  + att.getId(), e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    logger.warn(uuid + ": Unable to close the input stream" + att.getId());
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    logger.warn(uuid + ": Unable to close the output stream" + att.getId());
                }
            }
        }
    }

    public String sendHTTPSOAPMessage(String uuid, String soapMsg, String url, String username, String password, String logInfo) throws DAOException {
    	BufferedWriter outputStream = null;
        InputStream in = null;

        try {
        	//logger.info("-----sendHTTPSOAPMessage infos:------");
        	//logger.info("URL: '" + url + "'");
        	//logger.info("username: '" + username + "'");
        	//logger.info("password: '" + password + "'");
            HttpURLConnection conn = null;

            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setChunkedStreamingMode(Integer.decode(chunkSize));
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", REQUEST_CONTENT_TYPE_PROPS);
            String encoded = new String(new Base64().encode((username + ":" + password).getBytes()));
            conn.setRequestProperty("Authorization", "Basic " + encoded);
           
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            outputStream = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"));

            outputStream.write(soapMsg);
            outputStream.flush();

            logger.info(uuid + ":-----sendHTTPSOAPMessage header infos:------");
            for (String key : conn.getHeaderFields().keySet()) {
                if (key != null && !key.equals("")) {
                	logger.info(uuid + ": " + key + ":" + conn.getHeaderField(key));
                }
            }
            // check HTTP Response
            if (HttpURLConnection.HTTP_OK != conn.getResponseCode()) {
            	 
                InputStream err = conn.getErrorStream();
                StringBuilder sb = new StringBuilder();
                byte[] buffer = new byte[1024 * 100];
                int count = 0;
                while ((count = err.read(buffer)) > 0) {
                    sb.append(new String(buffer, "UTF-8"));
                }

                String errString = sb.toString();
                if (logger.isDebugEnabled()) {
                    logger.debug(uuid + ": SendServicesEtrustExImpl - sendHTTPSOAPMessage - " + logInfo + " HTTP ErrorStream " + errString);
                }

                if (errString.indexOf(alreadyExistError) >= 0) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(uuid + ": SendServicesEtrustExImpl - sendHTTPSOAPMessage - " + logInfo + " the file is already sent !");
                    }
                    return "ALREADY SENT !";
                } else {
                    throw new DAOException(errString);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug(uuid + ": SendServicesEtrustExImpl - sendHTTPSOAPMessage - " + logInfo + " Done ! ");
            }

            return "SENT !";
        } catch (ClientProtocolException e) {
        	logger.error(uuid + ": SendServicesEtrustExImpl - sendHTTPSOAPMessage HTTP protocol error", e);
            throw new DAOException("HTTP protocol error occurred while sending a Soap message", e);
        } catch (IOException e) {
        	logger.error(uuid + ": SendServicesEtrustExImpl - sendHTTPSOAPMessage Communication error", e);        
            throw new DAOException("Communication error occurred while sending a Soap message", e);
        } catch (Exception e) {
            logger.error(uuid + ": SendServicesEtrustExImpl - sendHTTPSOAPMessage - " + logInfo + " exception " + e.getMessage(), e);
            throw new DAOException("An error occurred while sending a Soap message", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    logger.warn(uuid + ": Unable to close the output stream");
                }
            }
        }
    }

    private String generateSubmitBundleRequest(Message message, Wrapper wrapperPdf, Wrapper wrapperXml) throws DAOException {
    	String uuid = ((Message)message).getCorrelationId();
    	
    	try {
	        String dynamicSoap = SOAP_BUNDLE_REQUEST.replaceAll("__ID__", message.getDocumentId());
			switch (message.getLocalName()) {
			case DocumentService.XML_LOCALNAME_INVOICE:
				dynamicSoap = dynamicSoap.replaceAll("__PROFILE_ID__", "Invoice " + message.getDocumentId());
				break;
			case DocumentService.XML_LOCALNAME_CREDIT_NOTE:
				dynamicSoap = dynamicSoap.replaceAll("__PROFILE_ID__", "Credit Note " + message.getDocumentId());
				break;	        
			}
			
			XMLGregorianCalendar xgc = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(new GregorianCalendar());// 1900+aDate.getYear(),
																		// aDate.getMonth(),
																		// aDate.getDate())
	
			xgc.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
			xgc.setTime(DatatypeConstants.FIELD_UNDEFINED,
					DatatypeConstants.FIELD_UNDEFINED,
					DatatypeConstants.FIELD_UNDEFINED);
			
	        String issueDate = df.format(xgc.toGregorianCalendar().getTime());
	        String issueTime = dfTime.format(xgc.toGregorianCalendar().getTime());
	        dynamicSoap = dynamicSoap.replaceAll("__ISSUE_DATE__", issueDate);
	        dynamicSoap = dynamicSoap.replaceAll("__ISSUE_TIME__", issueTime);
	        
	        dynamicSoap = dynamicSoap.replaceAll("__SENDER_ID__", message.getSender().getIdValue());
			if (message.getSender().getIdScheme() != null && !message.getSender().getIdScheme().isEmpty()) {
				dynamicSoap = dynamicSoap.replaceAll("__SENDER_SCHEMEID__", message.getSender().getIdScheme());			
			}else{
				dynamicSoap = dynamicSoap.replaceAll("__SENDER_SCHEMEID__", "GLN");
			}
			
			dynamicSoap = dynamicSoap.replaceAll("__RECEIVER_ID__", message.getReceiver().getIdValue());
	        if (message.getReceiver().getIdScheme() != null && !message.getReceiver().getIdScheme().isEmpty()) {
	            dynamicSoap = dynamicSoap.replaceAll("__RECEIVER_SCHEMEID__", message.getReceiver().getIdScheme());
	        } else {
	            dynamicSoap = dynamicSoap.replaceAll("__RECEIVER_SCHEMEID__", "GLN");
	        }
	        
	        StringBuffer wrapperSoap = new StringBuffer();
	        for (Attachment att : message.getAttachments()) {
	        	wrapperSoap.append(createWrapper(att.getId(), att.getMimeType(), getHash(att)));
			}

	        wrapperSoap.append(createWrapper(message.getDocumentId() + ID_HUMAN_READIBLE, MIME_TYPE_PDF, wrapperPdf));
	        wrapperSoap.append(createWrapper(message.getDocumentId() + ID_XML, MIME_TYPE_XML, wrapperXml));
			
	        
			
	        dynamicSoap = dynamicSoap.replaceAll("__DOCUMENT_WRAPPER__", wrapperSoap.toString());
	        
	        if (logger.isDebugEnabled()) {
	            logger.debug(uuid + ":---bundle full--start-" + message.getDocumentId());
	            logger.debug(uuid + ": " + dynamicSoap);
	            logger.debug(uuid + ":---bundle full--end-" + message.getDocumentId());
	        }
	
	        return dynamicSoap;
		} catch (Exception e) {
			e.printStackTrace();
			throw new DAOException(e);
		}        
    }

    
    public String createWrapper(String id, String mimeType, Wrapper wrapper) {
    	String dynamicSoap = SOAP_BUNDLE_REQUEST_WRAPPER.replaceAll("__WRAPPER_ID__", id);
    	
    	String extension = null;
    	if(mimeType == null || "".equals(mimeType) ){
    	}else{
    		extension = mimeCodeUtil.getDomainFromCode(mimeType);
    	}
    	 
    	dynamicSoap = dynamicSoap.replaceAll("__FILE_NAME__", id + (mimeType == null ? "" : "." + extension));
    	dynamicSoap = dynamicSoap.replaceAll("__FILE_SIZE__", wrapper.getDocumentSize());
    	dynamicSoap = dynamicSoap.replaceAll("__FILE_HASH__", wrapper.getDocumentHash());
        return dynamicSoap;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public String sendBundleXML( Message message, Wrapper wrapperPdf, Wrapper wrapperXml) throws DAOException {
        initBundle();
        String soapMsg = generateSubmitBundleRequest(message, wrapperPdf, wrapperXml);
        return sendHTTPSOAPMessage(((Message)message).getCorrelationId(), soapMsg, urlBundle, usernameBundle, passwordBundle, "sendBundleXML");
    }
/*
    private String generateCreatePartyRequest(TenderDto tenderDto) {

        String dynamicSoap = SOAP_CREATE_PARTY_REQUEST.replaceAll("__SENDER_ID__", senderIdParty);
        dynamicSoap = dynamicSoap.replaceAll("__PARTY_ID__", tenderDto.getTbUserId().toUpperCase());
        if (logger.isDebugEnabled()) {
            logger.debug("---CreatePartyRequest full--start-" + tenderDto.getTenId());
            logger.debug(dynamicSoap);
            logger.debug("---CreatePartyRequest full--end-" + tenderDto.getTenId());
        }

        return dynamicSoap;
    }

    private String generateCreateInterchangeAgreementRequest(TenderDto tenderDto, CallForTendersType callForTendersType) {

        String dynamicSoap = SOAP_CREATE_ICA_REQUEST.replaceAll("__SENDER_ID__", senderIdICA);
        dynamicSoap = dynamicSoap.replaceAll("__PARTY_SENDER_ID__", tenderDto.getTbUserId().toUpperCase());
        if (callForTendersType != null && callForTendersType.getContractingParty() != null && callForTendersType.getContractingParty().getParty().getEndpointID() != null) {
            String schemeId = callForTendersType.getContractingParty().getParty().getEndpointID().getSchemeID();
            String value = callForTendersType.getContractingParty().getParty().getEndpointID().getValue();
            if (schemeId != null && !schemeId.isEmpty()) {
                dynamicSoap = dynamicSoap.replaceAll("__PARTY_RECEIVER_SCHEMEID__", schemeId);
            } else {
                dynamicSoap = dynamicSoap.replaceAll("__PARTY_RECEIVER_SCHEMEID__", "GLN");
            }
            dynamicSoap = dynamicSoap.replaceAll("__PARTY_RECEIVER_ID__", value);
        } else {
            logger.error("SendServicesEtrustExImpl - generateSubmitBundleRequest There is no Receiver for CreateInterchangeAgreementRequest XML  " + tenderDto.getTenId());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("---CreateInterchangeAgreementRequest full--start-" + tenderDto.getTenId());
            logger.debug(dynamicSoap);
            logger.debug("---CreateInterchangeAgreementRequest full--end-" + tenderDto.getTenId());
        }

        return dynamicSoap;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void setRights(TenderDto tenderDto, CallForTendersType callForTendersType) throws ESubmissionException {
    	//EventLogDtoBuilder eventLogDtoBuilder = eventLogService.getEsubmissionEventLogBuilder(null, EventLogConst.SET_RIGHTS_ID,
    	//		 EventLogConst.SET_RIGHTS_OPERATION,
    	//		 EventLogConst.SET_RIGHTS_DESCRIPTION,
    	//		 tenderDto);

        //eventLogDtoBuilder.withTenderId(tenderDto.getTenId());
		//eventLogDtoBuilder.withAdditionalInfo("EO ecasID: " + tenderDto.getTbUserId());
        
    	try {    		
	        initRights();
	        String soapMsg = generateCreatePartyRequest(tenderDto);
	        sendHTTPSOAPMessage(soapMsg, urlParty, usernameParty, passwordParty, "setRights-CreateParty");
	        soapMsg = generateCreateInterchangeAgreementRequest(tenderDto, callForTendersType);
	        sendHTTPSOAPMessage(soapMsg, urlICA, usernameICA, passwordICA, "setRights-CreateICA");
            //eventLogDtoBuilder.withResult(EventLogConst.SUCCESS);            

            //eventLogService.saveEventLog(eventLogDtoBuilder);
        } catch (Exception e) {
            //eventLogDtoBuilder.logAsError(e.getMessage());

            //eventLogService.saveEventLog(eventLogDtoBuilder);             
            logger.error(e);
            throw new ESubmissionException("An error occurred while setting the rights", e);
        }
    }
*/    
    private String generateViewRequest(String uuid, Message message) throws DAOException {
    	try {

	        String dynamicSoap = SOAP_VIEW_REQUEST.replaceAll("__ID__", message.getDocumentId());
	        dynamicSoap = dynamicSoap.replaceAll("__DOC_TYPE__", message.getTypeCode());
	        
	        dynamicSoap = dynamicSoap.replaceAll("__SENDER_ID__", message.getSender().getIdValue());
			if (message.getSender().getIdScheme() != null && !message.getSender().getIdScheme().isEmpty()) {
				dynamicSoap = dynamicSoap.replaceAll("__SENDER_SCHEMEID__", message.getSender().getIdScheme());			
			}else{
				dynamicSoap = dynamicSoap.replaceAll("__SENDER_SCHEMEID__", "GLN");
			}
			
			dynamicSoap = dynamicSoap.replaceAll("__RECEIVER_ID__", message.getReceiver().getIdValue());
            if (message.getReceiver().getIdScheme() != null && !message.getReceiver().getIdScheme().isEmpty()) {
                dynamicSoap = dynamicSoap.replaceAll("__RECEIVER_SCHEMEID__", message.getReceiver().getIdScheme());
            } else {
                dynamicSoap = dynamicSoap.replaceAll("__RECEIVER_SCHEMEID__", "GLN");
            }
            
            
	        if (logger.isDebugEnabled()) {
	            logger.debug(uuid + ":---generate View Request--start-" + message.getDocumentId());
	            logger.debug(uuid + ": " + dynamicSoap);
	            logger.debug(uuid + ":---generate View Request--end-" + message.getDocumentId());
	        }
	
	        return dynamicSoap;
		} catch (Exception e) {
			e.printStackTrace();
			throw new DAOException(e);
		}
    }
    
    public Attachment sendHTTPSOAPMessageView(Message message) throws DAOException {
    	BufferedWriter outputStream = null;
        InputStream in = null;
        
        String parseString = null;
        String uuid = ((Message)message).getCorrelationId();
        
        try {
        	initView();
        	
   		
        	//logger.info("-----sendHTTPSOAPMessage infos:------");
        	//logger.info("URL: '" + url + "'");
        	//logger.info("username: '" + username + "'");
        	//logger.info("password: '" + password + "'");
            HttpURLConnection conn = null;

            conn = (HttpURLConnection) new URL(urlView).openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setChunkedStreamingMode(Integer.decode(chunkSize));
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", REQUEST_CONTENT_TYPE_PROPS);
            String encoded = new String(new Base64().encode((usernameView + ":" + passwordView).getBytes()));
            conn.setRequestProperty("Authorization", "Basic " + encoded);
           
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            outputStream = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"));

            outputStream.write(generateViewRequest(uuid, message));
            outputStream.flush();

            logger.info(uuid + ":-----sendHTTPSOAPMessageView header infos:------");
            for (String key : conn.getHeaderFields().keySet()) {
                if (key != null && !key.equals("")) {
                	logger.info(key + ":" + conn.getHeaderField(key));
                }
            }
            // check HTTP Response
            if (HttpURLConnection.HTTP_OK != conn.getResponseCode()) {
            	 
                InputStream err = conn.getErrorStream();
                StringBuilder sb = new StringBuilder();
                byte[] buffer = new byte[1024 * 100];
                int count = 0;
                while ((count = err.read(buffer)) > 0) {
                    sb.append(new String(buffer, "UTF-8"));
                }

                String errString = sb.toString();
                if (logger.isDebugEnabled()) {
                    logger.debug(uuid + ": SendServicesEtrustExImpl - sendHTTPSOAPMessageView - HTTP ErrorStream " + errString);
                }

                throw new DAOException(errString);

            }else{
                String responseString = "";
                StringBuffer outputString  = new StringBuffer("");
                InputStreamReader isr = new InputStreamReader(conn.getInputStream());
                BufferedReader inBuf = new BufferedReader(isr);
                //Write the SOAP message response to a String.
                while ((responseString = inBuf.readLine()) != null) {
                    outputString.append(responseString);     }

                //int pos = outputString.indexOf(":Envelope>");
                //if(pos < 0){
                //    String str = outputString.toString();
                //}
                
                Attachment att = null;
                
                String patternStrStart = "<(\\w+):EmbeddedDocumentBinaryObject ";
                Pattern pattern = Pattern.compile(patternStrStart);
                Matcher matcher = pattern.matcher(outputString);
                
                
                
                if(matcher.find()){
                	att = new Attachment();
                	
                	att.setId(message.getDocumentId() + ID_HUMAN_READIBLE);
                	int tagFirstStart = matcher.start();
                	int tagFirstEnd = outputString.indexOf(">", tagFirstStart + 1);
                	
                	String startTag = outputString.substring(tagFirstStart, tagFirstEnd);
                	int startMime = startTag.indexOf("mimeCode=");
                	att.setMimeType(startTag.substring(startMime + 10, startTag.indexOf("\"", startMime + 10)));
                	
                	String patternStrEnd = "</(\\w+):EmbeddedDocumentBinaryObject>";
                	pattern = Pattern.compile(patternStrEnd);
                	matcher = pattern.matcher(outputString);
                	
                	if(matcher.find()){
                		att.setContent(new Base64().decode(outputString.substring(tagFirstEnd+1, matcher.start())));
                		
                		return att;
                	}else{
                		parseString = "Missing closing EmbeddedDocumentBinaryObject tag";	
                	}
                	
                	

                }else{
                	parseString = "Missing opening EmbeddedDocumentBinaryObject tag";
                }
                
            }
            
            if (logger.isDebugEnabled()) {
                logger.debug("SendServicesEtrustExImpl - sendHTTPSOAPMessageView - Done! ");
            }

            
            if (logger.isDebugEnabled()) {
                logger.debug(uuid + ": SendServicesEtrustExImpl - sendHTTPSOAPMessageView - HTTP ErrorStream " + parseString);
            }

            throw new DAOException(parseString); 
            
        } catch (ClientProtocolException e) {
        	logger.error(uuid + ": SendServicesEtrustExImpl - sendHTTPSOAPMessage HTTP protocol error", e);
            throw new DAOException("HTTP protocol error occurred while sending a Soap message", e);
        } catch (IOException e) {
        	logger.error(uuid + ": SendServicesEtrustExImpl - sendHTTPSOAPMessage Communication error", e);        
            throw new DAOException("Communication error occurred while sending a Soap message", e);
        } catch (Exception e) {
            logger.error(uuid + ": SendServicesEtrustExImpl - sendHTTPSOAPMessage - exception " + e.getMessage(), e);
            throw new DAOException("An error occurred while sending a Soap message", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    logger.warn(uuid + ": Unable to close the output stream");
                }
            }
        }
    }  
    
    public Attachment generateDoctXmlRequest(Message message) throws DAOException {
    	Attachment att = new Attachment();
    	att.setId(message.getDocumentId() + ID_XML);
    	att.setMimeType("text/xml");
    	att.setContent(message.getDocument().getContent());
    	return att;
    	
    }
    
    public Wrapper getHash(Attachment att){

    	Wrapper wrapper = new Wrapper();
    	
        Digest digest = new SHA512Digest();        
        int count = att.getContent().length;
        wrapper.setDocumentSize("" + count);       
        
        digest.update(att.getContent(),0,count);

        byte[] retValue = new byte[digest.getDigestSize()];
        digest.doFinal(retValue, 0); 
        wrapper.setDocumentHash(Hex.toHexString(retValue));
        
        return wrapper;
    }

	@Override
	public void sendDocument(String uuid, Object message, Party sender, Party receiver){
		
	}
}
