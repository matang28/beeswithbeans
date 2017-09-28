package com.taykey.bwb.app;

import com.taykey.bwb.core.definitions.Extension;
import com.taykey.bwb.core.definitions.Plugin;
import com.taykey.bwb.core.definitions.RemoteClient;
import com.taykey.bwb.core.definitions.RemoteCluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by matan on 26/09/2017.
 */

public class BwbContext {

    private final Map<String, Plugin<?>> pluginList;

    private final Map<String, RemoteCluster<?>> clusterList;

    private final Map<String, Extension> extensionMap;

    public BwbContext() {
        this.pluginList = new HashMap<>();
        this.clusterList = new HashMap<>();
        this.extensionMap = new HashMap<>();
    }

    public void addCluster(String clusterName, RemoteCluster<? extends RemoteClient> cluster){
        this.clusterList.put(clusterName, cluster);
    }

    public RemoteCluster<? extends RemoteClient> getCluster(String clusterName){
        return this.clusterList.getOrDefault(clusterName, null);
    }

    public void deleteCluster(String clusterName){
        this.clusterList.remove(clusterName);
    }

    public void addPlugin(String pluginName, Plugin<?> plugin){
        this.pluginList.put(pluginName, plugin);
    }

    public void addPlugins(List<Plugin<?>> pluginList){
        pluginList.forEach(item->this.addPlugin(item.getName(), item));
    }

    public Plugin<? extends RemoteClient> getPlugin(String pluginName){
        return this.pluginList.getOrDefault(pluginName, null);
    }

    public List<String> getPluginsHelp(){
        return this.pluginList.entrySet().stream().map(entry->entry.getValue().help()).collect(Collectors.toList());
    }

    public void addExtensions(Set<Extension> extensions){
        extensions.forEach(ext->this.extensionMap.put(ext.getName(), ext));
    }

    public Extension getExtenstion(String name){
        return extensionMap.getOrDefault(name, null);
    }

    public List<String> getExtensionsHelp(){
        return this.extensionMap.entrySet().stream().map(entry->entry.getValue().help()).collect(Collectors.toList());
    }
}
