package util;

public class Feedback {
	public static enum Type{
		ACADEMIC("Academic"),
		FACILITY("Facility"),
		SAFETY("Safety"),
		SUGGESTION("Suggestion"),
		TECHNOLOGY("Technology");
		
		private final String value;

		private Type(String value) {
			this.value = value;
		}
		public String getValue() {
			return this.value;
		}
	}
	public static enum Confirmation{
		PENDING("Pending"),
		CONFIRMED("Confirmed"),
		RESOLVED("Resolved"),
		INVALID("Invalid");
		
		private final String value;

		private Confirmation(String value) {
			this.value = value;
		}
		public String getValue() {
			return this.value;
		}
	}
	public static enum Status{
		PENDING("Pending"),
		REVIEWED("Reviewed"),
		RESOLVED("Resolved"),
		NO_ACTION("No Action Needed");
		
		private final String value;
		
		private Status(String value) {
			this.value = value;
		}
		public String getValue() {
			return this.value;
		}
	}
	public static enum Label{
		//ACADEMIC
		GENERAL_ACADEMIC("General Academic"),
		CC_101("CC 101"),
	    CC_102("CC 102"),
	    HUM_1("HUM 1"),
	    NSTP_1("NSTP 1"),
	    RIZAL("RIZAL"),
	    SOCSCI_3("SOCSCI 3"),
	    WS_101("WS 101"),
		
		
		//FACILITIES
	    GENERAL_FACILITY("General Facility"),
		//First floor
		ROOM_101("Room 101"),
		ROOM_102("Room 102"),
		ROOM_103("Room 103"),
		ROOM_104("Room 104"),
		ROOM_105("Room 105"),
		ROOM_106("Room 106"),
		ROOM_107("Room 107"),
		ROOM_108("Room 108"),
		
		//Second floor
		ROOM_201("Room 201"),
		ROOM_202("Room 202"),
		ROOM_203("Room 203"),
		ROOM_204("Room 204"),
		ROOM_205("Room 205"),
		ROOM_206("Room 206"),
		ROOM_207("Room 207"),
		ROOM_208("Room 208"),
		
		//Third floor
		ROOM_301("Room 301"),
		ROOM_302("Room 302"),
		ROOM_303("Room 303"),
		ROOM_304("Room 304"),
		ROOM_305("Room 305"),
		ROOM_306("Room 306"),
		ROOM_307("Room 307"),
		ROOM_308("Room 308"),
		
		//Fourth floor
		ROOM_401("Room 401"),
		ROOM_402("Room 402"),
		ROOM_403("Room 403"),
		ROOM_404("Room 404"),
		ROOM_405("Room 405"),
		ROOM_406("Room 406"),
		ROOM_407("Room 407"),
		ROOM_408("Room 408"),
		
		
		// SAFETY
		GENERAL_SAFETY("General Safety"),
		HAZARD("Hazard"),
//		INCIDENT("Incident"),
		SECURITY("Security"),
		

		// SUGGESTION
		GENERAL_SUGGESTION("General Suggestion"),
		IMPROVEMENT("Improvement"),
//		IDEA("Idea"),
//		COMMENT("Comment"),
		

		// TECHNOLOGY
		GENERAL_TECH("General Technology"),
		SOFTWARE("Software"),
		HARDWARE("Hardware"),
		NETWORK("Network"),
		;
		
		private final String NAME;
		
		private Label(String name) {
			this.NAME = name;
		}
		
		public String getName() {return this.NAME;}
	}
	
	private String summary, message, createTime, updateTime, confirmTime, resolveTime, resolvedMessage;
	private int id, userId, voteCount;
	private Type type;
	private Confirmation confirmation;
	private Status status;
	private Label label;
	private boolean isPrivate;
	
	public Feedback(int id, int userId, String summary, Type type, Confirmation confirmation, Label label, String message, String createTime, String updateTime, String confirmTime, String resolveTime, Status status, String resolvedMessage, Object isPrivate, int voteCount) {
		this.id = id;
		this.userId = userId;
		this.summary = summary;
		this.type = type;
		this.confirmation = confirmation;
		this.label = label;
		this.message = message;
		this.createTime = createTime;
		this.updateTime = updateTime;
		this.confirmTime = confirmTime;
		this.resolveTime = resolveTime;
		this.status = status;
		this.resolvedMessage = resolvedMessage;
		this.isPrivate = isPrivate == null ? true : (boolean)isPrivate;
		this.voteCount = voteCount;
	}
	
	public int getId() { return this.id; }
	public int getUserId() { return this.userId; }
	public String getSummary() { return this.summary; }
	public Type getType() { return this.type; }
	public Confirmation getConfirmation() { return this.confirmation; }
	public Label getLabel() { return this.label; }
	public String getMessage() { return this.message; }
	public String getCreateDateTime() { return this.createTime; }
	public String getUpdateDateTime() { return this.updateTime; }
	public String getConfirmDateTime() { return this.confirmTime; }
	public String getResolveDateTime() { return this.resolveTime; }
	public Status getStatus() { return this.status; }
	public String getResolvedMessage() { return this.resolvedMessage; }
	public boolean getIsPrivate() { return this.isPrivate; }
	public int getVoteCount() { return this.voteCount; }
	public void incrementVote() { this.voteCount++; }
	public void decrementVote() { this.voteCount--; }
}
