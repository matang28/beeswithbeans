package com.taykey.bwb.app.controllers;

import com.taykey.bwb.app.BwbContext;
import com.taykey.bwb.core.definitions.Plugin;
import com.taykey.bwb.core.definitions.RemoteCluster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.*;

/**
 * Created by matan on 26/09/2017.
 */
@ShellComponent
public class PluginController {

    private final BwbContext context;

    @Autowired
    public PluginController(BwbContext context) {
        this.context = context;
    }

    @ShellMethod(key = "plugin-list", value = "Return a list of the installed plugins.")
    public String listPlugins(){

        StringJoiner sj = new StringJoiner("###########################\n###########################\n", "###########################\n", "\n###########################");

        this.context.getPluginsHelp().forEach(sj::add);

        return sj.toString();
    }

    @ShellMethod(key = "plugin-run", value = "Runs a plugin against a cluster.")
    public String runPlugin(@ShellOption(value = {"-c", "--cluster"}) String clusterName,
                            @ShellOption(value = {"-p", "--plugin"}) String pluginName,
                            @ShellOption(value = {"-o", "--options"}) String params){

        RemoteCluster cluster = this.context.getCluster(clusterName);

        if(cluster!=null){
            Plugin plugin = this.context.getPlugin(pluginName);

            if(plugin!=null){
                plugin.setup(cluster);
                String result = plugin.apply(cluster, extractParams(Arrays.asList(params.split(" "))));
                plugin.tearDown(cluster);

                return result;
            }

            return String.format("Cannot find plugin named: %s.", pluginName);
        }

        return String.format("Cannot find cluster named: %s.", clusterName);
    }

    private static Map<String, String> extractParams(List<String> params) {

        Map<String, String> result = new HashMap<>();

        if(params.size()%2==0){
            for(int i=0;i<params.size();i+=2){
                result.put(params.get(i), params.get(i+1));
            }
        }
        else{
            throw new RuntimeException("Cannot decode odd number of command line args.");
        }

        return result;
    }
}
