/*
 * ChatClientChatHandler.java
 * This class handles data transfer on the client side, after a perma-connection was established (ie a chat session) 
 * All of its members are static members, since we won't need object instances of this class at all. (one per client). 
 */
package chatclient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class ChatClientChatHandler {
    static Socket clientSocket = null; 
    static DataOutputStream outToServer; 
    static BufferedReader inFromServer;
    static boolean connected = false; 
    static private SwingWorker startServerWorker;
    static private SwingWorker FileReceiverWorker;
    static public String fileLocation; 
    static private boolean bSendingFile = false; 
    /* This function is called when a chat session is established. 
     * takes the username as a parameter, which will be sent to the server so that it registers this socket with this username. 
     * (we worked on username not on IP, since users could be behind NAT) 
     */
    static void connect(String username) throws FileNotFoundException, IOException{
        String hostname;
        int port;

        BufferedReader inputStream = new BufferedReader(new FileReader("settings.ini"));
        hostname = inputStream.readLine();
        port = Integer.parseInt(inputStream.readLine());
        inputStream.close();
        
        // we want to run a background thread listening for input from the server. This will be text other users have sent. 
        startServerWorker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                listen();
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        FileReceiverWorker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                fileListener(); 
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        clientSocket = new Socket(hostname, port);
        outToServer = new DataOutputStream(clientSocket.getOutputStream()); // tell server i want to establish a persistent connection
        outToServer.writeBytes("CHAT:"+username+"\n");
        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 
        connected = true; 
        FileReceiverWorker.execute(); 
        startServerWorker.execute();
    }
    
    /*
     * This function is called on connection. It runs as a background thread, listening on a port for incoming file transfers. 
     * If a file transfer is requested, it calls ChatClientFileReceiver, which is also run in the background as a thread. 
     */
    static void fileListener() {
        int port = 0; 
        String targetpath; 
        try {
            BufferedReader inputStream = new BufferedReader(new FileReader("settings.ini"));
            inputStream.readLine(); 
            port = Integer.parseInt(inputStream.readLine())+1; // let the listening port be the same port that the server port is. 
            targetpath = "RECEIVED_FILES";                                    // hardcoded for now 
            inputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(ChatClientChatHandler.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Could not find port to listen on..."); 
            return; 
        }
        
        ServerSocket welcomeSocket=null;
        try {
            welcomeSocket = new ServerSocket(port);
        } catch (IOException ex) {
            System.out.println("Could not listen on port: "+String.valueOf(port)); 
            ex.printStackTrace();
        }
        while(connected) {
            try {
                new ChatClientFileReceiver(welcomeSocket.accept(), targetpath).start();
            } catch (IOException ex) {
                System.out.println("Could not accept connection on port "+String.valueOf(port)); 
                Logger.getLogger(ChatClientChatHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /* This function takes no parameters. It simply listens from input from the server. This input will be either messages sent by other users, or 
     * other data that the server might be sending, in form of ping replies, whois replies, etc.. 
     */
    static void listen() throws IOException {
        while (connected) { // some condition that makes us stop listening -- It doesn't really happens, since only way for this to happen is the socket closes. essentially, while true. 
            String message = inFromServer.readLine(); // when socket closes, this will throw an exception? 
            
            //// we have to add something to check if data is simple chat data, or special commands.. (similar to what we did in server side -- we might have to also append '-' to msgs on the server side when sending) 
            if (message.startsWith("/")) { // if server sent a special command 
                if (message.startsWith("/SEND")) {
                    if (bSendingFile) {
                        if (message.substring("/SEND ".length()).startsWith("ERR")) {
                            ChatClientView.txtMessages.setText(ChatClientView.txtMessages.getText() + "User " + message.substring(message.indexOf("ERR")+3) + "is not logged in. \n");
                        }
                        else { // send file 
                            final InetAddress IP = InetAddress.getByName(message.substring("/SEND ".length()+1)); // IP now holds the IP address of the person to send to. 
                            // we have to create a new thread to send the data through. 
                            SwingWorker fileSender = new SwingWorker() {
                                @Override
                                protected Object doInBackground() throws Exception {
                                    new ChatClientFileSender(IP, fileLocation); 
                                    throw new UnsupportedOperationException("Not supported yet.");
                                }
                            };
                            fileSender.execute();
                        }
                        bSendingFile = false; 
                    }
                    else // this won't happen anymore... 
                        ChatClientView.txtMessages.setText(ChatClientView.txtMessages.getText() + "\nYou can only send one file at a time\n\n"); // deprecated
                }
                // if server sends /UPDA:, update the user list
                else if (message.startsWith("/UPDA:")) {
                    message = message.substring("/UPDA:".length());
                    String nickname = "";
                    DefaultTableModel model = (DefaultTableModel) ChatClientView.tblUsers.getModel();
                    while (model.getRowCount() > 0)
                        model.removeRow(0);
                    while (!message.isEmpty()) {
                        nickname = message.substring(0, message.indexOf(","));
                        message = message.substring(message.indexOf(",") + 1);
                        if (!nickname.isEmpty()) {
                            model.insertRow(ChatClientView.tblUsers.getRowCount(),new Object[]{null,nickname});
                            ChatClientView.tblUsers.setModel(model);
                        }
                    }
                }
                else if (message.startsWith("/NICK:")) {
                    message = message.substring("/NICK:".length());
                    if (message.startsWith("/EXISTS"))
                        ChatClientView.txtMessages.setText(ChatClientView.txtMessages.getText() + "Nickname already exists.\n");
                }
                else if (message.startsWith("/WHOIS:")) {
                    message = message.substring("/WHOIS:".length());
                    ChatClientView.txtMessages.setText(ChatClientView.txtMessages.getText() + message + "\n");
                }
                else if (message.startsWith("/MSG: ")) {
                    String msg = message.substring("/MSG: ".length());
                    System.out.println(msg);
                    boolean isIgnored = false;
                    File checkFile = new File ("ignore.ini");
                    if (checkFile.exists()) {
                        BufferedReader inputStream = new BufferedReader(new FileReader("ignore.ini"));
                        String entry = null;
                        while ((entry = inputStream.readLine()) != null && !isIgnored) {
                            if (msg.indexOf(",") != -1) {
                                if (entry.equals(msg.substring(0, msg.indexOf(","))))
                                    isIgnored = true;
                            }
                        }
                        inputStream.close();
                    }
                    if (!isIgnored)
                        ChatClientView.txtMessages.setText(ChatClientView.txtMessages.getText() + "Received a PM from " + msg + "\n");
                }
                else 
                    System.out.println("Invalid code sent by server..."); // this should never happen since the server should always send valid codes. 
            }
            else {// server sent the client a text message by a user in a channel this user is in. 
                boolean isIgnored = false;
                File checkFile = new File ("ignore.ini");
                if (checkFile.exists()) {
                    BufferedReader inputStream = new BufferedReader(new FileReader("ignore.ini"));
                    String entry = null;
                    while ((entry = inputStream.readLine()) != null && !isIgnored) {
                        if (message.indexOf(":") != -1) {
                            if (entry.equals(message.substring(0, message.indexOf(":"))))
                                isIgnored = true;
                        }
                    }
                    inputStream.close();
                }
                if (!isIgnored)
                    ChatClientView.txtMessages.setText(ChatClientView.txtMessages.getText() + message + "\n");
            }
        }
    }
    
    /* This function simply sends the String provided as the only parameter to the server. 
     */
    static void send(String message) throws IOException {
        if (!connected)  {
            System.out.println("client socket didn't connect for a chat session. "); // we can't send data if chat session wasn't established. 
            return; 
        }
        if (message.toUpperCase().startsWith("/SEND ")) { // special case, we need to save the filename, so that when the server replies with the target address, we would still remember what to send. 
            if (message.indexOf(" ", "/SEND ".length()) == -1) { // user put: "/send nick", without specifying a filename
                ChatClientView.txtMessages.setText(ChatClientView.txtMessages.getText() + "You didn't specify which file to send. \n"); 
                bSendingFile = false;
                return; 
            }
            if (bSendingFile) { // since we can only remember one location, we can't send another file beofre the first one establishes a connection with its target destination. 
                ChatClientView.txtMessages.setText(ChatClientView.txtMessages.getText() + "Cannot send files this fast...\n");
                return;
            }
            bSendingFile = true;
            fileLocation = message.substring(message.indexOf(" ","/SEND ".length()+1)+1, message.length()-1); // store the file location of the file we want to send.
            String username = message.substring("/SEND ".length(), message.indexOf(" ", "/SEND ".length()));
            outToServer.writeBytes("/SEND "+username+'\n'); // send to server /SEND username
        }
        else if (message.toUpperCase().startsWith("/MSG ")) {
            String msg = message.substring("/MSG ".length());
            int rows[] = ChatClientView.tblUsers.getSelectedRows(); 
            if (rows.length == 0) {
                ChatClientView.txtMessages.setText(ChatClientView.txtMessages.getText() + "You have not selected anyone\n");
                return;
            }
            String toSend = "/MSG "+String.valueOf(rows.length)+" "; // attach number of people we're sending the message to. 
            
            for (int i = 0; i < rows.length; i++) {
                toSend += ChatClientView.tblUsers.getValueAt(rows[i], 1)+" ";
            }
            toSend = toSend.substring(0, toSend.length() - 1);
            toSend += ", "+msg; 
            outToServer.writeBytes(toSend); 
        }
        else { // other text
            try {
                outToServer.writeBytes(message);
            } catch (IOException ex) {
                System.out.println("Could not send the data message to the server..."); 
                Logger.getLogger(ChatClientChatHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
    }
    
    /* This function takes no parameters, and tells the server that the client is disconnecting.  
     */
    static void disconnect() {
        try {
            if (clientSocket != null && clientSocket.isConnected()) {
                send("/DISC");
                connected = false; 
                // inFromServer.close(); 
                outToServer.close(); 
                clientSocket.close();
            }
            else 
                System.out.println ("disconnecting is unecessary... we're already disconnected..."); 
        } catch (IOException ex) {
            Logger.getLogger(ChatClientChatHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
