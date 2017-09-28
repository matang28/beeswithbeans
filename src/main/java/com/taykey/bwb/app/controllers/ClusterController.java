package com.taykey.bwb.app.controllers;

import com.taykey.bwb.app.BwbContext;
import com.taykey.bwb.core.definitions.ClientResult;
import com.taykey.bwb.core.definitions.RemoteCluster;
import com.taykey.bwb.core.impl.clusters.SimpleRemoteCluster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;

/**
 * Created by matan on 26/09/2017.
 */
@ShellComponent
public class ClusterController {

    private BwbContext context;

    @Autowired
    public ClusterController(BwbContext context) {
        this.context = context;
    }

    @ShellMethod(key="cluster-create", value = "Creates an empty cluster.")
    public String createCluster(String name){
        this.context.addCluster(name, new SimpleRemoteCluster<>());
        return String.format("Cluster %s Created", name);
    }

    @ShellMethod(key="cluster-size", value = "Gets the number of the remote clients in the cluster.")
    public String getClusterSize(String name){
        RemoteCluster cluster = this.context.getCluster(name);

        if(cluster!=null)
            return String.format("Cluster %s has %d clients attached...", name, cluster.getSize());
        else
            return String.format("No cluster with name %s found.", name);
    }

    @ShellMethod(key = "cluster-connect", value = "Connect all remote clients inside the cluster.")
    public String connectCluster(String name){

        RemoteCluster cluster = this.context.getCluster(name);

        if(cluster!=null){
            try {
                cluster.connect();
                return String.format("Cluster %s is connected...", name);
            } catch (IOException e) {
                return String.format("Error thrown while trying to connect cluster %s with the following exception: %s", name, e.getMessage());
            }
        }
        else
            return String.format("No cluster with name %s found.", name);
    }

    @ShellMethod(key = "cluster-disconnect", value = "Disconnects all remote clients inside the cluster.")
    public String disconnectCluster(String name){
        RemoteCluster cluster = this.context.getCluster(name);

        if(cluster!=null){
            try {
                cluster.disconnect();
                return String.format("Cluster %s is disconnected...", name);
            } catch (IOException e) {
                return String.format("Error thrown while trying to disconnect cluster %s with the following exception: %s", name, e.getMessage());
            }
        }
        else
            return String.format("No cluster with name %s found.", name);
    }

    @ShellMethod(key = "cluster-run", value = "Runs a command on each one of the cluster members. (sync)")
    public String runCommandOnCluster(String clusterName, String command){
        RemoteCluster cluster = this.context.getCluster(clusterName);

        if(cluster!=null){
            List<ClientResult> result = cluster.run(command);
            StringJoiner joiner = new StringJoiner("\n", String.format("Running %s on %d members of %s:\n", command, cluster.getSize(), clusterName), "\n");
            result.forEach(item->joiner.add(item.toString()));
            return joiner.toString();
        }
        else
            return String.format("No cluster with name %s found.", clusterName);
    }

}
