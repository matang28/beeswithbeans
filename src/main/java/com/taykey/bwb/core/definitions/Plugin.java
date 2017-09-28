package com.taykey.bwb.core.definitions;

import java.util.Map;

/**
 * Created by matan on 26/09/2017.
 */
public interface Plugin <T extends RemoteClient>{

    String getName();

    void setup(RemoteCluster<T> cluster);

    void tearDown(RemoteCluster<T> cluster);

    ResultAggregator<String> getResultAggregator();

    String apply (RemoteCluster<T> cluster, Map<String, String> args);

    default String help(){
        return getName();
    }

}
