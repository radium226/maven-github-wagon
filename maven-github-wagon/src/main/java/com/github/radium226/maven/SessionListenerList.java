package com.github.radium226.maven;

import com.github.radium226.common.ListenerList;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.events.SessionEvent;
import org.apache.maven.wagon.events.SessionListener;

public class SessionListenerList {

    private Wagon wagon;
    private ListenerList<SessionListener> listeners;

    private SessionListenerList(Wagon wagon) {
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

    protected void fireSessionOpening() {
        listeners.fire("sessionOpening", new SessionEvent(wagon, SessionEvent.SESSION_OPENING));
    }

    public void fireSessionDisconnected() {
        listeners.fire("sessionDisconnected", new SessionEvent(this.wagon, SessionEvent.SESSION_DISCONNECTED));
    }

    public boolean contains(SessionListener listener) {
        return listeners.contains(listener);
    }

    public void fireSessionOpened() {
        listeners.fire("sessionOpened", new SessionEvent(this.wagon, SessionEvent.SESSION_OPENED));
    }

    public void fireSessionLoggedIn() {
        listeners.fire("sessionLoggedIn", new SessionEvent(this.wagon, SessionEvent.SESSION_LOGGED_IN));
    }

    public void fireSessionLoggedOff() {
        listeners.fire("sessionLoggedOff", new SessionEvent(this.wagon, SessionEvent.SESSION_LOGGED_OFF));
    }

    public void fireSessionConnectionRefused() {
        listeners.fire("sessionConnectionRefused", new SessionEvent(this.wagon, SessionEvent.SESSION_CONNECTION_REFUSED));
    }

    public void fireSessionDisconnecting() {
        listeners.fire("sessionDisconnecting", new SessionEvent(this.wagon, SessionEvent.SESSION_DISCONNECTING));
    }
}
