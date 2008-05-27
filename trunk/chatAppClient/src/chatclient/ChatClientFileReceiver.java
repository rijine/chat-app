/*
 * ChatClientFileReceiver.java
 */

package chatclient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatClientFileReceiver extends Thread{
    private Socket connectionSocket = null; 
    private DataInputStream inFromPeer;
    private DataOutputStream outToPeer; 
    private BufferedReader inFromPeerMetaData;
    String targetpath; 
    
    ChatClientFileReceiver(Socket accept, String path) {
        super("ChatClientFileReceiver"); 
        connectionSocket = accept; 
        targetpath = path; 
    }
    
    @Override
    public void run() {
        System.out.println("Receiver socket........"); 
        try {
            outToPeer = new DataOutputStream(connectionSocket.getOutputStream());
            inFromPeerMetaData = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            String metadata = inFromPeerMetaData.readLine();
            
            inFromPeer = new DataInputStream(new BufferedInputStream(connectionSocket.getInputStream()));
            String filename = metadata.substring(0,metadata.indexOf("/"));
            ChatClientView.addMessage("File " + filename + " was offered to you, for free! "); 
            int fileSize = Integer.parseInt(metadata.substring(metadata.indexOf("/")+1));
            byte file[] =new byte[fileSize];
            // ask for user input, do you want to accept? popup or whatever... 
            outToPeer.writeBytes("ACK\n"); // send ACK toindicate we're ready to receive data. (probably should ask the user first). send any other string to indicate refuse. 
            inFromPeer.readFully(file); // store file in "file".
            // now we have to store it on our harddrive.. 
            
            // check if targetpath exists
            File path = new File(targetpath);
            if (!path.exists())
                path.mkdir(); 
            
            BufferedOutputStream bus=new BufferedOutputStream(new FileOutputStream(new File(targetpath+"/"+filename)));
            bus.write(file,0,file.length);
            bus.close();
            ChatClientView.addMessage("File " + filename + " successfully received. "); 
            
            inFromPeerMetaData.close();
            outToPeer.close(); 
            inFromPeer.close(); 
            connectionSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(ChatClientFileReceiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
