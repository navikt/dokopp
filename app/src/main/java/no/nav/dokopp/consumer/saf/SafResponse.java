package no.nav.dokopp.consumer.saf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.util.List;

@Data
@Builder
public class SafResponse {
	private SafHentJournalpost data;
	private List<SafError> errors;

	@Data
	@Builder
	public static class SafHentJournalpost{
		private SafJournalpost journalpost;
	}

	@Data
	@Builder
	public static class SafJournalpost {
		private String journalfoerendeEnhet;
		private String tema;
		private String antallRetur;
		private Sak sak;
		private Bruker bruker;
		private AvsenderMottaker avsenderMottaker;

		@Value
		@Builder
		public static class AvsenderMottaker {
			private String id;
			private String type;
		}

		@Value
		@Builder
		public static class Sak {
			private String arkivsaksnummer;
			private String arkivsaksystem;
		}

		@Value
		@Builder
		public static class Bruker {
			private String id;
			private String type;
		}
	}

	@Data
	@JsonIgnoreProperties({"locations", "path"})
	public static class SafError {
		private String message;
		private SafErrorExtension extensions;
	}

	@Data
	public static class SafErrorExtension {
		private String code;
		private String classification;
	}
}
