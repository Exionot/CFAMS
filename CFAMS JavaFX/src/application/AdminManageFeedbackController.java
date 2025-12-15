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
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import util.FancyAlert;

public class AdminManageFeedbackController implements Initializable{
	
	@FXML private TextField searchTextField;
	@FXML private RadioButton searchFeedbackIdRadio, searchStudentIdRadio, sortFeedbackIdRadio, sortVotesRadio;
	@FXML private VBox unreviewedCardContainer, reviewedCardContainer, allCardContainer;
	@FXML private TabPane tabPane;
	@FXML private Tab unreviewedTab, reviewedTab, allTab;
	
	private util.Feedback[] feedbackList;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		refreshFeedback(null);
		if (util.Session.getSession().getUser().getRole() != util.User.UserRole.ADMIN) {
			tabPane.getTabs().remove(allTab);
		}
	}
	
	public void goToDashboard(ActionEvent e) throws IOException {
		util.SceneChanger.closeWindow(e);
	}
	
	public void changeTab() {
		refreshFeedback(null);
	}
	
	public void refreshFeedback(ActionEvent e) {
		if(!util.DatabaseConnection.isDatabaseConnected()) {
			new FancyAlert(AlertType.ERROR, "Database Connection", "Could not connect to databse...\n").showAndWait();
			return;
		}
		
		boolean searchByFeedbackId = searchFeedbackIdRadio.isSelected() ? true : false;
		boolean sortByVotes = sortVotesRadio.isSelected() ? true : false;
		String searchItem = searchTextField.getText().trim();
		feedbackList = fetchFeedback(tabPane.getSelectionModel().getSelectedIndex(), sortByVotes, searchByFeedbackId, searchItem);
		
		int tabIndex = tabPane.getSelectionModel().getSelectedIndex();
		VBox cardContainer = tabIndex == 0 ? unreviewedCardContainer : tabIndex == 1 ? reviewedCardContainer : allCardContainer;
		cardContainer.getChildren().clear();
		
		for (util.Feedback feedback : feedbackList) { 
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource(application.Main.FXML_DIR + "Card.fxml"));
				VBox card = loader.load();
				CardController controller = loader.getController();
				controller.setCardData(feedback.getId(), feedback.getIsPrivate(), fetchFeedbackAuthor(feedback.getUserId())[1], feedback.getSummary());
				card.setPickOnBounds(true);
				card.addEventHandler(MouseEvent.MOUSE_CLICKED, e1 -> {
						e1.consume();
						try {
							openFeedback(e1, feedback, fetchFeedbackAuthor(feedback.getUserId()));
						} catch (IOException e2) {
							e2.printStackTrace();
						}
					});
				Separator separator = new Separator();
				separator.prefWidthProperty().bind(cardContainer.widthProperty());

				cardContainer.getChildren().addAll(card, separator);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public void openFeedback(MouseEvent e, util.Feedback feedback, String[] feedbackAuthor) throws IOException {
		util.SceneChanger.openFeedbackAsWindow(e, feedback, feedbackAuthor, () -> refreshFeedback(null));
	}
	
	private util.Feedback[] fetchFeedback(int selectedTab, boolean sortByVotes, boolean searchByFeedbackId, String serachItem) { 
		String query;
		if (searchByFeedbackId) {
			query = "SELECT *, "
					+ "DATE_FORMAT(created_at, '%m/%d/%Y, %H:%i:%s') AS formatted_create_date, "
					+ "DATE_FORMAT(updated_on, '%m/%d/%Y, %H:%i:%s') AS formatted_update_date,  "
					+ "DATE_FORMAT(confirm_date, '%m/%d/%Y, %H:%i:%s') AS formatted_confirm_date,  "
					+ "DATE_FORMAT(resolve_date, '%m/%d/%Y, %H:%i:%s') AS formatted_resolve_date  "
					+ "FROM feedback "
					+ "WHERE status LIKE ? AND (? IS NULL OR id = ?)";
		}else {
			query =  "SELECT *, "
					+ "DATE_FORMAT(created_at, '%m/%d/%Y, %H:%i:%s') AS formatted_create_date, "
					+ "DATE_FORMAT(updated_on, '%m/%d/%Y, %H:%i:%s') AS formatted_update_date,  "
					+ "DATE_FORMAT(confirm_date, '%m/%d/%Y, %H:%i:%s') AS formatted_confirm_date,  "
					+ "DATE_FORMAT(resolve_date, '%m/%d/%Y, %H:%i:%s') AS formatted_resolve_date  "
					+ "FROM feedback "
					+ "WHERE status LIKE ? AND (? IS NULL OR user_id = ?)";
		}
		
		String statusFilter = selectedTab == 0 ? util.Feedback.Status.PENDING.toString() : 
								selectedTab == 1 ? util.Feedback.Status.REVIEWED.toString() : 
								"%";
		Integer searchId = null;
		if (searchByFeedbackId) {
			searchId = serachItem == null || serachItem.isBlank() ? null : Integer.parseInt(serachItem);
		}else {
			searchId = serachItem == null || serachItem.isBlank() ? null : fetchUserIdByStudentId(serachItem);
		}
		

		try(Connection conn = util.DatabaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, statusFilter);
			if (searchId == null) {
				ps.setNull(2, java.sql.Types.INTEGER);
				ps.setNull(3, java.sql.Types.INTEGER);
			}else {
				ps.setInt(2, searchId);
				ps.setInt(3, searchId);
			}
			

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
			if (sortByVotes) {
				rows.sort((b, a) -> Integer.compare(b.getVoteCount(), a.getVoteCount()));
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
	
	private int fetchUserIdByStudentId(String studentId) {
		String query = "SELECT id FROM users WHERE student_id = ?";

		try (Connection conn = util.DatabaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, studentId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("id");
				}
				return 0;
			}

		}catch (SQLException e) {return 0;}
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
