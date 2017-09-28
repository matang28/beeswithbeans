package com.taykey.bwb.core.impl.clusters;

import com.taykey.bwb.core.definitions.ClientResult;
import com.taykey.bwb.core.definitions.RemoteClient;
import com.taykey.bwb.core.definitions.RemoteCluster;
import com.taykey.bwb.core.exceptions.ClusterModificationException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by matan on 26/09/2017.
 */
public class SimpleRemoteCluster<T extends RemoteClient> implements RemoteCluster<T>{

    private final List<T> clientList;
    private boolean isConnected = false;

    private ExecutorService executorService;

    private final Map<String, String> localStorage = new HashMap<>();

    public SimpleRemoteCluster() {
        this.clientList = new LinkedList<T>();
    }

    public void connect() throws IOException {
        try{
            for (T client : clientList){
                client.connect();
            }

            this.isConnected = true;
        }
        catch (Exception ex){
            disconnect();

            if (ex instanceof IOException)
                throw new IOException(ex);
        }
    }

    public void disconnect() throws IOException {

        for (T client : clientList){
            try{
                if(!client.isConnected())
                    client.disconnect();
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }

        this.isConnected = false;
    }

    public boolean isConnected() {
        return this.isConnected;
    }

    public void addClient(T remoteClient) throws ClusterModificationException {
        if(!this.isConnected)
            this.clientList.add(remoteClient);
        else
            throw new ClusterModificationException();
    }

    public void addClients(List<T> remoteClients) throws ClusterModificationException {
        if(!this.isConnected)
            this.clientList.addAll(remoteClients);
        else
            throw new ClusterModificationException();
    }

    public List<ClientResult> run(String command) {

        BlockingQueue<ClientResult> queue = new LinkedBlockingDeque<>();
        executorService = Executors.newCachedThreadPool();

        for (T client : clientList){
            this.executorService.submit(new RemoteClusterRunnable(queue, client, command));
        }

        try {
            this.executorService.shutdown();
            this.executorService.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<ClientResult> result = new ArrayList<>();
        queue.drainTo(result);

        return result;
    }

    @Override
    public void put(String name, String value) {
        this.localStorage.put(name, value);
    }

    @Override
    public String load(String name) {
        return this.localStorage.getOrDefault(name, null);
    }

    @Override
    public List<T> getClients() {
        return clientList;
    }


    @Override
    public int getSize() {
        return this.clientList.size();
    }
}
