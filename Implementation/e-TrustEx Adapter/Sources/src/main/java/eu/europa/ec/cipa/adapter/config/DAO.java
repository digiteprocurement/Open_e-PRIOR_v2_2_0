package eu.europa.ec.cipa.adapter.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import eu.europa.ec.cipa.adapter.dao.db.EtrustexDBDAO;
import eu.europa.ec.cipa.adapter.dao.db.OxalysDBDAO;
import eu.europa.ec.cipa.adapter.dao.ws.AttachmentWSDAO;
import eu.europa.ec.cipa.adapter.dao.ws.CreditNoteWSDAO;
import eu.europa.ec.cipa.adapter.dao.ws.InvoiceWSDAO;
import eu.europa.ec.cipa.adapter.dao.ws.SendServicesEtrustExImpl;

@Configuration
public class DAO {

	@Autowired
	private DataSource oxalysDs;

	@Autowired
	private DataSource etrustexDs;

	@Bean
	public JdbcTemplate oxalysJdbcTemplate() {
		final JdbcTemplate template = new JdbcTemplate(oxalysDs);
		return template;
	}

	@Bean
	public JdbcTemplate etrustexJdbcTemplate() {
		final JdbcTemplate template = new JdbcTemplate(etrustexDs);
		return template;
	}

	@Bean
	public EtrustexDBDAO etrustexDBDAO() {
		final EtrustexDBDAO dao = new EtrustexDBDAO(etrustexJdbcTemplate());
		return dao;
	}

	@Bean
	public OxalysDBDAO oxalysDBDAO() {
		final OxalysDBDAO dao = new OxalysDBDAO(oxalysJdbcTemplate());
		return dao;
	}

	@Bean
	public InvoiceWSDAO invoiceWSDAO() {
		InvoiceWSDAO dao = new InvoiceWSDAO();
		dao.setOxalysDBDAO(oxalysDBDAO());
		return dao;
	}

	@Bean
	public CreditNoteWSDAO creditNoteWSDAO() {
		CreditNoteWSDAO dao = new CreditNoteWSDAO();
		dao.setOxalysDBDAO(oxalysDBDAO());
		return dao;
	}

	@Bean
	public AttachmentWSDAO attachmentWSDAO() {
		AttachmentWSDAO dao = new AttachmentWSDAO();
		dao.setOxalysDBDAO(oxalysDBDAO());
		return dao;
	}
	
	@Bean
	public SendServicesEtrustExImpl sendServicesEtrustExImpl() {
		SendServicesEtrustExImpl dao = new SendServicesEtrustExImpl();
		dao.setOxalysDBDAO(oxalysDBDAO());
		return dao;
	}	
}
