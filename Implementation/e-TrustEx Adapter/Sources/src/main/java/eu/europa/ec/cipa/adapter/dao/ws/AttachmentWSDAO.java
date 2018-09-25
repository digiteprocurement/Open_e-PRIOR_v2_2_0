package eu.europa.ec.cipa.adapter.dao.ws;

import java.util.GregorianCalendar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import oasis.names.specification.ubl.schema.xsd.attacheddocument_2.AttachedDocumentType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_2.AttachmentType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_2.PartyType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_2.IDType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_2.IssueDateType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_2.ParentDocumentIDType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_2.ParentDocumentTypeCodeType;

import org.springframework.xml.transform.StringResult;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.Partner;
import org.unece.cefact.namespaces.standardbusinessdocumentheader.PartnerIdentification;

import ec.schema.xsd.commonaggregatecomponents_2.BusinessHeaderType;
import ec.schema.xsd.commonaggregatecomponents_2.HeaderType;
import ec.services.wsdl.attacheddocument_2.ObjectFactory;
import ec.services.wsdl.attacheddocument_2.SubmitAttachedDocumentRequest;
import eu.europa.ec.cipa.adapter.dao.exception.DAOException;
import eu.europa.ec.cipa.adapter.model.Attachment;
import eu.europa.ec.cipa.adapter.model.Party;

public class AttachmentWSDAO extends AbstractWSDAO {

	@Override
	public void sendDocument(String uuid, Object message, Party sender, Party receiver)
			throws DAOException {
		try {

			logger.debug(uuid + ": send Document attachment start");
			Attachment att = (Attachment) message;

			StringResult attRequestStr = new StringResult();
			StringResult headerRequestStr = new StringResult();
			ObjectFactory of = new ObjectFactory();
			{

				SubmitAttachedDocumentRequest sir = of
						.createSubmitAttachedDocumentRequest();
				AttachedDocumentType attDoc = new AttachedDocumentType();

				attDoc.setID(new IDType());
				attDoc.getID().setValue(att.getId());

				// Date aDate = new Date();

				XMLGregorianCalendar xgc = DatatypeFactory.newInstance()
						.newXMLGregorianCalendar(new GregorianCalendar());// 1900+aDate.getYear(),
																			// aDate.getMonth(),
																			// aDate.getDate())

				xgc.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
				xgc.setTime(DatatypeConstants.FIELD_UNDEFINED,
						DatatypeConstants.FIELD_UNDEFINED,
						DatatypeConstants.FIELD_UNDEFINED);

				attDoc.setIssueDate(new IssueDateType());
				attDoc.getIssueDate().setValue(xgc);

				attDoc.setParentDocumentID(new ParentDocumentIDType());
				attDoc.getParentDocumentID().setValue(att.getParrentId());

				attDoc.setParentDocumentTypeCode(new ParentDocumentTypeCodeType());
				attDoc.getParentDocumentTypeCode().setValue(
						att.getParrentTypeCode());

				attDoc.setSenderParty(new PartyType());
				attDoc.setReceiverParty(new PartyType());

				attDoc.setAttachment(new AttachmentType());

				sir.setAttachedDocument(attDoc);

				JAXBContext jc2 = JAXBContext
						.newInstance("ec.services.wsdl.attacheddocument_2");
				Marshaller marshaller = jc2.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
				marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
				marshaller.marshal(sir, attRequestStr);
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
						.newInstance("ec.services.wsdl.attacheddocument_2");

				JAXBElement<HeaderType> jaxbHeader = of.createHeader(header);
				Marshaller marshaller = jc.createMarshaller();

				marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
				marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);

				marshaller.marshal(jaxbHeader, headerRequestStr);
			}

			callWebService(uuid, attRequestStr.toString(),
					headerRequestStr.toString(), att);
			logger.debug(uuid + ": send Document attachment stop");
		} catch (Exception e) {
			e.printStackTrace();
			throw new DAOException(e);
		}
	}

}
