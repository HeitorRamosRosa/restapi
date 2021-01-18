package pt.isec.tppd.restapi.businessLogic;

import java.io.Serializable;
import java.util.Vector;

public class Channel implements Serializable {
    private String name;
    private String owner;
    private int nClients = 0;
    private int nMessages = 0;
    Vector <String> messages;

    public Channel() {
        messages = new Vector<>();
    }

    public Channel(String n, String o) {
        name = n;
        owner = o;
        messages = new Vector<>();
    }

    public String getName() {
        return name;
    }

    public String getOwner() {
        return owner;
    }

    public int getnClients() {
        return nClients;
    }

    public int getnMessages() {
        return nMessages;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setnClients(int nClients) {
        this.nClients = nClients;
    }

    public void setnMessages(int nMessages) {
        this.nMessages = nMessages;
    }
}
