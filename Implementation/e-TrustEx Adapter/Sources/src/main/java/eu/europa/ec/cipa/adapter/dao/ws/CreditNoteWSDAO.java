package eu.europa.ec.cipa.adapter.dao.ws;

import static eu.europa.ec.cipa.adapter.utils.StaxUtils.getSingleXML;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import oasis.names.specification.ubl.schema.xsd.creditnote_2.CreditNoteType;

import org.springframework.xml.transform.StringResult;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.Partner;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.PartnerIdentification;
import org.w3c.dom.Node;

import ec.schema.xsd.commonaggregatecomponents_2.BusinessHeaderType;
import ec.schema.xsd.commonaggregatecomponents_2.HeaderType;
import ec.services.wsdl.creditnote_2.ObjectFactory;
import ec.services.wsdl.creditnote_2.SubmitCreditNoteRequest;
import eu.europa.ec.cipa.adapter.dao.exception.DAOException;
import eu.europa.ec.cipa.adapter.model.Message;
import eu.europa.ec.cipa.adapter.model.Party;

public class CreditNoteWSDAO extends AbstractWSDAO {

	@Override
	public void sendDocument(String uuid, Object message, Party sender, Party receiver)
			throws DAOException {
		// TODO Auto-generated method stub
		
		try {
			Message msg = (Message) message;
			Node node = getSingleXML(uuid, msg.getLocalName(), msg.getDocument().getContent());

			StringResult cnRequestStr = new StringResult();
			StringResult headerRequestStr = new StringResult();
			ObjectFactory of = new ObjectFactory();
			{
				SubmitCreditNoteRequest sir = of
						.createSubmitCreditNoteRequest();
				JAXBContext jc = JAXBContext
						.newInstance("oasis.names.specification.ubl.schema.xsd.creditnote_2");
				Unmarshaller unmarshaller = jc.createUnmarshaller();

				JAXBElement<CreditNoteType> jbcnt = unmarshaller.unmarshal(
						node, CreditNoteType.class);
				CreditNoteType cn = jbcnt.getValue();
				sir.setCreditNote(cn);

				if (cn != null && cn.getID() != null) {
					logger.debug(uuid + ": Found id" + cn.getID().getValue());
					msg.setDocumentId(cn.getID().getValue());
				}

				JAXBContext jc2 = JAXBContext
						.newInstance("ec.services.wsdl.creditnote_2");
				Marshaller marshaller = jc2.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
				marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
				marshaller.marshal(sir, cnRequestStr);
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
						.newInstance("ec.services.wsdl.creditnote_2");

				JAXBElement<HeaderType> jaxbHeader = of.createHeader(header);
				Marshaller marshaller = jc.createMarshaller();

				marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
				marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);

				marshaller.marshal(jaxbHeader, headerRequestStr);
			}

			callWebService(uuid, cnRequestStr.toString(),
					headerRequestStr.toString(), null);
		} catch (Exception e) {
			e.printStackTrace();
			throw new DAOException(e);
		}

	}

}
