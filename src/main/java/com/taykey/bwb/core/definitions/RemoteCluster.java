package com.taykey.bwb.core.definitions;

import com.taykey.bwb.core.exceptions.ClusterModificationException;

import java.io.IOException;
import java.util.List;

/**
 * Created by matan on 26/09/2017.
 */
public interface RemoteCluster<T extends RemoteClient> {

    /**
     * Runs the connect command on each one of the remote clients.
     * @throws IOException
     */
    void connect() throws IOException;

    /**
     * Runs the disconnect command on each of the remote clients.
     * @throws IOException
     */
    void disconnect() throws IOException;

    /**
     * @return true if all clients in the cluster are connected.
     */
    boolean isConnected();

    /**
     * Adds a client to the cluster.
     * @param remoteClient the remote client to be added.
     * @throws ClusterModificationException if the cluster is already connected.
     */
    void addClient(T remoteClient) throws ClusterModificationException;

    /**
     * Adds a list of clients to the cluster.
     * @param remoteClients the list of remote clients.
     * @throws ClusterModificationException if the cluster is already connected.
     */
    void addClients(List<T> remoteClients) throws ClusterModificationException;

    /**
     * Will run the provided command on each one of the members of the cluster.
     * @param command the command to be executed.
     * @return a list of plain text output of each one of the clients, null indicates that a command has thrown an exception.
     */
    List<ClientResult> run(String command);

    void put(String name, String value);

    String load(String name);

    List<T> getClients();

    int getSize();

}
