package com.github.radium.maven;

import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.events.SessionEvent;
import org.apache.maven.wagon.events.SessionListener;
import org.apache.maven.wagon.events.TransferListener;

public class TransferListenerList {

	private Wagon wagon;
	private ListenerList<TransferListener> listeners;
	
	private  TransferListenerList(Wagon wagon) {
		super();
		
		this.wagon = wagon;
		this.listeners = ListenerList.of(TransferListener.class);
	}
	
	public boolean add(TransferListener listener) {
		return listeners.add(listener);
	}
	
	public boolean remove(TransferListener listener) {
		return listeners.remove(listener);
	}
	
	public static TransferListenerList create(Wagon wagon) {
		return new TransferListenerList(wagon);
	}
	
	public boolean contains(TransferListener listener) {
		return listeners.contains(listener);
	}

}
