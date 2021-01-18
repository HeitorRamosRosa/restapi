package pt.isec.tppd.restapi.communicationLogic;

import pt.isec.tppd.restapi.businessLogic.ClientData;
import pt.isec.tppd.restapi.businessLogic.ServerData;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client extends Thread{
    private String serverIp;
    private int serverPort;
    private static int clientN = 0;
    private int clientNumber;
    private int threadNumber;
    boolean shutdown;
    boolean UICanRun = false;
    ClientData clientData;
    ServerData serverData;
    Socket socket;
    ObjectOutputStream oOSTCP;
    ObjectInputStream oISTCP;
    private boolean respondedToRegister = false;
    private int respondedToRegisterValue = -1;
    private boolean respondedToLogin = false;
    private int respondedToLoginValue = -1;
    private boolean respondedToUserList = false;
    private String userListStr= "";
    private boolean respondedToChannelList = false;
    private String ChannelStr= "";
    private boolean respondedToDm = false;
    private int respondedToDmValue = -1;
    private boolean respondedToCreateChannel = false;
    private int respondedToCreateChannelValue = -1;
    private boolean respondedToEnterChannel = false;
    private int respondedToEnterChannelValue = -1;
    private boolean respondedToDeleteChannel = false;
    private int respondedToDeleteChannelValue = -1;
    private boolean respondedToSpeak = false;
    private int respondedToSpeakValue = -1;
    private boolean respondedToLeaveChannel = false;
    private int respondedToLeaveChannelValue = -1;
    private boolean respondedToChangeChannel = false;
    private int respondedToChangeChannelValue = -1;



    public Client(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        threadNumber = 0;
        clientData = new ClientData();
        clientData.setName("notloggedin");
    }

    public String getServerIp() {
        return serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public static int getClientN() {
        return clientN;
    }

    public String getClientName() {
        return clientData.getName();
    }

    public int getClientNumber() {
        return clientNumber;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public static void setClientN(int clientN) {
        Client.clientN = clientN;
    }

    public void setClientName(String n) {clientData.setName(n);}

    public void setClientNumber(int clientNumber) {
        this.clientNumber = clientNumber;
    }

    public boolean isUICanRun() {
        return UICanRun;
    }

    class HandleFirstConnection extends Thread {
        @Override
        public void run() {

            try {
                System.out.println("Thread " + threadNumber + " is running.");
                threadNumber++;

                DatagramSocket dS = new DatagramSocket();
                dS.setSoTimeout(10000);
                ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
                ObjectOutputStream oOS = new ObjectOutputStream(bAOS);
                UDPCommunication c = new UDPCommunication("firstConnect",false, new ClientData("Maria","Amelia","Dinis","localhost",9008));
                c.setRequest("firstConnect");
                c.setAccepted(false);
                oOS.writeObject(c);

                byte[] bufDPOut = bAOS.toByteArray();

                InetAddress ip = InetAddress.getByName(serverIp);
                DatagramPacket dP = new DatagramPacket(bufDPOut, bufDPOut.length, ip, serverPort);
                dS.send(dP);
                System.out.println("UDPCommunication sent with resquest: " + c.getRequest());
                dP = new DatagramPacket(new byte[1024], 1024);
                dS.receive(dP);

                byte[] bufDP = dP.getData();
                ByteArrayInputStream bAIS = new ByteArrayInputStream(bufDP);
                ObjectInputStream oIS = new ObjectInputStream(bAIS);
                c = (UDPCommunication) oIS.readObject();

                if (c.isAccepted() == true) {
                    clientData = c.getClient();
                    serverPort = clientData.getServerPort();
                    System.out.println("You've been accepted to: ["+serverPort+"]");
                    EstablishTCPConnection eTC = new EstablishTCPConnection();
                    eTC.run();
                } else {
                    switch(c.getRequest()){
                        case "noAvailableServers":
                            System.out.println("There are no available servers for you to connect.");
                            break;
                        case "redirected":
                            serverPort = c.getClient().getServerPort();
                            System.out.println("Redirected to:[" + serverPort + "]");
                            HandleFirstConnection hFC = new HandleFirstConnection();
                            hFC.start();
                            break;
                    }
                }

                dS.close();
            } catch (IOException | ClassNotFoundException e) {

                System.out.println("Client timedout.");
                serverPort++;
                if(serverPort >= 9015){
                    serverPort = 9008;
                }


            }
        }
    }


    class EstablishTCPConnection extends Thread {
        @Override
        public void run() {

            try {

                socket = new Socket(serverIp,serverPort);
                oOSTCP = new ObjectOutputStream(socket.getOutputStream());
                oISTCP = new ObjectInputStream(socket.getInputStream());
                TCPCommunication c = new TCPCommunication("firstTCPConnection",false, clientData,new ServerData());
                oOSTCP.writeObject(c); /*writeUnshared por causa da cache e a referência ser a mesma ou fazer oOS.reset()*/
                oOSTCP.flush();

                c= (TCPCommunication)oISTCP.readObject();

                if(c.isAccepted() == true){
                    System.out.println("TCP connection established.");
                    serverTCPConnection sTC = new serverTCPConnection();
                    sTC.start();
                    clientData.setClientN(c.getClientData().getClientN());
                    clientData.setServerIp(c.getClientData().getServerIp());
                    clientData.setServerPort(c.getClientData().getServerPort());
                    UICanRun = true;
                }else{
                    System.out.println("TCP connection failed.");
                }


            } catch (IOException | ClassNotFoundException e) {
                System.out.println("IO Exception");
                e.printStackTrace();
            }

        }
    }


    class serverTCPConnection extends Thread{
        @Override
        public void run() {
            System.out.println("TCP connection with server is being held by an independent thread.");


                do {

                    try {
                    TCPCommunication c = (TCPCommunication)oISTCP.readObject();

                    switch (c.getRequest()){
                        case "registerResponse":
                            respondedToRegister = true;
                            respondedToRegisterValue = c.getValue();
                            break;

                        case "loginResponse":
                            respondedToLogin = true;
                            respondedToLoginValue = c.getValue();
                            break;

                        case "userListResponse":
                            respondedToUserList = true;
                            userListStr = c.getClientData().getPassword();
                            break;

                        case "channelListResponse":
                            respondedToChannelList = true;
                            ChannelStr = c.getClientData().getPassword();
                            break;

                        case "dm":
                            String temp ="[" + c.getsClientName() + "]: " + c.getMessage();
                            System.out.println(temp);
                            break;

                        case "dmResponse":
                            respondedToDm = true;
                            respondedToDmValue = c.getValue();
                            break;

                        case "createChannelResponse":
                            respondedToCreateChannel = true;
                            respondedToCreateChannelValue = c.getValue();
                            break;

                        case "enterChannelResponse":
                            respondedToEnterChannel = true;
                            respondedToEnterChannelValue = c.getValue();
                            break;

                        case "deleteChannelResponse":
                            respondedToDeleteChannel = true;
                            respondedToDeleteChannelValue = c.getValue();
                            break;

                        case "channelYouWereInWasDeleted":
                            System.out.println("The channel you were in was deleted.");
                            break;

                        case "speakResponse":
                            respondedToSpeak = true;
                            respondedToSpeakValue = c.getValue();
                            break;

                        case "leaveChannelResponse":
                            respondedToLeaveChannel = true;
                            respondedToLeaveChannelValue = c.getValue();
                            break;

                        case "changeChannelResponse":
                            respondedToChangeChannel = true;
                            respondedToChangeChannelValue = c.getValue();
                            break;

                    }

                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("IOException | ClassNotFoundException at serverTCPConnection");
                    System.out.println("Exception handled.");
                }
                }while(shutdown == false);

        }
    }

    class SendRequest extends Thread{
        private String request;

        public SendRequest(String request) {
            this.request = request;
        }

        @Override
        public void run() {

            try {

                TCPCommunication c = new TCPCommunication(request,false, new ClientData(clientData),new ServerData());
                c.setChannelToCreateName(clientData.getChannelToCreateName());
                c.setChannelToEnterName(clientData.getChannelName());
                oOSTCP.writeObject(c); /*writeUnshared por causa da cache e a referência ser a mesma ou fazer oOS.reset()*/
                oOSTCP.flush();
                System.out.println("Wrote request: "+ c.getRequest());
                if(request.equals("enterChannel")){
                    System.out.println("Wrote request: "+ c.getClientData().getChannelName());
                }

            } catch (IOException e) {
                System.out.println("IOException while sending request in SendRequest thread.");
                e.printStackTrace();
            }

        }

    }

    public void run() {
        System.out.println("Client " + clientNumber + " is running.");
        HandleFirstConnection hFC = new HandleFirstConnection();
        hFC.start();
        Scanner sc = new Scanner(System.in);
        String op = "buHU";
        do{

        }while(shutdown != true);
        shutdown = true;
    }

    //must use wait and notify
    public int register(String n,String pw) {
        int toReturn = -1;
        clientData.setName(n);
        clientData.setPassword(pw);
        System.out.println("info sent register: " + clientData.getPassword() + " " + clientData.getName());
        SendRequest sR =  new SendRequest("register");
        sR.start();
        do{
            //System.out.println("[client]: waiting for server's response (...)");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(respondedToRegister == true){
                System.out.println("[client]: server responded!" + respondedToRegisterValue);
                toReturn = respondedToRegisterValue;

            }
        }while(respondedToRegister == false);
        respondedToRegister = false;
        respondedToRegisterValue = -1;
        return toReturn;
    }

    //must use wait and notify
    public int login(String n,String pw) {
        int toReturn = -1;
        clientData.setName(n);
        clientData.setPassword(pw);
        System.out.println("info sent login: " + clientData.getPassword() + " " + clientData.getName());
        SendRequest sR =  new SendRequest("login");
        sR.start();
        do{
            //System.out.println("[client]: waiting for server's response (...)");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(respondedToLogin == true){
                System.out.println("[client]: server responded!" + respondedToLoginValue);
                toReturn = respondedToLoginValue;

            }
        }while(respondedToLogin == false);
        respondedToLogin = false;
        respondedToLoginValue = -1;
        return toReturn;
    }

    //must use wait and notify
    public String requestUserList() {

        SendRequest sR =  new SendRequest("userList");
        sR.start();
        do{
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //System.out.println("[client]: waiting for server's response (...)");
            if(respondedToUserList == true){
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("[client]: server responded! - ");
                return userListStr;
            }
        }while(respondedToUserList == false);
        respondedToUserList = false;
        return "noUsersRegistered";
    }

    public String requestChannelList() {

        SendRequest sR =  new SendRequest("channelList");
        sR.start();
        do{
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //System.out.println("[client]: waiting for server's response (...)");
            if(respondedToChannelList == true){
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("[client]: server responded! - ");
                return ChannelStr;
            }
        }while(respondedToChannelList == false);
        respondedToChannelList = false;
        return "noUsersRegistered";
    }

    public int dmUser(String message,String recipient){
        int toReturn = -1;
        clientData.setMessage(message);
        clientData.setRecipient(recipient);
        System.out.println("recipient:" + recipient);
        SendRequest sR =  new SendRequest("dm");
        sR.start();
        do{
            //System.out.println("[client]: waiting for server's response (...)");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(respondedToDm == true){
                System.out.println("[client]: server responded! " + respondedToDmValue);
                toReturn = respondedToDmValue;

            }
        }while(respondedToDm == false);
        respondedToDm = false;
        respondedToDmValue = -1;
        return toReturn;
    }

    public int createChannel(String cn){
        int toReturn = -1;
        clientData.setChannelToCreateName(cn);
        SendRequest sR =  new SendRequest("createChannel");
        sR.start();
        do{
            //System.out.println("[client]: waiting for server's response (...)");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(respondedToCreateChannel == true){
                System.out.println("[client]: server responded! " + respondedToCreateChannelValue);
                toReturn = respondedToCreateChannelValue;

            }
        }while(respondedToCreateChannel == false);
        respondedToCreateChannel = false;
        respondedToCreateChannelValue = -1;
        return toReturn;
    }

    public int deleteChannel(String cn){
        int toReturn = -1;
        clientData.setChannelToCreateName(cn);
        SendRequest sR =  new SendRequest("deleteChannel");
        sR.start();
        do{
            //System.out.println("[client]: waiting for server's response (...)");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(respondedToDeleteChannel == true){
                System.out.println("[client]: server responded! " + respondedToDeleteChannelValue);
                toReturn = respondedToDeleteChannelValue;

            }
        }while(respondedToDeleteChannel == false);
        respondedToDeleteChannel = false;
        respondedToDeleteChannelValue = -1;
        return toReturn;
    }

    public int enterChannel(String cn){
        int toReturn = -1;
        clientData.setChannelName(cn);
        SendRequest sR =  new SendRequest("enterChannel");
        sR.start();
        do{
            //System.out.println("[client]: waiting for server's response (...)");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(respondedToEnterChannel == true){
                System.out.println("[client]: server responded! " + respondedToEnterChannelValue);
                toReturn = respondedToEnterChannelValue;

            }
        }while(respondedToEnterChannel == false);
        respondedToEnterChannel = false;
        respondedToEnterChannelValue = -1;
        return toReturn;
    }

    public int speak(String message){
        int toReturn = -1;
        clientData.setMessage(message);
        SendRequest sR =  new SendRequest("speak");
        sR.start();
        do{
            //System.out.println("[client]: waiting for server's response (...)");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(respondedToSpeak == true){
                System.out.println("[client]: server responded! " + respondedToSpeakValue);
                toReturn = respondedToSpeakValue;

            }
        }while(respondedToSpeak == false);
        respondedToSpeak = false;
        respondedToSpeakValue = -1;
        return toReturn;
    }

    public int leaveChannel(String cn){
        int toReturn = -1;
        clientData.setChannelName(cn);
        SendRequest sR =  new SendRequest("leaveChannel");
        sR.start();
        do{
            //System.out.println("[client]: waiting for server's response (...)");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(respondedToLeaveChannel == true){
                System.out.println("[client]: server responded! " + respondedToLeaveChannelValue);
                toReturn = respondedToLeaveChannelValue;

            }
        }while(respondedToLeaveChannel == false);
        respondedToLeaveChannel = false;
        respondedToLeaveChannelValue = -1;
        return toReturn;
    }

    public int changeChannel(String cn,String nn){
        int toReturn = -1;
        clientData.setChannelToCreateName(cn);
        clientData.setMessage(nn);
        SendRequest sR =  new SendRequest("changeChannel");
        sR.start();
        do{
            //System.out.println("[client]: waiting for server's response (...)");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(respondedToChangeChannel == true){
                System.out.println("[client]: server responded! " + respondedToChangeChannelValue);
                toReturn = respondedToChangeChannelValue;

            }
        }while(respondedToChangeChannel == false);
        respondedToChangeChannel = false;
        respondedToChangeChannelValue = -1;
        return toReturn;
    }
}
