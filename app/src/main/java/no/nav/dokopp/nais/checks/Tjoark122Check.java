package no.nav.dokopp.nais.checks;

import no.nav.dokopp.config.fasit.DokumentproduksjonInfoV1Alias;
import no.nav.dokopp.nais.selftest.AbstractDependencyCheck;
import no.nav.dokopp.nais.selftest.ApplicationNotReadyException;
import no.nav.dokopp.nais.selftest.DependencyType;
import no.nav.dokopp.nais.selftest.Importance;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.DokumentproduksjonInfoV1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class Tjoark122Check extends AbstractDependencyCheck {

	public static final String DOKUMENTPRODUKSJON_INFO_V1 = "DokumentproduksjonInfo_v1";
	private final DokumentproduksjonInfoV1 dokumentproduksjonInfoV1;

	@Autowired
	public Tjoark122Check(DokumentproduksjonInfoV1 dokumentproduksjonInfoV1, DokumentproduksjonInfoV1Alias dokumentproduksjonInfoV1Alias) {
		super(DependencyType.SOAP, "Tjoark220", dokumentproduksjonInfoV1Alias.getEndpointurl(), Importance.WARNING);
		this.dokumentproduksjonInfoV1 = dokumentproduksjonInfoV1;
	}

	@Override
	protected void doCheck() {
		try {
			dokumentproduksjonInfoV1.ping();
		} catch (Exception e) {
			throw new ApplicationNotReadyException("Could not ping tjoark122", e);
		}
	}

}
