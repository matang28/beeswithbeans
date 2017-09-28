package com.taykey.bwb.core.definitions;

import com.taykey.bwb.app.BwbContext;

import java.util.Map;

/**
 * Created by matan on 27/09/2017.
 */
public interface Extension {

    String getName();

    String apply(final BwbContext context, Map<String, String> options) throws Exception;

    default String help(){
        return getName();
    }

}
