/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author John Prime
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {

    private static final int PORT = 9090;
    //private static int PEER_PORT = 9900;

    private static final HashMap<String, Boolean> onlineUsers = new HashMap<>();
    //private static final HashMap<String, Integer> usersPorts = new HashMap<>();

    private static Connection connection;
    private static Statement statement;
    private static ResultSet resultSet;

    private static void ConnectToSQL() {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            try {
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "");
                statement = connection.createStatement();
            } catch (SQLException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ClassNotFoundException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private static void GetData() {
        try {
            String query = "select * from useri";
            resultSet = statement.executeQuery(query);
            System.out.println("Got Data:");

            while (resultSet.next()) {
                String name = resultSet.getString("nume");
                System.out.println("Nume: " + name);
            }
        } catch (SQLException e) {
        }
    }

    private static void DeleteUser(String username) {
        try {
            String query = "delete from useri where nume=" + "?;";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, username);

            ps.execute();

            String query1 = "delete from groups where owner=" + "?;";
            PreparedStatement ps1 = connection.prepareStatement(query1);
            ps1.setString(1, username);

            ps1.execute();

            String query2 = "delete from files where utilizator=" + "?;";
            PreparedStatement ps2 = connection.prepareStatement(query2);
            ps2.setString(1, username);

            ps2.execute();
        } catch (SQLException e) {
        }
    }

    private static void CreateGroup(String groupName, String owner) {
        try {
            String query = "insert into groups (groupName, owner, utilizatori)" + " values (?, ?, ?);";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, groupName);
            ps.setString(2, owner);
            ps.setString(3, owner + ";");
            ps.execute();

        } catch (SQLException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private static void DeleteGroup(String groupName, String pseudoOwner) {
        try {
            String searchQuery = "select owner from groups where groupName='" + groupName + "';";
            resultSet = statement.executeQuery(searchQuery);
            if (resultSet.next()) {

                String owner = resultSet.getString("owner");

                if (owner.equals(pseudoOwner)) {
                    String query = "delete from groups where groupName=" + "?;";
                    PreparedStatement ps = connection.prepareStatement(query);
                    ps.setString(1, groupName);

                    ps.execute();

                    String query1 = "update files set group_owned=" + "? where utilizator=" + "?;";
                    PreparedStatement ps1 = connection.prepareStatement(query1);
                    ps1.setString(1, null);
                    ps1.setString(2, pseudoOwner);

                    ps1.execute();
                }
            }
        } catch (SQLException e) {
        }
    }

    private static void AddUsersToGroup(String groupName, ArrayList<String> users) {
        StringBuilder result = new StringBuilder();

        try {
            String searchQuery = "select utilizatori from groups where groupName='" + groupName + "';";
            resultSet = statement.executeQuery(searchQuery);

            if (resultSet.next()) {

                String utilizatori = resultSet.getString("utilizatori");

                for (String user : users) {
                    if (CheckDatabase(user) && !utilizatori.contains(user)) {
                        utilizatori += user + ";";
                    }
                }

                utilizatori += result;

                String query = "update groups set utilizatori=" + "? where groupName=" + "?;";

                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, utilizatori);
                ps.setString(2, groupName);
                ps.execute();
            }
        } catch (SQLException e) {
        }
    }

    private static void DeleteUsersFromGroup(String groupName, ArrayList<String> users) {
        StringBuilder result = new StringBuilder();

        try {
            String searchQuery = "select utilizatori, owner from groups where groupName='" + groupName + "';";
            resultSet = statement.executeQuery(searchQuery);

            if (resultSet.next()) {

                String[] utilizatori = resultSet.getString("utilizatori").split(";");
                String owner = resultSet.getString("owner");

                // owner, you can't remove yourself!
                if (users.contains(owner)) {
                    result.append(owner);
                    result.append(";");
                }

                // delete the users that are specified in the "users" list
                for (String utilizator : utilizatori) {
                    if (!users.contains(utilizator)) {
                        result.append(utilizator);
                        result.append(";");
                    }
                }

                String query = "update groups set utilizatori=" + "? where groupName=" + "?;";

                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, result.toString());
                ps.setString(2, groupName);
                ps.execute();
            }
        } catch (SQLException e) {
        }
    }

    private static void AddMetaData(String filename, Integer port, String sender, String group) {
        try {
            String query = "insert into files (name, port, utilizator, group_owned)" + " values (?, ?, ?, ?);";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, filename);
            ps.setString(2, port.toString());
            ps.setString(3, sender);
            ps.setString(4, group);
            ps.execute();

        } catch (SQLException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private static UserFile FindFileAtUsers(String filename) {
        try {
            String query = "select * from files where name='" + filename + "';";
            resultSet = statement.executeQuery(query);
            if (resultSet.next()) {

                Integer id = Integer.parseInt(resultSet.getString("id"));
                String name = resultSet.getString("name");
                String ip = resultSet.getString("ip");
                Integer port = Integer.parseInt(resultSet.getString("port"));
                String utilizator = resultSet.getString("utilizator");
                String group = resultSet.getString("group_owned");

                return new UserFile(id, name, ip, port, utilizator, group, onlineUsers.get(utilizator));
            }
        } catch (SQLException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }

        return null;
    }

    private static String GetUserGroup(String user) {
        try {
            String query = "select * from groups where owner='" + user + "';";
            resultSet = statement.executeQuery(query);
            if (resultSet.next()) {

                String group = resultSet.getString("groupName");

                return group;
            }
        } catch (SQLException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }

        return null;
    }

    private static boolean CheckIfUserInGroup(String user, String group) {

        if (group == null) {
            return true;
        }
        try {
            String query = "select * from groups where groupName='" + group + "';";
            resultSet = statement.executeQuery(query);
            if (resultSet.next()) {

                String got_users = resultSet.getString("utilizatori");

                if (got_users.contains(user)) {
                    return true;
                }

            }
        } catch (SQLException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }

        return false;
    }

    private static ArrayList<String> GetAllRegisteredUsers() {

        ArrayList<String> allUsers = new ArrayList<>();

        try {
            String query = "select * from useri";
            resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String name = resultSet.getString("nume");

                allUsers.add(name);
            }
        } catch (SQLException e) {
        }

        return allUsers;
    }

    private static boolean CheckDatabase(String username) {

        try {
            String query = "select nume from useri where nume='" + username + "';";
            resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }

        return false;
    }

    private static void UpdatePortAtUserInDB(String user, Integer port) {
        String query = "update files set port=" + "? where utilizator=" + "?;";

        PreparedStatement ps;
        try {
            ps = connection.prepareStatement(query);
            ps.setString(1, port.toString());
            ps.setString(2, user);
            ps.execute();
        } catch (SQLException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void Register(String username, String password) {
        try {
            String query = "insert into useri (nume, password)" + " values (?, ?);";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.execute();

        } catch (SQLException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private static Boolean LogIn(String username, String password) {

        try {
            String query = "select * from useri where nume='" + username + "' and password='" + password + "';";
            resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                return true;
            }

        } catch (SQLException e) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }

        return false;
    }

    public static void main(String args[]) throws SQLException {

        try {
            Socket socket;

            ServerSocket listener = new ServerSocket(PORT);

            // connect to Database
            ConnectToSQL();

            for (String user : GetAllRegisteredUsers()) {
                onlineUsers.put(user, Boolean.FALSE);
            }

            System.out.println((char) 27 + "[38;42m Server is listening...");

            while (true) {
                try {
                    socket = listener.accept();

                    System.out.println("Connection established...");
                    new ServerThread(socket).start();
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static class ServerThread extends Thread {

        String line = null;
        BufferedReader is = null;
        PrintWriter os = null;
        Socket s = null;
        String clientName = null;
        Boolean signedIn = Boolean.FALSE, signedUp = Boolean.FALSE;

        public ServerThread(Socket s) {
            this.s = s;
        }

        @Override
        public void run() {
            try {
                is = new BufferedReader(new InputStreamReader(s.getInputStream()));
                os = new PrintWriter(s.getOutputStream(), true);

            } catch (IOException e) {
                System.out.println("IO error in server thread");
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
            }

            try {

                while (true) {

                    while (!signedIn) {
                        os.println("Sign Up or Sign In");
                        line = is.readLine();

                        System.out.println("Logged out now. I got: " + line);

                        if (line.toLowerCase().equals("sign up")) {
                            os.println("signup:name");
                            // got name
                            line = is.readLine();
                            String cName = line;

                            os.println("signup:password");
                            // got psswd
                            line = is.readLine();
                            String cPsswd = line;

                            // daca nu exista, adauga-l!
                            if (!CheckDatabase(cName)) {
                                Register(cName, cPsswd);
                                os.println("Registered Client");
                            } else {
                                // start again
                                os.println("Sign Up Failed");
                            }
                        } else if (line.toLowerCase().equals("sign in")) {
                            os.println("signin:name");
                            // got name
                            line = is.readLine();

                            // the client has this name when is signed in!
                            // even if he is a hecker:)
                            clientName = line;

                            os.println("signin:password");
                            // got psswd
                            line = is.readLine();
                            String clientPsswd = line;

                            if (LogIn(clientName, clientPsswd)) {
                                os.println("Signed In");

                                // he is signed in, he is surely online
                                synchronized (onlineUsers) {
                                    onlineUsers.put(clientName, Boolean.TRUE);
                                }

                                String userss = "";
                                for (String s : GetAllRegisteredUsers()) {
                                    userss += s + " ";
                                }

                                // send users
                                os.println(userss);

                                // update and send the port
                                int clientPort = this.s.getPort() + 1000;

                                UpdatePortAtUserInDB(clientName, clientPort);
                                os.println(clientPort);

                                signedIn = true;
                            } else {
                                os.println("Sign In Failed");
                            }
                        }
                    }

                    // get a command from the client
                    line = is.readLine();

                    if (line.toLowerCase().contains("upload ")) {
                        System.out.println("Got from client " + clientName + ", the command: " + line);

                        // create user's dir within Server dir, if it doesn't exist
                        File dirs = new File("Server\\" + clientName);
                        if (!dirs.exists()) {
                            dirs.mkdirs();
                        }

                        String[] comm = line.split(" ");

                        for (int i = 1; i < comm.length; i++) {
                            String filename = comm[i];

                            // get the file now
                            line = is.readLine();

                            // file doesn't exist
                            if (line.equals("failedUpload")) {
                                continue;
                            }

                            try (FileOutputStream out = new FileOutputStream("Server\\" + clientName + "\\" + filename)) {
                                out.write(line.getBytes());
                            }

                            //port
                            line = is.readLine();
                            Integer port = Integer.parseInt(line);

                            AddMetaData(filename, port, clientName, GetUserGroup(clientName));
                        }
                    } else if (line.toLowerCase().contains("find ")) {
                        System.out.println("Got from client " + clientName + ", the command: " + line);

                        // filename
                        line = is.readLine();
                        String filename = line;
                        UserFile userfile = null;
                        userfile = FindFileAtUsers(filename);

                        if (userfile != null && CheckIfUserInGroup(clientName, userfile.group_owned) == true) {

                            os.println("foundFile");

                            os.println(userfile.id);
                            os.println(userfile.name);
                            os.println(userfile.ip);
                            os.println(userfile.port);
                            os.println(userfile.utilizator);
                            os.println(userfile.group_owned);
                            os.println(userfile.isOnline);
                        } else if (userfile != null && CheckIfUserInGroup(clientName, userfile.group_owned) == false) {
                            os.println("not_allowed");
                        } else {
                            os.println("notFoundFile");
                        }
                    } else if (line.toLowerCase().contains("create group")) {
                        String[] comm = line.split(" ");
                        CreateGroup(comm[2], clientName);
                    } else if (line.toLowerCase().contains("delete group")) {
                        String[] comm = line.split(" ");

                        DeleteGroup(comm[2], clientName);
                    } else if (line.toLowerCase().contains("add ") && line.toLowerCase().contains(" to group ")) {
                        String[] comm = line.split(" ");
                        ArrayList<String> usersList = new ArrayList<>();
                        String groupName = comm[comm.length - 1];

                        for (int i = 1; i < comm.length - 3; i++) {
                            usersList.add(comm[i]);
                        }

                        AddUsersToGroup(groupName, usersList);

                    } else if (line.toLowerCase().contains("delete ") && line.toLowerCase().contains(" from group ")) {
                        String[] comm = line.split(" ");
                        ArrayList<String> usersList = new ArrayList<>();
                        String groupName = comm[comm.length - 1];

                        for (int i = 1; i < comm.length - 3; i++) {
                            usersList.add(comm[i]);
                        }

                        DeleteUsersFromGroup(groupName, usersList);

                    } else if (line.toLowerCase().contains("log out")) {
                        System.out.println("Got from client " + clientName + ", the command: " + line);
                        signedIn = false;
                        synchronized (onlineUsers) {
                            // he closed the client, he is offline
                            onlineUsers.put(clientName, Boolean.FALSE);
                        }
                    } else if (line.toLowerCase().contains("delete") && !line.toLowerCase().contains(" from group ")) {
                        String[] comm = line.split(" ");

                        String user = comm[2];

                        System.out.println(user);

                        DeleteUser(user);

                    } else if (line.toLowerCase().equals("quit")) {
                        synchronized (onlineUsers) {
                            // he signed out, he is definetly offline, 
                            onlineUsers.put(clientName, Boolean.FALSE);
                        }
                        System.out.println("Got from client " + clientName + ", the command: " + line);
                        break;
                    }
                }
            } catch (IOException e) {

                line = this.getName();
                System.out.println("IO Error / Client " + line + " terminated abruptly.");
            } catch (NullPointerException e) {
                line = this.getName();
                System.out.println("Client " + line + " Closed.");
            } finally {
                try {
                    System.out.println("Connection Closing...");
                    if (is != null) {
                        is.close();
                        System.out.println("Socket Input Stream Closed.");
                    }

                    if (os != null) {
                        os.close();
                        System.out.println("Socket Output Stream Closed.");
                    }
                    if (s != null) {
                        s.close();
                        System.out.println("Socket Closed.");
                    }

                } catch (IOException ie) {
                    System.out.println("Socket Close Error.");
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ie);
                }
            }
        }
    }
}
