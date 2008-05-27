/*
 * FileSender.java
 */

package chatclient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatClientFileSender {
    private Socket senderSocket = null; 
    private DataOutputStream outToPeer; 
    private BufferedReader inFromPeer;
    private InetAddress receiverIP; 
    private int receiverPort; 
    
    ChatClientFileSender(InetAddress IP, String filename) {
        BufferedReader inputStream = null;
        try {
            inputStream = new BufferedReader(new FileReader("settings.ini"));
            inputStream.readLine(); 
            receiverIP = IP;
            receiverPort = Integer.parseInt(inputStream.readLine())+1;
            inputStream.close();
            
            System.out.println("filename is: "+filename); 
            File f = new File(filename);
            System.out.println(f.length());
            System.out.println(f.getPath());
            byte[] fileArray = new byte[(int)f.length()];
            
            if (!f.exists()) {
                ChatClientView.addMessage("File not found"); 
                return; 
            }
            
            senderSocket = new Socket(receiverIP, receiverPort); 
            outToPeer = new DataOutputStream(senderSocket.getOutputStream());
            inFromPeer = new BufferedReader(new InputStreamReader(senderSocket.getInputStream()));
            
            BufferedInputStream buffStream = new BufferedInputStream(new FileInputStream(f));
            buffStream.read(fileArray,0,fileArray.length); // fileArray now holds the contents of the file. . 
            
            outToPeer.writeBytes(filename+"/"+Integer.toString(fileArray.length) + '\n');     // tell target what the filename and the length are. 
            String ready = inFromPeer.readLine();  // wait for target to request start of transfer
            if (ready.equals("ACK")) { // client accepted the request 
                ChatClientView.addMessage("Sending"+ filename + "("+fileArray.length+" bytes)"); 
                outToPeer.write(fileArray,0,fileArray.length); // send the file to the target. 
                outToPeer.flush(); // just in case
                ChatClientView.addMessage("File " + filename + " successfully sent. "); 
            } 
            else 
                ChatClientView.addMessage("File " + filename + " peer refused file transfer. "); 
            buffStream.close();
            outToPeer.close();
            inFromPeer.close(); 
            senderSocket.close(); 
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ChatClientFileSender.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println("could not establish connection on port: "+receiverPort);
            Logger.getLogger(ChatClientFileSender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
