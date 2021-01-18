package pt.isec.tppd.restapi.communicationLogic;

import pt.isec.tppd.restapi.businessLogic.ServerData;

import java.io.Serializable;

public class MulticastCommunication implements Serializable {
    private int sendingServer;
    private String message;
    private ServerData serverData;
    private String sClientName; /*para quando o multicast request for um dm*/
    private String rClientName; /*para quando o multicast request for um dm*/
    private String dmText; /*para quando o multicast request for register*/
    private String channel;

    public MulticastCommunication(int sendingServer, String message, ServerData serverData) {
        this.sendingServer = sendingServer;
        this.message = message;
        this.serverData = serverData;
        sClientName = "notprocessed";
    }


    public String getsClientName() {
        return sClientName;
    }

    public String getrClientName() {
        return rClientName;
    }

    public void setsClientName(String sClientName) {
        this.sClientName = sClientName;
    }

    public void setrClientName(String rClientName) {
        this.rClientName = rClientName;
    }

    public int getSendingServer() {
        return sendingServer;
    }

    public String getMessage() {
        return message;
    }

    public ServerData getServerdata() {
        return serverData;
    }

    public void setSendingServer(int sendingServer) {
        this.sendingServer = sendingServer;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setServerdata(ServerData serverdata) {
        this.serverData = serverData;
    }

    public String getDmText() {
        return dmText;
    }

    public void setDmText(String dmText) {
        this.dmText = dmText;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
