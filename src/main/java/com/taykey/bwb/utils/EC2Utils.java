package com.taykey.bwb.utils;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.waiters.WaiterParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by matan on 28/09/2017.
 */
public class EC2Utils {

    public static List<Instance> createInstances(String awsToken, String awsSecret,
                                                  String zone, String instanceType, String imageId, Integer instanceCount,
                                                  String securityGroup, String keyName, String subnetId) {

        AmazonEC2 ec2 = createEC2Client(awsToken, awsSecret, zone);

        //Build the instances request:
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
                .withInstanceType(instanceType)
                .withImageId(imageId)
                .withMinCount(instanceCount)
                .withMaxCount(instanceCount)
                .withSecurityGroupIds(securityGroup)
                .withKeyName(keyName)
                .withSubnetId(subnetId);

        //Execute the run instances requests:
        RunInstancesResult result = ec2.runInstances(runInstancesRequest);

        //Get the list of the instance ids:
        List<String> instanceIds = result.getReservation().getInstances().stream().map(Instance::getInstanceId).collect(Collectors.toList());

        //And wait until the instances are up:
        ec2.waiters().instanceRunning().run(new WaiterParameters<>(new DescribeInstancesRequest().withInstanceIds(instanceIds)));

        //Now we go and get the updated status of the instances:
        List<Instance> out = new ArrayList<>();
        ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(instanceIds)).getReservations().forEach(reservation -> out.addAll(reservation.getInstances()));

        return out;
    }

    public static void deleteInstances(String awsToken, String awsSecret, String zone, List<String> instanceIds){

        AmazonEC2 ec2 = createEC2Client(awsToken, awsSecret, zone);

        ec2.terminateInstances(new TerminateInstancesRequest(instanceIds));

        ec2.waiters().instanceTerminated().run(new WaiterParameters<>(new DescribeInstancesRequest().withInstanceIds(instanceIds)));

    }

    private static AmazonEC2 createEC2Client(String awsToken, String awsSecret, String zone){
        //Build the AWS EC2 client:
        AmazonEC2 ec2 = AmazonEC2ClientBuilder
                .standard()
                .withRegion(zone)
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsToken, awsSecret)))
                .build();

        return ec2;
    }

}
