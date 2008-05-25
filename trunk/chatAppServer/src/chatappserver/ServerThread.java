/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package chatappserver;

import java.sql.*;
import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            boolean found = results.next();
            if (found) {
                reply = "ERR1\n"; // user is already logged in
            } else {
                // check password
                sendSQLQuery.executeQuery("SELECT username FROM user WHERE username = '" + parseQuery + "' AND password = '" + query.substring(query.indexOf(",")+1) + "';");
                results = sendSQLQuery.getResultSet();
                boolean validCredentials = results.next(); 
                if (!validCredentials) {
                    // either username doesn't exist, or the password is invalid..
                    reply = "ERR2\n"; 
                }
                else {
                    reply = "SUCC\n";
                    // this will be done in chat now: sendSQLQuery.execute("INSERT into threadlookup(username, threadid) Values('"+parseQuery+"', '"+this.getId()+"');"); // register this thread with this username. 
                }
            }
            outToClient.writeBytes(reply);
            /*
             we're now in the chatting section... 
             */
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

    private void updateList(String query) {
        try {
            Statement sendSQLQuery = null;
            String reply = "/UPDA:";
            sendSQLQuery = connectToMysql.createStatement();
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            query = "chan_" + query.substring("UPDA:".length());
            ResultSet results = null;
            sendSQLQuery.executeQuery("SELECT * FROM " + query + ";");
            results = sendSQLQuery.getResultSet();
            String usernames = "";
            String processUname = "";
            // Get the list of all the usernames
            while (results.next()) {
                usernames += results.getString("usernames").toLowerCase() + ",";
            }
            // Convert usernames to nicknames
            while (!usernames.isEmpty()) {
                processUname = usernames.substring(0, usernames.indexOf(","));
                usernames = usernames.substring(usernames.indexOf(",") + 1);
                if (!processUname.isEmpty()) {
                    sendSQLQuery.executeQuery("SELECT nickname FROM user WHERE username = '" + processUname + "';");
                    results = sendSQLQuery.getResultSet();
                    if (results.next())
                        reply += results.getString("nickname") + ",";
                }
                else 
                    break;
            }
            results.close();
            outToClient.writeBytes(reply + "\n");
            outToClient.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void chat(String username) throws SQLException {
        Statement sendSQLQuery = null;
        sendSQLQuery = connectToMysql.createStatement();
        BufferedReader inFromClient = null;
        
        // register this thread with this username. 
        sendSQLQuery.execute("INSERT into threadlookup(username, threadid) Values('"+username+"', '"+this.getId()+"');");
        // Insert the user into the channel
        sendSQLQuery.execute("INSERT into chan_main(usernames) Values('"+username+"');");
        
        ResultSet results = null;
        String nickname = null;
        sendSQLQuery.executeQuery("SELECT nickname FROM user WHERE username = '" + username.toLowerCase() + "';");
        results = sendSQLQuery.getResultSet();
        if (results.next())
            nickname = results.getString("nickname");
        results.close();
                    
        try {
            inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            String message;
            while (true) { 
                message = inFromClient.readLine();  // get new message
                if  (message.startsWith("-")) {
                    message = message.substring(1);
                    // broadcast message to others.
                    outToClient.writeBytes(nickname + ": " + message + "\n");
                }
                else { // message is a special command, treat it accordingly. 
                    if (message.equalsIgnoreCase("/DISC")) {
                        sendSQLQuery.execute("DELETE FROM threadlookup WHERE threadid = '"+ this.getId() +"';");
                        sendSQLQuery.execute("DELETE FROM chan_main WHERE usernames = '"+ username +"';");
                        break; 
                    }
                    else if (message.equalsIgnoreCase("/PING")) {
                    }
                    else if (message.equalsIgnoreCase("/WHOIS")) {
                    }
                    else if (message.equalsIgnoreCase("/TIME")) {
                    }
                    else {
                        // invalid command
                        System.out.println("Invalid command. "); 
                    }
                }
            }
            outToClient.close();
            System.out.println("Disconnecting..."); // debug
            //outToClient.writeBytes("DISC"); // tell client we're discing
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                inFromClient.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @Override
    public void run() {
        try {
            String query;
            try {
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                query = inFromClient.readLine();
                // reset timer back to 5. 
                if (query.startsWith("CHEK:")) { // check for duplicates
                    check(query);
                } else if (query.startsWith("NEWA:")) { // new account 
                    newaccnt(query);
                } else if (query.startsWith("LOGN:")) { // login
                    login(query); 
                } else if (query.startsWith("UPDA:")) { // update user list
                    updateList(query); 
                } else if (query.startsWith("CHAT:")) { // chat session initiated
                    chat(query.substring("CHAT:".length()));
                } else {
                    System.out.println("invalid code... ");
                }
                inFromClient.close(); 
            } catch (SQLException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            connectionSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    } // end run
}
