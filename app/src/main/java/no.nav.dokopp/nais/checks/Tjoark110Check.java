package no.nav.dokopp.nais.checks;

import no.nav.dokopp.config.fasit.ArkiverDokumentproduksjonV1Alias;
import no.nav.dokopp.nais.selftest.AbstractSelftest;
import no.nav.dokopp.nais.selftest.ApplicationNotReadyException;
import no.nav.dokopp.nais.selftest.Ping;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverDokumentproduksjonV1;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class Tjoark110Check extends AbstractSelftest {
	
	public static final String ARKIVER_DOKUMENTPRODUKSJON_V1 = "ArkiverDokumentproduksjon_v1";
	private final ArkiverDokumentproduksjonV1 arkiverDokumentproduksjonV1;
	
	@Inject
	public Tjoark110Check(ArkiverDokumentproduksjonV1 arkiverDokumentproduksjonV1, ArkiverDokumentproduksjonV1Alias arkiverDokumentproduksjonV1Alias) {
		super(Ping.Type.Soap,
				ARKIVER_DOKUMENTPRODUKSJON_V1,
				arkiverDokumentproduksjonV1Alias.getEndpointurl(),
				arkiverDokumentproduksjonV1Alias.getDescription() == null ? ARKIVER_DOKUMENTPRODUKSJON_V1 : arkiverDokumentproduksjonV1Alias
						.getDescription());
		this.arkiverDokumentproduksjonV1 = arkiverDokumentproduksjonV1;
	}
	
	@Override
	protected void doCheck() {
		try {
			arkiverDokumentproduksjonV1.ping();
		} catch (Exception e) {
			throw new ApplicationNotReadyException("Could not ping tjoark110", e);
		}
	}
	
}
