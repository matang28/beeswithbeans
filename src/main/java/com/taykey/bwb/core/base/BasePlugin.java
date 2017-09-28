package com.taykey.bwb.core.base;

import com.taykey.bwb.core.definitions.ClientResult;
import com.taykey.bwb.core.definitions.Plugin;
import com.taykey.bwb.core.definitions.RemoteClient;
import com.taykey.bwb.core.definitions.RemoteCluster;

import java.util.List;
import java.util.Map;

/**
 * Created by matan on 26/09/2017.
 */
public abstract class BasePlugin<T extends RemoteClient> implements Plugin<T>{

    @Override
    public void setup(RemoteCluster<T> cluster) {
        //Default is to do nothing...
    }

    @Override
    public void tearDown(RemoteCluster<T> cluster) {
        //Default is to do nothing...
    }

    @Override
    public String apply(RemoteCluster<T> cluster, Map<String, String> args) {

        if(cluster.isConnected()){
            //If the cluster is connected, run on each client the required command:
            List<ClientResult> list = cluster.run(unitOfWork(cluster, args));

            //Use the result aggregator to manipulate each result from the shell:
            return this.getResultAggregator().aggregate(list);
        }

        return null;
    }

    /**
     * @return the command that should be run on each on of the cluster members.
     */
    protected abstract String unitOfWork(RemoteCluster<T> cluster, Map<String, String> args);
}
