package eu.europa.ec.cipa.adapter.dao.ws;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceTransformerException;
import org.springframework.ws.client.core.FaultMessageResolver;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import eu.europa.ec.cipa.adapter.dao.db.EtrustexDBDAO;
import eu.europa.ec.cipa.adapter.dao.db.OxalysDBDAO;
import eu.europa.ec.cipa.adapter.dao.exception.DAOException;
import eu.europa.ec.cipa.adapter.model.Attachment;
import eu.europa.ec.cipa.adapter.model.Party;

public abstract class AbstractWSDAO {

	private static final String AS2ADAPTER_ETRUSTEX_ENDPOINT = "AS2ADAPTER_ETRUSTEX_ENDPOINT";
	private static final String AS2ADAPTER_ETRUSTEX_LOGIN = "AS2ADAPTER_ETRUSTEX_LOGIN";
	private static final String AS2ADAPTER_ETRUSTEX_PASSWORD = "AS2ADAPTER_ETRUSTEX_PASSWORD";

	protected static Logger logger = Logger.getLogger(AbstractWSDAO.class);

	private OxalysDBDAO oxalysDBDAO;

	public abstract void sendDocument(String uuid, Object message, Party sender,
			Party receiver) throws DAOException;

	protected String getServiceURL() {
		String url = getOxalisDBDAO().getMetadata(
				AS2ADAPTER_ETRUSTEX_ENDPOINT);
		logger.debug("Found URL:" + url);
		return url;
		// return "http://wltdig07.cc.cec.eu.int:1043/eprior/services";
	}

	protected String getServiceLogin() {
		String login = getOxalisDBDAO()
				.getMetadata(AS2ADAPTER_ETRUSTEX_LOGIN);
		logger.debug("Found login:" + login);
		return login;
	}

	protected String getServicePassword() {
		String password = getOxalisDBDAO().getMetadata(
				AS2ADAPTER_ETRUSTEX_PASSWORD);
		
		return password;		
	}

	protected void callWebService(String uuid, final String body, final String jaxbHeader,
			final Attachment att) throws SOAPException, MalformedURLException,
			KeyManagementException, UnrecoverableKeyException,
			NoSuchAlgorithmException, KeyStoreException {

		// TODO handle attachments!
		logger.debug(uuid + "callWebService start");
		
		String urlStr = getServiceURL();
		String login = getServiceLogin();
		String password = getServicePassword();

		WebServiceTemplate ws = new WebServiceTemplate();
		ws.setDefaultUri(urlStr);
		ws.setMessageFactory(new SaajSoapMessageFactory(MessageFactory
				.newInstance()));

		DefaultHttpClient client = null;

		URL url = new URL(urlStr);

		if (url.getProtocol() != null
				&& url.getProtocol().equalsIgnoreCase("https")) {
			// strategy to always accept server certificate
			TrustStrategy easyStrategy = new TrustStrategy() {

				@Override
				public boolean isTrusted(X509Certificate[] certificate,
						String authType) throws CertificateException {
					return true;
				}
			};

			SSLSocketFactory sf = new SSLSocketFactory(easyStrategy,
					SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			SchemeRegistry registry = new SchemeRegistry();

			if (url.getPort() != -1) {// if there is a port: use it
				registry.register(new Scheme("https", url.getPort(), sf));
			} else { // use default port value otherwise
				registry.register(new Scheme("https", url.getDefaultPort(), sf));
			}

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(
					registry);
			client = new DefaultHttpClient(ccm);

		} else {
			// assume it's http.
			client = new DefaultHttpClient();
		}

		HttpComponentsMessageSender messageSender = new HttpComponentsMessageSender(
				client);
		//remove the Content-Length HTTP header as it is also added by the Spring WS implementation 
		if(att == null)
			client.removeRequestInterceptorByClass(org.apache.http.protocol.RequestContent.class);
		
		Credentials creds = new UsernamePasswordCredentials(login, password);
		messageSender.setCredentials(creds);
		messageSender.setAuthScope(AuthScope.ANY);
		client.getCredentialsProvider().setCredentials(AuthScope.ANY, creds);
		client.addRequestInterceptor(new HttpRequestInterceptor() {

			@Override
			public void process(HttpRequest request, HttpContext context)
					throws HttpException, IOException {
				AuthState authState = (AuthState) context
						.getAttribute(ClientContext.TARGET_AUTH_STATE);

				// If no auth scheme available yet, try to initialize it
				// preemptively
				if (authState.getAuthScheme() == null) {
					AuthScheme authScheme = new BasicScheme();
					CredentialsProvider credsProvider = (CredentialsProvider) context
							.getAttribute(ClientContext.CREDS_PROVIDER);
					HttpHost targetHost = (HttpHost) context
							.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
					if (authScheme != null) {
						Credentials creds = credsProvider
								.getCredentials(new AuthScope(targetHost
										.getHostName(), targetHost.getPort()));
						if (creds == null) {
							throw new HttpException(
									"No credentials for preemptive authentication");
						}
						authState.setAuthScheme(authScheme);
						authState.setCredentials(creds);
					}
				}
				Header[] allHeaders = request.getAllHeaders();
				for(Header h : allHeaders){
					logger.debug("header: " + h.getName() + " -> " + h.getValue());
				}				
			}

		}, 1);

		ws.setMessageSender(messageSender);
		FaultMessageResolver faultResolver = new FaultMessageResolver() {

			@Override
			public void resolveFault(WebServiceMessage fault)
					throws IOException {
				throw new IOException("WS Fault:" + transformSourceToStreamSourceWithStringReader(fault.getPayloadSource()));
			}
		};
		ws.setFaultMessageResolver(faultResolver);
		WebServiceMessageCallback requestCallBack = new WebServiceMessageCallback() {

			@Override
			public void doWithMessage(WebServiceMessage msg)
					throws IOException, TransformerException {
				SaajSoapMessage soapMessage = (SaajSoapMessage) msg;

				DocumentBuilderFactory docFactory = DocumentBuilderFactory
						.newInstance();
				docFactory.setNamespaceAware(true);

				final String templateStart = "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/'><soapenv:Header>";
				final String templateMid = "</soapenv:Header><soapenv:Body>";
				final String templateStop = "</soapenv:Body></soapenv:Envelope>";

				StringBuffer buffer = new StringBuffer();
				buffer.append(templateStart);
				buffer.append(jaxbHeader);
				buffer.append(templateMid);
				buffer.append(body);
				buffer.append(templateStop);
				logger.debug("SOAP message: " + buffer.toString());

				Document doc = null;
				try {
					InputStream stream = new ByteArrayInputStream(buffer.toString().getBytes("UTF-8"));
					Reader reader = new InputStreamReader(stream,"UTF-8");
					InputSource is = new InputSource(reader);
					is.setEncoding("UTF-8");
					
					doc = docFactory.newDocumentBuilder().parse(is);
				} catch (SAXException | ParserConfigurationException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}

				soapMessage.setDocument(doc);

				if (att != null && att.getContent() != null) {
					logger.debug("Attachment binary detected");
					// AttachmentPart atp =
					// soapMessage.getSaajMessage().createAttachmentPart();
					// atp.setContentId("att-content-id");
					// try {
					// atp.setRawContent(new
					// ByteArrayInputStream(att.getContent()),
					// att.getMimeType());
					// } catch (SOAPException e) {
					// throw new RuntimeException(e);
					// }
					// soapMessage.getSaajMessage().addAttachmentPart(atp);

					InputStreamSource source = new ByteArrayResource(
							att.getContent());
					String contentId = "att-content-id";
					soapMessage.addAttachment(contentId, source,
							att.getMimeType());

				}

			}
		};
		WebServiceMessageCallback responseCallback = new WebServiceMessageCallback() {

			@Override
			public void doWithMessage(WebServiceMessage msg)
					throws IOException, TransformerException {
				// do nothing						
			}
		};
		
		ws.sendAndReceive(requestCallBack, responseCallback);
		
		logger.debug(uuid + "callWebService end");

	}

	public OxalysDBDAO getOxalisDBDAO() {
		return oxalysDBDAO;
	}

	public void setOxalysDBDAO(OxalysDBDAO oxalysDBDAO) {
		this.oxalysDBDAO = oxalysDBDAO;
	}
	
	private String transformSourceToStreamSourceWithStringReader(Source notValidatableSource) {
        
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
 
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                    "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            StringWriter writer = new StringWriter();
            transformer.transform(notValidatableSource, new StreamResult(
                    writer));
 
            return writer.toString();
 
        } catch (TransformerException transformerException) {
            throw new WebServiceTransformerException(
                    "Could not convert the source to a StreamSource with a StringReader",
                    transformerException);
        }
 
        
    }
	
}
