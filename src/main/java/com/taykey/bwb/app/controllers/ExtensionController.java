package com.taykey.bwb.app.controllers;

import com.taykey.bwb.app.BwbContext;
import com.taykey.bwb.core.definitions.Extension;
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
public class ExtensionController {

    private final BwbContext context;

    @Autowired
    public ExtensionController(BwbContext context) {
        this.context = context;
    }

    @ShellMethod(key = "ext-list", value = "Return a list of the installed extensions.")
    public String listExtension(){

        StringJoiner sj = new StringJoiner("###########################\n###########################\n", "###########################\n", "\n###########################");

        this.context.getExtensionsHelp().forEach(sj::add);

        return sj.toString();
    }

    @ShellMethod(key = "ext-run", value = "Runs an extension against")
    public String runExtension(@ShellOption(value = {"-e", "--ext"}) String pluginName,
                            @ShellOption(value = {"-o", "--options"}) String params) throws Exception {

        Extension extension = this.context.getExtenstion(pluginName);

        if(extension!=null){
            return extension.apply(context, extractParams(Arrays.asList(params.split(" "))));
        }

        return String.format("Cannot find extension named: %s.", pluginName);
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
