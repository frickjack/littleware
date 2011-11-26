package littleware.apps.browser.gwt.client.controller.action;

import littleware.apps.browser.gwt.controller.GreetingServiceAsync;
import littleware.apps.browser.gwt.model.internal.FieldVerifier;
import littleware.apps.browser.gwt.client.view.DemoPanelView;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * GreetingService UI handler
 */
public class GreetingAction implements ClickHandler, KeyUpHandler {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";
	
	private final GreetingServiceAsync greetingService;
	private final DemoPanelView  demoView;


	public GreetingAction( GreetingServiceAsync greetingService,
			DemoPanelView demoView
			 ) {
		this.greetingService = greetingService;
		this.demoView = demoView;
	}

	/**
	 * Fired when the user clicks on the sendButton.
	 */
	public void onClick(ClickEvent event) {
		sendNameToServer();
	}

	/**
	 * Fired when the user types in the nameField.
	 */
	public void onKeyUp(KeyUpEvent event) {
		if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
			sendNameToServer();
		}
	}

	/**
	 * Send the name from the nameField to the server and wait for a response.
	 */
	private void sendNameToServer() {
		// First, we validate the input.
		demoView.errorLabel.setText("");
		final String textToServer = demoView.nameField.getText();
		if (!FieldVerifier.isValidName(textToServer)) {
			demoView.errorLabel.setText("Please enter at least four characters");
			return;
		}

		// Then, we send the input to the server.
		demoView.sendButton.setEnabled(false);
		demoView.dialogBox.textToServerLabel.setText( textToServer );
		demoView.dialogBox.serverResponseLabel.setText("");
		greetingService.greetServer(textToServer, new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				// Show the RPC error message to the user
				demoView.dialogBox.setText("Remote Procedure Call - Failure");
				demoView.dialogBox.serverResponseLabel.addStyleName("serverResponseLabelError");
				demoView.dialogBox.serverResponseLabel.setHTML(SERVER_ERROR);
				demoView.dialogBox.center();
				demoView.dialogBox.closeButton.setFocus(true);
			}

			public void onSuccess(String result) {
				demoView.dialogBox.setText("Remote Procedure Call");
				demoView.dialogBox.serverResponseLabel.removeStyleName("serverResponseLabelError");
				demoView.dialogBox.serverResponseLabel.setHTML(result);
				demoView.dialogBox.center();
				demoView.dialogBox.closeButton.setFocus(true);
			}
		});
	}
}
