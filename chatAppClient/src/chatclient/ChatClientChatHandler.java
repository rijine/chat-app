/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
    
    static void connect(String username) throws FileNotFoundException, IOException{
        String hostname;
        int port;

        BufferedReader inputStream = new BufferedReader(new FileReader("settings.ini"));
        hostname = inputStream.readLine();
        port = Integer.parseInt(inputStream.readLine());
        inputStream.close();
        
        startServerWorker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                listen();
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        startServerWorker.execute();
        
        clientSocket = new Socket(hostname, port);
        outToServer = new DataOutputStream(clientSocket.getOutputStream()); 
        outToServer.writeBytes("CHAT:"+username+"\n");
        connected = true; 
    }
    
    static void listen() throws IOException {
        System.out.println("hi1"); 
        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 
        System.out.println("hi2"); 
        System.out.println("received this: "+inFromServer.readLine()); 
    }
    
    static void send(String message) throws IOException {
        if (!connected)  {
            System.out.println("client socket didn't connect for a chat session. ");
            return; 
        }
        outToServer.writeBytes(message);
    }
    
    static void disconnect() {
        try {
            if (clientSocket != null && clientSocket.isConnected()) {
                send("/DISC");
                clientSocket.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(ChatClientChatHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
