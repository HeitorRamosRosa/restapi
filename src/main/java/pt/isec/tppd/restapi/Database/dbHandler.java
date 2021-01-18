package pt.isec.tppd.restapi.Database;

import pt.isec.tppd.restapi.businessLogic.Channel;
import pt.isec.tppd.restapi.businessLogic.ClientData;

import java.sql.*;
import java.util.Vector;

public class dbHandler {

    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/?useLegacyDatetimeCode=false&serverTimezone=UTC";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "nop133166";


    public static void createDatabase(String database){
        try
        {

            Connection con = DriverManager.getConnection(
                    DATABASE_URL,
                    USERNAME,
                    PASSWORD);
            Statement st = con.createStatement();
            String sqlQuery1 = "create database if not exists "+database+";";
            String sqlQuery2 = "use "+database+";";
            String sqlQuery3 = "create table if not exists utilizadores (username VARCHAR(20) PRIMARY KEY ,  password VARCHAR(20));";
            String sqlQuery4 = "create table if not exists channels (channelname VARCHAR(20) PRIMARY KEY, creator VARCHAR(20), nmessages INT);";

            st.addBatch(sqlQuery1);
            st.addBatch(sqlQuery2);
            st.addBatch(sqlQuery3);
            st.addBatch(sqlQuery4);

            st.executeBatch();
            st.close();


        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
    }

    public static void addUser(String server, String user, String password){
        Connection con = null;
        try {
            con = DriverManager.getConnection(
                    DATABASE_URL,
                    USERNAME,
                    PASSWORD);

            Statement st = con.createStatement();
            String sqlQuery1 = "use "+server;
            String sqlQuery2 = "insert into utilizadores (username, password) values " +
                    "('"+user+"','"+password+"');";

            st.addBatch(sqlQuery1);
            st.addBatch(sqlQuery2);
            st.executeBatch();
            st.close();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void removeChannelTable(String server){
        try
        {
            Connection con = DriverManager.getConnection(
                    DATABASE_URL,
                    USERNAME,
                    PASSWORD);


            Statement st = con.createStatement();
            String sqlQuery1 = "use "+server;
            String sqlQuery2 = "delete from channels;";



            st.addBatch(sqlQuery1);
            st.addBatch(sqlQuery2);
            st.executeBatch();


        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
    }

    public static void removeUsersTable(String server){
        try
        {
            Connection con = DriverManager.getConnection(
                    DATABASE_URL,
                    USERNAME,
                    PASSWORD);


            Statement st = con.createStatement();
            String sqlQuery1 = "use "+server;
            String sqlQuery2 = "delete from utilizadores;";


            st.addBatch(sqlQuery1);
            st.addBatch(sqlQuery2);
            st.executeBatch();


        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
    }

    public static void addChannel(String server, String chName, String chCreator){
        try
        {
            Connection con = DriverManager.getConnection(
                    DATABASE_URL,
                    USERNAME,
                    PASSWORD);


            Statement st = con.createStatement();
            String sqlQuery1 = "use "+server;
            String sqlQuery2 = "insert into channels (channelname, creator) values" +
                    " ('"+chName+"','"+chCreator+"');";


            st.addBatch(sqlQuery1);
            st.addBatch(sqlQuery2);
            st.executeBatch();


        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
    }

    public static void loadUsers(String server, Vector<ClientData> cData){
        try
        {
            Connection con = DriverManager.getConnection(
                    DATABASE_URL,
                    USERNAME,
                    PASSWORD);


            Statement st = con.createStatement();
            String sqlQuery1 = "use "+server;
            String sqlQuery2 = "select * from utilizadores";

            st.execute(sqlQuery1);

            ResultSet rs = st.executeQuery(sqlQuery2);

            while(rs.next()){
                System.out.println("User: "+rs.getString("username"));
                System.out.println("Password: "+rs.getString("password"));
                ClientData temp = new ClientData();
                temp.setName(rs.getString("username"));
                temp.setPassword(rs.getString("password"));
                cData.add(temp);
            }

        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }

    }

    public static void loadChannel(String server, Vector<Channel> vChannels){
        try
        {
            Connection con = DriverManager.getConnection(
                    DATABASE_URL,
                    USERNAME,
                    PASSWORD);


            Statement st = con.createStatement();
            String sqlQuery1 = "use "+server;
            String sqlQuery2 = "select * from channels;";

            // String sqlQuery3 = "insert into utilizadores (username, password) values " +
            //         "('"+user+"','"+password+"');";

            st.execute(sqlQuery1);
            ResultSet rs;
            rs = st.executeQuery(sqlQuery2);

            while(rs.next()){
                Channel temp = new Channel(rs.getString("channelname"), rs.getString("creator"));
                vChannels.add(temp);
            }


        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
    }

    public static void main(String[] args) {
        createDatabase("server0");
        //loadUsers("server0",new ClientData());
        removeChannelTable("server0");
    }


}
