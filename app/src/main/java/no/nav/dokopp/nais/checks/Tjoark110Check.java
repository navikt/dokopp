package no.nav.dokopp.nais.checks;

import no.nav.dokopp.config.fasit.ArkiverDokumentproduksjonV1Alias;
import no.nav.dokopp.nais.selftest.AbstractDependencyCheck;
import no.nav.dokopp.nais.selftest.ApplicationNotReadyException;
import no.nav.dokopp.nais.selftest.DependencyType;
import no.nav.dokopp.nais.selftest.Importance;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverDokumentproduksjonV1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class Tjoark110Check extends AbstractDependencyCheck {

	public static final String ARKIVER_DOKUMENTPRODUKSJON_V1 = "ArkiverDokumentproduksjon_v1";
	private final ArkiverDokumentproduksjonV1 arkiverDokumentproduksjonV1;

	@Autowired
	public Tjoark110Check(ArkiverDokumentproduksjonV1 arkiverDokumentproduksjonV1, ArkiverDokumentproduksjonV1Alias arkiverDokumentproduksjonV1Alias) {
		super(DependencyType.SOAP, "Tjoark110", arkiverDokumentproduksjonV1Alias.getEndpointurl(), Importance.WARNING);
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
