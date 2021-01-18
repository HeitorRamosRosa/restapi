package pt.isec.tppd.restapi.businessLogic;

import java.io.Serializable;
import java.net.InetAddress;

public class ClientData implements Serializable {
    private String name;
    private String password;
    private String serverIp;
    private int serverPort;
    private InetAddress clientIp;
    private int clientPort;
    private int clientN;
    boolean loggedIn = false;
    private String message ="defaultMessage"; // when using dm feature
    private String recipient = "defaultrecipient"; // when using dm feature
    private String channelName = "none";
    private boolean inChannel = false;
    private String channelToCreateName;
    private String token = "invalid";
    public ClientData() {
    }

    public ClientData(String name, String userName, String password, String serverIp, int serverPort) {
        this.name = name;
        this.password = password;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        clientN = -1;
    }

    public ClientData(ClientData cp) {
        name = cp.getName();
        password = cp.getPassword();
        serverIp = cp.getServerIp();
        serverPort = cp.getServerPort();
        clientIp = cp.getClientIp();
        clientPort = cp.getClientPort();
        clientN = cp.getClientN();
        loggedIn = cp.isLoggedIn();
        message = cp.getMessage();
        recipient = cp.getRecipient();
        inChannel = cp.isInChannel();
        channelName = cp.getChannelName();
        channelToCreateName = cp.getChannelToCreateName();
    }

    public ClientData(String n,String pw) {
        name = n;
        password = pw;
    }

    public String getName() {
        return name;
    }


    public String getPassword() {
        return password;
    }

    public String getServerIp() {
        return serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setName(String name) {
        this.name = name;
    }


    public void setPassword(String password) {
        this.password = password;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public InetAddress getClientIp() {
        return clientIp;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientIp(InetAddress clientIp) {
        this.clientIp = clientIp;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public int getClientN() {
        return clientN;
    }

    public void setClientN(int clientN) {
        this.clientN = clientN;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getChannelName() {
        return channelName;
    }

    public boolean isInChannel() {
        return inChannel;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public void setInChannel(boolean inChannel) {
        this.inChannel = inChannel;
    }

    public String getChannelToCreateName() {
        return channelToCreateName;
    }

    public void setChannelToCreateName(String channelToCreateName) {
        this.channelToCreateName = channelToCreateName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
