package com.taykey.bwb.extensions;

import com.amazonaws.services.ec2.model.Instance;
import com.taykey.bwb.app.BwbContext;
import com.taykey.bwb.core.definitions.Extension;
import com.taykey.bwb.core.definitions.RemoteCluster;
import com.taykey.bwb.core.impl.clients.PlainSSHClient;
import com.taykey.bwb.core.impl.clusters.SimpleRemoteCluster;
import com.taykey.bwb.utils.EC2Utils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Created by matan on 27/09/2017.
 */
public class AwsCreateClusterExtension implements Extension{
    @Override
    public String getName() {
        return "ec2-cluster-create";
    }

    @Override
    public String apply(BwbContext context, Map<String, String> options) throws Exception {

        String clusterName = options.get("-c");
        String awsToken = options.get("-id");
        String awsSecret = options.get("-secret");
        String awsZone = options.get("-z");

        //Parse the private key name:
        String keyName = options.get("-k");
        int lastSlash = keyName.lastIndexOf(File.separator);
        keyName = keyName.substring(lastSlash + 1, keyName.length());
        keyName = keyName.replace(".pem", "");
        keyName = keyName.replace(".ppk", "");

        //Create the AWS instances:
        List<Instance> instances = EC2Utils.createInstances(
                awsToken,
                awsSecret,
                awsZone,
                options.get("-t"),
                options.get("-i"),
                Integer.valueOf(options.get("-n")),
                options.get("-g"),
                keyName,
                options.get("-v")
        );

        //Now we need to create the bwb cluster:
        RemoteCluster<PlainSSHClient> cluster = new SimpleRemoteCluster<>();

        //Add each instance to remote cluster:
        instances.forEach(instance -> cluster.addClient(new PlainSSHClient(instance.getPublicIpAddress(), options.get("-u"), options.get("-k"), "", 22, instance.getInstanceId())));

        //Save the key and secret on the cluster's local storage:
        cluster.put("awsKey", awsToken);
        cluster.put("awsSecret", awsSecret);
        cluster.put("awsZone", awsZone);

        cluster.connect();

        context.addCluster(clusterName, cluster);

        return String.format("Cluster %s created successfully containing %d instances.", clusterName, cluster.getSize());
    }

    @Override
    public String help() {
        StringJoiner sj = new StringJoiner("\n");

        sj.add("Create EC2 instances cluster");
        sj.add("Options:");
        sj.add("  -c => The name of the cluster to create.");
        sj.add("  -id => AWS application token");
        sj.add("  -secret => AWS application secret key");
        sj.add("  -n => Number of instances to create.");
        sj.add("  -k => The private key to use.");
        sj.add("  -z => The aws region");
        sj.add("  -u => The machine username");
        sj.add("  -i => The AMI to be deployed.");
        sj.add("  -t => The type of the EC2 instance.");
        sj.add("  -v => The subnet id");
        sj.add("  -g => The security group to apply");
        sj.add("Example: ext-run -e ec2-cluster-create -o \"-id xxx -secret xxx -c cluster -n 1 -k /home/ubuntu/.ssh/privatekey.pem -z us-east-2a -u ubuntu -i image -t t1.micro -v subnet-222 -g secruitygroup\"");

        return sj.toString();
    }
}
