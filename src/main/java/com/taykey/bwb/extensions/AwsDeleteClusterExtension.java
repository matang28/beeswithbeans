package com.taykey.bwb.extensions;

import com.taykey.bwb.app.BwbContext;
import com.taykey.bwb.core.definitions.Extension;
import com.taykey.bwb.core.definitions.RemoteClient;
import com.taykey.bwb.core.definitions.RemoteCluster;
import com.taykey.bwb.utils.EC2Utils;

import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Created by matan on 28/09/2017.
 */
public class AwsDeleteClusterExtension implements Extension {


    @Override
    public String getName() {
        return "ec2-cluster-delete";
    }

    @Override
    public String apply(BwbContext context, Map<String, String> options) throws Exception {

        String clusterName = options.get("-c");

        RemoteCluster<?> cluster = context.getCluster(clusterName);

        if(cluster!=null){

            final String key = cluster.load("awsKey");
            final String secret = cluster.load("awsSecret");
            final String zone = cluster.load("awsZone");

            if(key!=null && secret!=null && zone!=null){

                cluster.disconnect();

                EC2Utils.deleteInstances(key, secret, zone, cluster.getClients().stream().map(RemoteClient::getName).collect(Collectors.toList()));

                context.deleteCluster(clusterName);

                return String.format("Cluster %s is deleted successfully.", clusterName);
            }
        }

        return String.format("Cannot find cluster named %s", clusterName);
    }

    @Override
    public String help() {
        StringJoiner sj = new StringJoiner("\n");

        sj.add("Delete EC2 instances cluster");
        sj.add("Options:");
        sj.add("  -c => The name of the cluster to delete.");
        sj.add("Example: ext-run -e ec2-cluster-delete -o \"-c cluster-name\"");

        return sj.toString();
    }
}
