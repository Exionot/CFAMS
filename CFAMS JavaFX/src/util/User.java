package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import application.Main;

public class User{
	public static enum UserRole {
		ADMIN,
		MODERATOR,
		USER;
	}
	public static enum Course{
		BSA("BS Accountancy"),
		BSENTREP("BS Entrepreneurship"),
		BECED("BS Early Childhood Education"),
		BSIE("BS Industrial Engineering"),
		BSECE("BS Electronics Engineering"),
		BSIT("BS Information Technology");
		
		private final String NAME;
		private Course(String name) {
			this.NAME = name;
		}
		
		public String getName() {
			return this.NAME;
		}
	}
	public static enum Year{
		YEAR_1("1st Year"),
		YEAR_2("2nd Year"),
		YEAR_3("3rd Year"),
		YEAR_4("4th Year");
		
		private final String NAME;
		private Year(String name) {
			this.NAME = name;
		}
		
		public String getName() {
			return this.NAME;
		}
	}
	private final int userId;
	private String name, studentId, imagePath;
	private Course course;
	private Year year;
	private UserRole role;

	public User(int userId, String name, String studentId, String course, String year, String imagePath) {
		this.userId = userId;
		this.name = name;
		this.studentId = studentId;
		this.course = course != null ? Course.valueOf(course) : null;
		this.year = year != null ? Year.valueOf(year) : null;
		this.imagePath = imagePath != null ? imagePath : Main.RESOURCE_DIR + "default-profile.jpg";
		this.role = fetchUserRole(name, userId);
	}
	
	public int getUserId() { return this.userId; }
	public String getUsername() { return this.name; }
	public String getStudentId() { return this.studentId; }
	public Course getCourse() { return this.course; }
	public Year getYear() { return this.year; }
	public String getImagePath() { return this.imagePath; }
	public UserRole getRole() { return this.role; }
	public static String[] fetchUserRoles() {
		ArrayList<String> roleList = new ArrayList<>();
		roleList.add("Select User Role");
		for (UserRole role : UserRole.values()) {
			roleList.add(role.toString());
		}
		return roleList.toArray(new String[0]);
	}

	private UserRole fetchUserRole(String name, int userId) {
		String query = "SELECT user_role FROM users WHERE username = ? AND id = ?";

		try (Connection conn = util.DatabaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, name);
			ps.setInt(2, userId);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return UserRole.valueOf(rs.getString("user_role"));
				}else return null;
			}

		}catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
}