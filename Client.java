/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author John Prime
 */
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;

public class Client {

    private static final int PORT = 9090;
    private static final String SERVER_ADDRESS = "188.25.228.66";
    private static final String ADMIN = "vlad";
    private FileTransferWindow ftw = null;

    static String username;

    public Client() {
        this.username = null;
        ftw = new FileTransferWindow();
        ftw.start();

    }

    private void runClient() throws IOException {
        Socket socket = null;
        String line = null;
        String name = null;
        String message = null;
        BufferedReader br = null;
        BufferedReader is = null;
        PrintWriter os = null;
        BufferedReader isPeer = null;
        PrintWriter osPeer = null;
        ClientListener cl = null;
        Integer myPort = null;
        UserFile userFile = null;
        Boolean wantQuit = Boolean.FALSE,
                loggedIn = Boolean.FALSE, registered = Boolean.FALSE;
        Boolean fromClientQuit = Boolean.FALSE;

        try {
            //InetAddress.getByName(SERVER_ADDRESS)
            socket = new Socket(InetAddress.getLocalHost(), PORT);
            br = new BufferedReader(new InputStreamReader(System.in));
            is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            os = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.print("IO Exception");
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        }

        //System.out.println("Server Address : " + SERVER_ADDRESS);
        System.out.println("Client Address : " + InetAddress.getLocalHost() + "\n");

        System.out.println((char) 27 + "[34;41m***************File Sharing System Client***************\n");

        System.out.println("Use these commands first: Sign Up, Sign In. "
                + "After, you can:");
        System.out.println("Use one of the manage group commands.");
        System.out.println("Upload file1 [file2 ... fileN], log out, find file(s)");
        System.out.println("Use the optionally command - quit - to quit the client.\n");

        ArrayList<String> fromGUI = new ArrayList<>();
        try {
            while (true) {

                // here the user uses the client to sign in / sign up
                while (!loggedIn) {
                    message = is.readLine();

                    //System.out.println("87 client: message=" + message);
                    if (message.equals("Sign Up or Sign In")) {
                        System.out.print("Do you want to Sign Up or Sign In(or you can easily quit)? ");

                        while (true) {

                            if (!this.ftw.getFrame().isVisible()) {

                                fromClientQuit = true;

                                // just to leave the loop
                                loggedIn = true;

                                os.println("quit");
                                break;
                            }

                            if (this.ftw.getEventStatus()) {

                                os.println(this.ftw.getEventData().get(0));
                                fromGUI.clear();
                                fromGUI.addAll(this.ftw.getEventData());
                                this.ftw.resetEvent();

                                break;
                            }

                            try {
                                Thread.currentThread().sleep(500);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                    }

                    if (message.equals("signup:name")) {
                        System.out.println("You want to register.");
                        System.out.print("Input your name: ");

                        line = fromGUI.get(1);
                        os.println(line);
                    }

                    if (message.equals("signup:password")) {
                        System.out.print("Input your password: ");

                        line = fromGUI.get(2);
                        os.println(line);
                    }

                    if (message.equals("Registered Client")) {
                        registered = true;
                        System.out.println("You are registered. You can Sign In now.");
                        this.ftw.getEvent().setText("You are registered.");
                    }

                    if (message.equals("Sign Up Failed")) {
                        System.out.println("User already registered. Use other username to register...");
                        this.ftw.getEvent().setText("User already registered!");
                    }

                    if (message.equals("signin:name")) {
                        System.out.println("You want to Sign In.");
                        System.out.print("Input your name: ");
                        line = fromGUI.get(1);
                        name = line;
                        username = name;
                        os.println(line);
                    }

                    if (message.equals("signin:password")) {
                        System.out.print("Input your password: ");
                        line = fromGUI.get(2);
                        os.println(line);

                        message = is.readLine();
                        if (message.equals("Signed In")) {
                            System.out.println("You are Signed in.");

                            if (!name.equals(ADMIN)) {
                                System.out.println((char) 27 + "[38;42m ClientListener is listening...");
                            }

                            System.out.print(name + ", send a command to Server (or quit to end): ");

                            String[] allUsers = is.readLine().split(" ");

                            String finalString = "";

                            finalString += "Registered users\n\n";
                            finalString += "*refresh on sign in*\n\n";

                            for (String s : allUsers) {
                                finalString += s + "\n";
                            }

                            this.ftw.textField.setText(finalString);

                            // get the PORT
                            message = is.readLine();

                            myPort = Integer.parseInt(message);

                            loggedIn = true;

                            // assign a port to the online user and start the listener
                            if (!name.toLowerCase().equals(ADMIN)) {
                                try {
                                    this.ftw.setLoggedIn(name, "user");
                                } catch (BadLocationException ex) {
                                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                this.ftw.normalUser.set(true);
                                this.ftw.adminUser.set(false);

                                cl = new ClientListener(myPort, loggedIn);
                                cl.start();
                            } else {
                                try {
                                    this.ftw.setLoggedIn(name, "admin");
                                } catch (BadLocationException ex) {
                                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                // for Admin
                                this.ftw.adminUser.set(true);
                                this.ftw.normalUser.set(false);
                            }

                        } else if (message.equals("Sign In Failed")) {
                            System.out.println("Invalid username / password. Try again...");
                            this.ftw.getEvent().setText("Invalid username / password. Try again...");
                        }
                    }
                }

                // exited before sign in
                if (fromClientQuit) {
                    break;
                }

                // now, that he is logged in
                // send a new command to the server
                while (true) {

                    // when the window is closed, close the client's sockets, too
                    if (!this.ftw.getFrame().isVisible()) {

                        try {
                            if (cl != null && cl.clientListener != null && !cl.clientListener.isClosed()) {
                                cl.clientListener.close();
                                cl.loggedIn = Boolean.FALSE;
                            }

                            if (cl != null) {
                                cl.join();
                            }

                        } catch (InterruptedException ex) {
                            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        fromClientQuit = true;

                        os.println("quit");
                        break;
                    }

                    if (this.ftw.getEventStatus()) {
                        //System.out.println("lungime este " + this.ftw.getEventData().size() + " " + this.ftw.getEventData().get(0));
                        fromGUI.clear();
                        fromGUI.addAll(this.ftw.getEventData());
                        this.ftw.resetEvent();

                        break;
                    }

                    try {
                        Thread.currentThread().sleep(500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                if (fromClientQuit) {
                    break;
                }

                line = fromGUI.get(0);
                //System.out.println("My command: " + line);

                if (!name.toLowerCase().equals(ADMIN)) {

                    if (line.toLowerCase().contains("upload")) {
                        String[] comm = fromGUI.get(1).split(" ");

                        // send the command
                        os.println(line + " " + fromGUI.get(1));

                        // send multiple files
                        for (int i = 0; i < comm.length; i++) {

                            FileInputStream fis = null;
                            BufferedInputStream bis = null;

                            String filename = comm[i];

                            File file = new File("Client\\" + name + "\\" + filename);

                            if (file.exists() && !file.isDirectory()) {

                                long size = file.length();

                                byte[] myByteArray = new byte[(int) size];
                                fis = new FileInputStream(file);
                                bis = new BufferedInputStream(fis);
                                bis.read(myByteArray, 0, myByteArray.length);

                                String s = new String(myByteArray);

                                // send file now
                                os.println(s);

                                // also, share the file on DB
                                os.println(myPort);

                                this.ftw.getEvent().setText("You uploaded \"" + filename + "\"");
                            } else {
                                System.out.println("File " + filename + " doesn't exist!!! Try again with other file...");
                                this.ftw.getEvent().setText("File \"" + filename + "\" doesn't exist!");
                                os.println("failedUpload");
                            }
                        }

                    } else if (line.toLowerCase().contains("find ")) {

                        // send the command
                        os.println(line);

                        os.println(fromGUI.get(1));

                        String response = is.readLine();

                        switch (response) {
                            case "foundFile":
                                userFile = new UserFile(Integer.parseInt(is.readLine()), is.readLine(),
                                        is.readLine(), Integer.parseInt(is.readLine()), is.readLine(), is.readLine(), is.readLine().equals("true"));
                                this.ftw.setFoundFileMessage("YES - " + userFile.name);
                                this.ftw.getEvent().setText("You are allowed to download");
                                break;
                            case "not_allowed":
                                System.out.println("Not allowed to download.");
                                userFile = null;
                                this.ftw.setFoundFileMessage("YES - " + fromGUI.get(1));
                                this.ftw.getEvent().setText("Not allowed to download");
                                break;
                            default:
                                System.out.println("File not found.");
                                userFile = null;
                                this.ftw.setFoundFileMessage("NO - " + fromGUI.get(1));
                                this.ftw.getEvent().setText("File not found. Try again...");
                                break;
                        }

                    } else if (line.toLowerCase().contains("create group")) {
                        os.println(line + " " + fromGUI.get(1));
                        this.ftw.getEvent().setText("Group " + fromGUI.get(1) + " created");
                    } else if (line.toLowerCase().contains("delete group")) {
                        os.println(line + " " + fromGUI.get(1));
                        this.ftw.getEvent().setText("Group " + fromGUI.get(1) + " deleted");
                    } else if (line.toLowerCase().contains("add users")) {
                        os.println("add " + fromGUI.get(1) + " to group " + fromGUI.get(2));
                        this.ftw.getEvent().setText("You added [" + fromGUI.get(1) + "] to group " + fromGUI.get(2));
                    } else if (line.toLowerCase().contains("delete users")) {
                        os.println("delete " + fromGUI.get(1) + " from group " + fromGUI.get(2));
                        this.ftw.getEvent().setText("You deleted [" + fromGUI.get(1) + "] from group " + fromGUI.get(2));
                    } else if (line.toLowerCase().contains("log out")) {
                        this.ftw.setLoggedOut();

                        loggedIn = false;
                        os.println(line);

                        try {
                            if (cl != null && cl.clientListener != null && !cl.clientListener.isClosed()) {
                                cl.clientListener.close();
                                cl.loggedIn = Boolean.FALSE;
                            }
                            if (cl != null) {
                                cl.join();
                            }

                        } catch (InterruptedException ex) {
                            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        continue;
                    } else if (line.toLowerCase().contains("download")) {
                        Socket socketPeer = null;

                        if (userFile != null && userFile.isOnline) {
                            socketPeer = new Socket(InetAddress.getLocalHost(), userFile.port);

                            isPeer = new BufferedReader(new InputStreamReader(socketPeer.getInputStream()));
                            osPeer = new PrintWriter(socketPeer.getOutputStream(), true);

                            osPeer.println("send " + userFile.name);

                            String fileContent = isPeer.readLine();

                            this.ftw.setDownloadStatusMessage("finished -" + userFile.name);

                            System.out.println("Downloaded the file with: " + fileContent);
                            this.ftw.getEvent().setText("Downloaded \"" + userFile.name + "\"");

                            File dirs = new File("Client\\" + name);
                            if (!dirs.exists()) {
                                dirs.mkdirs();
                            }

                            try (FileOutputStream out = new FileOutputStream("Client\\" + name + "\\" + userFile.name)) {
                                out.write(fileContent.getBytes());
                            }
                        } else {
                            this.ftw.setDownloadStatusMessage("unfinished");
                            if (userFile == null) {
                                this.ftw.getEvent().setText("Find a file first!");
                            } else if (!userFile.isOnline) {
                                this.ftw.getEvent().setText("Other user is offline!");
                            }
                        }
                    }
                } else {

                    if (line.toLowerCase().contains("delete")) {
                        os.println(line + " " + fromGUI.get(1));

                        this.ftw.getEvent().setText("You deleted user " + fromGUI.get(1));
                        System.out.println("You deleted " + fromGUI.get(1) + ".");
                    } else if (line.toLowerCase().equals("log out")) {
                        this.ftw.setLoggedOut();

                        loggedIn = false;
                        os.println(line);

                        continue;
                    }
                }

                System.out.print(name + ", send a command to Server (or quit to end): ");
            }

        } catch (IOException e) {
            System.out.println("Socket read Error");
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, e);
        } finally {

            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
            if (br != null) {
                br.close();
            }
            if (osPeer != null) {
                osPeer.close();
            }
            if (socket != null) {
                socket.close();
            }
            System.out.println("Connection Closed");
        }
    }

    private static class ClientServer extends Thread {

        Socket s = null;
        Client client = null;

        public ClientServer(Socket s) {
            this.s = s;
        }

        public ClientServer(Client client) {
            this.client = client;
        }

        @Override
        public void run() {
            if (client != null) {
                try {
                    client.runClient();
                } catch (IOException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    private static class ClientListener extends Thread {

        Integer listenerPort;
        Socket socket = null;
        ServerSocket clientListener = null;
        Boolean loggedIn;
        BufferedReader is1 = null;
        PrintWriter os1 = null;

        public ClientListener(Integer listenerPort, Boolean loggedIn) {
            this.listenerPort = listenerPort;
            this.loggedIn = loggedIn;
        }

        @Override
        public void run() {

            try {

                clientListener = new ServerSocket(listenerPort);

                while (loggedIn) {
                    try {

                        socket = clientListener.accept();

                        System.out.println("Connection established...");

                        is1 = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        os1 = new PrintWriter(socket.getOutputStream(), true);

                        String line = is1.readLine();

                        System.out.print("I got this: " + line + ". Press Enter to continue...");

                        if (line.contains("send")) {
                            String[] command = line.split(" ");

                            String fileName = command[1];

                            FileInputStream fis = null;
                            BufferedInputStream bis = null;

                            File file = new File("Client\\" + username + "\\" + fileName);

                            long size = file.length();

                            byte[] myByteArray = new byte[(int) size];
                            fis = new FileInputStream(file);
                            bis = new BufferedInputStream(fis);
                            bis.read(myByteArray, 0, myByteArray.length);

                            String s = new String(myByteArray);

                            // send file now
                            os1.println(s);
                            //Integer port = socket.getPort();
                        }

                    } catch (IOException ex) {
                        // no need to handle it
                    }
                }
            } catch (IOException ex) {
                // no need to handle it
            } finally {
                try {
                    System.out.println((char) 27 + "[38;42m ClientListener is stopping...");
                    if (is1 != null) {
                        is1.close();
                        System.out.println("Socket Input Stream Closed.");
                    }

                    if (os1 != null) {
                        os1.close();
                        System.out.println("Socket Output Stream Closed.");
                    }

                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

    }

    public static void main(String args[]) throws IOException {
        Client client = new Client();

        ClientServer c = new ClientServer(client);

        c.start();

        try {
            client.ftw.join();
            c.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
