package de.swa.ui.command;

import javax.swing.JOptionPane;

import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

import de.swa.gmaf.SessionFactory;
import de.swa.gmaf.api.GMAF_Facade;
import de.swa.ui.LoginDialog;
import de.swa.ui.ProgressFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** class to encapsulate the Export Command **/

public class LoginCommand extends AbstractCommand {
	private static final Logger log = LoggerFactory.getLogger(LoginCommand.class);
	private String host, port, pass;

	public LoginCommand(String host, String port, String pass) {
		this.host = host;
		this.port = port;
		this.pass = pass;
	}

	public void execute() {
		String msg = "";
		try {
			JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
			factory.setServiceClass(GMAF_Facade.class);
			factory.setAddress("http://" + host + ":" + port + "/gmaf/gmafApi");
			GMAF_Facade f = (GMAF_Facade) factory.create();
			String sid = f.getAuthToken(pass);
			System.out.println("SID: " + sid);

			SessionFactory.api = f;
			SessionFactory.sessionId = sid;

			if (sid != null) {
				//new ProgressFrame();
				ProgressFrame.getInstance();
				return;
			}
		} catch (Exception ex) {
			log.error("Exception", ex);
			System.out.println("Exception: " + ex.getMessage());
			ex.printStackTrace();
			msg = "\n" + ex.getMessage();
		}
		JOptionPane.showMessageDialog(null, "Login not successful. Please check your details." + msg);
		new LoginDialog();
	}
}
