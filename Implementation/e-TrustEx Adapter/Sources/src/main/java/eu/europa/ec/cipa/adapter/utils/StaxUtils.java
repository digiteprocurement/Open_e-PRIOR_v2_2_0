package eu.europa.ec.cipa.adapter.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stax.StAXSource;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.springframework.util.Base64Utils;
import org.w3c.dom.Node;

import eu.europa.ec.cipa.adapter.model.Attachment;
import eu.europa.ec.cipa.adapter.model.ByteArrayWrapper;

public class StaxUtils {

	private static XMLInputFactory aXMLInputFactory;
	private static TransformerFactory aTransformerFactory;
	private static XMLOutputFactory aXMLOutputFactory;

	private static Logger logger = Logger.getLogger(StaxUtils.class);

	public static XMLOutputFactory getXMLOutputFactory() {
		if (aXMLOutputFactory == null)
			aXMLOutputFactory = XMLOutputFactory.newInstance();
		return aXMLOutputFactory;
	}

	public static XMLInputFactory getXMLInputFactory() {
		if (aXMLInputFactory == null)
			aXMLInputFactory = XMLInputFactory.newInstance();
		return aXMLInputFactory;
	}

	public static TransformerFactory getTransformerFactory() {
		if (aTransformerFactory == null)
			aTransformerFactory = TransformerFactory.newInstance();
		return aTransformerFactory;
	}

	public static boolean isXMLExistsAndUnique(String xmlName, String xmlNS,
			byte[] content) throws XMLStreamException, TransformerException {
		boolean found = false;

		if (xmlName == null || content == null)
			return found;

		XMLStreamReader reader = getXMLInputFactory().createXMLStreamReader(
				new ByteArrayInputStream(content));

		while (reader.hasNext()) {
			int event = reader.next();
			if (event == XMLStreamConstants.START_ELEMENT
					&& xmlName.equals(reader.getLocalName())) {

				if (xmlNS != null && !xmlNS.equals(reader.getNamespaceURI())) {
					continue; // skip
				}

				if (found) {// if found already
					return false;
				}
				found = true;
			}
		}

		return found;
	}

	public static Node getSingleXML(String uuid, String xmlName, String xmlNS, byte[] content)
			throws XMLStreamException, TransformerException {
		Node n = null;

		if (xmlName == null || content == null)
			return n;

		XMLStreamReader reader = getXMLInputFactory().createXMLStreamReader(
				new ByteArrayInputStream(content));

		boolean found = false;

		while (reader.hasNext()) {
			int event = reader.next();
			if (event == XMLStreamConstants.START_ELEMENT
					&& xmlName.equals(reader.getLocalName())) {

				if (xmlNS != null && !xmlNS.equals(reader.getNamespaceURI())) {
					continue; // skip
				}

				if (found) {
					throw new RuntimeException("Only one XML allowed");
				}
				Transformer t = getTransformerFactory().newTransformer();
				DOMResult result = new DOMResult();
				t.transform(new StAXSource(reader), result);
				n = result.getNode();
				found = true;
			}
		}
		return n;
	}
	
	public static Node getSingleXML(String uuid, String xmlNodeName, byte[] content)
			throws XMLStreamException, TransformerException, XmlException {
		Node n = null;

		if (xmlNodeName == null || content == null)
			return n;
		
		XmlObject doc = XmlObject.Factory.parse(new String(content));
		XmlObject[] refs = doc.execQuery("*:StandardBusinessDocument/*:" + xmlNodeName);
		
		if(refs.length != 1){
			throw new RuntimeException("Only one node " + xmlNodeName + " is allowed in the incoming XML message!");			
		} else{
			logger.debug("found one " + xmlNodeName + " node in the incoming XML message!");
			n = refs[0].getDomNode();
		}

		return n;
	}

	public static ByteArrayWrapper removeXmNode(String uuid, String xmlName, String xmlNS,
			ByteArrayWrapper content, List<Attachment> attachments)
			throws XMLStreamException, TransformerException {

		XMLEventReader reader = getXMLInputFactory().createXMLEventReader(
				new ByteArrayInputStream(content.getContent()));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XMLEventWriter writer = getXMLOutputFactory().createXMLEventWriter(out);

		boolean deleteSection = false;
		XMLEvent event;
		Attachment att = null;
		int count = 0;
		while (reader.hasNext()) {

			event = reader.nextEvent();
			if (event.getEventType() == XMLStreamConstants.START_ELEMENT
					&& event.asStartElement().getName().toString()
							.equals(new QName(xmlNS, xmlName).toString())) {
				deleteSection = true;
				String mime = event.asStartElement()
						.getAttributeByName(new QName("mimeCode")).getValue();
				att = new Attachment();
				att.setId("" + (++count));
				att.setMimeType(mime);

				logger.info(uuid + ": Found an attachment with mime type " + mime);
			} else if (deleteSection
					&& event.getEventType() == XMLStreamConstants.END_ELEMENT
					&& event.asEndElement().getName().toString()
							.equals(new QName(xmlNS, xmlName).toString())) {
				deleteSection = false;
				attachments.add(att);
				att = null;
			} else if (!deleteSection) {
				writer.add(event);
			} else {
				if (deleteSection
						&& event.getEventType() == XMLStreamConstants.CHARACTERS) {

					byte[] attcontent = Base64Utils.decodeFromString(event
							.asCharacters().getData());
					att.setContent(attcontent);
				}
			}
		}
		writer.flush();
		return new ByteArrayWrapper(out.toByteArray());

	}

}
