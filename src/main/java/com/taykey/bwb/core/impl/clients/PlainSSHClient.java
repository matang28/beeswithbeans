package com.taykey.bwb.core.impl.clients;

import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
import com.taykey.bwb.core.definitions.RemoteClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by matan on 26/09/2017.
 */
public class PlainSSHClient implements RemoteClient{

    private final String address, username, key, password;
    private final int port;

    private final String name;

    private Shell.Plain shell;
    private boolean isConnected = false;

    public PlainSSHClient(String address, String username, String key, String password, int port) {
        this.name = address;
        this.address = address;
        this.username = username;
        this.key = key;
        this.password = password;
        this.port = port;
    }

    public PlainSSHClient(String address, String username, String key, String password, int port, String name) {
        this.address = address;
        this.username = username;
        this.key = key;
        this.password = password;
        this.port = port;
        this.name = name;
    }

    public void connect() throws IOException {
        /*StringBuilder sb = new StringBuilder(); <--- This fucking lines caused me searching for the wrong thing for 4 hours, FUUUUUUUCK
        Files.readAllLines(Paths.get(key)).forEach(sb::append);*/
        String pk = new String(Files.readAllBytes(Paths.get(key)));
        shell = new Shell.Plain((new Ssh(address, port, username, pk)));
        isConnected = true;
    }

    public void disconnect() throws IOException {
        shell = null;
        isConnected = false;
    }

    public boolean isConnected() {
        return this.isConnected;
    }

    public String run(String command) throws IOException {
        return this.shell.exec(command);
    }

    @Override
    public String getName() {
        return name;
    }
}
