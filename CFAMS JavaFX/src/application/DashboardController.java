package application;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import util.FancyAlert;

public class DashboardController implements Initializable{
	@FXML private Button userViewProfileButton, addFeedbackButton, viewFeedbackButton, reviewFeedbackButton, addUserButton, analyticsButton, systemLogButton;
	@FXML private Label userStudentIdLabel, usernameLabel, userTypeLabel;
	@FXML private ImageView iconImageVIew, userProfileImageContainer;
	
	private util.User user = util.Session.getSession().getUser();

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		System.out.println(new File(user.getImagePath()).toURI().toString());
		
		Rectangle clip = new Rectangle(userProfileImageContainer.getFitWidth(), userProfileImageContainer.getFitHeight());
		clip.setArcWidth(20);  
		clip.setArcHeight(20); 
		userProfileImageContainer.setClip(clip);
		
		iconImageVIew.setImage(new Image(new File(Main.RESOURCE_DIR + "icon.png").toURI().toString()));
		Path imagePath = Paths.get(user.getImagePath());
		Path defaultPath = Paths.get(Main.RESOURCE_DIR + "default-profile.jpg");

		Path pathToUse = Files.exists(imagePath)
		        ? imagePath
		        : defaultPath;

		userProfileImageContainer.setImage(new Image(pathToUse.toUri().toString()));

		userStudentIdLabel.setText(user.getStudentId());
		usernameLabel.setText(user.getUsername());
		userTypeLabel.setText(user.getRole().toString());
		
		if (user.getRole() == util.User.UserRole.USER) {
			((Pane)reviewFeedbackButton.getParent()).getChildren().remove(reviewFeedbackButton);
			((Pane)addUserButton.getParent()).getChildren().remove(addUserButton);
			((Pane)analyticsButton.getParent()).getChildren().remove(analyticsButton);
			((Pane)systemLogButton.getParent()).getChildren().remove(systemLogButton);
		}else {
			((Pane)addFeedbackButton.getParent()).getChildren().remove(addFeedbackButton);
			((Pane)userStudentIdLabel.getParent()).getChildren().remove(userStudentIdLabel);
			usernameLabel.setTranslateY(0);
		}
		
		if (user.getRole() == util.User.UserRole.MODERATOR) {
			((Pane)addUserButton.getParent()).getChildren().remove(addUserButton);
			((Pane)systemLogButton.getParent()).getChildren().remove(systemLogButton);
		}
		
	}

	public void showAddFeedbackPane(ActionEvent e) throws IOException {
		System.out.println("ADD");
		util.SceneChanger.newWindow(e, util.SceneChanger.SceneName.ADD_FEEDBACK.getName()); 
	}

	public void showViewFeedbackPane(ActionEvent e) throws IOException {
		System.out.println("VIEW");
		util.SceneChanger.changeScene(e, util.SceneChanger.SceneName.USER_VIEW_FEEDBACK.getName());
	}
	
	public void showReviewFeedbackPane(ActionEvent e) throws IOException {
		System.out.println("REVIEW");
		util.SceneChanger.newWindow(e, util.SceneChanger.SceneName.MANAGE_FEEDBACK.getName()); 
	}
	
	public void showAddUserPane(ActionEvent e) throws IOException {
		System.out.println("ADD USER");
		util.SceneChanger.newWindow(e, util.SceneChanger.SceneName.ADD_USER.getName()); 
	}
	
	public void showViewProfilePane(ActionEvent e) throws IOException {
		System.out.println("VIEW PROFILE");
		util.SceneChanger.newWindowStandBy(e, util.SceneChanger.SceneName.VIEW_PROFILE.getName(), () -> refreshProfile()); 
	}
	
	public void showAnalyticsPane(ActionEvent e) throws IOException {
		System.out.println("ANALYTICS");
		util.SceneChanger.changeScene(e, util.SceneChanger.SceneName.ANALYTICS.getName()); 
	}
	
	public void showLogPane(ActionEvent e) throws IOException{
		System.out.println("LOGS");
		util.SceneChanger.changeScene(e, util.SceneChanger.SceneName.SYSTEM_LOG.getName()); 
	}

	public void refreshProfile() {
		util.Session.getSession().setUser(refreshUser(user.getUserId()));
		user = util.Session.getSession().getUser();
		try {
			userProfileImageContainer.setImage(new Image(new File(user.getImagePath()).toURI().toString()));
		} catch (Exception e) {
			userProfileImageContainer.setImage(new Image(new File(Main.UPLOAD_DIR + "default-profile.jpg").toURI().toString()));
			e.printStackTrace();
		}
	}
	
	public void logoutAttempt(ActionEvent e) {
		FancyAlert alert = new FancyAlert(AlertType.CONFIRMATION);
		alert.setTitle("Logout");
		alert.setHeaderText("Are you sure you want to logout?");
		((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(false);
		((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton(true);
		if (alert.showAndWait().get() == ButtonType.OK){
			try {
				util.SystemLog.logAction(
						util.SystemLog.Type.ACTION, 
						util.Session.getSession().getUser().getUserId(), 
						util.SystemLog.ActionMessage.LOGOUT
						);
				util.Session.getSession().clearSession();
				util.SceneChanger.changeScene(e, util.SceneChanger.SceneName.LOGIN.getName());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} 
	}
	
	private util.User refreshUser(int userId){
		String query = "SELECT id, username, student_id, course, student_year, image_url FROM users WHERE id = ?";

		try (Connection conn = util.DatabaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(query)){
			ps.setInt(1, userId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return new util.User(
							rs.getInt("id"),
							rs.getString("username"),
							rs.getString("student_id"),
							rs.getString("course"),
							rs.getString("student_year"),
							rs.getString("image_url")
							);
				} 
				return null;
			}
		}catch (SQLException e) {
			new FancyAlert(AlertType.ERROR, "Dashboard", "Something went wrong... \nSee error log for more info").showAndWait();
			return null;
		}
	}

}
