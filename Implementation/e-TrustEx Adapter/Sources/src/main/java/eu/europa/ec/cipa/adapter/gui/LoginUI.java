package eu.europa.ec.cipa.adapter.gui;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.vaadin.annotations.Theme;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import eu.europa.ec.cipa.adapter.utils.Constants;

@Theme("chameleon")
public class LoginUI extends UI {

	private static final String TITLE_LOGIN = "AS2 - e-TrustEx Adapter Administration Console - Login";
	private static final String NAME = "Name";
	private static final String PASSWORD = "Password";
	private static final String LOGIN = "Login";
	private static final String ETRUSTEX_PEPPOL_ADAPTER_APP_MAIN = "/etrustex-peppol-adapter/app/main";
	private static final String ERROR = "Error";
	private static final String INVALID_USER_NAME_OR_PASSWORD = "Invalid user name or password";
	private static final String USER = "user";


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void init(final VaadinRequest request) {

		String title = TITLE_LOGIN;
		Page.getCurrent().setTitle(title);

		ApplicationContext ctx = WebApplicationContextUtils
				.getWebApplicationContext(VaadinServlet.getCurrent()
						.getServletContext());
		final Controller controller = ctx.getBean(Controller.class);


		Panel panel = new Panel(title);
		
		FormLayout form = new FormLayout();
		
		final TextField tfname = new TextField(NAME);
		tfname.setWidth(200, Unit.PIXELS);
		tfname.setEnabled(true);		
		form.addComponent(tfname);
		
		final PasswordField pfPassword = new PasswordField(PASSWORD);
		pfPassword.setWidth(200, Unit.PIXELS);
		pfPassword.setEnabled(true);		
		form.addComponent(pfPassword);
		
		Button loginButton = new Button(LOGIN);
		loginButton.addClickListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {

				String user= tfname.getValue();
				String password = pfPassword.getValue();
			
				if(controller.authenticate(user, password)){
					request.getWrappedSession(true).setAttribute(USER, Constants.AS2ADMIN);
					Page.getCurrent().setLocation(ETRUSTEX_PEPPOL_ADAPTER_APP_MAIN);
				}

				tfname.setValue("");
				pfPassword.setValue("");
				
		        Notification notification = new Notification(ERROR, INVALID_USER_NAME_OR_PASSWORD,
		                Type.ERROR_MESSAGE, true);
		        notification.setDelayMsec(2500);
		        notification.show(Page.getCurrent());
			}
		});
		
		form.addComponent(loginButton);
		
		panel.setContent(form);

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
