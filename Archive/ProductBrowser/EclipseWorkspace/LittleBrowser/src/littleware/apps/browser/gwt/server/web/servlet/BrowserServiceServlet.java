package littleware.apps.browser.gwt.server.web.servlet;

import java.util.Map;

import littleware.apps.browser.gwt.controller.BrowserService;
import littleware.apps.browser.gwt.model.GwtAsset;
import littleware.apps.browser.gwt.model.GwtOption;
import littleware.apps.browser.gwt.model.GwtUUID;
import littleware.apps.browser.gwt.model.SimpleBrowserModel;
import littleware.apps.browser.gwt.model.internal.FieldVerifier;
import littleware.apps.browser.gwt.server.internal.MockBrowserService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class BrowserServiceServlet extends RemoteServiceServlet implements
		BrowserService {
	
	private final MockBrowserService mock = new MockBrowserService();

	private String greetServer(String input) throws IllegalArgumentException {
		// Verify that the input is valid. 
		if (!FieldVerifier.isValidName(input)) {
			// If the input is not valid, throw an IllegalArgumentException back to
			// the client.
			throw new IllegalArgumentException(
					"Name must be at least 4 characters long");
		}

		String serverInfo = getServletContext().getServerInfo();
		String userAgent = getThreadLocalRequest().getHeader("User-Agent");

		// Escape data from the client to avoid cross-site script vulnerabilities.
		input = escapeHtml(input);
		userAgent = escapeHtml(userAgent);

		return "Hello, " + input + "!<br><br>I am running " + serverInfo
				+ ".<br><br>It looks like you are using:<br>" + userAgent;
	}

	/**
	 * Escape an html string. Escaping data received from the client helps to
	 * prevent cross-site script vulnerabilities.
	 * 
	 * @param html the html string to escape
	 * @return the escaped string
	 */
	private String escapeHtml(String html) {
		if (html == null) {
			return null;
		}
		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
	}


	@Override
	public GwtOption<GwtAsset> getAsset(GwtUUID id) {
		return mock.getAsset(id);
	}

	@Override
	public Map<String, GwtUUID> getHomeIds() {
		return mock.getHomeIds();
	}

	@Override
	public Map<String, GwtUUID> getAssetsUnder(GwtUUID parentId) {
		return mock.getAssetsUnder(parentId);
	}

	@Override
	public SimpleBrowserModel loadBrowserModel(GwtUUID assetId) {
		// TODO Auto-generated method stub
		return mock.loadBrowserModel( assetId );
	}
}
