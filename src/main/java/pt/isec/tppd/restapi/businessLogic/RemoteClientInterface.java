package pt.isec.tppd.restapi.businessLogic;

import java.io.IOException;
import java.rmi.Remote;

public interface RemoteClientInterface extends Remote {
    public void showResult(String msg) throws IOException;
}
