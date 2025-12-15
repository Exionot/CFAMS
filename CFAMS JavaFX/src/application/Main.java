package application;
	
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import util.FancyAlert;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;

public class Main extends Application {
	public static final String CSS_DIR = "/css/";
	public static final String FXML_DIR = "/fxml/";
	public static final String UPLOAD_DIR;
	static {
	    try {
	        String userHome = System.getProperty("user.home");
	        File uploadDir = new File(userHome, "CFAMS/uploads");
	        if (!uploadDir.exists()) {
	            uploadDir.mkdirs();
	        }
	        UPLOAD_DIR = uploadDir.getAbsolutePath() + File.separator;
	    } catch (Exception e) {
	        throw new RuntimeException("Failed to initialize upload directory", e);
	    }
	}
	public static final String RESOURCE_DIR;
	static {
	    File jarDir;
	    try {
	        jarDir = new File(Main.class.getProtectionDomain()
	                              .getCodeSource()
	                              .getLocation()
	                              .toURI())
	                         .getParentFile();
	        File uploadDir = new File(jarDir, "resource");
	        if (!uploadDir.exists()) {
	            uploadDir.mkdirs(); 
	        }
	        RESOURCE_DIR = uploadDir.getAbsolutePath() + File.separator;
	    } catch (Exception e) {
	        throw new RuntimeException("Failed to initialize upload directory", e);
	    }
	}
	public static final String DB_UPLOAD_PATH = "uploads/";
	public static final String GLOBAL_CSS = Main.class.getResource(CSS_DIR + "root.css").toExternalForm();
	
	private static Stage primaryStage;
	private static Parent root;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			root = FXMLLoader.load(getClass().getResource(FXML_DIR+"Login.fxml"));
			Scene scene = new Scene(root);
			scene.getStylesheets().add(GLOBAL_CSS);
			Main.primaryStage = primaryStage;
			primaryStage.setScene(scene);
			primaryStage.setTitle("CFAMS");
			primaryStage.setResizable(false);
			
			util.Session.getSession().setOnTimeout(() -> {
			    System.out.println("Session expired. Logging out...");
			    FancyAlert alert = new FancyAlert(AlertType.INFORMATION);
				alert.setTitle("Session");
				alert.setHeaderText("Session Expired");
				alert.showAndWait();
				logOut();
			});
			
			primaryStage.getIcons().add(new Image(new File(Main.RESOURCE_DIR + "icon.png").toURI().toString()));
			primaryStage.show();
			primaryStage.toFront();
			primaryStage.requestFocus();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void logOut() {
		ArrayList<Window> windows = new ArrayList<>(Window.getWindows());
		for (Window w : windows) {
		    if (w instanceof Stage s && 
		    		s != primaryStage && 
		    		s.getModality() != Modality.NONE && 
		    		s.isShowing()) {
		        s.close();
		    }
		}
		util.SystemLog.logAction(
				util.SystemLog.Type.ACTION, 
				util.Session.getSession().getUser().getUserId(), 
				util.SystemLog.ActionMessage.LOGOUT
				);
		util.Session.getSession().clearSession();
		try {
			util.SceneChanger.changeScene(new ActionEvent(primaryStage , null), util.SceneChanger.SceneName.LOGIN.getName());
		} catch (IOException e) {
			e.printStackTrace();
		}
		primaryStage.toFront();
		primaryStage.requestFocus();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
