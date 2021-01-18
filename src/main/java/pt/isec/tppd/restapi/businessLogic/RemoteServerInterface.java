package pt.isec.tppd.restapi.businessLogic;

import java.io.IOException;
import java.rmi.Remote;

public interface RemoteServerInterface extends Remote {
    void registaCliente(ClientData ClientData, RemoteClientInterface RCI) throws IOException; //adicionar refenrecia para RemoteClienteInterface para poder dar callback
    void sendMensagemToServer(ClientData ClientData, RemoteClientInterface RCI) throws IOException;
    void registaObserver(RemoteClientInterface RCI) throws IOException;
    void unregisterObserver() throws IOException;
}
