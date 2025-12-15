package util;

import java.io.File;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class FancyAlert extends Alert{
	
	private static final Image ALERT_ICON = new Image(new File(application.Main.RESOURCE_DIR + "icon.png").toURI().toString());

	public FancyAlert(AlertType alert) {
		super(alert);
		getDialogPane().getStylesheets().add(application.Main.GLOBAL_CSS);
		
		Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.getIcons().setAll(ALERT_ICON);
		
		Button okButton = (Button) this.getDialogPane().lookupButton(ButtonType.OK);
		Button cancelButton = (Button) this.getDialogPane().lookupButton(ButtonType.CANCEL);
		
		try {
			okButton.getStyleClass().add("ok-button");
			cancelButton.getStyleClass().add("cancel-button");
		} catch (NullPointerException e) { return; }
	}
	
	public FancyAlert(AlertType alert, String title, String message) {
		super(alert);
		getDialogPane().getStylesheets().add(application.Main.GLOBAL_CSS);
		
		Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.getIcons().setAll(ALERT_ICON);
        
        this.setTitle(title);
		this.setHeaderText(message);
		
		Button okButton = (Button) this.getDialogPane().lookupButton(ButtonType.OK);
		Button cancelButton = (Button) this.getDialogPane().lookupButton(ButtonType.CANCEL);
		
		try {
			okButton.getStyleClass().add("ok-button");
			cancelButton.getStyleClass().add("cancel-button");
		} catch (NullPointerException e) { return; }
	}
}
