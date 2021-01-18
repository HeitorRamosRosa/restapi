package pt.isec.tppd.restapi.communicationLogic;


import pt.isec.tppd.restapi.businessLogic.ClientData;
import pt.isec.tppd.restapi.businessLogic.RemoteClientInterface;
import pt.isec.tppd.restapi.businessLogic.RemoteServerInterface;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ClientRMI extends UnicastRemoteObject implements RemoteClientInterface
{
    static ArrayList<RMI_ServerData> RemoteServerData = new ArrayList<>();

    ClientRMI() throws RemoteException{

    }

    public static void main(String[] args) throws IOException
    {
        System.out.println("Client RMI\n\n");

        try
        {
            for(String port : args)
            {
                System.out.println("Connecting to [" + port + "]");

                int RegistryPort = Integer.parseInt(port) + Registry.REGISTRY_PORT;
                Registry registry = LocateRegistry.getRegistry(RegistryPort);
                RemoteServerInterface RS = (RemoteServerInterface) registry.lookup("Server"+port);

                RMI_ServerData temp = new RMI_ServerData(port,RS);
                RemoteServerData.add(temp);
                System.out.println("Created RMI_Serverdata with port ["+port+"]");
            }
        } catch (RemoteException | NotBoundException e)
        {
            e.printStackTrace();
        }

        run();
    }

    private static void run() throws IOException {
        Scanner sc = new Scanner(System.in);
        String command;
        boolean exit = false;
        if(RemoteServerData.size()==0)
        {
            System.out.println("Nenhum server encontrado.\n");
            return;
        }
        do
            {
            ShowServersOnline();
            System.out.println("Command: ");
            command = sc.next();

            switch(command.toLowerCase())
            {
                case "register":
                    registerCommand();
                    break;
                case "message":
                     sendMessage();
                    break;
                case "exit":
                    exit = true;
                    break;
            }
        }while(!exit);

    }

    private static void sendMessage() throws IOException {
        int portChosen = -1;
        String message,portString;
        Scanner sc = new Scanner(System.in);
        ClientData ClientData = new ClientData();
        if(RemoteServerData.size()>1)
        {
            try {
                ShowServersOnline();
                System.out.println("Escolha um server: ");
                portString = sc.nextLine();
                portChosen = Integer.parseInt(portString);
            }catch(InputMismatchException e)
            {
                System.out.println("O port tem de ser um inteiro.");
                return;
            }
        }
        else
            portChosen = Integer.parseInt(RemoteServerData.get(0).port);

        if(!isPortOnServerList(portChosen))
        {
            System.out.println("Port ["+portChosen+"] was not found.\n");
            return;
        }

        System.out.println("Sending message to all clients on server with port: ["+portChosen+"]");

        System.out.println("Insira a mensagem a enviar:");
        message = sc.nextLine();

        System.out.println("Sending \n["+message+"]\n to all clients on server with port: ["+portChosen+"]");

        int index = -1;
        RemoteServerInterface RSI = null;

        for(RMI_ServerData server : RemoteServerData)
        {
            if(portChosen == Integer.parseInt(server.port))
                RSI = server.RemoteServer;
        }

        ClientData.setMessage(message);
        ClientRMI RCI = new ClientRMI(); //para callback
        RSI.sendMensagemToServer(ClientData,RCI);


    }

    private static void registerCommand() throws IOException {
        Scanner sc = new Scanner(System.in);
        ClientData ClientData = new ClientData();
        System.out.println("Insert Name:");
        ClientData.setName(sc.next());
        System.out.println("Insert Password");
        ClientData.setPassword(sc.next());


        System.out.println("Registando cliente no primeiro servidor.");
        RemoteServerInterface RSI = RemoteServerData.get(0).RemoteServer;
        ClientRMI RCI = new ClientRMI(); //para callback
        RSI.registaCliente(ClientData, RCI);
    }

    private static void ShowServersOnline() {
        System.out.println("\nLista de servers online:");

        for(RMI_ServerData Server : RemoteServerData){
            System.out.println(Server.port);
        }
    }

     private static boolean isPortOnServerList(int port){
        for(RMI_ServerData server : RemoteServerData){
            if(port == Integer.parseInt(server.port))
                return true;
        }
        return false;
    }

    @Override
    public void showResult(String msg) throws IOException {
        System.out.println("["+msg+"]");
    }

}
