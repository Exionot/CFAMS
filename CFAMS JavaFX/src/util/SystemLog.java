package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class SystemLog {
	public static enum Type{
		ACTION,
		ERROR
	}
	public static enum ActionMessage{
		LOGIN("logged in"),
		ERROR_LOGIN("tried to login"),
		ADD_FEEDBACK("added a feedback"),
		EDIT_FEEDBACK("edited a feedback"),
		CONFIRM_FEEDBACK("confirmed a feedback"),
		INVALIDATE_FEEDBACK("invalidated feedback"),
		RESOLVE_FEEDBACK("resolved a feedback"),
		CHANGE_PROFILE("changed profile image"),
		CHANGE_PASSWORD("changed password"),
		ADD_USER("added a new user"),
		LOGOUT("logged out");
		
		private final String message;
		private ActionMessage(String message) {
			this.message = message;
		}
		
		public String getMessage() {
			return this.message;
		}
	}
	String username, logDateTime;
	User.UserRole userRole;
	Type logType;
	ActionMessage message;
	
	public SystemLog(Type logType, int userId, String logDateTime, ActionMessage message) {
		this.logType = logType;
		this.username= fetchUsername(userId);
		this.userRole = fetchUserRole(userId);
		this.logDateTime = logDateTime;
		this.message = message;
	}
	
	/**
	 * This logs the action of the user
	 * @param logType - This determines the type of log
	 * @param userRole - This is the current user's role
	 * @param username - This is the current user's username
	 * @param message - This is the message of the action the user did
	 */
	public static void logAction(Type logType, int userId, ActionMessage message) {
		String query = "INSERT INTO system_logs(log_type, user_id, log_message, log_date) VALUE(?, ?, ?, ?)";
		
		LocalDateTime now = LocalDateTime.now();
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setString(1, logType.toString());
			ps.setInt(2, userId);
			ps.setString(3, message.toString());
			ps.setString(4, now.toString());
			ps.execute();
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public String getLogMessage() {
		String logMessage = "[" + this.logType.toString() + " " + this.logDateTime + "] '" + this.userRole.toString() + "' " + this.username + " " + message.getMessage();
		return logMessage;	
	}
	
	private User.UserRole fetchUserRole(int userId){
		String query = "SELECT user_role FROM users WHERE id = ?";
		
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setInt(1, userId);
			try (ResultSet rs = ps.executeQuery();){
				if (rs.next()) {
					return User.UserRole.valueOf(rs.getString("user_role"));
				}
				return null;
			}
		}catch(SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private String fetchUsername(int userId){
		String query = "SELECT username FROM users WHERE id = ?";
		
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(query)) {
			ps.setInt(1, userId);
			try (ResultSet rs = ps.executeQuery();){
				if (rs.next()) {
					return rs.getString("username");	
				}
				return null;
			}
		}catch(SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
}
