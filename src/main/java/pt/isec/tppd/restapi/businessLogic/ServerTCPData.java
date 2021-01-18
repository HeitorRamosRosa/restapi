package pt.isec.tppd.restapi.businessLogic;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerTCPData {
    Socket socket;
    int connectionNumber;
    ObjectOutputStream oOS;
    ObjectInputStream oIS;

    public ServerTCPData(Socket socket, int connectionNumber) {
        this.socket = socket;
        this.connectionNumber = connectionNumber;
    }

    public Socket getSocket() {
        return socket;
    }

    public int getConnectionNumber() {
        return connectionNumber;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setConnectionNumber(int connectionNumber) {
        this.connectionNumber = connectionNumber;
    }

    public void setoOS(ObjectOutputStream oOS) {
        this.oOS = oOS;
    }

    public void setoIS(ObjectInputStream oIS) {
        this.oIS = oIS;
    }

    public ObjectOutputStream getoOS() {
        return oOS;
    }

    public ObjectInputStream getoIS() {
        return oIS;
    }

}
