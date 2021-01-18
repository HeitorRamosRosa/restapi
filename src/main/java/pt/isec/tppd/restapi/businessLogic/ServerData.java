package pt.isec.tppd.restapi.businessLogic;

import pt.isec.tppd.restapi.Database.dbHandler;
import java.io.Serializable;
import java.util.Vector;

public class ServerData implements Serializable {
    private String serverIp;
    private int serverPort;
    private int nClients;
    private int serverNumber;
    private Vector<ClientData> clientDataArray;
    private Vector <Channel> channels;

    public ServerData() {
        clientDataArray = new Vector<>();
        channels = new Vector<>();
    }

    public ServerData(ServerData svd) {
        this.serverIp = svd.getServerIp();
        this.serverPort = svd.getServerPort();
        this.nClients = svd.getnClients();
        this.serverNumber = svd.getServerNumber();
        clientDataArray = new Vector<>();
        channels = new Vector<>();
        for(int i = 0 ; i < clientDataArray.size() ; i++){
            clientDataArray.add(svd.getClientDataArray().get(i));
        }
        for(int i = 0 ; i < channels.size() ; i++){
            channels.add(svd.getChannels().get(i));
        }
    }

    public ServerData(String serverIp, int serverPort, int nClients, int serverNumber) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.nClients = nClients;
        this.serverNumber = serverNumber;
        clientDataArray = new Vector<>();
        channels = new Vector<>();
    }

    public String getServerIp() {
        return serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getnClients() {
        return nClients;
    }

    public int getServerNumber() {
        return serverNumber;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setnClients(int nClients) {
        this.nClients = nClients;
    }

    public void setServerNumber(int serverNumber) {
        this.serverNumber = serverNumber;
    }

    public Vector<ClientData> getClientDataArray() {
        return clientDataArray;
    }


    public void setClientDataArray(Vector<ClientData> c) {
       clientDataArray.clear();
       dbHandler.removeUsersTable("server"+serverNumber);
            for(int i = 0; i < c.size(); i++){
                dbHandler.addUser("server"+serverNumber,c.get(i).getName(),c.get(i).getPassword());
                clientDataArray.add(c.get(i));
            }

    }

    /*
    public void setClientDataArray(Vector<ClientData> c) {
        clientDataArray = c;
    }*/

    public Vector<Channel> getChannels() {
        return channels;
    }

    public void setChannels(Vector<Channel> c) {
        channels.clear();
        dbHandler.removeChannelTable("server"+serverNumber);
            for(int i = 0; i < c.size(); i++){
                dbHandler.addChannel("server"+serverNumber,c.get(i).getName(), c.get(i).getOwner());
                channels.add(c.get(i));
            }
    }
}
