package no.nav.dokopp.nais.checks;

import no.nav.dokopp.config.fasit.ArkiverDokumentmottakV2Alias;
import no.nav.dokopp.nais.selftest.AbstractSelftest;
import no.nav.dokopp.nais.selftest.ApplicationNotReadyException;
import no.nav.dokopp.nais.selftest.Ping;
//import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.ArkiverDokumentmottakV2;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class Tjoark203Check {
//		extends AbstractSelftest {

//	public static final String ARKIVER_DOKUMENTMOTTAK_V2 = "ArkiverDokumentmottak_v2";
//	private final ArkiverDokumentmottakV2 arkiverDokumentmottakV2;
//
//	@Inject
//	public Tjoark203Check(ArkiverDokumentmottakV2 arkiverDokumentmottakV2, ArkiverDokumentmottakV2Alias arkiverDokumentmottakV2Alias) {
//		super(Ping.Type.Soap,
//				ARKIVER_DOKUMENTMOTTAK_V2,
//				arkiverDokumentmottakV2Alias.getEndpointurl(),
//				arkiverDokumentmottakV2Alias.getDescription() == null ? ARKIVER_DOKUMENTMOTTAK_V2 : arkiverDokumentmottakV2Alias.getDescription());
//		this.arkiverDokumentmottakV2 = arkiverDokumentmottakV2;
//	}
//
//	@Override
//	protected void doCheck() {
//		try {
//			arkiverDokumentmottakV2.ping();
//		} catch(Exception e) {
//			throw new ApplicationNotReadyException("Could not ping tjoark203", e);
//		}
//	}

}
