package application;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Alert.AlertType;
import util.FancyAlert;

public class AdminAnalyticsController implements Initializable{
	private class KeyPair {
	    String k1;
	    String k2;

	    KeyPair(String k1, String k2) {
	        this.k1 = k1;
	        this.k2 = k2;
	    }

	    @Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (!(o instanceof KeyPair)) return false;
	        KeyPair kp = (KeyPair) o;
	        return k1.equals(kp.k1) && k2.equals(kp.k2);
	    }

	    @Override
	    public int hashCode() {
	        return k1.hashCode() * 31 + k2.hashCode();
	    }
	}
	
	private enum CategoryFilter{
		ALL,
		RECEIVED,
		REVIEWED,
		RESOLVED,
		CONFIRMED,
		INVALID;
	}
	
	private enum TimeFilter{
		WEEK,
		MONTH,
		YEAR;
	}
	
	private enum TypeFilter{
		ALL("All"),
		ACADEMIC("Academic"),
		FACILITY("Facility"),
		SAFETY("Safety"),
		SUGGESTION("Suggestion"),
		TECHNOLOGY("Technology");
		
		private final String value;

		private TypeFilter(String value) {
			this.value = value;
		}
		public String getValue() {
			return this.value;
		}
	}
	
	private NumberAxis yAxis = new NumberAxis(0, 10, 1);
	private CategoryAxis xAxis = new CategoryAxis();
	private CategoryFilter activeCateg;
	private TimeFilter selectedTime;
	private TypeFilter selectedType = TypeFilter.ALL;
	
	@FXML private Button allFeedbackButton, receivedFeedbackButton, reviewedFeedbackButton, resolvedFeedbackButton;
	@FXML private RadioButton thisWeekRadio, thisMonthRadio, thisYearRadio, allRadio, academicRadio, facilityRadio, safetyRadio, suggestionRadio, technologyRadio;
	@FXML private BarChart<String, Number> dataTable = new BarChart<>(xAxis, yAxis);
	@FXML private Label mainTableTitle;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		showAllFeedbackData(null);
		Platform.runLater(() -> allFeedbackButton.requestFocus());
	}
	
	public void goBackToDashboard(ActionEvent e) throws IOException {
		util.SceneChanger.changeScene(e, util.SceneChanger.SceneName.USER_DASHBOARD.getName()); 
	}
	
	public void showAllFeedbackData(ActionEvent e) {
		activeCateg = CategoryFilter.ALL;
		changeTimeFilter(e);
		changeActiveButton();
	}

	public void showReceivedFeedbackData(ActionEvent e) {
		activeCateg = CategoryFilter.RECEIVED;
		changeTimeFilter(e);
		changeActiveButton();
	}

	public void showReviewedFeedbackData(ActionEvent e) {
		activeCateg = CategoryFilter.REVIEWED;
		changeTimeFilter(e);
		changeActiveButton();
	}

	public void showResolvedFeedbackData(ActionEvent e) {
		activeCateg = CategoryFilter.RESOLVED;
		changeTimeFilter(e);
		changeActiveButton();
	}
	
	public void changeTimeFilter(ActionEvent e) {
		selectedTime = thisWeekRadio.isSelected() ? TimeFilter.WEEK :
						thisMonthRadio.isSelected() ? TimeFilter.MONTH :
						thisYearRadio.isSelected() ? TimeFilter.YEAR :
						TimeFilter.WEEK;
		if (selectedTime == TimeFilter.WEEK) dataTable.setTitle("This Week (" + getThisWeek() + ")");
		else if (selectedTime == TimeFilter.MONTH) dataTable.setTitle("This Month (" + YearMonth.now().getMonth().toString() + ")");
		else if (selectedTime == TimeFilter.YEAR) dataTable.setTitle("This Year (" + LocalDate.now().getYear() + ")");
		changeTypeFilter(null);
	}
	
	public void changeTypeFilter(ActionEvent e) {
		selectedType = allRadio.isSelected() ? TypeFilter.ALL :
						academicRadio.isSelected() ? TypeFilter.ACADEMIC :
						facilityRadio.isSelected() ? TypeFilter.FACILITY :
						safetyRadio.isSelected() ? TypeFilter.SAFETY :
						suggestionRadio.isSelected() ? TypeFilter.SAFETY :
						technologyRadio.isSelected() ? TypeFilter.TECHNOLOGY :
						TypeFilter.ALL;
		
		if(!util.DatabaseConnection.isDatabaseConnected()) {
			new FancyAlert(AlertType.ERROR, "Database Connection", "Could not connect to databse...\n").showAndWait();
			return;
		}
		refreshTable(activeCateg, selectedTime, selectedType);
	}
	
	private void refreshTable(CategoryFilter category, TimeFilter time, TypeFilter type) {
		dataTable.getData().clear();
		
		switch (category) {
		case CategoryFilter.ALL:
			mainTableTitle.setText("All Feedback");
			
			if (type == TypeFilter.ALL) {
				XYChart.Series<String, Number> allReceivedData = getAnalytics(CategoryFilter.RECEIVED);
				allReceivedData.setName("Feedback Received");
				dataTable.getData().add(allReceivedData);
				
				XYChart.Series<String, Number> allReviewedData = getAnalytics(CategoryFilter.REVIEWED);
				allReviewedData.setName("Feedback Reviewed");
				dataTable.getData().add(allReviewedData);
				
				XYChart.Series<String, Number> allConfirmedData = getAnalytics(CategoryFilter.CONFIRMED);
				allConfirmedData.setName("Feedback Confirmed");
				dataTable.getData().add(allConfirmedData);
				
				XYChart.Series<String, Number> allInvalidData = getAnalytics(CategoryFilter.INVALID);
				allInvalidData.setName("Feedback Invalidated");
				dataTable.getData().add(allInvalidData);
				
				XYChart.Series<String, Number> allResolvedData = getAnalytics(CategoryFilter.RESOLVED);
				allResolvedData.setName("Feedback Resolved");
				dataTable.getData().add(allResolvedData);
			}else {
				XYChart.Series<String, Number> allReceivedData = getAnalytics(CategoryFilter.RECEIVED, type);
				allReceivedData.setName(type.getValue() + " Feedback Received");
				dataTable.getData().add(allReceivedData);
				
				XYChart.Series<String, Number> allReviewedData = getAnalytics(CategoryFilter.REVIEWED, type);
				allReviewedData.setName(type.getValue() + " Feedback Reviewed");
				dataTable.getData().add(allReviewedData);
				
				XYChart.Series<String, Number> allConfirmedData = getAnalytics(CategoryFilter.CONFIRMED, type);
				allConfirmedData.setName(type.getValue() + " Feedback Confirmed");
				dataTable.getData().add(allConfirmedData);
				
				XYChart.Series<String, Number> allInvalidData = getAnalytics(CategoryFilter.INVALID, type);
				allInvalidData.setName(type.getValue() + "Feedback Invalidated");
				dataTable.getData().add(allInvalidData);
				
				XYChart.Series<String, Number> allResolvedData = getAnalytics(CategoryFilter.RESOLVED, type);
				allResolvedData.setName(type.getValue() + " Feedback Resolved");
				dataTable.getData().add(allResolvedData);
			}
			
			break;
		case CategoryFilter.RECEIVED: 
			mainTableTitle.setText("Recieved Feedback");
			
			if (type == TypeFilter.ALL) {
				XYChart.Series<String, Number> receivedData = getAnalytics(CategoryFilter.RECEIVED);
				receivedData.setName("Feedback Received");
				dataTable.getData().add(receivedData);
			}else {
				XYChart.Series<String, Number> receivedData = getAnalytics(CategoryFilter.RECEIVED, type);
				receivedData.setName(type.getValue() + " Feedback Received");
				dataTable.getData().add(receivedData);
			}
			
			break;
		case CategoryFilter.REVIEWED: 
			mainTableTitle.setText("Reviewed Feedback");
			
			if (type == TypeFilter.ALL) {
				XYChart.Series<String, Number> reviewedData = getAnalytics(CategoryFilter.REVIEWED);
				reviewedData.setName("Feedback Reviewed");
				dataTable.getData().add(reviewedData);
				
				XYChart.Series<String, Number> confirmedData = getAnalytics(CategoryFilter.CONFIRMED);
				confirmedData.setName("Feedback Confirmed");
				dataTable.getData().add(confirmedData);
				
				XYChart.Series<String, Number> invalidData = getAnalytics(CategoryFilter.INVALID);
				invalidData.setName("Feedback Invalidated");
				dataTable.getData().add(invalidData);
			}else {
				XYChart.Series<String, Number> reviewedData = getAnalytics(CategoryFilter.REVIEWED, type);
				reviewedData.setName(type.getValue() + " Feedback Reviewed");
				dataTable.getData().add(reviewedData);
				
				XYChart.Series<String, Number> confirmedData = getAnalytics(CategoryFilter.CONFIRMED, type);
				confirmedData.setName(type.getValue() + " Feedback Confirmed");
				dataTable.getData().add(confirmedData);
				
				XYChart.Series<String, Number> invalidData = getAnalytics(CategoryFilter.INVALID, type);
				invalidData.setName(type.getValue() + "Feedback Invalidated");
				dataTable.getData().add(invalidData);
			}
			
			break;
		case CategoryFilter.RESOLVED:
			mainTableTitle.setText("Resolved Feedback");
			
			if (type == TypeFilter.ALL) {
				XYChart.Series<String, Number> resolvedData = getAnalytics(CategoryFilter.RESOLVED);
				resolvedData.setName("Feedback Resolved");
				dataTable.getData().add(resolvedData);
			}else {
				XYChart.Series<String, Number> resolvedData = getAnalytics(CategoryFilter.RESOLVED, type);
				resolvedData.setName(type.getValue() + " Feedback Resolved");
				dataTable.getData().add(resolvedData);
			}
			
			break;
		default:
			throw new IllegalArgumentException("Unexpected value: " + category);
		}
		
		
		
	}
	
	private Object[][] fetchFeedbackTime(CategoryFilter category, TimeFilter time) {
		String query;
		switch (category) {
		case CategoryFilter.ALL, CategoryFilter.RECEIVED, CategoryFilter.REVIEWED, CategoryFilter.RESOLVED: 
			if (time != TimeFilter.YEAR) {
				query= "SELECT feedback_type, created_at, confirm_date, resolve_date FROM feedback WHERE status LIKE ?";
			}else {
				query= "SELECT feedback_type, DATE_FORMAT(created_at, '%m') AS formatted_created_at, DATE_FORMAT(confirm_date, '%m') AS formatted_confirm_date, DATE_FORMAT(resolve_date, '%m') AS formatted_resolve_date FROM feedback WHERE status LIKE ?";
			}
			break;
		case CategoryFilter.CONFIRMED, CategoryFilter.INVALID: 
			if (time != TimeFilter.YEAR) {
				query= "SELECT feedback_type, created_at, confirm_date, resolve_date FROM feedback WHERE feedback_confirmation LIKE ?";
			}else {
				query= "SELECT feedback_type, DATE_FORMAT(created_at, '%m') AS formatted_created_at, DATE_FORMAT(confirm_date, '%m') AS formatted_confirm_date, DATE_FORMAT(resolve_date, '%m') AS formatted_resolve_date FROM feedback WHERE feedback_confirmation LIKE ?";
			}
			
			
			break;
		default:
			throw new IllegalArgumentException("Unexpected value: " + category);
		}
		
		String categFilter = category == CategoryFilter.RECEIVED || category == CategoryFilter.ALL ? "%" : category.toString();
		System.out.println(categFilter);
		try(Connection conn = util.DatabaseConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(query)){
			ps.setString(1, categFilter);
			
			List<String[]> feedbackDates = new ArrayList<>();
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					String feedbackType = rs.getString("feedback_type");
					
					String createDate = time != TimeFilter.YEAR ? rs.getString("created_at") : rs.getString("formatted_created_at");  
					System.out.println("FetchFeed: " + createDate);
					if (rs.wasNull()) {
					    createDate = null;
					}

					String reviewDate = time != TimeFilter.YEAR ? rs.getString("confirm_date") : rs.getString("formatted_confirm_date");  
					if (rs.wasNull()) {
					    reviewDate = null;
					}

					String resolveDate = time != TimeFilter.YEAR ? rs.getString("resolve_date") : rs.getString("formatted_resolve_date"); 
					if (rs.wasNull()) {
					    resolveDate = null;
					}
					
					if (time != TimeFilter.YEAR) {
					    if (createDate != null) {
					        createDate = createDate.substring(0, Math.min(createDate.length(), 10));
					    }

					    if (reviewDate != null) {
					        reviewDate = reviewDate.substring(0, Math.min(reviewDate.length(), 10));
					    }

					    if (resolveDate != null) {
					        resolveDate = resolveDate.substring(0, Math.min(resolveDate.length(), 10));
					    }
					}
					
					switch (category) {
					case CategoryFilter.RECEIVED: 
						feedbackDates.add(new String[] {createDate, feedbackType});
						break;
					case CategoryFilter.CONFIRMED, CategoryFilter.INVALID, CategoryFilter.REVIEWED: 
						feedbackDates.add(new String[] {reviewDate, feedbackType});
						break;
					case CategoryFilter.RESOLVED:
						feedbackDates.add(new String[] {resolveDate, feedbackType});
						break;
					default:
						throw new IllegalArgumentException("Unexpected value: " + category);
					}
				}
			}
			
			return feedbackDates.toArray(new String[0][]);
			
		}catch(SQLException e) { e.printStackTrace(); return null; }
	}
	
	private synchronized XYChart.Series<String, Number> getAnalytics(CategoryFilter category){
		List<String> countedDates = new ArrayList<>();
		for(Object[] dates : fetchFeedbackTime(category, selectedTime)) {
			countedDates.add((String)dates[0]);
		}
		System.out.println("CoutedDates ------");
		countedDates.forEach(System.out::println);
		System.out.println("------");
		
		String[] dates = new String[0];
		if (selectedTime == TimeFilter.WEEK) 
				dates = getDateWeek(); 
		else if (selectedTime == TimeFilter.MONTH)  
			{ dates = getDateMonth(); }
		else if (selectedTime == TimeFilter.YEAR) 
			{ dates = getMonths(); }
		
		//TODO for each dates, create a hashmap with that date as key, and integer as quantity
		// now, for each countedDates, try to call the hash map using [0] as key, if it exists, replace the quantity with [1] else continue;
		
		HashMap<String, Integer> dataMap = new HashMap<>();
		for(String filteredDates : dates){
			dataMap.put(filteredDates, 0);
		}
		
		for(Object[] allFeedbackDates : countDates(countedDates.toArray(new String[0]))) {
			try {
				dataMap.replace((String)allFeedbackDates[0], dataMap.get((String)allFeedbackDates[0]), (int)allFeedbackDates[1]);
			}catch (Exception e) {
				continue;
			}
		}
		
		Object[][] entryArray = new Object[dataMap.size()][2];
		int i = 0;
        for (Map.Entry<String, Integer> entry : dataMap.entrySet()) {
        	entryArray[i][0] = entry.getKey();
        	entryArray[i][1] = entry.getValue();
        	i++;
        }
        
        Arrays.sort(entryArray, new Comparator<Object[]>() {
            @Override
            public int compare(Object[] a, Object[] b) {
              String dateA = (String) a[0];
              String dateB = (String) b[0];
              return dateA.compareTo(dateB); 
            }
          });
        
		XYChart.Series<String, Number> chartData = new XYChart.Series<>();
		for (Object[] data : entryArray) {
			if (selectedTime == TimeFilter.YEAR) 
				chartData.getData().add(new XYChart.Data<>((Month.of(Integer.parseInt((String)data[0]))).toString(), (Number)data[1]));
			else
				chartData.getData().add(new XYChart.Data<>((String)data[0], (Number)data[1]));
				System.out.println("Added to Chart: " + data[0] + " " + data[1]);
		}
		
		return chartData;
	}
	
	private XYChart.Series<String, Number> getAnalytics(CategoryFilter category, TypeFilter type){
		String[] dates = new String[0];
		List<Object[]> tableData = new ArrayList<>();
		if (selectedTime == TimeFilter.WEEK) 
				dates = getDateWeek(); 
		else if (selectedTime == TimeFilter.MONTH) 
			{ dates = getDateMonth(); }
		else if (selectedTime == TimeFilter.YEAR) 
			{ dates = getMonths(); }
		
		HashMap<KeyPair, Integer> dataMap = new HashMap<>();
		for(Object[] allFeedbackDates : countDates((String[][])fetchFeedbackTime(category, selectedTime))) {
			KeyPair key = new KeyPair((String)allFeedbackDates[0], (String)allFeedbackDates[1]);
			System.out.println("Loop1 Key: " + key.k1 + " " + key.k2);
			if (!dataMap.containsKey(key)) {
				dataMap.put(key, (int)allFeedbackDates[2]);
				System.out.println("Loop1 dataMap put: " + key.k1 + " " + key.k2 + " " + allFeedbackDates[2]);
			}
		}
		
		for (String date : dates) {
			KeyPair key = new KeyPair(date, type.toString());
			System.out.println("Loop2 Key: " + key.k1 + " " + key.k2);
			if (!dataMap.containsKey(key)) {
				System.out.println("dataMap did not have Key: " + key.k1 + " " + key.k2);
			    dataMap.put(key, 0);
			}
		}
		
		for (String date : dates) {
			for (Map.Entry<KeyPair, Integer> entry : dataMap.entrySet()) {
				if (date.equals(entry.getKey().k1) && type.toString().equals(entry.getKey().k2)) {
					tableData.add(new Object[] {entry.getKey().k1, entry.getValue()});
					System.out.println("Added: " + entry.getKey().k1 + " " + entry.getValue());
				}
			}
		}
		
		XYChart.Series<String, Number> chartData = new XYChart.Series<>();
		for (Object[] data : tableData) {
			if (selectedTime == TimeFilter.YEAR) 
				chartData.getData().add(new XYChart.Data<>((Month.of(Integer.parseInt((String)data[0]))).toString(), (Number)data[1]));
			else
				chartData.getData().add(new XYChart.Data<>((String)data[0], (Number)data[1]));
		}
		
		return chartData;
	}
	
	private Object[][] countDates(String[] dates) {
        Map<String, Integer> freq = new HashMap<>();

        for (String d : dates) {
        	System.out.println("d:"+d);
        	try {
        		freq.replace(d, freq.get(d), freq.get(d) + 1);
			} catch (Exception e) {
				freq.put(d,1);
			}
        }

        Object[][] result = new Object[freq.size()][2];
        int i = 0;
        for (Map.Entry<String, Integer> entry : freq.entrySet()) {
            result[i][0] = entry.getKey();
            result[i][1] = entry.getValue();
            System.out.println("CountDates HashMap =====\n" + entry.getKey() + " " + entry.getValue());
            i++;
        }
        System.out.println("=====");
        return result;
    }
	
	private Object[][] countDates(String[][] datesTypes) {
        Map<KeyPair, Integer> freq = new HashMap<>();

        for (String[] dt : datesTypes) {
        	KeyPair key = new KeyPair(dt[0], dt[1]);
        	try {
        		freq.replace(key, freq.get(key), freq.get(key) + 1);
			} catch (Exception e) {
				freq.put(key,1);
			}
        }

        Object[][] result = new Object[freq.size()][3];
        int i = 0;
        for (Map.Entry<KeyPair, Integer> entry : freq.entrySet()) {
            result[i][0] = entry.getKey().k1;
            result[i][1] = entry.getKey().k2;
            result[i][2] = entry.getValue();
            i++;
        }

        return result;
    }
	
	private String[] getDateWeek() {
		LocalDate dateToday = LocalDate.now();
		int day = dateToday.getDayOfWeek().getValue();	
		LocalDate firtsDateOfWeek = dateToday.minusDays(day);
		String[] datesThisWeek = new String[7];
		for (int i = 0; i < 7; i++) {
			if (i == 0) {
				datesThisWeek[i] = firtsDateOfWeek.toString();
				continue;
			}
			datesThisWeek[i] = firtsDateOfWeek.plusDays(i).toString();
		}
		return datesThisWeek;
	}
	
	private String[] getDateMonth() {
		YearMonth thisMonth = YearMonth.now();
		int days = thisMonth.lengthOfMonth();
		String[] thisMonthDates = new String[days];
		for (int i = 0; i < days; i++) {
			thisMonthDates[i] = LocalDate.of(thisMonth.getYear(), thisMonth.getMonth(), i+1).toString();
		}
		return thisMonthDates;
	}
	
	private String[] getMonths() {
		List<String> monthList = new ArrayList<>();
		for (Month month : Month.values()) {
			monthList.add(String.format("%02d", month.getValue()));
		}
		return monthList.toArray(new String[0]);
	}
	
	private String getThisWeek() {
		LocalDate dateToday = LocalDate.now();
		int day = dateToday.getDayOfWeek().getValue();	
		LocalDate firtsDateOfWeek = dateToday.minusDays(day);
		
		return firtsDateOfWeek.toString() + " to " + firtsDateOfWeek.plusDays(6);
	}
	
	private void changeActiveButton() {
		Button[] allButtons = {
				allFeedbackButton, 
				receivedFeedbackButton, 
				reviewedFeedbackButton, 
				resolvedFeedbackButton
		};
		
		for (Button b : allButtons) {
			b.setStyle(
					"-fx-background-color: ui-secondary-blue; " +
							"-fx-background-radius: 0px; " +
							"-fx-text-fill: ui-white; " +
							"-fx-font-weight: NORMAL; " +
							"-fx-font-size: 13;"
					);
			b.hoverProperty().addListener((_, _, isHovered) -> {
			    if (isHovered) {
			    	b.setStyle(
			    			"-fx-background-color: ui-primary-red; " +
						    	    "-fx-translate-x: 10; " +
						    	    "-fx-background-radius: 0 5 5 0; " +
						    	    "-fx-font-weight: bold; " +
						    	    "-fx-font-size: 14;"
			        );
			    } else {
			    	b.setStyle(
			    			"-fx-background-color: ui-secondary-blue; " +
			    					"-fx-background-radius: 0px; " +
			    					"-fx-text-fill: ui-white; " +
			    					"-fx-font-weight: NORMAL; " +
			    					"-fx-font-size: 13;"
			        );
			    }
			});
		}
		
		Button selectedButton = activeCateg == CategoryFilter.ALL ? allFeedbackButton :
								activeCateg == CategoryFilter.RECEIVED ? receivedFeedbackButton :
								activeCateg == CategoryFilter.REVIEWED ? reviewedFeedbackButton :
								activeCateg == CategoryFilter.RESOLVED ? resolvedFeedbackButton :
								null;
		if (selectedButton != null) {
			selectedButton.setStyle(
				    "-fx-background-color: ui-primary-red; " +
				    	    "-fx-translate-x: 10; " +
				    	    "-fx-background-radius: 0 5 5 0; " +
				    	    "-fx-font-weight: bold; " +
				    	    "-fx-font-size: 14;"
				    	);
			
			selectedButton.hoverProperty().addListener((_, _, isHovered) -> {
			    if (isHovered) {
			    	selectedButton.setStyle(
			    			"-fx-background-color: ui-primary-red; " +
						    	    "-fx-translate-x: 10; " +
						    	    "-fx-background-radius: 0 5 5 0; " +
						    	    "-fx-font-weight: bold; " +
						    	    "-fx-font-size: 14;"
			        );
			    } else {
			    	selectedButton.setStyle(
			    			"-fx-background-color: ui-primary-red; " +
						    	    "-fx-translate-x: 10; " +
						    	    "-fx-background-radius: 0 5 5 0; " +
						    	    "-fx-font-weight: bold; " +
						    	    "-fx-font-size: 14;"
			        );
			    }
			});
		}
	}
}
