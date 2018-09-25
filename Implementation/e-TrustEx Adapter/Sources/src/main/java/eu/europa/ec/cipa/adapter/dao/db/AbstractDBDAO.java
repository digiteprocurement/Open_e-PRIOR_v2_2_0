package eu.europa.ec.cipa.adapter.dao.db;

import org.springframework.jdbc.core.JdbcTemplate;

public abstract class AbstractDBDAO {

	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public AbstractDBDAO(JdbcTemplate jdbcTemplate) {
		this.setJdbcTemplate(jdbcTemplate);
	}

}
