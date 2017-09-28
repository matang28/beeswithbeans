package com.taykey.bwb.core.definitions;

import java.io.IOException;

/**
 * Created by matan on 26/09/2017.
 */
public interface RemoteClient {

    /**
     * Connects to the remote host.
     * @throws IOException
     */
    void connect() throws IOException;

    /**
     * Disconnect from the remote host.
     * @throws IOException
     */
    void disconnect() throws IOException;

    /**
     * @return true if the client is connected.
     */
    boolean isConnected();

    /**
     * Run a command on the remote host.
     * @param command the command to be executed.
     * @return the plain text output of the command.
     * @throws IOException
     */
    String run(String command) throws IOException;

    String getName();

}
