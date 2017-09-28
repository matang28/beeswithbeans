package com.taykey.bwb.core.definitions;

import java.util.List;

/**
 * Created by matan on 28/09/2017.
 */
public class SumSingleNumberAggregator implements ResultAggregator<String>{

    @Override
    public String aggregate(List<ClientResult> resultList) {

        //Calculate the total file count by summing up the values for each result:
        int totalFileCount = resultList.stream() //For each result

                //The command result is string so transform it to integer:
                .map(result-> Integer.parseInt(result.getCommandResult()))

                //Sum up all integers in the stream:
                .mapToInt(i-> i.intValue())
                .sum();

        return String.format("The total number of files in the cluster is: %s", totalFileCount);
    }
}
