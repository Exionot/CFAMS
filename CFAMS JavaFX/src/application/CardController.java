package application;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class CardController implements Initializable{
	@FXML private Label cardIdLabel, cardStudentIdLabel, cardSummaryLabel;
	
	public void setCardData(int id, boolean isPrivate, String studentId, String summary) {
		String shareLabel = isPrivate ? " (Private)" : " (Public)";
		cardIdLabel.setText("BA-"+String.format("%05d", id) + shareLabel);
		cardStudentIdLabel.setText(studentId);
		cardSummaryLabel.setText(summary);
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
	}
}
