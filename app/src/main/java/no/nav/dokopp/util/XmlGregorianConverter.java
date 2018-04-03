package no.nav.dokopp.util;

import no.nav.dokopp.exception.DokoppTechnicalException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.GregorianCalendar;

/**
 * Converter for {@link XMLGregorianCalendar}
 *
 * @author Roar Bjurstrøm, Visma Consulting.
 */
public final class XmlGregorianConverter {

	private static final DatatypeFactory DATATYPE_FACTORY;

	static {
		try {
			DATATYPE_FACTORY = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new DokoppTechnicalException("Unable to configure DataTypeFactory.", e);
		}
	}

	private XmlGregorianConverter() {
		// Do not instantiate
	}

	public static XMLGregorianCalendar toXmlGregorianCalendar(LocalDateTime localDateTime) {
		if (localDateTime == null) {
			return null;
		}

		GregorianCalendar gregorianCalendar = GregorianCalendar.from(localDateTime.atZone(ZoneId.systemDefault()));
		return DATATYPE_FACTORY.newXMLGregorianCalendar(gregorianCalendar);
	}

	public static LocalDateTime toLocalDateTime(XMLGregorianCalendar xmlGregorianCalendar) {
		return xmlGregorianCalendar == null ? null :
				xmlGregorianCalendar.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
	}
}
