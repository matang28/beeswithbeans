package com.taykey.bwb.app.controllers;

import com.taykey.bwb.app.BwbContext;
import com.taykey.bwb.core.definitions.RemoteClient;
import com.taykey.bwb.core.definitions.RemoteCluster;
import com.taykey.bwb.core.exceptions.ClusterModificationException;
import com.taykey.bwb.core.impl.clients.PlainSSHClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Created by matan on 26/09/2017.
 */
@ShellComponent
public class SshClientController {

    private BwbContext context;

    @Autowired
    public SshClientController(BwbContext context) {
        this.context = context;
    }

    @ShellMethod(key = "ssh-attach", value = "Creates a new ssh client and attach it to a cluster.")
    public String attachClientToCluster(@ShellOption({"-c", "--cluster"}) String clusterName,
                                        @ShellOption({"-h", "--host"}) String hostname,
                                        @ShellOption(value = {"-p", "--port"}, defaultValue = "22") int port,
                                        @ShellOption(value = {"-u", "--username"}) String username,
                                        @ShellOption(value = {"-k", "--key"}) String key,
                                        @ShellOption(value = {"--password"}, defaultValue = "") String password){

        //Get the cluster:
        RemoteCluster cluster = this.context.getCluster(clusterName);

        if(cluster!=null){
            //Create a new SSH client:
            RemoteClient client = new PlainSSHClient(hostname, username, key, password, port);

            try {
                cluster.addClient(client);
                return String.format("%s ssh connection was added to the %s cluster", hostname, clusterName);
            } catch (ClusterModificationException e) {
                return "You cannot modify the cluster when the cluster is connected.";
            }
        }

        return String.format("Cannot find cluster named %s.", clusterName);
    }
}
