/*
 * ChatClientChatHandler.java
 * This class handles data transfer on the client side, after a perma-connection was established (ie a chat session) 
 * All of its members are static members, since we won't need object instances of this class at all. (one per client). 
 */
package chatclient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

public class ChatClientChatHandler {
    static Socket clientSocket = null; 
    static DataOutputStream outToServer; 
    static BufferedReader inFromServer;
    static boolean connected = false; 
    static private SwingWorker startServerWorker;
    
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
        clientSocket = new Socket(hostname, port);
        outToServer = new DataOutputStream(clientSocket.getOutputStream()); 
        outToServer.writeBytes("CHAT:"+username+"\n");
        connected = true; 
        startServerWorker.execute();
    }
    
    /* This function takes no parameters. It simply listens from input from the server. This input will be either messages sent by other users, or 
     * other data that the server might be sending, in form of ping replies, whois replies, etc.. 
     */
    static void listen() throws IOException {
        while (connected) { // some condition that makes us stop listening -- It doesn't really happens, since only way for this to happen is the socket closes. essentially, while true. 
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // when socket closes, this will throw and exception? 
            //// we have to add something to check if data is simple chat data, or special commands.. (similar to what we did in server side -- we might have to also append '-' to msgs on the server side when sending) 
            ChatClientView.txtMessages.setText(ChatClientView.txtMessages.getText() + inFromServer.readLine() + "\n");
        }
        inFromServer.close(); 
    }
    
    /* This function simply sends the String provided as the only parameter to the server. 
     */
    static void send(String message) throws IOException {
        if (!connected)  {
            System.out.println("client socket didn't connect for a chat session. "); // we can't send data if chat session wasn't established. 
            return; 
        }
        outToServer.writeBytes(message);
    }
    
    /* This function takes no parameters, and tells the server that the client is disconnecting.  
     */
    static void disconnect() {
        try {
            if (clientSocket != null && clientSocket.isConnected()) {
                connected = false; 
                send("/DISC");
                outToServer.close(); 
                clientSocket.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(ChatClientChatHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
