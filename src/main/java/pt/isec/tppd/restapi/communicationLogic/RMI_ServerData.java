package pt.isec.tppd.restapi.communicationLogic;


import pt.isec.tppd.restapi.businessLogic.RemoteServerInterface;

public class RMI_ServerData
{
    String port;
    RemoteServerInterface RemoteServer;

    public RMI_ServerData(String port, RemoteServerInterface remoteServer) {
        this.port = port;
        RemoteServer = remoteServer;
    }
}
