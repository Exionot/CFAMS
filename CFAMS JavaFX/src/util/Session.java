package util;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class Session {
	private static Session session;
	private User user;
	
	private Timer timer;
    private TimerTask timerTask;
    private Duration timeoutDuration; 
    private Runnable onTimeout; 
	
	private Session () {}
	
	public static Session getSession() {
		if (session == null) session = new Session();
		return session;
	}
	
	public void setUser(User user) { 
		this.user = user; 
		resetTimeout();
	}
	public User getUser() { return this.user; }
	
	public void setTimeoutDuration(Duration duration) {
        this.timeoutDuration = duration;
    }

    public void setOnTimeout(Runnable callback) {
        this.onTimeout = callback;
    }

    public void resetTimeout() {
        if (timer == null) {
        	timer = new Timer(true);
        }

        if (timerTask != null) {
        	timerTask.cancel();
        }

        timerTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (onTimeout != null) onTimeout.run();
                    clearSession();
                });
            }
        };
        
        if (timeoutDuration == null) {
        	timeoutDuration = Duration.ofMinutes(30);
		}

        timer.schedule(timerTask, timeoutDuration.toMillis());
    }
    
    public void setListeners(Scene scene) {
	    	scene.addEventFilter(MouseEvent.ANY, _ -> Session.getSession().resetTimeout());
			scene.addEventFilter(KeyEvent.ANY, _ -> Session.getSession().resetTimeout());
    }

    public void clearSession() {
        user = null;
        timerTask.cancel();
    }

    public void stopTimer() {
        if (timer != null) timer.cancel();
    }
}
