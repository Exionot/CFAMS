package util;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import application.AddFeedbackController;
import application.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SceneChanger {
	public static enum SceneName {
		LOGIN("Login"),
		USER_DASHBOARD("DashUser"),
		ADD_FEEDBACK("AddFeedback"),
		USER_VIEW_FEEDBACK("UserViewFeedback"), 
		MANAGE_FEEDBACK("AdminManageFeedback"),
		ADD_USER("AdminAddUser"),
		VIEW_PROFILE("UserViewProfile"),
		CHANGE_PASSWORD("UserChangePassword"),
		ANALYTICS("AdminAnalytics"),
		SYSTEM_LOG("AdminSystemLogs")
		;
		
		private final String NAME;
		
		private SceneName(String name) {
			this.NAME = name;
		}
		
		public String getName() {
			return this.NAME;
		}
	}
	
	private static Parent root;
	private static Scene scene;
	private static Stage stage;
	private static Runnable storedMethod;
	
	
	public static void changeScene(ActionEvent e, String sceneName) throws IOException {
		root = FXMLLoader.load(SceneChanger.class.getResource(application.Main.FXML_DIR+sceneName+".fxml"));
		scene = new Scene(root);
		final URL CSS = SceneChanger.class.getResource(application.Main.CSS_DIR+sceneName+".css");
		if (CSS != null) scene.getStylesheets().add(CSS.toExternalForm());
		scene.getStylesheets().add(application.Main.GLOBAL_CSS);
		if (sceneName == SceneName.USER_DASHBOARD.getName()) {
			stage = (Stage)((Node)e.getSource()).getScene().getWindow();
		}
		stage.setOnCloseRequest(_ -> {});
		if (sceneName != SceneName.LOGIN.getName()) {
			stage.setOnCloseRequest(event -> {
				event.consume();
				FancyAlert alert = new FancyAlert(AlertType.CONFIRMATION);
				alert.setTitle("Logout");
				alert.setHeaderText("Are you sure you want to logout?");
				alert.setContentText("Continuing will log you out and close this window.");
				((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(false);
				((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton(true);
				if (alert.showAndWait().get() == ButtonType.OK) {
					System.out.println("LOGOUT");
					util.SystemLog.logAction(
							util.SystemLog.Type.ACTION, 
							util.Session.getSession().getUser().getUserId(), 
							util.SystemLog.ActionMessage.LOGOUT
							);
					util.Session.getSession().setUser(null);
					stage.close();
				}
			});
			util.Session.getSession().setListeners(scene);
		}

		stage.setScene(scene);
		stage.setResizable(false);
		stage.show();
		stage.centerOnScreen();
//		stage.setMaximized(true);
	}
	
	public static void newWindow(ActionEvent e, String sceneName) throws IOException {
		root = FXMLLoader.load(SceneChanger.class.getResource(application.Main.FXML_DIR+sceneName+".fxml"));
		Stage stage = new Stage();
		scene = new Scene(root);
		final URL CSS = SceneChanger.class.getResource(application.Main.CSS_DIR+sceneName+".css");
		if (CSS != null) scene.getStylesheets().add(CSS.toExternalForm());
		scene.getStylesheets().add(application.Main.GLOBAL_CSS);
		stage.setScene(scene);
		stage.initModality(Modality.APPLICATION_MODAL);
		System.out.println(((Node)e.getSource()));
		stage.initOwner((Stage)((Node)e.getSource()).getScene().getWindow());
		stage.setResizable(false);
		if (sceneName.equals(util.SceneChanger.SceneName.ADD_FEEDBACK.getName())) {
			stage.setOnCloseRequest(event -> {
				event.consume();
				
				FancyAlert alert = new FancyAlert(AlertType.CONFIRMATION);
				alert.setTitle("Unfinished Feedback");
				alert.setHeaderText("Feedback has not been submitted. Are you sure you want to exit?");
				((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(false);
				((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton(true);
				if (alert.showAndWait().get() == ButtonType.OK){
					stage.close();
				}
			});
		}else if (sceneName.equals(util.SceneChanger.SceneName.ADD_USER.getName())) {
			stage.setOnCloseRequest(event -> {
				event.consume();
				
				FancyAlert alert = new FancyAlert(AlertType.CONFIRMATION);
				alert.setTitle("Unfinished User");
				alert.setHeaderText("User has not been added. Are you sure you want to exit?");
				((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(false);
				((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton(true);
				if (alert.showAndWait().get() == ButtonType.OK){
					stage.close();
				}
			});
		}
		
		util.Session.getSession().setListeners(scene);
		stage.setTitle("CFAMS");
		stage.getIcons().add(new Image(new File(Main.RESOURCE_DIR + "icon.png").toURI().toString()));
		stage.show();
		stage.centerOnScreen();
	}
	
	public static void newWindowStandBy(ActionEvent e, String sceneName, Runnable method) throws IOException {
		storedMethod = method;
		FXMLLoader loader = new FXMLLoader(SceneChanger.class.getResource(application.Main.FXML_DIR + sceneName + ".fxml"));
		root = loader.load();
		Stage stage = new Stage();
		scene = new Scene(root);
		final URL CSS = SceneChanger.class.getResource(application.Main.CSS_DIR+sceneName+".css");
		if (CSS != null) scene.getStylesheets().add(CSS.toExternalForm());
		scene.getStylesheets().add(application.Main.GLOBAL_CSS);
		stage.setScene(scene);
		stage.initModality(Modality.APPLICATION_MODAL);
		System.out.println(((Node)e.getSource()));
		stage.initOwner((Stage)((Node)e.getSource()).getScene().getWindow());
		stage.setResizable(false);
		if (sceneName.equals(util.SceneChanger.SceneName.VIEW_PROFILE.getName())) {
			stage.setOnHidden(event -> {
				event.consume();
				method.run();
			});
		}
		util.Session.getSession().setListeners(scene);
		stage.setTitle("CFAMS");
		stage.getIcons().add(new Image(new File(Main.RESOURCE_DIR + "icon.png").toURI().toString()));
		stage.show();
		stage.centerOnScreen();
	}
	
	public static void openFeedbackAsWindow(MouseEvent e, util.Feedback feedback, String[] feedbackAuthor, Runnable refresh) throws IOException {
		storedMethod = refresh;
		FXMLLoader loader = new FXMLLoader(AddFeedbackController.class.getResource(application.Main.FXML_DIR + "AddFeedback.fxml"));
		root = loader.load();
		Stage stage = new Stage();
		AddFeedbackController controller = loader.getController();
		controller.openAsNewWindow(feedback, feedbackAuthor);
		scene = new Scene(root);
		final URL CSS = SceneChanger.class.getResource(application.Main.CSS_DIR+"AddFeedback.css");
		if (CSS != null) scene.getStylesheets().add(CSS.toExternalForm());
		scene.getStylesheets().add(application.Main.GLOBAL_CSS);
		stage.setScene(scene);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner((Stage)((Node)e.getSource()).getScene().getWindow());
		stage.setResizable(false);
		stage.setOnCloseRequest(event -> {
			event.consume();
			stage.close();
		});
		stage.setOnHidden(event -> {
			event.consume();
			refresh.run();
			stage.close();
		});
		
		util.Session.getSession().setListeners(scene);
		stage.setTitle("CFAMS");
		stage.getIcons().add(new Image(new File(Main.RESOURCE_DIR + "icon.png").toURI().toString()));
		stage.show();
		stage.centerOnScreen();
	}
	
	public static void openFeedbackEditView(ActionEvent e, util.Feedback feedback, String[] feedbackAuthor, Runnable refresh) throws IOException {
		storedMethod = refresh;
		FXMLLoader loader = new FXMLLoader(AddFeedbackController.class.getResource(application.Main.FXML_DIR + "AddFeedback.fxml"));
		root = loader.load();
		Stage stage = new Stage();
		AddFeedbackController controller = loader.getController();
		controller.openAsEditView(feedback, feedbackAuthor);
		scene = new Scene(root);
		final URL CSS = SceneChanger.class.getResource(application.Main.CSS_DIR+"AddFeedback.css");
		if (CSS != null) scene.getStylesheets().add(CSS.toExternalForm());
		scene.getStylesheets().add(application.Main.GLOBAL_CSS);
		stage.setScene(scene);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initOwner((Stage)((Node)e.getSource()).getScene().getWindow());
		stage.setResizable(false);
		stage.setOnCloseRequest(event -> {
			event.consume();
			FancyAlert alert = new FancyAlert(AlertType.CONFIRMATION);
			alert.setTitle("Unsaved Changes");
			alert.setHeaderText("Changes have not been saved. Are you sure you want to exit?");
			((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setDefaultButton(false);
			((Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL)).setDefaultButton(true);
			if (alert.showAndWait().get() == ButtonType.OK){
				refresh.run();
				stage.close();
			}
		});
		stage.setOnHidden(event -> {
			event.consume();
			refresh.run();
			stage.close();
		});
		
		util.Session.getSession().setListeners(scene);
		stage.setTitle("CFAMS");
		stage.getIcons().add(new Image(new File(Main.RESOURCE_DIR + "icon.png").toURI().toString()));
		stage.show();
		stage.centerOnScreen();
	}
	
	public static void closeWindow(ActionEvent e) {
		Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
		e.consume();
		stage.close();
	}
	
	public static void closeWindowAndRun(ActionEvent e) {
		Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
		stage.setOnHidden(event -> {
			storedMethod.run();
			event.consume();
		});
		stage.close();
	}
	
}
