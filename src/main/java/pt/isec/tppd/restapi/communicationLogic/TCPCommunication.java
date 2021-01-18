package pt.isec.tppd.restapi.communicationLogic;


import pt.isec.tppd.restapi.businessLogic.ClientData;
import pt.isec.tppd.restapi.businessLogic.ServerData;

import java.io.Serializable;

public class TCPCommunication implements Serializable {
    private String request;
    private boolean accepted;
    private ClientData clientData;
    private ServerData serverData;
    private String rClientName; /*para quando o request for um dm, username de destino*/
    private String sClientName;
    private String message;
    private String channelToCreateName;
    private String channelToEnterName;
    private int value;

    public TCPCommunication(String request, boolean accepted, ClientData clientData, ServerData serverData) {
        this.request = request;
        this.accepted = accepted;
        this.clientData = clientData;
        this.serverData = serverData;
    }

    public String getRequest() {
        return request;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public ClientData getClientData() {
        return clientData;
    }

    public ServerData getServerData() {
        return serverData;
    }

    public int getValue() {
        return value;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public void setClientData(ClientData clientData) {
        this.clientData = clientData;
    }

    public void setServerData(ServerData serverData) {
        this.serverData = serverData;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getrClientName() {
        return rClientName;
    }

    public void setrClientName(String rClientName) {
        this.rClientName = rClientName;
    }

    public String getsClientName() {
        return sClientName;
    }

    public String getMessage() {
        return message;
    }

    public void setsClientName(String sClientName) {
        this.sClientName = sClientName;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getChannelToCreateName() {
        return channelToCreateName;
    }

    public void setChannelToCreateName(String channelToCreateName) {
        this.channelToCreateName = channelToCreateName;
    }

    public String getChannelToEnterName() {
        return channelToEnterName;
    }

    public void setChannelToEnterName(String channelToEnterName) {
        this.channelToEnterName = channelToEnterName;
    }
}
