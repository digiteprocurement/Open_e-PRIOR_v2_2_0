package eu.europa.ec.cipa.adapter.dao.db;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.europa.ec.cipa.adapter.model.etrustex.Log;
import eu.europa.ec.cipa.adapter.model.oxalys.OxalysMessage;

public class OxalysDBDAO extends AbstractDBDAO {

	private static final String SELECT_MESSAGE_BY_ID = "SELECT ID,  MESSAGEID,  DOCUMENTTYPEIDENTIFIER,    RECIPIENTID,    SENDERID,    RECEIVEDTIMESTAMP,    CONTENT FROM oxa_messages where ID = ?";
	//Oracle statement
	/*private static final String INSERT_LOG = "INSERT INTO OXA_ADAPTER_LOG (LOG_ID, LOG_TYPE, LOG_OPERATION, LOG_DESCRIPTION, LOG_VALUE, LOG_CORRELATION_ID, LOG_DOCUMENT_ID, LOG_DOCUMENT_TYPE_CD, CRE_DT, CRE_ID, MOD_DT, MOD_ID, LOG_MODULE)   "
				+ "VALUES   (oxa_adapter_log_seq.nextval, ?, ?, ?, ?, ?, ?, ?, sysdate, 'AS2ADAPTER', sysdate, 'AS2ADAPTER', 'AS2ADAPTER')";*/
	//MySql statement
	private static final String INSERT_LOG = "INSERT INTO OXA_ADAPTER_LOG (LOG_TYPE, LOG_OPERATION, LOG_DESCRIPTION, LOG_VALUE, LOG_CORRELATION_ID, LOG_DOCUMENT_ID, LOG_DOCUMENT_TYPE_CD, CRE_DT, CRE_ID, MOD_DT, MOD_ID, LOG_MODULE)   "
					+ "VALUES   (?, ?, ?, ?, ?, ?, ?, sysdate(), 'AS2ADAPTER', sysdate(), 'AS2ADAPTER', 'AS2ADAPTER')";
	private static final String SELECT_METADATA_BY_KEY = "SELECT md_value FROM OXA_ADAPTER_METADATA WHERE md_type = ?";
	
	private static Logger logger = Logger.getLogger(OxalysDBDAO.class);

	public OxalysDBDAO(JdbcTemplate jdbcTemplate) {
		super(jdbcTemplate);
	}
	
	public String getMetadata(String key) {
		logger.debug("Start getMetadata");
		logger.debug("Search for " + key);
		String value = getJdbcTemplate().queryForObject(SELECT_METADATA_BY_KEY,
				new Object[] { key }, String.class);
		logger.debug("Found value" + value);
		logger.debug("Stop getMetadata");
		return value;
	}

	public OxalysMessage findMessageReceivedById(String id) {
		logger.debug("Start findMessageReceivedById");
		logger.debug("Search for id:" + id);
		OxalysMessage msg = getJdbcTemplate().queryForObject(
				SELECT_MESSAGE_BY_ID, new Object[] { id }, new MessageMapper());
		logger.debug("Found=" + msg);
		logger.debug("Stop findMessageReceivedById");
		return msg;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void log(Log log) {
		getJdbcTemplate().update(INSERT_LOG, log.getType(), log.getOperation(),
				log.getDescription(), log.getValue(), log.getCorrelation(),
				log.getDocumentId(), log.getTypeCode());
	}

	private static class MessageMapper implements RowMapper<OxalysMessage> {

		@Override
		public OxalysMessage mapRow(ResultSet rs, int rowNum)
				throws SQLException {

			String id = rs.getString("ID");
			String type = rs.getString("DOCUMENTTYPEIDENTIFIER");
			String senderId = rs.getString("SENDERID");
			String receiverId = rs.getString("RECIPIENTID");
			String messageId = rs.getString("MESSAGEID");
			java.sql.Date date = rs.getDate("RECEIVEDTIMESTAMP");
			Date receiptionDate = date != null ? new Date(date.getTime())
					: null;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			InputStream in = rs.getBinaryStream("CONTENT");
			if (in != null) {
				int count = 0;
				byte[] buffer = new byte[1024];
				try {
					while ((count = in.read(buffer)) > 0) {
						baos.write(buffer, 0, count);
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			byte[] content = baos.toByteArray();

			OxalysMessage aMessage = new OxalysMessage(id, type, senderId,
					receiverId, messageId, receiptionDate, content);
			return aMessage;
		}

	}

}
