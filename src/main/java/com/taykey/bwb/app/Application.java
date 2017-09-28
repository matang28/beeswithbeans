package com.taykey.bwb.app;

import com.taykey.bwb.core.definitions.Extension;
import com.taykey.bwb.core.definitions.Plugin;
import com.taykey.bwb.utils.ReflectionUtils;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.*;

/**
 * Created by matan on 26/09/2017.
 */
@SpringBootApplication
@ComponentScan("com.taykey.bwb")
public class Application {

    private BwbContext context;

    public static void main(String[] args) throws Exception {

        new SpringApplicationBuilder()
                .bannerMode(Banner.Mode.OFF)
                .sources(Application.class).child(Application.class)
                .run(args);

    }

    @Bean
    BwbContext getBwbContext() throws InstantiationException, IllegalAccessException {

        if(context==null){
            context = new BwbContext();
            context.addPlugins(discoverPlugins());
            context.addExtensions(discoverExtensions());
        }

        return context;
    }

    private static List<Plugin<?>> discoverPlugins() throws IllegalAccessException, InstantiationException {

        List<Plugin<?>> pluginList = new ArrayList<Plugin<?>>();

        //Discover all plugins classes:
        Set<Class<? extends Plugin>> pluginClasses = ReflectionUtils.allClassesImplementedBy(Plugin.class);

        //Instantiate all plugins:
        for (Class<? extends Plugin> pluginClass : pluginClasses) {
            try{
                pluginList.add(pluginClass.newInstance());
            }
            catch (Exception e){
            }
        }

        return pluginList;
    }

    private static Set<Extension> discoverExtensions() throws IllegalAccessException, InstantiationException {

        Set<Extension> extList = new HashSet<>();

        //Discover all plugins classes:
        Set<Class<? extends Extension>> extClasses = ReflectionUtils.allClassesImplementedBy(Extension.class);

        //Instantiate all plugins:
        for (Class<? extends Extension> extClass : extClasses) {
            try{
                extList.add(extClass.newInstance());
            }
            catch (Exception e){
            }
        }

        return extList;
    }

}
