package no.nav.dokopp.config.cxf;

import org.apache.wss4j.common.ext.WSPasswordCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;

/**
 * Sets the password for the Usernametoken WS-Security
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class SystemuserPasswordCallback implements CallbackHandler {

	private final String systemUserPassword;

	public SystemuserPasswordCallback(String systemUserPassword) {
		this.systemUser***REMOVED***;
	}

	@Override
	public void handle(Callback[] callbacks) {
		WS***REMOVED***];

		wsPasswordCallback.setPassword(systemUserPassword);
	}
}
