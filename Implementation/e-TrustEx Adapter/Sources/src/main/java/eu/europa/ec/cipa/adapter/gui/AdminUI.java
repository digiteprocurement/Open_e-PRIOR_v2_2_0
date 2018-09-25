package eu.europa.ec.cipa.adapter.gui;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.vaadin.annotations.Theme;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import eu.europa.ec.cipa.adapter.gui.panel.TaskPanel;
import eu.europa.ec.cipa.adapter.gui.panel.TasksListPanel;

@Theme("chameleon")
public class AdminUI extends UI {

	private static final String TITLE = "AS2 - e-TrustEx Adapter Administration Console";

	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void init(VaadinRequest request) {

		String title = TITLE;
		Page.getCurrent().setTitle(title);

		ApplicationContext ctx = WebApplicationContextUtils
				.getWebApplicationContext(VaadinServlet.getCurrent()
						.getServletContext());
		Controller controller = ctx.getBean(Controller.class);

		Panel panel = new Panel(title);

		VerticalLayout hl = new VerticalLayout();
		panel.setContent(hl);

		final TasksListPanel tlpanel = new TasksListPanel(controller);		
		tlpanel.updateTaskList();
		final TaskPanel tpanel = new TaskPanel(controller);		
		tlpanel.addTaskSelectObserver(tpanel);		
		tpanel.addTaskUpdateObserver(tlpanel);

		hl.addComponent(tlpanel);
		hl.addComponent(tpanel);

		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();

		panel.setHeight(70, Unit.PERCENTAGE);
		panel.setWidth(70, Unit.PERCENTAGE);
		layout.addComponent(panel);
		layout.setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
		layout.setExpandRatio(panel, 0.8f);

		setContent(layout);

	}

}
