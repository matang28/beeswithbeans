# Bees with Beans (BwB)

## Motivation
Did you ever try to develop an highly scalable web application cluster? 
so you know that trying to create the high load is a very tedious task.  
There are many great tools for unix like "apache bench" or "wrk" but they 
all run on a single machine. We saw a great framework called [Bees with Machine Guns](https://github.com/newsapps/beeswithmachineguns) that spawns a bunch of EC2's micro 
instances "bees" and runs "apache bench" on each one of the "bees", 
collect the result and then shows the aggregated result. very useful.  
 This great idea was the inspiration for building this project.  
 The one thing [Bees with Machine Guns](https://github.com/newsapps/beeswithmachineguns) was missing is the lack of control of the cluster, you can only run "apache bench" on each one of the nodes.  
  So we've decided to create our own framework on-top of the idea of "Bees with Machine Guns".  
  
  The project is written in java and uses [Spring Shell](https://projects.spring.io/spring-shell/) as the framework Jcsh for SSHing, and AWS offical Java SDK.
  
## The shell
When running the jar you will get a shell:
```bash
bwb:> _
```

we are using the shell to specify commands that eventually will run on a cluster of remote nodes. For example lets create
a cluster of 4 hosts and run the command "echo Hello World!" on them.

```bash
# Creating a cluster named Taykey, the cluster will hold our instances.
bwb:> cluster-create Taykey
Cluster Taykey Created

# Adding 4 SSH connections to 4 different hosts:
bwb:> ssh-attach -c Taykey -h HOSTNAME1 -u USERNAME -k PRIVATE_KEY
HOSTNAME1 ssh connection was added to the Taykey cluster

bwb:> ssh-attach -c Taykey -h HOSTNAME2 -u USERNAME -k PRIVATE_KEY
HOSTNAME2 ssh connection was added to the Taykey cluster

bwb:> ssh-attach -c Taykey -h HOSTNAME3 -u USERNAME -k PRIVATE_KEY
HOSTNAME3 ssh connection was added to the Taykey cluster

bwb:> ssh-attach -c Taykey -h HOSTNAME4 -u USERNAME -k PRIVATE_KEY
HOSTNAME4 ssh connection was added to the Taykey cluster

# Connect all clients in the cluster:
bwb:> cluster-connect Taykey
Cluster Taykey connected.
 
# Run any linux command on the cluster, in parallel:
# Will simply run the command "echo Hello World!" on each on of the hosts.
bwb:> cluster-run "echo Hello World!"
------------------------------------------
RemoteClient(HOSTNAME1) returned:
Hello World!
------------------------------------------
------------------------------------------
RemoteClient(HOSTNAME2) returned:
Hello World!
------------------------------------------
------------------------------------------
RemoteClient(HOSTNAME3) returned:
Hello World!
------------------------------------------
------------------------------------------
RemoteClient(HOSTNAME4) returned:
Hello World!
------------------------------------------

```

You can always type "help" to get the list of all commands:
```bash
bwb:>help
AVAILABLE COMMANDS

        clear: Clear the shell screen.
        cluster-connect: Connect all remote clients inside the cluster.
        cluster-create: Creates an empty cluster.
        cluster-disconnect: Disconnects all remote clients inside the cluster.
        cluster-run: Runs a command on each one of the cluster members. (sync)
        cluster-size: Gets the number of the remote clients in the cluster.
        exit, quit: Exit the shell.
        ext-list: Return a list of the installed extensions.
        ext-run: Runs an extension against
        help: Display help about available commands.
        plugin-list: Return a list of the installed plugins.
        plugin-run: Runs a plugin against a cluster.
        script: Read and execute commands from a file.
        ssh-attach: Creates a new ssh client and attach it to a cluster.
        stacktrace: Display the full stacktrace of the last error.
```


## A Plugin
You can extend BwB by writing plugins, basically plugin is anyone that implements the `/core/definitions/Plugin.java` interface. but most of the plugins
should extend the `/core/base/BasePlugin.java` it removes a lot of boilerplate.  

Lets write a basic plugin, lets imagine that we have a cluster of servers that holds pictures and
we want to count the total number of photos in the server.

we can run simple linux shell command to count the number of files in a specific folder:
```bash
bash:> ls PATH | wc -c
288
```

so we want to create a plugin that runs the above command on each one of the servers, get all the results
back, aggregate all numbers and return the sum of the to the shell.

We can start by extending the BasePlugin class:
```java
/**
* Usage: count-files -path /mnt/images
*/
public class SamplePlugin extends BasePlugin{
    
    @Override
    public String getName() {
        //The plugin name (will be used to run it):
        return "count-files";
    }

    @Override
    public ResultAggregator<String> getResultAggregator() {
        // Aggregate the results from each host to a single one:
        return new SumSingleNumberAggregator();
    }

    @Override
    protected String unitOfWork(RemoteCluster cluster, Map args) {
        // The returned string is the command to be executed on each on of the hosts.
        return String.format("ls %s | wc -c", args.get("-path"));
    }
}
```

That's it(ish)! make sure you put this class in the 'com.taykey.bwb.plugins' package and the shell can run 
your plugin.

The only thing left unknown is the SumSingleNumberAggregator class, so let's explain what the ResultAggregator is.

The ResultAggregator responsible for transforming the responses from each host to a single one.
In our example we need to take the number of files from each host and sum it up to a single one and return this number
back to the shell.

The SumSingleNumberAggregator will look like that:
```java
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
```

Now we are really done! we can run the shell application and run our plugin (assuming we have a cluster initiated):
```bash
bwb:> plugin-run -p count-files -c Taykey -o "-path /mnt/images"
The total number of files in the cluster is: 60838
```

the plugin-run command takes three parameters:
1) -p -> the plugin name.
2) -c -> the cluster to run the plugin on.
3) -o -> a string with the parameters of the plugin

This is not the best way to run and We do have a plan to register plugins as regular commands.

## An Extension
Another way to extend Bwb is to extend the shell capabilities a.k.a managing the cluster itself or 
automating several commands or features, adding new logic, etc...

Extensions are made to handle this kind of abilities, for example you can check the code for the
`AwsCreateClusterExtension` or `AwsDeleteClusterExtension` that allows you to create and delete BwB clusters
by creating real EC2 instances on AWS. By running this extension you can create 100 t2.micro AWS instances 
and run commands against this cluster to perform a **distributed-load-testing**.

type `ext-list` to get help on these extensions.

## Where to go from here?
* You can clone the repository, run `mvn package` and execute the jar `java -jar target/bwb-1.0.jar`

* You can even contribute new plugins and extensions :)

* You can also use a plugin for `wrk` to run **distributed-load-testing** with `wrk` a very powerful tool to benchmark web servers.

* You can use the AWS cluster extensions to create the **distributed-load-test** clusters.

So from the moment you have the jar file you can run **distributed-load-tests** on any web server that you want. 
**BUT**, with great power comes great responsibility as stated in the [Bees with Machine Guns](https://github.com/newsapps/beeswithmachineguns) github page:

**If you decide to use the Bees, please keep in mind the following important caveat: they are, more-or-less a distributed denial-of-service attack in a fancy package and, therefore, if you point them at any server you don’t own you will behaving unethically, have your Amazon Web Services account locked-out, and be liable in a court of law for any downtime you cause.**
  
####**You have been warned.**
