package application;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import util.FancyAlert;
import javafx.scene.control.Alert.AlertType;

public class AddFeedbackController implements Initializable{
	@FXML private Label titleLabel;
	@FXML private Button backButton, submitButton, confirmFeedbackButton, invalidateFeedbackButton, resolveFeedbackButton, deleteButton;
	@FXML private TextField authorTextField, studentIdTextField, summaryTextField, createdTextField, updatedTextField, confirmedTextField, votesTextField;
	@FXML private ComboBox<util.Feedback.Type> feedbackTypeComboBox;
	@FXML private ComboBox<util.Feedback.Label> feedbackLabelComboBox;
	@FXML private RadioButton privateOnlyRadio, publicOnlyRadio;
	@FXML private TextArea descriptionTextField, feedbackResolvedMessageTextArea;
	@FXML private VBox openedAsFeedbackContainer, addFeedbackBody;
	@FXML private HBox detailsContainer, updatedContainer, confirmedContainer, votesContainer, actionButtonContainer;
	@FXML private BorderPane feedbackConfirmationContainer, feedbackResolvedMessageContainer;
	@FXML private Separator feedbackResolvedMessageSeparator;
	
	private boolean noPrompt;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		Platform.runLater(() -> {
			if (util.Session.getSession().getUser().getRole() != util.User.UserRole.ADMIN) {
				actionButtonContainer.getChildren().remove(deleteButton);
			}
		});
		
		authorTextField.setText(util.Session.getSession().getUser().getUsername());
		studentIdTextField.setText(util.Session.getSession().getUser().getStudentId());
		feedbackTypeComboBox.getItems().addAll(util.Feedback.Type.values());
		feedbackTypeComboBox.setConverter(new StringConverter<util.Feedback.Type>() {
			@Override
			public String toString(util.Feedback.Type arg0) {
				return (arg0 != null) ? arg0.getValue() : "";
			}
			
			@Override
			public util.Feedback.Type fromString(String arg0) {
				return null;
			}
		});
		feedbackLabelComboBox.setDisable(false);
		feedbackLabelComboBox.setConverter(new StringConverter<util.Feedback.Label>() {
			@Override
			public String toString(util.Feedback.Label arg0) {
				return (arg0 != null) ? arg0.getName() : "";
			}
			
			@Override
			public util.Feedback.Label fromString(String arg0) {
				return null;
			}
		});
		privateOnlyRadio.setSelected(true);
		detailsContainer.getChildren().remove(openedAsFeedbackContainer);
		addFeedbackBody.getChildren().remove(feedbackConfirmationContainer);
		addFeedbackBody.getChildren().remove(feedbackResolvedMessageContainer);
		addFeedbackBody.getChildren().remove(feedbackResolvedMessageSeparator);
	}
	
	public void goToDashboard(ActionEvent e) throws IOException {
		if (!noPrompt) {
			FancyAlert alert = new FancyAlert(AlertType.CONFIRMATION);
			alert.setTitle("Unfinished Feedback");
			alert.setHeaderText("Feedback has not been submitted. Are you sure you want to exit?");
			((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(false);
			((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton(true);
			if (alert.showAndWait().get() == ButtonType.OK){
				util.SceneChanger.closeWindow(e);
			}
		}else {
			noPrompt = false;
			util.SceneChanger.closeWindow(e);
		}
		
	}
	
	public void swapRadioPrivate() {
		publicOnlyRadio.setSelected(false);
	}
	
	public void swapRadioPublic() {
		privateOnlyRadio.setSelected(false);
	}
	
	public void changeLabel(ActionEvent e) {
		util.Feedback.Label[] selectedLabels = {};
		
		switch (feedbackTypeComboBox.getSelectionModel().getSelectedItem()) {
		case util.Feedback.Type.ACADEMIC:
			selectedLabels = new util.Feedback.Label[]{ 
		            util.Feedback.Label.GENERAL_ACADEMIC, 
		            util.Feedback.Label.CC_101, 
		            util.Feedback.Label.CC_102, 
		            util.Feedback.Label.HUM_1, 
		            util.Feedback.Label.NSTP_1, 
		            util.Feedback.Label.RIZAL, 
		            util.Feedback.Label.SOCSCI_3, 
		            util.Feedback.Label.WS_101 
		        };
			break;
		case util.Feedback.Type.FACILITY:
			selectedLabels = new util.Feedback.Label[]{ 
					util.Feedback.Label.GENERAL_FACILITY,
					util.Feedback.Label.ROOM_101, util.Feedback.Label.ROOM_102, util.Feedback.Label.ROOM_103, util.Feedback.Label.ROOM_104,
					util.Feedback.Label.ROOM_105, util.Feedback.Label.ROOM_106, util.Feedback.Label.ROOM_107, util.Feedback.Label.ROOM_108,
					util.Feedback.Label.ROOM_201, util.Feedback.Label.ROOM_202, util.Feedback.Label.ROOM_203, util.Feedback.Label.ROOM_204,
					util.Feedback.Label.ROOM_205, util.Feedback.Label.ROOM_206, util.Feedback.Label.ROOM_207, util.Feedback.Label.ROOM_208,
					util.Feedback.Label.ROOM_301, util.Feedback.Label.ROOM_302, util.Feedback.Label.ROOM_303, util.Feedback.Label.ROOM_304,
					util.Feedback.Label.ROOM_305, util.Feedback.Label.ROOM_402, util.Feedback.Label.ROOM_403, util.Feedback.Label.ROOM_404,
					util.Feedback.Label.ROOM_405, util.Feedback.Label.ROOM_406, util.Feedback.Label.ROOM_407, util.Feedback.Label.ROOM_408
		        };
			break;
		case util.Feedback.Type.SAFETY:
			selectedLabels = new util.Feedback.Label[]{ 
					util.Feedback.Label.GENERAL_SAFETY, 
					util.Feedback.Label.HAZARD, 
//					util.Feedback.Label.INCIDENT, 
					util.Feedback.Label.SECURITY
			};
			break;
		case util.Feedback.Type.SUGGESTION:
			selectedLabels = new util.Feedback.Label[]{ 
					util.Feedback.Label.GENERAL_SUGGESTION, 
					util.Feedback.Label.IMPROVEMENT, 
//					util.Feedback.Label.IDEA, 
//					util.Feedback.Label.COMMENT
			};
			break;
		case util.Feedback.Type.TECHNOLOGY:
			selectedLabels = new util.Feedback.Label[]{ 
					util.Feedback.Label.GENERAL_TECH, 
					util.Feedback.Label.SOFTWARE, 
					util.Feedback.Label.HARDWARE, 
					util.Feedback.Label.NETWORK
			};
			break;
		default:
			feedbackLabelComboBox.getItems().clear();
			feedbackLabelComboBox.setDisable(true);
		}
		
		if (feedbackTypeComboBox.getSelectionModel().getSelectedItem() != null) {
			feedbackLabelComboBox.setDisable(false);
			feedbackLabelComboBox.getItems().clear();
			feedbackLabelComboBox.getItems().addAll(selectedLabels);
		}
	}
	
	public void submitFeedback(ActionEvent e) throws IOException {
		if (feedbackTypeComboBox.getValue() == null ||
				feedbackLabelComboBox.getValue() == null ||
				(!privateOnlyRadio.isSelected() && !publicOnlyRadio.isSelected()) ||
				summaryTextField.getText().isBlank() ||
				descriptionTextField.getText().isBlank()) {
			FancyAlert alert = new FancyAlert(AlertType.ERROR);

			alert.setTitle("Add Feedback");
			alert.setHeaderText("Please fill all fields before submitting.");
			alert.show();
			return;
		}
			
		boolean isFeedbackPrivate = !publicOnlyRadio.isSelected() && privateOnlyRadio.isSelected() ? true : false;

		if (!isFeedbackPrivate) {
			FancyAlert alert = new FancyAlert(AlertType.CONFIRMATION);

			alert.setTitle("Add Feedback");
			alert.setHeaderText("Are you sure to submit this feedback?");
			alert.setContentText("Feedback is set to public. Everyone will be able to see your feedback!");

			if (alert.showAndWait().get() == ButtonType.OK){
				if(!util.DatabaseConnection.isDatabaseConnected()) {
					new FancyAlert(AlertType.ERROR, "Database Connection", "Could not connect to databse...\n").showAndWait();
					return;
				}
				
				if (submitFeedbackToDB(
						util.Session.getSession().getUser().getUserId(),
						summaryTextField.getText(),
						feedbackTypeComboBox.getValue(),
						feedbackLabelComboBox.getValue(),
						descriptionTextField.getText(),
						isFeedbackPrivate
						)) {
					FancyAlert successAlert = new FancyAlert(AlertType.INFORMATION);
					successAlert.setTitle("Add Feedback");
					successAlert.setHeaderText("Feedback has successfully been added!");
					successAlert.showAndWait();
					noPrompt = true;
					goToDashboard(e);
				} else {
					FancyAlert failAlert = new FancyAlert(AlertType.ERROR);
					failAlert.setTitle("Add Feedback");
					failAlert.setHeaderText("Something went wrong when submitting feedback.");
					failAlert.setContentText("Please check if all fields are filled");
					failAlert.show();
				}
			} 
		}else {
			if(!util.DatabaseConnection.isDatabaseConnected()) {
				new FancyAlert(AlertType.ERROR, "Database Connection", "Could not connect to databse...\n").showAndWait();
				return;
			}
			
			if (submitFeedbackToDB(
					util.Session.getSession().getUser().getUserId(),
					summaryTextField.getText(),
					feedbackTypeComboBox.getValue(),
					feedbackLabelComboBox.getValue(),
					descriptionTextField.getText(),
					isFeedbackPrivate
					)) {
				FancyAlert successAlert = new FancyAlert(AlertType.INFORMATION);
				successAlert.setTitle("Add Feedback");
				successAlert.setHeaderText("Feedback has successfully been added!");
				successAlert.showAndWait();
				noPrompt = true;
				goToDashboard(e);
			} else {
				FancyAlert failAlert = new FancyAlert(AlertType.ERROR);
				failAlert.setTitle("Add Feedback");
				failAlert.setHeaderText("Something went wrong when submitting feedback.");
				failAlert.setContentText("Please check if all fields are filled");
				failAlert.show();
			}
		}
	}
	
	public void openAsEditView(util.Feedback feedback, String[] feedbackAuthor) {
		submitButton.setOnAction(event -> {
			event.consume();
			
			FancyAlert failAlert = new FancyAlert(AlertType.ERROR);
			failAlert.setTitle("Edit Feedback");
			if (summaryTextField.getText().trim().isBlank() ||
				descriptionTextField.getText().trim().isBlank()
					) {
				failAlert.setHeaderText("Fields cannot be blank.");
				failAlert.show();
				return;
			}
			
			if(!util.DatabaseConnection.isDatabaseConnected()) {
				new FancyAlert(AlertType.ERROR, "Database Connection", "Could not connect to databse...\n").showAndWait();
				return;
			}
			if (updateFeedbackToDB(feedback.getId(), summaryTextField.getText(), descriptionTextField.getText())) {
				FancyAlert successAlert = new FancyAlert(AlertType.INFORMATION);
				successAlert.setTitle("Edit Feedback");
				successAlert.setHeaderText("Feedback has been updated!");
				successAlert.showAndWait();
				util.SceneChanger.closeWindowAndRun(event);
			}else {
				failAlert.setHeaderText("Something went wrong when updating feedback.");
				failAlert.show();
			}
			
		});
		backButton.setOnAction(event -> util.SceneChanger.closeWindowAndRun(event));
		detailsContainer.getChildren().add(openedAsFeedbackContainer);
		openedAsFeedbackContainer.getChildren().remove(updatedContainer);
		openedAsFeedbackContainer.getChildren().remove(confirmedContainer);
		
		privateOnlyRadio.setMouseTransparent(true);
		publicOnlyRadio.setMouseTransparent(true);
		feedbackTypeComboBox.setMouseTransparent(true);
		feedbackLabelComboBox.setMouseTransparent(true);
		
		titleLabel.setText("BA-" + String.format("%05d", feedback.getId()));
		authorTextField.setText(feedbackAuthor[0]);
		studentIdTextField.setText(feedbackAuthor[1]);
		feedbackTypeComboBox.getSelectionModel().select(feedback.getType());
		feedbackLabelComboBox.getSelectionModel().select(feedback.getLabel());
		if (feedback.getIsPrivate()) {
			privateOnlyRadio.setSelected(true);
			publicOnlyRadio.setSelected(false);
		}else {
			privateOnlyRadio.setSelected(false);
			publicOnlyRadio.setSelected(true);
		}
		createdTextField.setText(feedback.getCreateDateTime());
		updatedTextField.setText(feedback.getUpdateDateTime());
		confirmedTextField.setText(feedback.getConfirmDateTime());
		votesTextField.setText(Integer.toString(feedback.getVoteCount()));
		
		summaryTextField.setText(feedback.getSummary());
		descriptionTextField.setText(feedback.getMessage());
		
		Platform.runLater(() -> summaryTextField.requestFocus());
	}
	
	public void openAsNewWindow(util.Feedback feedback, String[] feedbackAuthor) {
		actionButtonContainer.getChildren().remove(deleteButton);
		if (util.Session.getSession().getUser().getRole() == util.User.UserRole.ADMIN) {
			actionButtonContainer.getChildren().add(deleteButton);
			deleteButton.setOnAction(event -> {
				event.consume();
				FancyAlert alert = new FancyAlert(AlertType.CONFIRMATION);
				alert.setTitle("Manage Feedback");
				alert.setHeaderText("Are you sure you want to DELETE this feedback?. This action is irreversable.");
				((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(false);
				((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton(true);
				
				if(!util.DatabaseConnection.isDatabaseConnected()) {
					new FancyAlert(AlertType.ERROR, "Database Connection", "Could not connect to databse...\n").showAndWait();
					return;
				}
				if (alert.showAndWait().get() == ButtonType.OK) {
					if (deleteFeedback(feedback)) {
						FancyAlert successAlert = new FancyAlert(AlertType.INFORMATION);
						successAlert.setTitle("Manage Feedback");
						successAlert.setHeaderText("Feedback has been deleted.");
						successAlert.showAndWait();
						util.SceneChanger.closeWindow(event);
					}else {
						FancyAlert failAlert = new FancyAlert(AlertType.ERROR);
						failAlert.setTitle("Manage Feedback");
						failAlert.setHeaderText("Something went wrong when deleting feedback.");
						failAlert.showAndWait();
					}
				}
			});
		}
		
		FancyAlert successAlert = new FancyAlert(AlertType.INFORMATION);
		successAlert.setTitle("Manage Feedback");
		successAlert.setHeaderText("Feedback has been updated!");
		
		FancyAlert failAlert = new FancyAlert(AlertType.ERROR);
		failAlert.setTitle("Manage Feedback");
		failAlert.setHeaderText("Something went wrong when updating feedback.");
		
		System.out.println("ADDCHILD");
		actionButtonContainer.getChildren().remove(submitButton);
		backButton.setOnAction(event -> util.SceneChanger.closeWindowAndRun(event));
		detailsContainer.getChildren().add(openedAsFeedbackContainer);
		openedAsFeedbackContainer.getChildren().remove(confirmedContainer);
		addFeedbackBody.getChildren().add(feedbackConfirmationContainer);
		if (feedback.getStatus() == util.Feedback.Status.REVIEWED) {
			addFeedbackBody.getChildren().add(feedbackResolvedMessageContainer);
			addFeedbackBody.getChildren().add(feedbackResolvedMessageSeparator);
			addFeedbackBody.getChildren().remove(feedbackConfirmationContainer);
			openedAsFeedbackContainer.getChildren().remove(votesContainer);
			openedAsFeedbackContainer.getChildren().add(confirmedContainer);
			openedAsFeedbackContainer.getChildren().add(votesContainer);
		}
		
		confirmFeedbackButton.setOnAction(event -> {
			event.consume();
			FancyAlert alert = new FancyAlert(AlertType.CONFIRMATION);
			alert.setTitle("Manage Feedback");
			alert.setHeaderText("This feedback will be marked as CONFIRMED and REVIEWED.");
			if(!util.DatabaseConnection.isDatabaseConnected()) {
				new FancyAlert(AlertType.ERROR, "Database Connection", "Could not connect to databse...\n").showAndWait();
				return;
			}
			if (alert.showAndWait().get() == ButtonType.OK) {
				if (confirmFeedback(feedback)) {
					successAlert.showAndWait();
					util.SceneChanger.closeWindow(event);
				}else {
					failAlert.show();
				}
			}
		});
		invalidateFeedbackButton.setOnAction(event -> {
			event.consume();
			FancyAlert alert = new FancyAlert(AlertType.CONFIRMATION);
			alert.setTitle("Manage Feedback");
			alert.setHeaderText("This feedback will be marked as INVALID and NO ACTION NEEDED.");
			if(!util.DatabaseConnection.isDatabaseConnected()) {
				new FancyAlert(AlertType.ERROR, "Database Connection", "Could not connect to databse...\n").showAndWait();
				return;
			}
			if (alert.showAndWait().get() == ButtonType.OK) {
				if (invalidateFeedback(feedback)) {
					successAlert.showAndWait();
					util.SceneChanger.closeWindow(event);
				}else {
					failAlert.show();
				}
			}
		});
		
		resolveFeedbackButton.setOnAction(event -> {
			event.consume();
			
			if (feedbackResolvedMessageTextArea.getText().isBlank()) {
				FancyAlert alert = new FancyAlert(AlertType.ERROR);
				alert.setTitle("Manage Feedback");
				alert.setHeaderText("Please fill out the field above before resolving.");
				alert.show();
				return;
			}
			FancyAlert alert = new FancyAlert(AlertType.CONFIRMATION);
			alert.setTitle("Manage Feedback");
			alert.setHeaderText("This feedback will be marked as RESOLVED.");
			if (alert.showAndWait().get() == ButtonType.OK) {
				if(!util.DatabaseConnection.isDatabaseConnected()) {
					new FancyAlert(AlertType.ERROR, "Database Connection", "Could not connect to databse...\n").showAndWait();
					return;
				}
				if (resolveFeedback(feedback, feedbackResolvedMessageTextArea.getText())) {
					successAlert.showAndWait();
					util.SceneChanger.closeWindow(event);
				}else {
					failAlert.show();
				}
			}
		});
		
		summaryTextField.setEditable(false);
		descriptionTextField.setEditable(false);
		privateOnlyRadio.setMouseTransparent(true);
		publicOnlyRadio.setMouseTransparent(true);
		feedbackTypeComboBox.setMouseTransparent(true);
		feedbackLabelComboBox.setMouseTransparent(true);
		
		titleLabel.setText("BA-" + String.format("%05d", feedback.getId()));
		authorTextField.setText(feedbackAuthor[0]);
		studentIdTextField.setText(feedbackAuthor[1]);
		feedbackTypeComboBox.getSelectionModel().select(feedback.getType());
		feedbackLabelComboBox.getSelectionModel().select(feedback.getLabel());
		if (feedback.getIsPrivate()) {
			privateOnlyRadio.setSelected(true);
			publicOnlyRadio.setSelected(false);
		}else {
			privateOnlyRadio.setSelected(false);
			publicOnlyRadio.setSelected(true);
		}
		createdTextField.setText(feedback.getCreateDateTime());
		updatedTextField.setText(feedback.getUpdateDateTime());
		confirmedTextField.setText(feedback.getConfirmDateTime());
		votesTextField.setText(Integer.toString(feedback.getVoteCount()));
		
		summaryTextField.setText(feedback.getSummary());
		descriptionTextField.setText(feedback.getMessage());
	}
	
	private boolean submitFeedbackToDB(int userId, String summary, util.Feedback.Type feedbackType, util.Feedback.Label feedbackLabel, String feedbackDescription, boolean isPrivate) {
		String query = "INSERT INTO feedback(user_id, summary, feedback_type, extra_info, feedback_message, is_private) VALUES(?, ?, ?, ?, ?, ?)";
		
		try(Connection conn = util.DatabaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(query)){
			
			ps.setInt(1, userId);
			ps.setString(2, summary);
			ps.setString(3, feedbackType.toString());
			ps.setString(4, feedbackLabel.toString());
			ps.setString(5, feedbackDescription);
			ps.setBoolean(6, isPrivate);
			int rowsAdded = ps.executeUpdate();
			System.out.println(rowsAdded);
			return rowsAdded == 1;
		}catch(SQLException e) { 
			new FancyAlert(AlertType.ERROR, "CFAMS", "Something went wrong... \nSee error log for more info").showAndWait();
			util.ErrorLogger.logError(e.toString());
			return false; 
		}
	}
	
	private boolean updateFeedbackToDB(int feedbackId, String summary, String feedbackDescription) {
		String query = "UPDATE feedback "
					+ "SET summary = ?, "
					+ "feedback_message = ?, "
					+ "updated_on = ? "
					+ "WHERE id = ?";
		
		try(Connection conn = util.DatabaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(query)){
			
			ps.setInt(4, feedbackId);
			ps.setString(1, summary);
			ps.setString(2, feedbackDescription);
			ps.setString(3, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toString());
			int rowsAdded = ps.executeUpdate();
			System.out.println(rowsAdded);
			return rowsAdded == 1;
		}catch(SQLException e) { 
			new FancyAlert(AlertType.ERROR, "CFAMS", "Something went wrong... \nSee error log for more info").showAndWait();
			util.ErrorLogger.logError(e.toString());
			return false; 
			}
	}
	
	private boolean confirmFeedback(util.Feedback feedback) {
		String query = "UPDATE feedback SET feedback_confirmation = ?, status = ?, confirm_date = ? WHERE id = ?";
		
		try(Connection conn = util.DatabaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, util.Feedback.Confirmation.CONFIRMED.toString());
			ps.setString(2, util.Feedback.Status.REVIEWED.toString());
			ps.setString(3, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toString());
			ps.setInt(4, feedback.getId());
			int rowsAdded = ps.executeUpdate();
			System.out.println(rowsAdded);
			return rowsAdded == 1;
		}catch(SQLException e) { 
			new FancyAlert(AlertType.ERROR, "CFAMS", "Something went wrong... \nSee error log for more info").showAndWait();
			util.ErrorLogger.logError(e.toString());
			return false; 
		}
	}

	private boolean invalidateFeedback(util.Feedback feedback) {
		String query = "UPDATE feedback SET feedback_confirmation = ?, status = ?, confirm_date = ? WHERE id = ?";
		
		try(Connection conn = util.DatabaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, util.Feedback.Confirmation.INVALID.toString());
			ps.setString(2, util.Feedback.Status.NO_ACTION.toString());
			ps.setString(3, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toString());
			ps.setInt(4, feedback.getId());
			int rowsAdded = ps.executeUpdate();
			System.out.println(rowsAdded);
			return rowsAdded == 1;
		}catch(SQLException e) { 
			new FancyAlert(AlertType.ERROR, "CFAMS", "Something went wrong... \nSee error log for more info").showAndWait();
			util.ErrorLogger.logError(e.toString());
			return false; 
		}
	}
	
	private boolean resolveFeedback(util.Feedback feedback, String resolveMessage) {
		String query = "UPDATE feedback SET feedback_confirmation = ?, status = ?, resolve_date = ?, resolve_message = ? WHERE id = ?";
		
		try(Connection conn = util.DatabaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, util.Feedback.Confirmation.RESOLVED.toString());
			ps.setString(2, util.Feedback.Status.RESOLVED.toString());
			ps.setString(3, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toString());
			ps.setString(4, resolveMessage);
			ps.setInt(5, feedback.getId());
			int rowsAdded = ps.executeUpdate();
			System.out.println(rowsAdded);
			return rowsAdded == 1;
		}catch(SQLException e) { 
			new FancyAlert(AlertType.ERROR, "CFAMS", "Something went wrong... \nSee error log for more info").showAndWait();
			util.ErrorLogger.logError(e.toString());
			return false; 
		}
	}
	
	private boolean deleteFeedback(util.Feedback feedback) {
		String query = "DELETE FROM feedback WHERE id = ?";

		try(Connection conn = util.DatabaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(query)){
			ps.setInt(1, feedback.getId());
			
			int rowsAdded = ps.executeUpdate();
			System.out.println(rowsAdded);
			return rowsAdded == 1;
		}catch(SQLException e) { 
			new FancyAlert(AlertType.ERROR, "CFAMS", "Something went wrong... \nSee error log for more info").showAndWait();
			util.ErrorLogger.logError(e.toString());
			return false; 
		}
	}
}
