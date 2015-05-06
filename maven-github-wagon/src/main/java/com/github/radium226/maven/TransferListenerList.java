package com.github.radium226.maven;

import com.github.radium226.common.ListenerList;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.events.SessionEvent;
import org.apache.maven.wagon.events.SessionListener;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.resource.Resource;

public class TransferListenerList {

    private Wagon wagon;
    private ListenerList<TransferListener> listeners;

    private TransferListenerList(Wagon wagon) {
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

    public void fireTransferCompleted(String resourceName, int requestType) {
        listeners.fire("transferCompleted", new TransferEvent(wagon, new Resource(resourceName), TransferEvent.TRANSFER_COMPLETED, requestType));
    }

    public void fireTransferInitiated(String resourceName, int requestType) {
        listeners.fire("transferInitiated", new TransferEvent(wagon, new Resource(resourceName), TransferEvent.TRANSFER_INITIATED, requestType));
    }

    public void fireTransferProgress(String resourceName, int requestType, byte[] buffer, int length) {
        listeners.fire("transferProgress", new TransferEvent(wagon, new Resource(resourceName), TransferEvent.TRANSFER_PROGRESS, requestType), buffer, length);
    }

    public void fireTransferStarted(String resourceName, int requestType) {
        listeners.fire("transferStarted", new TransferEvent(wagon, new Resource(resourceName), TransferEvent.TRANSFER_STARTED, requestType));
    }

    public void fireTransferError(String resourceName, int requestType) {
        listeners.fire("transferError", new TransferEvent(wagon, new Resource(resourceName), TransferEvent.TRANSFER_ERROR, requestType));
    }

}
