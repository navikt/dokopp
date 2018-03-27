package no.nav.dokopp.nais.checks;

import no.nav.dokopp.config.fasit.DokumentproduksjonInfoV1Alias;
import no.nav.dokopp.nais.selftest.AbstractSelftest;
import no.nav.dokopp.nais.selftest.ApplicationNotReadyException;
import no.nav.dokopp.nais.selftest.Ping;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.DokumentproduksjonInfoV1;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class Tjoark122Check extends AbstractSelftest {
	
	public static final String DOKUMENTPRODUKSJON_INFO_V1 = "DokumentproduksjonInfo_v1";
	private final DokumentproduksjonInfoV1 dokumentproduksjonInfoV1;
	
	@Inject
	public Tjoark122Check(DokumentproduksjonInfoV1 dokumentproduksjonInfoV1, DokumentproduksjonInfoV1Alias dokumentproduksjonInfoV1Alias) {
		super(Ping.Type.Soap,
				DOKUMENTPRODUKSJON_INFO_V1,
				dokumentproduksjonInfoV1Alias.getEndpointurl(),
				dokumentproduksjonInfoV1Alias.getDescription() == null ? DOKUMENTPRODUKSJON_INFO_V1 : dokumentproduksjonInfoV1Alias
						.getDescription());
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
