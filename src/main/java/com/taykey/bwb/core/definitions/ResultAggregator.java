package com.taykey.bwb.core.definitions;

import java.util.List;

/**
 * Created by matan on 26/09/2017.
 */
public interface ResultAggregator<T> {

    T aggregate(List<ClientResult> resultList);

}
