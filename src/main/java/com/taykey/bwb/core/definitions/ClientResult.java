package com.taykey.bwb.core.definitions;

/**
 * Created by matan on 26/09/2017.
 */
public class ClientResult {

    private final String clientName;
    private final String clientType;
    private final String commandResult;

    public ClientResult(String clientName, String clientType, String commandResult) {
        this.clientName = clientName;
        this.clientType = clientType;
        this.commandResult = commandResult;
    }

    public ClientResult(RemoteClient client, String commandResult){
        this.clientName = client.getName();
        this.clientType = client.getClass().getSimpleName();
        this.commandResult = commandResult;
    }

    public String getClientName() {
        return clientName;
    }

    public String getClientType() {
        return clientType;
    }

    public String getCommandResult() {
        return commandResult;
    }

    @Override
    public String toString() {
        try{
            StringBuilder sb = new StringBuilder();

            sb.append("------------------------------------------\n");
            sb.append(String.format("RemoteClient(%s) returned:\n", clientName));
            sb.append(commandResult);
            sb.append("\n");
            sb.append("------------------------------------------\n");

            return sb.toString();
        }
        catch (Exception e){
            return super.toString();
        }
    }
}
