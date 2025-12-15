package application;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import util.FancyAlert;
import util.SceneChanger;

public class UserViewFeedbackController implements Initializable{
	public static enum Filter{
		ALL("All Feedbacks"),
		REVIEWED("Reviewed Feedbacks"),
		RESOLVED("Resolved Feedbacks")
		;
		
		private final String NAME;
		private Filter(String name) {
			this.NAME = name;
		}
		public String getName() {
			return this.NAME;
		}
	}
	
	//TODO private radio and public radio must switch
	
	@FXML private VBox submittedCardContainer;
	@FXML private HBox userFilterSelect, feedbackVoteContainer;
	@FXML private AnchorPane feedbackHeaderContainer;
	@FXML private VBox fullFeedbackContainer, feedbackDescriptionContainer, resolvedMessageContainer, rightLabelContainer;
	@FXML private Button refreshButton, backButton, feedbackVoteButton, editFeedbackButton;
	@FXML private Label feedbackIdLabel, 
						feedbackSummaryLabel, 
						feedbackTypeLabel,
						feedbackExtraLabel,
						feedbackConfirmationLabel,
						feedbackStatusLabel,
						feedbackSubmittedLabel,
						feedbackStudentIdLabel,
						feedbackCreatedLabel,
						feedbackUpdatedLabel,
						feedbackDescriptionLabel,
						feedbackFilterLabel,
						feedbackVoteLabel,
						feedbackResolvedMessageLabel,
						feedbackConfirmedLabel,
						feedbackResolvedLabel;
	@FXML private ComboBox<Filter> feedbackFilterComboBox;
	@FXML private CheckBox userSubmissionCheckBox, privateOnlyCheckBox;
	@FXML private StackPane editFeedbackContainer;
	
	private final util.User user = util.Session.getSession().getUser();
	
	private util.Feedback openedFeedback;
	private boolean hasUserVoted;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		refreshFeedback(null);
		feedbackFilterComboBox.getItems().addAll(Filter.values());
		fullFeedbackContainer.setVisible(false);
		submittedCardContainer.setPickOnBounds(false);
		privateOnlyCheckBox.setVisible(false);
		fullFeedbackContainer.getChildren().remove(resolvedMessageContainer);
		
		if (user.getRole() != util.User.UserRole.USER) {
			((Pane)userFilterSelect.getParent()).getChildren().remove(userFilterSelect);
			((Pane)feedbackVoteContainer).getChildren().remove(feedbackVoteButton);
		}
	}
	
	public void refreshFeedback(ActionEvent e) {
		if(!util.DatabaseConnection.isDatabaseConnected()) {
			new FancyAlert(AlertType.ERROR, "Database Connection", "Could not connect to databse...\n").showAndWait();
			return;
		}
		openedFeedback = null;
		if (userSubmissionCheckBox.isSelected()) privateOnlyCheckBox.setVisible(true);
		else {
			privateOnlyCheckBox.setVisible(false);
			privateOnlyCheckBox.setSelected(false);
		}
		submittedCardContainer.getChildren().clear();
		fullFeedbackContainer.setVisible(false);
		try {
			feedbackFilterLabel.setText(feedbackFilterComboBox.getValue().getName());
		} catch (NullPointerException e1) {
			feedbackFilterLabel.setText(Filter.ALL.getName());
		} 
		
		for (util.Feedback feedback : fetchFeedback(feedbackFilterComboBox.getValue(), user.getUserId(), userSubmissionCheckBox.isSelected(), privateOnlyCheckBox.isSelected())) { 
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource(application.Main.FXML_DIR + "Card.fxml"));
				VBox card = loader.load();
				CardController controller = loader.getController();
				controller.setCardData(feedback.getId(), feedback.getIsPrivate(), fetchFeedbackAuthor(feedback.getUserId())[1], feedback.getSummary());
				card.setPickOnBounds(true);
				card.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
					event.consume();
					openFeedback(feedback, fetchFeedbackAuthor(feedback.getUserId()));
					});
				Separator separator = new Separator();
				separator.prefWidthProperty().bind(submittedCardContainer.widthProperty());

				submittedCardContainer.getChildren().addAll(card, separator);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public void goToDashboard(ActionEvent e) throws IOException {
		util.SceneChanger.changeScene(e, SceneChanger.SceneName.USER_DASHBOARD.getName()); 
	}
	
	public void voteFeedback(ActionEvent e) {
		if (hasUserVoted) {
			System.out.println("Vote -1");
			removeVoteFromFeedback(openedFeedback);
			openedFeedback.decrementVote();
		}
		else {
			System.out.println("Vote +1");
			addVoteToFeedback(openedFeedback);
			openedFeedback.incrementVote();
		}
		openFeedback(openedFeedback, fetchFeedbackAuthor(openedFeedback.getUserId()));
	}
	
	public void openFeedback(util.Feedback feedback, String[] feedbackAuthor) {
		hasUserVoted = hasUserVotedOnFeedback(user, feedback);
		openedFeedback = feedback;
		String privateStatus = feedback.getIsPrivate() ? " (Private)" : " (Public)";
		feedbackIdLabel.setText("BA-" + String.format("%05d", feedback.getId()) + privateStatus);
		feedbackSummaryLabel.setText(feedback.getSummary());
		feedbackSummaryLabel.autosize();
		feedbackHeaderContainer.setPrefHeight(feedbackSummaryLabel.getHeight()+55);
		feedbackHeaderContainer.layout();
		feedbackTypeLabel.setText("Type: " + feedback.getType().getValue());
		feedbackExtraLabel.setText("Label: " + feedback.getLabel().getName());
		feedbackConfirmationLabel.setText("Confirmation: " + feedback.getConfirmation().getValue());
		feedbackStatusLabel.setText("Status: " + feedback.getStatus().getValue());
		feedbackSubmittedLabel.setText("Submitted By: " + feedbackAuthor[0]); 
		feedbackStudentIdLabel.setText("Student Id: " + feedbackAuthor[1]); 
		feedbackCreatedLabel.setText("Created On: " + feedback.getCreateDateTime());
		feedbackUpdatedLabel.setText("Updated On: " + feedback.getUpdateDateTime());
		feedbackVoteLabel.setText("Votes: " + feedback.getVoteCount());
		
		editFeedbackContainer.getChildren().remove(editFeedbackButton); 
		if (user.getUserId() == feedback.getUserId() && feedback.getConfirmation() == util.Feedback.Confirmation.PENDING) {
			editFeedbackContainer.getChildren().add(editFeedbackButton); 
		}
		
		if (hasUserVoted) feedbackVoteButton.setText("Voted");
		else feedbackVoteButton.setText("Vote");
		
		feedbackDescriptionLabel.setText(feedback.getMessage());
		
		rightLabelContainer.getChildren().remove(feedbackConfirmedLabel);
		rightLabelContainer.getChildren().remove(feedbackResolvedLabel);
		if (feedback.getConfirmDateTime() != null) {
			rightLabelContainer.getChildren().add(feedbackConfirmedLabel);
			feedbackConfirmedLabel.setText("Confirmed On: " + feedback.getConfirmDateTime());
			
		}
		
		fullFeedbackContainer.getChildren().remove(resolvedMessageContainer);
		if (feedback.getStatus() == util.Feedback.Status.RESOLVED) {
			rightLabelContainer.getChildren().add(feedbackResolvedLabel);
			feedbackResolvedLabel.setText("Resolved On: " + feedback.getResolveDateTime());
			fullFeedbackContainer.getChildren().add(resolvedMessageContainer);
			feedbackResolvedMessageLabel.setText(feedback.getResolvedMessage());
		}
		
		((Pane)feedbackVoteContainer).getChildren().remove(feedbackVoteButton);
		if (feedback.getConfirmation() == util.Feedback.Confirmation.INVALID || feedback.getConfirmation() == util.Feedback.Confirmation.RESOLVED) {
			((Pane)feedbackVoteContainer).getChildren().remove(feedbackVoteButton);
		}else if (user.getRole() == util.User.UserRole.USER) {
			((Pane)feedbackVoteContainer).getChildren().add(feedbackVoteButton);
		}
		
		fullFeedbackContainer.setVisible(true);
	}
	
	public void showEditFeedbackPane(ActionEvent e) throws IOException {
		util.SceneChanger.openFeedbackEditView(e, openedFeedback, fetchFeedbackAuthor(openedFeedback.getUserId()), () -> refreshFeedback(e));
	}
	
	private boolean hasUserVotedOnFeedback(util.User user, util.Feedback feedback) {
		String query = "SELECT user_id, feedback_id FROM voted_feedback WHERE user_id = ? AND feedback_id = ?";
		
		try(Connection conn = util.DatabaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(query)){
			ps.setInt(1, user.getUserId());
			ps.setInt(2, feedback.getId());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) return true;
				return false;
			}
		}catch (SQLException e) { return false; }
	}
	
	private boolean addVoteToFeedback(util.Feedback feedback) {
		String query = "INSERT INTO voted_feedback(user_id, feedback_id) VALUES(?, ?)";
		int insertRowsAdded = 0, updateRowsAdded = 0;
		
		try(Connection conn = util.DatabaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(query)){
			ps.setInt(1, user.getUserId());
			ps.setInt(2, feedback.getId());
			insertRowsAdded = ps.executeUpdate();
		}catch (SQLException e) { e.printStackTrace(); }
		
		if (insertRowsAdded == 1) {
			query = "UPDATE feedback SET votes = ? WHERE id = ?";
			int votes = feedback.getVoteCount() + 1;
			
			try(Connection conn = util.DatabaseConnection.getConnection();
					PreparedStatement ps = conn.prepareStatement(query)){
				ps.setInt(1, votes);
				ps.setInt(2, feedback.getId());
				updateRowsAdded = ps.executeUpdate();
			}catch (SQLException e) { e.printStackTrace(); }
		}
		
		if (insertRowsAdded == 1 && updateRowsAdded == 1) {
			return true;
		}
		
		return false;
	}
	
	private boolean removeVoteFromFeedback(util.Feedback feedback) {
		String query = "DELETE FROM voted_feedback WHERE user_id = ? AND feedback_id = ?";
		int deleteRowsAffected = 0, updateRowsAffected = 0;
		
		try(Connection conn = util.DatabaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(query)){
			ps.setInt(1, user.getUserId());
			ps.setInt(2, feedback.getId());
			deleteRowsAffected = ps.executeUpdate();
		}catch (SQLException e) { e.printStackTrace();}
		
		if (deleteRowsAffected == 1) {
			query = "UPDATE feedback SET votes = ? WHERE id = ?";
			int votes = feedback.getVoteCount() - 1;
			
			try(Connection conn = util.DatabaseConnection.getConnection();
					PreparedStatement ps = conn.prepareStatement(query)){
				ps.setInt(1, votes);
				ps.setInt(2, feedback.getId());
				updateRowsAffected = ps.executeUpdate();
			}catch (SQLException e) {e.printStackTrace();}
		}
		
		if (deleteRowsAffected == 1 && updateRowsAffected == 1) {
			return true;
		}
		
		return false;
	}
	
	private util.Feedback[] fetchFeedback(Filter filter, int userId, boolean userSumissionOnly, boolean privateOnly) { 
		String query =  "SELECT *, "
				+ "DATE_FORMAT(created_at, '%m/%d/%Y, %H:%i:%s') AS formatted_create_date, "
				+ "DATE_FORMAT(updated_on, '%m/%d/%Y, %H:%i:%s') AS formatted_update_date,  "
				+ "DATE_FORMAT(confirm_date, '%m/%d/%Y, %H:%i:%s') AS formatted_confirm_date,  "
				+ "DATE_FORMAT(resolve_date, '%m/%d/%Y, %H:%i:%s') AS formatted_resolve_date  "
				+ "FROM feedback "
				+ "WHERE status LIKE ? AND (? = 0 OR user_id = ?) AND (? = 0 OR is_private = ?)";
		
		String feedbackFilter = filter == Filter.ALL ? "%" : 
								filter == Filter.REVIEWED ? "REVIEWED" :
								filter == Filter.RESOLVED ? "RESOLVED" :
								"%";
		int userFilter = userSumissionOnly ? 1 : 0;
		int privateFilter = privateOnly || util.Session.getSession().getUser().getRole() != util.User.UserRole.USER ? 0 : 1;
		try(Connection conn = util.DatabaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, feedbackFilter);
			ps.setInt(2, userFilter);
			ps.setInt(3, userId);
			ps.setInt(4, privateFilter);
			ps.setBoolean(5, privateOnly);

			java.util.ArrayList<util.Feedback> rows = new java.util.ArrayList<>();
			
			try(ResultSet rs = ps.executeQuery()) {
				while(rs.next()) {
					rows.add(new util.Feedback(
							rs.getInt("id"),
			                rs.getInt("user_id"),
			                rs.getString("summary"),
			                util.Feedback.Type.valueOf(rs.getString("feedback_type")),
			                util.Feedback.Confirmation.valueOf(rs.getString("feedback_confirmation")),
			                util.Feedback.Label.valueOf(rs.getString("extra_info")),
			                rs.getString("feedback_message"),
			                rs.getString("formatted_create_date"),
			                rs.getString("formatted_update_date"),
			                rs.getString("formatted_confirm_date"),
			                rs.getString("formatted_resolve_date"),
			                util.Feedback.Status.valueOf(rs.getString("status")),
			                rs.getString("resolve_message"),
			                rs.getObject("is_private"),
			                rs.getInt("votes")
			            ));
				}
			}
			util.Feedback[] arr = rows.toArray(new util.Feedback[0]);
			
			for (int i = 0; i < arr.length / 2; i++) {
			    util.Feedback temp = arr[i];
			    arr[i] = arr[arr.length - 1 - i];
			    arr[arr.length - 1 - i] = temp;
			}
			
			return arr;

		}catch(SQLException e) { e.printStackTrace();;}
		System.out.println("Error");
		return new util.Feedback[0];
	}
	
	private String[] fetchFeedbackAuthor(int userId) {
		String query = "SELECT username, student_id FROM users WHERE id = ?";

		try (Connection conn = util.DatabaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(query)){
			ps.setInt(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				String[] feedbackAuthor;
				if (rs.next()) {
					feedbackAuthor = new String[]{
							rs.getString("username"),
							rs.getString("student_id")
						};
					return feedbackAuthor;
				}
				return new String[0];
			}

		}catch (SQLException e) {return new String[0];}
	}

	
}
