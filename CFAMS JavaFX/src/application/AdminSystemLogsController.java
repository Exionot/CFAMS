package application;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import util.FancyAlert;

public class AdminSystemLogsController implements Initializable{
	@FXML private TextArea logTextArea;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		if(!util.DatabaseConnection.isDatabaseConnected()) {
			new FancyAlert(AlertType.ERROR, "Database Connection", "Could not connect to databse...\n").showAndWait();
			return;
		}
		refreshLogs();
	}
	
	public void goBackToDashboard(ActionEvent e) throws IOException {
		util.SceneChanger.changeScene(e, util.SceneChanger.SceneName.USER_DASHBOARD.getName());
	}
	
	private void refreshLogs() {
		logTextArea.clear();
		for (util.SystemLog log : fetchLogs()) {
			logTextArea.appendText(log.getLogMessage() + "\n");
		}
	}
	
	private util.SystemLog[] fetchLogs() {
		String query = "SELECT * FROM system_logs";
		
		try (Connection conn = util.DatabaseConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query)){
			List<util.SystemLog> logList = new ArrayList<>();
			while(rs.next()) {
				logList.add(new util.SystemLog(
						util.SystemLog.Type.valueOf(rs.getString("log_type")),
						rs.getInt("user_id"), 
						rs.getString("log_date"),
						util.SystemLog.ActionMessage.valueOf(rs.getString("log_message"))
						));
			}
			return logList.toArray(new util.SystemLog[0]);

		}catch (SQLException e) {return null;}
	}
}
