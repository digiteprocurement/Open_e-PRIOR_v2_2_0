package eu.europa.ec.cipa.adapter.model.etrustex;

public class PartyIdScheme {

	private String schemeId;
	private String iso6523;

	public PartyIdScheme(String schemeId, String iso6523) {
		super();
		this.schemeId = schemeId;
		this.iso6523 = iso6523;
	}

	public String getSchemeId() {
		return schemeId;
	}

	public void setSchemeId(String schemeId) {
		this.schemeId = schemeId;
	}

	public String getIso6523() {
		return iso6523;
	}

	public void setIso6523(String iso6523) {
		this.iso6523 = iso6523;
	}

	@Override
	public String toString() {
		return "[" + schemeId + "," + iso6523 + "]";
	}

}
