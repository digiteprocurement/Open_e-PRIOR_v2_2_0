package eu.europa.ec.cipa.adapter.dao.ws;

import static eu.europa.ec.cipa.adapter.utils.StaxUtils.getSingleXML;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import oasis.names.specification.ubl.schema.xsd.invoice_2.InvoiceType;

import org.apache.xmlbeans.XmlObject;
import org.springframework.xml.transform.StringResult;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.Partner;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.PartnerIdentification;
import org.w3c.dom.Node;

import ec.schema.xsd.commonaggregatecomponents_2.BusinessHeaderType;
import ec.schema.xsd.commonaggregatecomponents_2.HeaderType;
import ec.services.wsdl.invoice_2.ObjectFactory;
import ec.services.wsdl.invoice_2.SubmitInvoiceRequest;
import eu.europa.ec.cipa.adapter.dao.exception.DAOException;
import eu.europa.ec.cipa.adapter.model.Message;
import eu.europa.ec.cipa.adapter.model.Party;

public class InvoiceWSDAO extends AbstractWSDAO {

	@Override
	public void sendDocument(String uuid, Object message, Party sender, Party receiver)
			throws DAOException {

		try {
			Message msg = (Message) message;
			Node node = getSingleXML(uuid, msg.getLocalName(), msg.getDocument().getContent());
			
			StringResult invoiceRequestStr = new StringResult();
			StringResult headerRequestStr = new StringResult();
			ObjectFactory of = new ObjectFactory();
			{

				SubmitInvoiceRequest sir = of.createSubmitInvoiceRequest();
				JAXBContext jc = JAXBContext.newInstance("oasis.names.specification.ubl.schema.xsd.invoice_2");
				Unmarshaller unmarshaller = jc.createUnmarshaller();
				
				JAXBElement<InvoiceType> jbinvt = unmarshaller.unmarshal(node,
						InvoiceType.class);
				InvoiceType invoice = jbinvt.getValue();
				sir.setInvoice(invoice);
				
				if (invoice != null && invoice.getID() != null) {
					logger.debug(uuid + ": Found id" + invoice.getID().getValue());
					msg.setDocumentId(invoice.getID().getValue());
				}

				JAXBContext jc2 = JAXBContext.newInstance("ec.services.wsdl.invoice_2");
				Marshaller marshaller = jc2.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
				marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
				marshaller.marshal(sir, invoiceRequestStr);
			}

			{
				HeaderType header = new HeaderType();
				header.setBusinessHeader(new BusinessHeaderType());
				Partner senderPartener = new Partner();

				senderPartener.setIdentifier(new PartnerIdentification());
				if (sender.getIdScheme() != null) {
					senderPartener.getIdentifier().setSchemeID(
							sender.getIdScheme());
				}
				senderPartener.getIdentifier().setValue(sender.getIdValue());

				header.getBusinessHeader().getSender().add(senderPartener);

				Partner receiverPartener = new Partner();

				receiverPartener.setIdentifier(new PartnerIdentification());
				if (receiver.getIdScheme() != null) {
					receiverPartener.getIdentifier().setSchemeID(
							receiver.getIdScheme());
				}
				receiverPartener.getIdentifier()
						.setValue(receiver.getIdValue());

				header.getBusinessHeader().getReceiver().add(receiverPartener);

				JAXBContext jc = JAXBContext
						.newInstance("ec.services.wsdl.invoice_2");

				JAXBElement<HeaderType> jaxbHeader = of.createHeader(header);
				Marshaller marshaller = jc.createMarshaller();

				marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
				marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);

				marshaller.marshal(jaxbHeader, headerRequestStr);
			}

			callWebService(uuid, invoiceRequestStr.toString(),
					headerRequestStr.toString(), null);
		} catch (Exception e) {
			e.printStackTrace();
			throw new DAOException(e);
		}

	}

}
