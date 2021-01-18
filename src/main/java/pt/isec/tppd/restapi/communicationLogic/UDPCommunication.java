package pt.isec.tppd.restapi.communicationLogic;

import pt.isec.tppd.restapi.businessLogic.ClientData;

import java.io.Serializable;

public class UDPCommunication implements Serializable {
    private String request;
    private boolean accepted;
    private ClientData clientData;

    public UDPCommunication(String request, boolean accepted, ClientData clientData) {
        this.request = request;
        this.accepted = accepted;
        this.clientData = clientData;
    }

    public String getRequest() {
        return request;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public ClientData getClient() {
        return clientData;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public void setClientData (ClientData c) {
        this.clientData = c;
    }
}
