package pt.isec.tppd.restapi.communicationLogic;

import pt.isec.tppd.restapi.Database.dbHandler;
import pt.isec.tppd.restapi.businessLogic.*;

import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;
import java.util.Vector;

public class Server extends Thread{
    private Vector <ServerData> otherServers;
    private Vector <ServerTCPData> connetionsArray;
    private int serverPort;
    private int serverNumber;
    boolean shutdown = false;
    private DatagramSocket dS;
    private ServerSocket sS;
    private static int nTCPConnection = 0;
    private ServerData serverData;
    private boolean threadEnd;
    private int MAX_CHANNELS = 3;
    private Connection dbConn;
    static RemoteClientInterface RCI = null;


    static int RMI_CNUMBER = -666;
    private ArrayList<String> messages = new ArrayList<>();

    public Server(int serverPort) throws SQLException {

        this.serverPort = serverPort;
        connetionsArray = new Vector<>();
        otherServers = new Vector<>();
        serverData = new ServerData();
        serverData.setServerPort(serverPort);
        serverData.setnClients(0);
        serverData.setServerNumber(serverNumber);
        threadEnd = false;
        //dbConn = DriverManager.getConnection(DATABASE_URL,USERNAME,PASSWORD);

    }




    class HandleFirstConnection extends Thread{
        @Override
        public void run(){
            System.out.println("["+serverPort+"] UDP connection manager is up and running.");
            try {

                DatagramPacket dP;

                while(threadEnd != true){

                    dP = new DatagramPacket(new byte[1024],1024);
                    dS.receive(dP);

                    byte[] bufDP = dP.getData();
                    ByteArrayInputStream bAIS = new ByteArrayInputStream(bufDP);
                    ObjectInputStream oIS = new ObjectInputStream(bAIS);
                    UDPCommunication c = (UDPCommunication)oIS.readObject();
                    if(c.getRequest().equals("firstConnect")){
                        InetAddress clientIp = dP.getAddress();
                        int clientPort = dP.getPort();
                        System.out.println("["+serverPort+"]Client trying to connect from: [" +clientIp + "] : ["+clientPort + "].");

                        ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
                        ObjectOutputStream oOS = new ObjectOutputStream(bAOS);

                        System.out.println("["+serverPort+"] Checking if transfer...");
                        //server cheio e unico
                        if(nTCPConnection == 5 && otherServers.isEmpty()){
                            c.setAccepted(false);
                            c.setRequest("noAvailableServers");
                        }
                        //server com espaco e unico
                        else if(nTCPConnection < 5 && otherServers.isEmpty()){
                            ClientData temp = new ClientData();
                            temp.setServerPort(serverPort);
                            temp.setClientN(nTCPConnection);
                            temp.setServerIp("localhost");
                            c.setAccepted(true);
                            c.setClientData(temp);
                        }
                        //server cheio e nao unico:
                        else if (nTCPConnection == 5 && !otherServers.isEmpty()){
                            int nMinCon = 5;
                            int indexServerLight = 0;
                            for(int i=0; i<otherServers.size();i++){
                                if(otherServers.get(i).getnClients() < nMinCon)
                                    nMinCon = otherServers.get(i).getnClients();
                                indexServerLight = i;
                            }
                            //se existir um server com espaco, redireciona para mais leve
                            if(nMinCon<5){
                                ClientData temp = new ClientData();
                                temp.setServerIp("localhost");
                                temp.setServerPort(otherServers.get(indexServerLight).getServerPort());
                                temp.setClientN(nMinCon);
                                c.setRequest("redirected");
                                c.setAccepted(false);
                                c.setClientData(temp);
                            }
                            //se nao existe um server com espaco
                            else{
                                c.setAccepted(false);
                                c.setRequest("noAvailableServers");
                            }
                        }
                        //se server nao cheio, verifica se existe um consideravelmente mais leve
                        else if(nTCPConnection < 5 && !otherServers.isEmpty()){
                            int indexServerLight = -1;
                            for(int i=0; i<otherServers.size();i++){
                                if(otherServers.get(i).getnClients() <= nTCPConnection / 2 && nTCPConnection>0){
                                    indexServerLight = i;
                                    System.out.println("["+serverPort+"] Found server considerably lighter: index["+i+"] with load ["+otherServers.get(i).getnClients()+"] vs currentLoad: ["+nTCPConnection+"]");
                                    break;
                                }
                            }
                            System.out.println("["+serverPort+"] Sending client to server index:["+indexServerLight+"]");
                            //se nao existe consideravelmente mais leve
                            if(indexServerLight == -1){
                                ClientData temp = new ClientData();
                                temp.setServerIp("localhost");
                                temp.setServerPort(serverPort);
                                c.setAccepted(true);
                                c.setClientData(temp);
                            }
                            //se existe consideravelmente mais leve
                            else{
                                ClientData temp = new ClientData();
                                temp.setServerIp("localhost");
                                temp.setServerPort(otherServers.get(indexServerLight).getServerPort());
                                c.setAccepted(false);
                                c.setRequest("redirected");
                                c.setClientData(temp);
                            }
                        }

                        oOS.writeUnshared(c);
                        byte[] bufDPout = bAOS.toByteArray();

                        dP = new DatagramPacket(bufDPout,bufDPout.length,clientIp,clientPort);
                        dS.send(dP);
                    }
                }
                dS.close();
            } catch (IOException | ClassNotFoundException e) {

                e.printStackTrace();

            }
        }
    }


    class EstablishTCPConnection extends Thread {
        @Override
        public void run() {

            try {

                sS = new ServerSocket(serverPort);

                do{
                    System.out.println("["+serverPort+"]Trying to establish TCP connection " + nTCPConnection + ".");
                    Socket s = sS.accept();
                    ObjectInputStream oIS = new ObjectInputStream(s.getInputStream());
                    ObjectOutputStream oOS = new ObjectOutputStream(s.getOutputStream());
                    TCPCommunication c = (TCPCommunication)oIS.readObject();

                    if(c.getRequest().equals("firstTCPConnection")){

                        ServerTCPData sTD = new ServerTCPData(s,nTCPConnection);
                        sTD.setoIS(oIS);
                        sTD.setoOS(oOS);
                        nTCPConnection++;
                        connetionsArray.add(sTD);
                        ClientData temp = new ClientData();
                        temp.setServerIp(serverData.getServerIp());
                        temp.setClientPort(serverPort);
                        temp.setClientN(serverData.getnClients());
                        c.setClientData(temp);
                        serverData.setnClients(serverData.getnClients() + 1);
                        System.out.println("["+serverPort+"] TCP connection with client was successful. ["+nTCPConnection+"] connections established.");
                        c.setAccepted(true);
                        clientTCPConnection cTC = new clientTCPConnection();
                        cTC.start();
                        System.out.println("["+serverPort+"]Informing other servers of new connection");
                        MulticastSender ms = new MulticastSender("dataUpdated");
                        ms.start();
                    }else{
                        //nunca ocorre, transferencias acontecem nas tentativas de UDP
                        System.out.println("["+serverPort+"] TCP connection with client was redirected to another server.");
                        c.setAccepted(false);

                    }

                    oOS.writeUnshared(c);
                    oOS.flush();

                }while(shutdown == false);

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("["+serverPort+"] A server was already runnning at this port.");
                e.printStackTrace();
            }
        }
    }

    class clientTCPConnection extends Thread{
        @Override
        public void run() {

            int n = nTCPConnection - 1;
            Socket s = connetionsArray.get(n).getSocket();
            System.out.println("["+serverPort+"] TCP connection with client [" + n +"] is being held by an independent thread.");

            try {

                do{
                    TCPCommunication c = (TCPCommunication)connetionsArray.get(n).getoIS().readObject();
                    switch (c.getRequest()){
                        case "requestOne":
                            System.out.println("["+serverPort+"] Request one was received.");
                            SendRequest sR1 = new SendRequest("ResponseToRequest1",c.getClientData().getClientN());
                            sR1.start();
                            break;

                        case "requestTwo":
                            System.out.println("["+serverPort+"] Request two was received.");
                            SendRequest sR2 = new SendRequest("ResponseToRequest2",c.getClientData().getClientN());
                            sR2.start();
                            break;

                        case "register":
                            boolean exists1 = checkIfClientExists(c.getClientData().getName());
                            System.out.println("["+serverPort+"] register was received name: "+c.getClientData().getName()+ " pw: "+c.getClientData().getPassword());
                            System.out.println("["+serverPort+"] exists: " + exists1);
                            if(exists1 == false){//register user
                                registerClient(c.getClientData());
                                SendRequest sR3 = new SendRequest("registerResponse",c.getClientData().getClientN(),0);
                                sR3.start();
                                //MulticastSender ms2 = new MulticastSender("clientRegistered",c.getClientData().getName(),c.getClientData().getPassword());
                                //ms2.start();
                                MulticastSender ms = new MulticastSender("dataUpdated");
                                ms.start();
                            }else{ //user already exists
                                SendRequest sR3 = new SendRequest("registerResponse",c.getClientData().getClientN(),1);
                                sR3.start();
                            }
                            break;

                        case "login":
                            boolean exists2 = checkIfClientExists(c.getClientData().getName());
                            System.out.println("["+serverPort+"] register was received name: "+c.getClientData().getName()+ " pw: "+c.getClientData().getPassword());
                            System.out.println("["+serverPort+"] exists: " + exists2);
                            if(exists2 == false){
                                SendRequest sR4 = new SendRequest("loginResponse",c.getClientData().getClientN(),1);
                                sR4.start();
                            }else{
                                boolean pwmatch = checkIfPasswordMatches(c.getClientData().getName(),c.getClientData().getPassword());
                                if(pwmatch == true){
                                    if(!checkIfUserIsLoggedIn(c.getClientData().getName())){
                                        logUser(c.getClientData());
                                        SendRequest sR4 = new SendRequest("loginResponse",c.getClientData().getClientN(),0);
                                        sR4.start();
                                        MulticastSender ms = new MulticastSender("dataUpdated");
                                        ms.start();
                                        if(RCI!=null)
                                            RCI.showResult("User ["+c.getClientData().getName()+"] was authenticated.");

                                    }else{
                                        SendRequest sR4 = new SendRequest("loginResponse",c.getClientData().getClientN(),3);
                                        sR4.start();
                                    }
                                    //addNameToClientArray();
                                    //MulticastSender ms1 = new MulticastSender("clientConnected",c.getClientData().getName());
                                    //ms1.start();
                                }else{
                                    SendRequest sR4 = new SendRequest("loginResponse",c.getClientData().getClientN(),2);
                                    sR4.start();
                                }

                            }
                            break;

                        case "userList":
                            System.out.println("["+serverPort+"] User list request was received.");
                            System.out.println("["+serverPort+"] sent list:" + getUserList());
                            SendRequest sR5 = new SendRequest("userListResponse",c.getClientData().getClientN(),getUserList());
                            sR5.start();
                            break;

                        case "channelList":
                            System.out.println("["+serverPort+"] channel list request was received.");
                            System.out.println("["+serverPort+"] sent list:" + getChannelList());
                            sR5 = new SendRequest("channelListResponse",c.getClientData().getClientN(),getChannelList());
                            sR5.start();
                            break;

                        case "dm":
                            System.out.println("["+serverPort+"] Dm request was received.");
                            boolean sent = false;
                            int value = 2; //default
                            if(checkIfClientExists(c.getClientData().getRecipient())){ //pus estes dois ifs para poder dar dois valores de resposta diferentes
                                value = 1; // user is not logged in
                                if(checkIfUserIsLoggedIn(c.getClientData().getRecipient())){
                                    for(int i = 0; i < serverData.getClientDataArray().size() ; i++){
                                        if(serverData.getClientDataArray().get(i).getName().equals(c.getClientData().getRecipient()) &&
                                                serverData.getClientDataArray().get(i).getServerPort() == serverPort){
                                            //rq sender receiver message n
                                            SendRequest sR = new SendRequest("dm",c.getsClientName(),c.getrClientName(),c.getMessage(),serverData.getClientDataArray().get(i).getClientN());
                                            //o campo password foi passado porque o message refere-se a request que não é o que se quer passar
                                            sR.start();
                                            System.out.println("["+serverPort+"] Checking if RMI was null. case DM");
                                            if(RCI!=null)
                                                RCI.showResult("["+serverPort+"] Message from ["+c.getsClientName()+"] to ["+c.getrClientName()+"]. Message:[+"+c.getMessage()+"].");
                                            sent = true;
                                            value = 0;
                                        }
                                    }
                                    if(sent == false){
                                        //System.out.println("receiveeeeer SR SENDER: "+ c.getClientData().getRecipient());
                                        System.out.println("["+serverPort+"] Checking if RMI was null. case DM Multicast");
                                        if(RCI!=null)
                                            RCI.showResult("["+serverPort+"] Message from ["+c.getClientData().getName()+"] to ["+c.getClientData().getRecipient()+"]. Message:["+c.getClientData().getMessage()+"].");
                                        MulticastSender ms = new MulticastSender("dm",c.getClientData().getName(),c.getClientData().getRecipient(),c.getClientData().getMessage());
                                        ms.start();
                                        value = 0; //message sent to user
                                    }
                                }
                            }
                            SendRequest sR6 = new SendRequest("dmResponse",c.getClientData().getClientN(),value);
                            sR6.start();
                            break;

                        case "createChannel":
                            value = 2; // too many channels
                            System.out.println("["+serverPort+"] Create channel request was received.");
                            if(serverData.getChannels().size() == 3){

                            }else if(checkIfChannelExists(c.getChannelToCreateName())){
                                value = 1; // channel name taken
                            }else{
                                value = 0; // channel created
                                createChannel(c.getChannelToCreateName(),c.getClientData().getName());
                                MulticastSender ms = new MulticastSender("dataUpdated");
                                ms.start();
                            }
                            SendRequest sR7 = new SendRequest("createChannelResponse",c.getClientData().getClientN(),value);
                            sR7.start();
                            break;

                        case "enterChannel":
                            value = 2; // channel does not exist
                            System.out.println("["+serverPort+"] Enter channel request was received.");
                            if(checkIfChannelExists(c.getClientData().getChannelName())){
                                value = 1; // user is already in a channel
                                if(!checkIfUserIsInChatRoom(c.getClientData().getName())){
                                    userEnterChannel(c.getClientData().getName(),c.getChannelToEnterName());
                                    value = 0; // entered the channel
                                    MulticastSender ms = new MulticastSender("dataUpdated");
                                    ms.start();
                                }
                            }
                            SendRequest sR8 = new SendRequest("enterChannelResponse",c.getClientData().getClientN(),value);
                            sR8.start();
                            break;

                        case "deleteChannel":
                            value = 2; // channel does not exist
                            System.out.println("["+serverPort+"] Delete channel request was received.");
                            if(checkIfChannelExists(c.getChannelToCreateName())){
                                value = 1;// user does not own channel
                                if(checkIfUserOwnsChannel(c.getClientData().getName(),c.getChannelToCreateName())){
                                    value = 0; // channel deleted
                                    deleteChannel(c.getChannelToCreateName());
                                    MulticastSender ms = new MulticastSender("dataUpdated");
                                    ms.start();
                                }
                            }
                            SendRequest sR9 = new SendRequest("deleteChannelResponse",c.getClientData().getClientN(),value);
                            sR9.start();
                            break;

                        case "speak":
                            System.out.println("["+serverPort+"] Speak in channel request was received.");
                            value = 1; //user cannot speak out of a channel
                            if(checkIfUserIsInChatRoom(c.getClientData().getName())){
                                value = 0;
                                System.out.println("["+serverPort+"] Checking if RMI was null. case speak");
                                if(RCI!=null)
                                    RCI.showResult("["+serverPort+"] Message from ["+c.getClientData().getName()+"] to channel["+c.getClientData().getChannelName()+"]. Message:[+"+c.getClientData().getMessage()+"].");
                                for(int i = 0; i < serverData.getClientDataArray().size() ; i ++){
                                    if(serverData.getClientDataArray().get(i).getChannelName().equals(c.getClientData().getChannelName()) && serverPort == serverData.getClientDataArray().get(i).getServerPort()){
                                        incChannelMessages(serverData.getClientDataArray().get(i).getName());
                                        if(!serverData.getClientDataArray().get(i).getName().equals(c.getClientData().getName())){
                                            SendRequest sR10 = new SendRequest("dm",c.getClientData().getName(),"none",c.getClientData().getMessage(),serverData.getClientDataArray().get(i).getClientN());
                                            sR10.start();
                                        }

                                    }
                                }
                            }
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            SendRequest sR11 = new SendRequest("speakResponse",c.getClientData().getClientN(),value);
                            sR11.start();
                            MulticastSender ms1 = new MulticastSender("dataUpdated");
                            ms1.start();
                            MulticastSender ms2 = new MulticastSender("spoke",c.getClientData().getName(),c.getClientData().getChannelName(),c.getClientData().getMessage());
                            //request sender channel message
                            ms2.start();
                            break;

                        case "leaveChannel":
                            value = 2; // channel does not exist
                            System.out.println("["+serverPort+"] Enter channel request was received.");
                            if(checkIfChannelExists(c.getClientData().getChannelName())){
                                value = 1; // user is not in a channel
                                if(checkIfUserIsInChatRoom(c.getClientData().getName())){
                                    userLeaveChannel(c.getClientData().getName(),c.getChannelToEnterName());
                                    value = 0; // user left the channel
                                    MulticastSender ms = new MulticastSender("dataUpdated");
                                    ms.start();
                                }
                            }
                            SendRequest sR10 = new SendRequest("leaveChannelResponse",c.getClientData().getClientN(),value);
                            sR10.start();
                            break;

                        case "changeChannel":
                            value = 2; // channel does not exist
                            System.out.println("["+serverPort+"] Change channel request was received.");
                            if(checkIfChannelExists(c.getChannelToCreateName())){
                                value = 1;// user does not own channel
                                if(checkIfUserOwnsChannel(c.getClientData().getName(),c.getChannelToCreateName())){
                                    value = 0; // channel changed
                                    userChangeChannel(c.getChannelToCreateName(),c.getClientData().getMessage());
                                    MulticastSender ms = new MulticastSender("dataUpdated");
                                    ms.start();
                                }
                            }
                            SendRequest sR13 = new SendRequest("changeChannelResponse",c.getClientData().getClientN(),value);
                            sR13.start();
                            break;
                    }

                }while(shutdown == false);

            } catch (IOException | ClassNotFoundException e) {

                System.out.println("["+serverPort+"] IO Exception within TCP connection " + n + ".");
                e.printStackTrace();

            }

        }
    }



    class SendRequest extends Thread{
        private String request;
        private int clientNumber = 0, value = -1;
        //pus o default clienteNumber a 0 para ter a certesa que ao usar o 3 construtor (pela primeira vez), o run nao vai dar multicast [se clienteNumber = -1]
        String str,sender,receiver,message;
        public SendRequest(String request,int n) {
            this.request = request;
            this.clientNumber = n;
        }

        public SendRequest(String request,int n,int v) {
            this.request = request;
            this.clientNumber = n;
            value = v;
        }

        public SendRequest(String request,int n,String s) {
            this.request = request;
            this.clientNumber = n;
            str = s;
        }

        public SendRequest(String rq, String s,String r,String message) {
            request = rq;
            sender = s;
            receiver = r;
            message = message;

        }

        public SendRequest(String rq, String s,String r,String m,int n) {
            request = rq;
            sender = s;
            receiver = r;
            message = m;
            clientNumber = n;
            /*
            System.out.println("request was rightfully constructed");
            */

        }

        @Override
        public void run() {

            try {
                if(clientNumber == RMI_CNUMBER)
                {
                    for(ServerTCPData server : connetionsArray)
                    {
                        TCPCommunication c = new TCPCommunication(request,false, new ClientData("ds",str), serverData);
                        c.setrClientName(receiver);
                        c.setsClientName(sender);
                        c.setMessage(message);
                        server.getoOS().writeUnshared(c);
                        server.getoOS().flush();
                        System.out.println("["+serverPort+"] RMI_DM:");
                        System.out.println("["+serverPort+"] to: " + receiver + " from: " + sender);
                        System.out.println("["+serverPort+"] message:" + message);
                        return;
                    }
                }
                if(serverData.getClientDataArray().get(clientNumber).isLoggedFromBrowser() == true){
                    return;
                }
                if(clientNumber == -1){
                    for(int i = 0 ; i < connetionsArray.size() ; i++){
                        TCPCommunication c = new TCPCommunication(request,false, new ClientData(),new ServerData(serverData));
                        connetionsArray.get(i).getoOS().writeObject(c); /*writeUnshared por causa da cache e a referência ser a mesma ou fazer oOS.reset()*/
                        connetionsArray.get(i).getoOS().flush();
                        System.out.println("["+serverPort+"] request: " + c.getRequest() + " was written with value: " + c.getValue());
                    }
                }else{
                    TCPCommunication c = new TCPCommunication(request,false, new ClientData("ds",str),new ServerData(serverData));
                    c.setrClientName(receiver);
                    c.setsClientName(sender);
                    c.setMessage(message);
                    if(value != -1){
                        c.setValue(value);
                    }
                    connetionsArray.get(clientNumber).getoOS().writeObject(c); /*writeUnshared por causa da cache e a referência ser a mesma ou fazer oOS.reset()*/
                    connetionsArray.get(clientNumber).getoOS().flush();
                    System.out.println("["+serverPort+"] request: " + c.getRequest() + " was written with value: " + c.getValue());

                   /* if(c.getRequest().equals("dm")){
                        System.out.println("to: " + sender + " from: " +receiver);
                        System.out.println("message:" + message);
                        System.out.println("to2: " + c.getrClientName() + " from2: " + c.getsClientName());
                        System.out.println("message2:" + c.getMessage());
                    }*/

                }

            } catch (IOException e) {
                System.out.println("["+serverPort+"] IOException while sending request in SendRequest thread.");
                e.printStackTrace();
            }

        }

    }

    class MulticastReceiver extends Thread{
        @Override
        public void run() {
            System.out.println("["+serverPort+"]Server is listening to multicast requests.");

            try {

                InetAddress group = InetAddress.getByName("225.4.5.6");
                MulticastSocket mS = new MulticastSocket(9007);
                mS.joinGroup(group);
                byte [] buf = new byte[3072];
                DatagramPacket dP = new DatagramPacket(buf,buf.length);

                do{
                    mS.receive(dP);
                    byte[] bufDP = dP.getData();
                    ByteArrayInputStream bAIS = new ByteArrayInputStream(bufDP);
                    ObjectInputStream oIS = new ObjectInputStream(bAIS);
                    MulticastCommunication mC = (MulticastCommunication)oIS.readObject();
                    if(mC.getSendingServer() != serverNumber) {
                        System.out.println("["+serverPort+"]Multicast request from server [" + mC.getServerdata().getServerPort() + "]: [" + mC.getMessage() + "]");

                        switch (mC.getMessage()) {
                            case "serverConnect":
                                boolean alreadyConnected = false;
                                for (int i = 0; i < otherServers.size(); i++) {
                                    if (otherServers.get(i).getServerNumber() == mC.getServerdata().getServerNumber()) {
                                        alreadyConnected = true;
                                    }
                                }

                                if (alreadyConnected == false) {
                                    otherServers.add(mC.getServerdata());
                                    System.out.println("["+serverPort+"] The server connected to the system.");
                                    MulticastSender ms = new MulticastSender("dataUpdated");
                                    ms.start();

                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    MulticastSender mSS = new MulticastSender("serverConnect");
                                    mSS.start();

                                }
                                break;

                            case "serverDisconnect":
                                for (int i = 0; i < otherServers.size(); i++) {
                                    if (otherServers.get(i).getServerNumber() == mC.getServerdata().getServerNumber()) {
                                        otherServers.remove(i);
                                        System.out.println("[\"+serverPort+\"] The server disconnected from the system.");
                                    }
                                }
                                MulticastSender mSS = new MulticastSender("oke");
                                mSS.start();
                                break;

                            case "dataUpdated": // refering to user updates
                                serverData.setClientDataArray(mC.getServerdata().getClientDataArray());
                                serverData.setChannels(mC.getServerdata().getChannels());
                                for (int i = 0; i < otherServers.size(); i++) {
                                    if (otherServers.get(i).getServerNumber() == mC.getServerdata().getServerNumber()) {
                                        otherServers.remove(i);
                                        otherServers.add(mC.getServerdata());
                                        System.out.println("["+serverPort+"]The server received a data update.");
                                    }
                                }
                                break;

                            case "dm":
                                for(int i = 0; i < serverData.getClientDataArray().size(); i++){
                                    /*
                                    System.out.println("name1: " + serverData.getClientDataArray().get(i).getName() + " name2: " + mC.getrClientName());
                                    System.out.println("port1: " + serverData.getClientDataArray().get(i).getServerPort() + " port2: " + serverPort);*/
                                    if(serverData.getClientDataArray().get(i).getName().equals(mC.getrClientName()) && serverData.getClientDataArray().get(i).getServerPort() == serverPort){
                                        SendRequest sR = new SendRequest("dm", mC.getsClientName(), mC.getrClientName(), mC.getDmText(), serverData.getClientDataArray().get(i).getClientN());
                                        if(RCI!=null)
                                            RCI.showResult("["+serverPort+"]: Message Received. Sender: ["+mC.getsClientName()+"] Receiver: ["+mC.getrClientName()+"] Message:["+mC.getMessage()+"]");
                                        //rq sender receiver message n
                                        //o campo password foi passado porque o message refere-se a request que não é o que se quer passar
                                        sR.start();
                                    }
                                }
                                break;

                            case "spoke":
                                for(int i = 0; i < serverData.getClientDataArray().size() ; i ++){
                                    if(serverData.getClientDataArray().get(i).getServerPort() == serverPort && serverData.getClientDataArray().get(i).getChannelName().equals(mC.getChannel())){
                                        if(!mC.getsClientName().equals(serverData.getClientDataArray().get(i).getName())){
                                            //podemos por aqui uma notificacao tambem para dizer que chegou uma mensagem do chX para cliente Y
                                            SendRequest sR10 = new SendRequest("dm",mC.getsClientName(),"none", mC.getDmText(),serverData.getClientDataArray().get(i).getClientN());
                                            sR10.start();
                                        }
                                    }
                                }
                                break;

                                /*
                            case "clientRegistered":
                                for (int i = 0; i < otherServers.size(); i++) {
                                    if (otherServers.get(i).getServerNumber() == mC.getServerdata().getServerNumber()) {
                                        otherServers.get(i).getClientDataArray().add(new ClientData(mC.getrClientName(),mC.getDmText()));
                                    }
                                }
                                break;*/

                        }
                        //System.out.println(mC.getMessage());
                    }

                }while(threadEnd != true);

                mS.close();

            } catch (IOException | ClassNotFoundException e) {

                System.out.println("["+serverPort+"] IO or ClassNotFound Exception within multicast receiver thread.");
                e.printStackTrace();

            }

        }
    }

    class MulticastSender extends Thread{
        private String message;
        private String client;
        private String pass;
        private String sender;
        private String receiver;
        private String chat;
        private String channel;
        public MulticastSender(String message) {
            this.message = message;
            client = "unused";
            pass = "unused";
        }

        public MulticastSender(String message, String c) {
            this.message = message;
            client = c;
        }

        public MulticastSender(String message, String c,String pw) {
            this.message = message;
            client = c;
            pass = pw;

        }

        public MulticastSender(String message, String s,String r,String c) {
            this.message = message;
            sender = s;
            receiver = r;
            chat = c;
            if(message.equals("spoke")){
                //request sender channel message
                sender = s;
                channel = r;
            }
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }



        @Override
        public void run() {

            try {

                InetAddress group = InetAddress.getByName("225.4.5.6");
                MulticastSocket mS = new MulticastSocket();
                ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
                ObjectOutputStream oOS = new ObjectOutputStream(bAOS);
                //serverData.setClientDataArray(clientDataArray);
                MulticastCommunication m = new MulticastCommunication(serverNumber,message,serverData);
                m.setrClientName(client);
                m.setDmText(pass);
                m.setsClientName(sender);
                m.setrClientName(receiver);
                m.setDmText(chat);
                m.setChannel(channel);
                //System.out.println("receiveeeeer MS SENDER: "+ receiver);
                oOS.writeUnshared(m);
                byte [] bufOut = bAOS.toByteArray();
                DatagramPacket dP = new DatagramPacket(bufOut,bufOut.length,group,9007);
                mS.send(dP);
                System.out.println("["+serverPort+"] [Multicast request sent] : ["+message+"]");
                mS.close();


            } catch (IOException e) {
                System.out.println("["+serverPort+"] IO Exception within multicast sender thread.");
                e.printStackTrace();

            }

        }
    }

    static class RMI_Handler{

        public static void Register(int serverPort, ServerRemoteObject SRO){
            try
            {
                Registry registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT+serverPort);
                System.out.println("["+serverPort+"] Registry in port ["+(Registry.REGISTRY_PORT+serverPort)+"]");
                String bindName = "Server"+serverPort;
                System.out.println("["+serverPort+"] Bindname: ["+bindName+"]");
                registry.rebind(bindName,SRO);
            } catch (RemoteException e)
            {
                e.printStackTrace();
            }
        }
    }

    class ServerRemoteObject extends UnicastRemoteObject implements RemoteServerInterface{
        public ServerRemoteObject() throws RemoteException
        {

        }

        @Override
        public void registaCliente(ClientData ClientData, RemoteClientInterface RCI) throws IOException {
            boolean exists = checkIfClientExists(ClientData.getName());
            if(exists){
                RCI.showResult("Client With Name ["+ClientData.getName()+"] Already Exists.");
                return;
            }
            ClientData.setServerPort(serverPort);
            registerClient(ClientData);
            RCI.showResult("Client ["+ClientData.getName()+"] registered successfully.");

            //update databases
            MulticastSender ms = new MulticastSender("dataUpdated");
            ms.start();
        }

        @Override
        public void sendMensagemToServer(ClientData ClientData, RemoteClientInterface RCI) throws IOException
        {
            for(ClientData cliente : serverData.getClientDataArray())
            {
                SendRequest sr;
                if(cliente.isLoggedIn() && cliente.getServerPort() == serverData.getServerPort()){
                    System.out.println("["+serverPort+"] RMI_CLIENT sending to: ["+cliente.getName()+"] with message: ["+ClientData.getMessage()+"]");
                    sr = new SendRequest("dm","RMIClient", cliente.getName(),ClientData.getMessage(),RMI_CNUMBER);
                    System.out.println("["+serverPort+"] Sending Request");
                    sr.start();
                }
            }
            RCI.showResult("Mensagem entrege aos clientes");
        }

        @Override
        public void registaObserver(RemoteClientInterface RCI) throws IOException
        {
            Server.RCI = RCI;
            System.out.println("["+serverPort+"] [Server is being observed by Remote Client.]");
        }

        @Override
        public void unregisterObserver() throws IOException {
            Server.RCI = null;
            System.out.println("["+serverPort+"] [Server is not being observed by Remote Client anymore.]");
        }

    }

    @Override
    public void run(){
        HandleFirstConnection hFC = new HandleFirstConnection();
        EstablishTCPConnection eTC = new EstablishTCPConnection();
        MulticastReceiver mR = new MulticastReceiver();
        chooseAvailablePort();
        System.out.println("["+serverPort+"] Server [" + serverNumber +"] is running.");

        //ConnectToRemoteRegistry ;
        ServerRemoteObject SRO = null;
        try {
            SRO = new ServerRemoteObject();
        } catch (RemoteException e) {
            e.printStackTrace();
            System.out.println("["+serverPort+"] Error in server run() server remote object.");
        }
        RMI_Handler.Register(this.serverPort,SRO);

        System.out.print("Server ["+serverNumber+"] connected to remote registry.");

        dbHandler.createDatabase("Server"+serverNumber);
        dbHandler.loadUsers("server"+serverNumber, this.serverData.getClientDataArray());
        dbHandler.loadChannel("server"+serverNumber, serverData.getChannels());

        hFC.start();
        eTC.start();
        mR.start();
        connectToOtherServers();

        messages.add("Mensagem1;");
        messages.add("Mensagem2;");
        messages.add("Mensagem3;");



        Scanner sc = new Scanner(System.in);
        String op = "buHU";
        do{
            System.out.println(("["+this.serverPort+"]") +"insert command:");
            op = sc.nextLine();
            switch (op){
                case "exit":
                    System.out.println("Server is shutting down.");
                    threadEnd = true;
                    MulticastSender mS = new MulticastSender("serverDisconnect");
                    mS.start();
                    SendRequest sR = new SendRequest("serverShutdown ",0);
                    sR.start();
                    break;

                case "listClients":

                    for(int i = 0; i < serverData.getClientDataArray().size() ; i++){
                        System.out.println("ClientN: " + serverData.getClientDataArray().get(i).getClientN() + " name: "+ serverData.getClientDataArray().get(i).getName() + " pw " + serverData.getClientDataArray().get(i).getPassword());
                    }

                    break;

                case "listOnlineClients":

                    System.out.println("Online Clients:");
                    for(int i = 0; i < serverData.getClientDataArray().size() ; i++){
                        if(serverData.getClientDataArray().get(i).isLoggedIn()){
                            System.out.println("ClientN: " + serverData.getClientDataArray().get(i).getClientN() + " name: "+ serverData.getClientDataArray().get(i).getName() + " pw " + serverData.getClientDataArray().get(i).getPassword());
                            System.out.println("Port: " + serverData.getClientDataArray().get(i).getServerPort() + " svip: "+ serverData.getClientDataArray().get(i).getServerIp());
                        }
                    }
                    break;

                case "listChannels":
                    System.out.println("Available channels: ");
                    for(int i = 0; i < serverData.getChannels().size() ; i++){
                        System.out.println("cName: " + serverData.getChannels().get(i).getName());
                        System.out.println("Creator: "+ serverData.getChannels().get(i).getOwner());

                        System.out.println("Connected Clients: ");

                        for(int r = 0; r < serverData.getClientDataArray().size(); r++){
                            if(serverData.getClientDataArray().get(r).isInChannel()){
                                if(serverData.getClientDataArray().get(r).getChannelName().equals(serverData.getChannels().get(i).getName())){
                                    System.out.println("Client: "+ serverData.getClientDataArray().get(r).getName());
                                }
                            }
                        }
                    }
                    break;

            }

        }while(!op.equals("exit"));
        shutdown = true;

    }


    public void chooseAvailablePort(){
        boolean done = false;

        do{

            try {

                dS = new DatagramSocket(serverPort);
                done = true;

            } catch (SocketException e) {
                serverPort++;
                serverNumber++;
                serverData.setServerPort(serverPort);
                serverData.setServerNumber(serverNumber);
            }

        }while(done != true);


    }

    public void connectToOtherServers(){
        if(serverNumber != 0){
            MulticastSender mS = new MulticastSender("serverConnect");
            mS.start();
        }
    }


    public boolean checkIfClientExists(String name){

        if(!serverData.getClientDataArray().isEmpty()){
            for(int i = 0 ; i < serverData.getClientDataArray().size() ; i++){
                if(serverData.getClientDataArray().get(i).getName().equals(name)){
                    return true;
                }
            }
        }

        if(!otherServers.isEmpty()){
            for(int i = 0 ; i < otherServers.size() ; i++){
                for(int j = 0 ; j < otherServers.get(i).getClientDataArray().size() ; j++){
                    String n = name;
                    String n2 = otherServers.get(i).getClientDataArray().get(j).getName();
                    if( otherServers.get(i).getClientDataArray().get(j).getName().equals(name)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean checkIfPasswordMatches(String n, String pw){

        if(serverData.getClientDataArray().isEmpty()){
            return false;
        }else{
            for(int i = 0 ; i < serverData.getClientDataArray().size() ; i++){
                if(serverData.getClientDataArray().get(i).getName().equals(n)){
                    if(serverData.getClientDataArray().get(i).getPassword().equals(pw)){
                        return true;
                    }
                }
            }
            /*
            for(int i = 0 ; i < otherServers.size() ; i++){
                for(int j = 0 ; j < otherServers.get(i).getClientDataArray().size() ; j++){
                    if( otherServers.get(i).getClientDataArray().get(j).getName().equals(n)){
                        if(otherServers.get(i).getClientDataArray().get(j).getPassword().equals(pw)){
                            return true;
                        }
                    }
                }
            }*/
        }
        return false;
    }

    public void registerClient(ClientData x){
        dbHandler.addUser("server"+ serverNumber, x.getName(), x.getPassword());
        serverData.getClientDataArray().add(x);
    }

    public String getUserList(){
        String temp ="";
        if(serverData.getClientDataArray().size() == 0){
            temp ="There are no online users.";
        }
        for(int i = 0 ; i < serverData.getClientDataArray().size() ; i++){
            if(serverData.getClientDataArray().get(i).isLoggedIn()){
                temp += serverData.getClientDataArray().get(i).getClientN() + serverData.getClientDataArray().get(i).getName();
                temp += "\n";
            }
        }

        return temp;
    }

    public String getChannelList(){
        String temp ="";
        for(int i = 0 ; i < serverData.getChannels().size() ; i++){
            temp += serverData.getChannels().get(i).getName();
            temp += "\n";
        }

        return temp;
    }

    public boolean checkIfUserIsLoggedIn(String name){
        if(!serverData.getClientDataArray().isEmpty()){

            for(int i = 0 ; i < serverData.getClientDataArray().size() ; i++){

                if(serverData.getClientDataArray().get(i).getName().equals(name) && serverData.getClientDataArray().get(i).isLoggedIn()){
                    return true;
                }
            }
        }
        return false;
    }

    public void logUser(ClientData x){

        for(int i = 0 ; i < serverData.getClientDataArray().size() ; i++){

            if(serverData.getClientDataArray().get(i).getName().equals(x.getName())){
                serverData.getClientDataArray().get(i).setServerPort(serverPort);
                serverData.getClientDataArray().get(i).setLoggedIn(true);
                serverData.getClientDataArray().get(i).setClientN(x.getClientN());
                serverData.getClientDataArray().get(i).setToken("loggedViaClient");
                serverData.getClientDataArray().get(i).setLoggedFromBrowser(false);
            }
        }

    }

    public void logUser(String name, String password,String token){

        for(int i = 0 ; i < serverData.getClientDataArray().size() ; i++){
            if(serverData.getClientDataArray().get(i).getName().equals(name)){

                serverData.getClientDataArray().get(i).setServerPort(serverPort);
                int temp = this.getNUsersOnline();
                serverData.getClientDataArray().get(i).setClientN(temp);
                serverData.getClientDataArray().get(i).setLoggedIn(true);
                serverData.getClientDataArray().get(i).setToken(token);
                serverData.getClientDataArray().get(i).setLoggedFromBrowser(true);
                System.out.println("["+serverPort+"]User: " + name + " was logged on server with port: " + token);

                MulticastSender ms = new MulticastSender("dataUpdated");
                ms.start();

                if(RCI!=null) {
                    try {
                        RCI.showResult("User ["+ name +"] was authenticated.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public int getNUsersOnline(){
        int count = 0;

        for(int i = 0 ; i < serverData.getClientDataArray().size() ; i++){

            if(serverData.getClientDataArray().get(i).isLoggedIn() == true){
                count++;
            }
        }
        return count;
    }

    public boolean checkIfChannelExists(String name){
        for(int i = 0 ; i < serverData.getChannels().size() ; i++){
            System.out.println("["+serverPort+"] name1: " + serverData.getChannels().get(i).getName() + " name2: " +name);
            if(serverData.getChannels().get(i).getName().equals(name)){
                return true;
            }
        }
        return false;
    }

    private void createChannel(String cName, String oName){
        dbHandler.addChannel("server"+serverNumber,cName,oName);
        serverData.getChannels().add(new Channel(cName,oName));
    }

    private boolean checkIfUserIsInChatRoom(String n){
        for(int i = 0 ; i < serverData.getClientDataArray().size(); i++){
            System.out.println("["+serverPort+"] name 1: " + n);
            if(serverData.getClientDataArray().get(i).getName().equals(n) && serverData.getClientDataArray().get(i).isInChannel()){
                return true;
            }
        }
        return false;
    }

    private void userEnterChannel(String n,String cn){
        for(int i = 0 ; i < serverData.getClientDataArray().size(); i++){
            if(serverData.getClientDataArray().get(i).getName().equals(n)){
                serverData.getClientDataArray().get(i).setChannelName(cn);
                serverData.getClientDataArray().get(i).setInChannel(true);
            }
        }
        for(int i = 0 ; i < serverData.getChannels().size(); i++){
            if(serverData.getChannels().get(i).getName().equals(cn)){
                serverData.getChannels().get(i).setnClients(serverData.getChannels().get(i).getnClients() + 1);
            }
        }
    }

    private boolean checkIfUserOwnsChannel(String n, String cn){
        for(int i = 0 ; i < serverData.getChannels().size() ; i++){
            if(serverData.getChannels().get(i).getName().equals(cn) && serverData.getChannels().get(i).getOwner().equals(n)){
                return true;
            }
        }
        return false;
    }

    private void deleteChannel(String cn){
        for(int i = 0 ; i < serverData.getChannels().size() ; i++){
            if(serverData.getChannels().get(i).getName().equals(cn)){
                for(int j = 0 ; j < serverData.getClientDataArray().size(); j++){
                    if(serverData.getClientDataArray().get(j).getChannelName().equals(cn)){
                        serverData.getClientDataArray().get(j).setInChannel(false);
                        serverData.getClientDataArray().get(j).setChannelName("none");
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        SendRequest sR = new SendRequest("channelYouWereInWasDeleted",serverData.getClientDataArray().get(j).getClientN());
                        sR.start();
                    }
                }
                serverData.getChannels().remove(i);
                atualizaChannelsDB();
            }
        }
    }

    private void atualizaChannelsDB() {
        dbHandler.removeChannelTable("server" + serverNumber);
        Vector<Channel> c = serverData.getChannels();
        for (int i = 0; i < c.size(); i++) {
            dbHandler.addChannel("server" + serverNumber, c.get(i).getName(), c.get(i).getOwner());
        }
    }

    private void userLeaveChannel(String n,String cn){
        for(int i = 0 ; i < serverData.getClientDataArray().size(); i++){
            if(serverData.getClientDataArray().get(i).getName().equals(n)){
                serverData.getClientDataArray().get(i).setChannelName("none");
                serverData.getClientDataArray().get(i).setInChannel(false);
            }
        }
        for(int i = 0 ; i < serverData.getChannels().size(); i++){
            if(serverData.getChannels().get(i).getName().equals(cn)){
                serverData.getChannels().get(i).setnClients(serverData.getChannels().get(i).getnClients() - 1);
            }
        }
    }

    private void userChangeChannel(String cn,String nn){
        for(int i = 0 ; i < serverData.getChannels().size(); i++){
            if(serverData.getChannels().get(i).getName().equals(cn)){
                serverData.getChannels().get(i).setName(nn);
            }
        }
    }

    public void incChannelMessages(String cn){
        for(int i = 0 ; i < serverData.getChannels().size() ; i++){
            if(serverData.getChannels().get(i).getName().equals(cn)){
                serverData.getChannels().get(i).setnMessages(serverData.getChannels().get(i).getnMessages() + 1);
            }
        }
    }

    public String getApiTest(){
        String temp = "There are no clients online.";
        if(serverData.getClientDataArray().size() == 0){
            return temp;
        }else{
            temp = "";
            for(int i = 0; i < serverData.getClientDataArray().size() ; i++){

                temp +="ClientN: " + serverData.getClientDataArray().get(i).getClientN() + " name: "+ serverData.getClientDataArray().get(i).getName() + " pw " + serverData.getClientDataArray().get(i).getPassword() + "\n";
                temp +="Port: " + serverData.getClientDataArray().get(i).getServerPort() + " svip: "+ serverData.getClientDataArray().get(i).getServerIp() + "\n";

            }
            return temp;
        }
    }

    public String getLastMessages()
    {
        String retString = new String();

        for(String string : messages){
            retString += string+("\n");
        }
        return retString;

    }

    public void listCLients(){
        for(int i = 0; i < serverData.getClientDataArray().size() ; i++){
            System.out.println("["+serverPort+"] ClientN: " + serverData.getClientDataArray().get(i).getClientN() + " name: "+ serverData.getClientDataArray().get(i).getName() + " pw " + serverData.getClientDataArray().get(i).getPassword());
        }
    }

    public String getClientNames(){
        String temp = "";
        for(int i = 0; i < serverData.getClientDataArray().size() ; i++){
            temp = " name: "+ serverData.getClientDataArray().get(i).getName();
        }
        return temp;
    }

    public void messageToServerUsers(String fromUser,String message) throws IOException {
        for(int i = 0 ; i < serverData.getClientDataArray().size() ; i++){
            if(serverData.getClientDataArray().get(i).getServerPort() == serverPort){
                SendRequest sR = new SendRequest("dm",fromUser,serverData.getClientDataArray().get(i).getName(),message,serverData.getClientDataArray().get(i).getClientN());
                //o campo password foi passado porque o message refere-se a request que não é o que se quer passar
                sR.start();
                if(RCI!=null)
                    RCI.showResult("["+serverPort+"]: Message Received. Sender: ["+fromUser+"] Receiver: ["+serverData.getClientDataArray().get(i).getName()+"] Message:["+message+"]");
            }
        }
    }

    public String getUserToken(String name){

        for(int i = 0 ; i < serverData.getClientDataArray().size() ; i++){
            if(serverData.getClientDataArray().get(i).getName().equals(name)){
                return serverData.getClientDataArray().get(i).getToken();
            }
        }

        return "invalid token";
    }

    public String generateLogInToken(String name,String pw){
        String token = "";
        int nameHash = name.hashCode(),pwHash = pw.hashCode();
        Calendar c = Calendar.getInstance();
        token = Integer.toString(nameHash) + Integer.toString(pwHash) + c.get(Calendar.YEAR)
                + c.get(Calendar.MONTH) + c.get(Calendar.DAY_OF_MONTH) + c.get(Calendar.HOUR_OF_DAY)
                + c.get(Calendar.MINUTE) + c.get(Calendar.SECOND) + c.get(Calendar.MILLISECOND);
        return token;
    }

    public static void main(String[] args) throws SQLException, RemoteException {
        Server s = new Server(9008);
        s.run();
    }

}
