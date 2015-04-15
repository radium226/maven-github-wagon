package com.github.radium.maven;

import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.events.SessionEvent;
import org.apache.maven.wagon.events.SessionListener;

public class SessionListenerList {

	private Wagon wagon;
	private ListenerList<SessionListener> listeners;
	
	private  SessionListenerList(Wagon wagon) {
		super();
		
		this.wagon = wagon;
		this.listeners = ListenerList.of(SessionListener.class);
	}
	
	public boolean add(SessionListener listener) {
		return listeners.add(listener);
	}
	
	public boolean remove(SessionListener listener) {
		return listeners.remove(listener);
	}
	
	public static SessionListenerList create(Wagon wagon) {
		return new SessionListenerList(wagon);
	}
	
	public void fireSessionOpening(int eventType) {
		fireSessionOpening(new SessionEvent(wagon, eventType));
	}
	
	public void fireSessionOpening(SessionEvent event) {
		listeners.fire("sessionOpening", event);
	}
	
	public void fireSessionOpening(Exception exception) {
		fireSessionOpening(new SessionEvent(wagon, exception));
	}
	
	public boolean contains(SessionListener listener) {
		return listeners.contains(listener);
	}

}
