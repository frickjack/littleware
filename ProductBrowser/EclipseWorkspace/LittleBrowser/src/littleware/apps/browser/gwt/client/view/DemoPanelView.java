package littleware.apps.browser.gwt.client.view;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * UiBinder based refactor of GWT demo app ...
 */
public class DemoPanelView extends Composite {

	private static DemoPanelViewUiBinder uiBinder = GWT
			.create(DemoPanelViewUiBinder.class);

	interface DemoPanelViewUiBinder extends UiBinder<Widget, DemoPanelView> {
	}


	@UiField
	Label hellowLabel;
	@UiField
	public Label errorLabel;
	

	@UiField
	public Button sendButton;
	
	@UiField
	public TextBox  nameField;
	
	/**
	 * Dialog shows result of GreetingService action
	 */
	public class GreetingDialog extends DialogBox {
		public final Label textToServerLabel = new Label();
		public final HTML serverResponseLabel = new HTML();
		final VerticalPanel dialogVPanel = new VerticalPanel();
		public final Button closeButton = new Button("Close");

		
		
		{
			setText("Remote Procedure Call");
			setAnimationEnabled(true);

			// We can set the id of a widget by accessing its Element
			closeButton.getElement().setId("closeButton");
			dialogVPanel.addStyleName("dialogVPanel");
			dialogVPanel.add(new HTML("<b>Sending name to the server:</b>"));
			dialogVPanel.add(textToServerLabel);
			dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
			dialogVPanel.add(serverResponseLabel);
			dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
			dialogVPanel.add(closeButton);
			setWidget(dialogVPanel);

			// Add a handler to close the DialogBox
			closeButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					GreetingDialog.this.hide();
					sendButton.setEnabled(true);
					sendButton.setFocus(true);
				}
			});
		}		
	}
	
	public final GreetingDialog dialogBox = new GreetingDialog();
	
	public DemoPanelView() {
		initWidget(uiBinder.createAndBindUi(this));
		hellowLabel.setText( "Reuben" );
		sendButton.setText( "Send" );
		// We can add style names to widgets
		sendButton.addStyleName("sendButton");
		nameField.setText("GWT User");
	}


	public DemoPanelView(String firstName) {
		this();
		//initWidget(uiBinder.createAndBindUi(this));
		hellowLabel.setText(firstName);
	}

	/*
	@UiHandler("button")
	void onClick(ClickEvent e) {
		Window.alert("Hello!");
	}

	public void setText(String text) {
		button.setText(text);
	}

	public String getText() {
		return button.getText();
	}
*/
}
