/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package chatappserver;

import java.sql.*;
import java.net.*;
import java.io.*;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * LOGN - login
CHEK - check something (username available? nickname in use?)
NEWA - new account
CHAT - start chat

You have to be in "CHAT" mode to use these, they should be sent with a '/' preceding them: /DISC etc.
/DISC - Logout
/PING - pings the user
/WHOI - whois on user
/TIME - returns the time on the user's end.

ctrl+enter = disregard if a message starts with '/', just add another '/' so:
/hi
 * */
public class ServerThread extends Thread {
    private Socket connectionSocket = null; 
    private Connection connectToMysql; 
    public ServerThread(Socket socket, Connection sqlConnection) { // constructor
        super("ServerThread"); 
        connectToMysql = sqlConnection; 
        connectionSocket = socket; 
    }
    
    private void check(String query) {
        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            // checks database for dupliates
            String parseQuery = query;
            ResultSet results = null;
            String reply = null;
            Statement sendSQLQuery = null;
            sendSQLQuery = connectToMysql.createStatement();
            
            parseQuery = parseQuery.substring("CHEK:".length());
            if (parseQuery.startsWith("nickname:")) {
                // checks for duplicate nicknames
                try {
                    // checks for duplicate nicknames
                    parseQuery = parseQuery.substring("nickname:".length());
                    sendSQLQuery.executeQuery("SELECT nickname FROM user WHERE nickname = '" + parseQuery.toLowerCase() + "';");
                    results = sendSQLQuery.getResultSet();
                    boolean found = results.next();
                    if (found) {
                        reply = "AlreadyExists\n";
                    } else {
                        reply = "Available\n";
                    }
                    outToClient.writeBytes(reply);
                } catch (IOException ex) {
                    Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SQLException ex) {
                    Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (parseQuery.startsWith("username:")) {
                // check for duplicate usernames
                try {
                    // check for duplicate usernames
                    parseQuery = parseQuery.substring("username:".length());
                    sendSQLQuery.executeQuery("SELECT username FROM user WHERE username = '" + parseQuery.toLowerCase() + "';");
                    results = sendSQLQuery.getResultSet();
                    boolean found = results.next();
                    if (found) {
                        reply = "AlreadyExists\n";
                    } else {
                        reply = "Available\n";
                    }
                    outToClient.writeBytes(reply);
                } catch (IOException ex) {
                    Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SQLException ex) {
                    Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                
            }
            results.close();
            sendSQLQuery.close();
            inFromClient.close();
            outToClient.close();
            return;
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    } // end check 
    
    private void newaccnt(String query) {
        try {
            Statement sendSQLQuery = null;
            sendSQLQuery = connectToMysql.createStatement();
            String parseQuery = query;
            parseQuery = parseQuery.substring("NEWA:".length());
            int i = parseQuery.indexOf(",");
            String outQuery = "INSERT into user(username, email, fname, lname, password, nickname) Values('";

            outQuery += parseQuery.substring(0, i) + "', '"; // get username
            parseQuery = parseQuery.substring(i + 1); // remove username
            i = parseQuery.indexOf(","); // update comma location
            outQuery += parseQuery.substring(0, i) + "', '"; // get email
            parseQuery = parseQuery.substring(i + 1); // remove email
            i = parseQuery.indexOf(","); // update comma location
            outQuery += parseQuery.substring(0, i) + "', '"; // get fname
            parseQuery = parseQuery.substring(i + 1); // remove fname
            i = parseQuery.indexOf(","); // update comma location
            outQuery += parseQuery.substring(0, i) + "', '"; // get lname
            parseQuery = parseQuery.substring(i + 1); // remove lname
            i = parseQuery.indexOf(","); // update comma location
            outQuery += parseQuery.substring(0, i) + "', '";
            parseQuery = parseQuery.substring(i + 1); // remove password
            i = parseQuery.indexOf(","); // update comma location
            outQuery += parseQuery + "');"; // get nickname
            sendSQLQuery.execute(outQuery);

            // outToClient.writeBytes("feedback_here...");
            // we decided this is not necessary.
            sendSQLQuery.close();
            return;
        } catch (SQLException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    } // end new account 
    
    private void login(String query) {
        Statement sendSQLQuery = null;
        String reply = null;
        String parseQuery = query; 
        try {
            sendSQLQuery = connectToMysql.createStatement();
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            parseQuery = query.substring("LOGN:".length(),query.indexOf(",")); // holds username
            ResultSet results = null;
            sendSQLQuery.executeQuery("SELECT username FROM threadlookup WHERE username = '" + parseQuery + "';");
            results = sendSQLQuery.getResultSet();
            boolean login = false; 
            boolean found = results.next();
            if (found) {
                reply = "ERR1\n"; // user is already logged in
            } else {
                // check password
                sendSQLQuery.executeQuery("SELECT username FROM user WHERE username = '" + parseQuery + "' AND password = '" + query.substring(query.indexOf(",")) + "';");
                boolean validCredentials = results.next(); 
                if (!validCredentials) {
                    // either username doesn't exist, or the password is invalid..
                    reply = "ERR\n"; 
                }
                else {
                    login = true; 
                    reply = "SUCC\n";
                    sendSQLQuery.execute("INSERT into threadlookup(username, threadid) Values('"+query+"', '"+this.getId()+"');"); // register this thread with this username. 
                    // we won't close the thread anymore until the user disconnects. 
                }
            }
            outToClient.writeBytes(reply);
            /*
             we're now in the chatting section... 
             */
            
            
            if (login) {
                while (!inFromClient.readLine().equals("/DISC")) {
                    System.out.println("not disconnected! "); 

                }
                
                outToClient.writeBytes("DISC");  // tell cleint we're discing
                System.out.println("Disconnecting..."); // debug
            }
            
            inFromClient.close(); 
            outToClient.close(); 
            sendSQLQuery.close(); 
            results.close(); 
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run() {
        try {
            String query;
            try {
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                query = inFromClient.readLine();
                if (query.startsWith("CHEK:")) { // check for duplicates
                    check(query);
                } else if (query.startsWith("NEWA:")) { // new account 
                    newaccnt(query);
                } else if (query.equalsIgnoreCase("LOGN")) { // login
                    login(query); 
                } else {
                    // error, invalid code. 
                }
                inFromClient.close(); 
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            connectionSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    } // end run
}
