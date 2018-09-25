package eu.europa.ec.cipa.adapter.model;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Party implements Serializable {

	private static final long serialVersionUID = 1L;

	private String idValue;
	private String idScheme;

	public String getIdValue() {
		return idValue;
	}

	public void setIdValue(String idValue) {
		this.idValue = idValue;
	}

	public String getIdScheme() {
		return idScheme;
	}

	public void setIdScheme(String idScheme) {
		this.idScheme = idScheme;
	}

    @Override
    public String toString() {
        return new ToStringBuilder(this)
        .append("idValue", idValue)
        .append("idScheme", idScheme)
        .toString();

    }
}
