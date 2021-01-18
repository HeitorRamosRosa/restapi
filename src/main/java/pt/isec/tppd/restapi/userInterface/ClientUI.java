package pt.isec.tppd.restapi.userInterface;

import pt.isec.tppd.restapi.businessLogic.clientUIStates;
import pt.isec.tppd.restapi.communicationLogic.Client;
import pt.isec.tppd.restapi.communicationLogic.Server;

import java.util.Scanner;

import static pt.isec.tppd.restapi.businessLogic.clientUIStates.STATE_NOTLOGEDIN;
import static pt.isec.tppd.restapi.businessLogic.clientUIStates.STATE_OFFCHATROOM;

public class ClientUI {
    private Boolean over = false;
    private Client client;
    clientUIStates state = STATE_NOTLOGEDIN;
    boolean loggedIn = false;

    public ClientUI(Client client) {
        this.client = client;
    }

    public void run(){

        Scanner sc = new Scanner(System.in);
        String op="";

        do{

            if(client.isUICanRun() == false){

                System.out.println("Waiting for client process to connect.");

            }else{

                switch (state){

                    case STATE_NOTLOGEDIN:
                        int op2 = -1,resp = -1;
                        String usr ="";
                        String pwd ="";

                        do{
                            do {
                                try{
                                    System.out.println("You can either login or register.");
                                    System.out.println("1 - Login");
                                    System.out.println("2 - Register");
                                    System.out.print("Option: ");
                                    op = sc.nextLine();

                                    if (!op.isEmpty()) {

                                        op2 = Integer.parseInt(op);
                                        //wtf is this, use try catch

                                        System.out.print("Username: ");
                                        usr = sc.nextLine();
                                        System.out.print("Password: ");
                                        pwd = sc.nextLine();

                                        switch (op2) {

                                            case 1:
                                                resp = client.login(usr, pwd);

                                                switch (resp) {
                                                    case -1:    //couldn't connect
                                                        System.out.println("Failed to connect to server.");
                                                        break;


                                                    case 0: //logged in
                                                        System.out.println("Logged in sucessfully.");
                                                        loggedIn = true;
                                                        state = STATE_OFFCHATROOM;
                                                        break;

                                                    case 1:
                                                        System.out.println("Username does not exist.");
                                                        break;

                                                    case 2:
                                                        System.out.println("Password is incorrect.");
                                                        break;

                                                    case 3:
                                                        System.out.println("Client is already logged in.");
                                                        break;
                                                }

                                                break;

                                            case 2:

                                                resp = client.register(usr, pwd);

                                                switch (resp) {
                                                    case -1:
                                                        System.out.println("Failed to connect to server.");
                                                        break;

                                                    case 0:
                                                        System.out.println("Registered successfully. You can now log in!");

                                                        state = STATE_OFFCHATROOM;

                                                        break;

                                                    case 1:
                                                        System.out.println("Username already in use.");
                                                        break;
                                                }
                                                break;
                                        }

                                    } else {
                                        op2 = -1;
                                    }
                                }catch (NumberFormatException e){
                                    System.out.println("Please insert numbers 1 or 2.");
                                }
                            }while(op2 < 1 && op2 > 2);
                        }while(loggedIn != true);

                        break;

                    case STATE_INCHATROOM:

                        break;

                    case STATE_OFFCHATROOM:
                        String exit = "dbg dbg";
                        String[] com = exit.split(" ");;
                        do{
                            String option = "";
                            option = sc.nextLine();

                            if(!option.isEmpty()){
                                com = option.split(" ");
                                exit = com[0];
                            }
                            switch (com[0]){
                                case "listUsers":
                                    System.out.println("Users in the system:");
                                    String temp = client.requestUserList();
                                    System.out.println(temp);
                                    break;

                                case "listChannels":
                                    System.out.println("Channels in the system:");
                                    temp = client.requestChannelList();
                                    System.out.println(temp);
                                    break;


                                case "listChatRooms":
                                    break;

                                case "listMessages":
                                    break;

                                case "createChannel":

                                    resp = client.createChannel(com[1]);
                                    switch (resp){

                                        case 0:
                                            System.out.println("Channel was successfully created.");
                                            break;

                                        case 1:
                                            System.out.println("Channel name is already in use");
                                            break;

                                        case 2:
                                            System.out.println("Server does not support any more channels.");
                                            break;

                                    }
                                    break;

                                case "enterChannel":

                                    resp = client.enterChannel(com[1]);

                                    switch (resp){

                                        case 0:
                                            System.out.println("You entered the channel.");
                                            break;

                                        case 1:
                                            System.out.println("You can't be in two channels at the same time");
                                            break;

                                        case 2:
                                            System.out.println("Chosen channel does not exist.");
                                            break;

                                    }
                                    break;

                                case "leaveChannel":

                                    resp = client.leaveChannel(com[1]);

                                    switch (resp){

                                        case 0:
                                            System.out.println("You left the channel.");
                                            break;

                                        case 1:
                                            System.out.println("You're not in that channel.'");
                                            break;

                                        case 2:
                                            System.out.println("Chosen channel does not exist.");
                                            break;

                                    }
                                    break;

                                case "speak":
                                    String DM = getDM(option,1);
                                    resp = client.speak(DM);

                                    switch (resp){

                                        case 0:
                                            System.out.println("You spoke.");
                                            break;

                                        case 1:
                                            System.out.println("You can't speak out of a channel");
                                            break;


                                    }
                                    break;

                                case "deleteChannel":

                                    resp = client.deleteChannel(com[1]);

                                    switch (resp){

                                        case 0:
                                            System.out.println("Channel was deleted.");
                                            break;

                                        case 1:
                                            System.out.println("You can't delete channels you did not create.");
                                            break;

                                        case 2:
                                            System.out.println("Chosen channel does not exist.");
                                            break;

                                    }
                                    break;

                                case "changeChannel":

                                    resp = client.changeChannel(com[1],com[2]);

                                    switch (resp){

                                        case 0:
                                            System.out.println("Channel's name was changed.");
                                            break;

                                        case 1:
                                            System.out.println("You can't change channels you did not create.");
                                            break;

                                        case 2:
                                            System.out.println("Chosen channel does not exist.");
                                            break;

                                    }
                                    break;

                                case "channelStats":
                                    break;

                                case "dm":
                                    String DM2 = getDM(option, 2);

                                    resp = client.dmUser(DM2,com[1]);
                                    switch (resp){
                                        case 0:
                                            System.out.println("Dm sent.");
                                            break;

                                        case 1:
                                            System.out.println("User does not exist.");
                                            break;

                                        case 2:
                                            System.out.println("User is not online.");
                                            break;
                                    }

                                    break;

                                case "help":
                                    System.out.println("These are the available commands:");
                                    System.out.println("listUsers");
                                    System.out.println("listChannels");
                                    System.out.println("dm [name of the user you want to send a message to]");
                                    System.out.println("enter [name of the channel you want to enter]");
                                    System.out.println("delete [name of the channel you want to delete]");
                                    System.out.println("change [name of the channel you want to change name] [name you want to change to]");
                                    System.out.println("speak [what you want to say to the channel you're in]");
                                    break;
                            }
                        }while(!exit.equals("exit"));
                        break;
                }

            }
        }while(over != true);

        System.out.println("Client shutting down.");
    }

    public static String getDM(String com, int nPalavrasRemover){
        String words[] = {};
        words = com.split(" ");

        String DM = "";
        for(int i=nPalavrasRemover;i< words.length ; i++){
            DM += words[i]+" ";
        }

        System.out.println("DM: ["+DM+"]");
        return DM;
    }

    public static void main(String[] args){

        Client c = new Client("localhost", 9008);
        ClientUI cUI= new ClientUI(c);
        c.start();
        cUI.run();
    }
}
