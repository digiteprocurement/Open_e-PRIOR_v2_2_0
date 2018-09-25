package eu.europa.ec.cipa.adapter.dao.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import eu.europa.ec.cipa.adapter.model.etrustex.PartyIdScheme;

public class EtrustexDBDAO extends AbstractDBDAO {

	private static final String SELECT_SCHEME_BY_ISO_CODE = "select ISC_SCHEME_ID,ISC_ISO6523 from etrustex.ETR_TB_ID_SCHEME where ISC_ISO6523 = ?";
	private static final String SELECT_PARTY_ID = "select count(*) from etrustex.ETR_TB_PARTY_ID where PID_IDENTIFIER_VALUE = ? and PID_IDENTIFIER_SCHEME = ?";
		
	private static Logger logger = Logger.getLogger(EtrustexDBDAO.class);

	public EtrustexDBDAO(JdbcTemplate jdbcTemplate) {
		super(jdbcTemplate);
	}

	public PartyIdScheme findPartyIdSchemeByIsoCode(String uuid, String isoCode) {
		logger.debug(uuid + ": Start findPartyIdSchemeByIsoCode");
		logger.debug(uuid + ": Search for " + isoCode);
		PartyIdScheme id = getJdbcTemplate().queryForObject(
				SELECT_SCHEME_BY_ISO_CODE, new Object[] { isoCode },
				new PartyIdSchemeMapper());
		logger.debug(uuid + ": Found id scheme" + id);
		logger.debug(uuid + ": Stop findPartyIdSchemeByIsoCode");
		return id;
	}

	public boolean isPartyExist(String uuid, String id, String scheme) {
		logger.debug(uuid + ": Start isPartyExist, Party id:"+ id + ", Party scheme: " + scheme);
		//in ETX, in the table ETR_TB_PARTY_ID, the schemeID EC:ORG is stored as EC_ORG
		String partySchemeEtrustExInternal = scheme.replaceAll(":", "_");
		logger.debug(uuid + ": Party scheme eTrustEx internal: " + partySchemeEtrustExInternal);
		boolean found = false;
		Integer count = getJdbcTemplate().queryForObject(SELECT_PARTY_ID,
				new Object[] { id, partySchemeEtrustExInternal }, Integer.class);
		found = (count > 0); // db constraint impose unicity anyway
		logger.debug(uuid + ": Found: " + found);
		logger.debug("Stop isPartyExist");
		return found;
	}

	private static class PartyIdSchemeMapper implements
			RowMapper<PartyIdScheme> {

		@Override
		public PartyIdScheme mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			logger.debug("Start Map row");
			String id = rs.getString("ISC_SCHEME_ID");
			logger.debug("Id=" + id);
			String iso = rs.getString("ISC_ISO6523");
			logger.debug("Iso=" + iso);
			PartyIdScheme aPartyIdScheme = new PartyIdScheme(id, iso);
			logger.debug("Stop Map row");
			return aPartyIdScheme;
		}

	}

}
