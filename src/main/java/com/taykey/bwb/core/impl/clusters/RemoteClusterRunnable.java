package com.taykey.bwb.core.impl.clusters;

import com.taykey.bwb.core.definitions.ClientResult;
import com.taykey.bwb.core.definitions.RemoteClient;

import java.util.concurrent.BlockingQueue;

/**
 * Created by matan on 27/09/2017.
 */
public class RemoteClusterRunnable implements Runnable{

    private final BlockingQueue<ClientResult> queue;
    private final RemoteClient remoteClient;
    private final String command;

    public RemoteClusterRunnable(BlockingQueue<ClientResult> queue, RemoteClient remoteClient, String command) {
        this.queue = queue;
        this.remoteClient = remoteClient;
        this.command = command;
    }

    @Override
    public void run() {

        try{
            queue.put(new ClientResult(remoteClient, remoteClient.run(command)));
        }
        catch (Exception e){
            try {
                queue.put(new ClientResult(remoteClient, e.getMessage()));
            } catch (InterruptedException e1) {}
        }

    }
}
