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


/**
 *
 * @author kevin
 */
public class ServerThread extends Thread {
    private Socket connectionSocket = null; 
    private Connection connectToMysql; 
    public ServerThread(Socket socket, Connection sqlConnection) { // constructor
        super("ServerThread"); 
        connectToMysql = sqlConnection; 
        connectionSocket = socket; 
    }
    @Override
    public void run() {
        String query;
        String reply = null;
        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            query = inFromClient.readLine();
            Statement sendSQLQuery = null;
            sendSQLQuery = connectToMysql.createStatement();
            ResultSet results = null; 
            if (query.startsWith("CHEK:")) {
                // checks database for stuff
                String parseQuery = query;
                parseQuery = parseQuery.substring("CHEK:".length());
                if (parseQuery.startsWith("nickname:")) {
                    parseQuery = parseQuery.substring("nickname:".length());
                    sendSQLQuery.executeQuery("SELECT nickname FROM user WHERE nickname = '" + parseQuery.toLowerCase() + "';");
                    results = sendSQLQuery.getResultSet();
                    boolean found = results.next();
                    if (found)
                        reply = "AlreadyExists\n";
                    else
                        reply = "Available\n";
                    outToClient.writeBytes(reply);
                }
                else if (parseQuery.startsWith("username:")) {
                    parseQuery = parseQuery.substring("username:".length());
                    sendSQLQuery.executeQuery("SELECT username FROM user WHERE username = '" + parseQuery.toLowerCase() + "';");
                    results = sendSQLQuery.getResultSet();
                    boolean found = results.next();
                    if (found)
                        reply = "AlreadyExists\n";
                    else
                        reply = "Available\n";
                    outToClient.writeBytes(reply);
                }
                else {
                    // some error about not knowing what's after the check
                }
                results.close();
                sendSQLQuery.close();
                inFromClient.close(); 
                outToClient.close(); 
                connectionSocket.close();
                return; 
            } // end check
            else if (query.startsWith("NEWA:")) {
                String parseQuery = query;
                parseQuery = parseQuery.substring("NEWA:".length());
                int i = parseQuery.indexOf(",");
                String outQuery = "INSERT into user(username, email, fname, lname, password, nickname) Values('";

                outQuery += parseQuery.substring(0, i)+"', '"; // get username
                parseQuery = parseQuery.substring(i+1); // remove username
                i = parseQuery.indexOf(","); // update comma location

                outQuery += parseQuery.substring(0, i)+"', '"; // get email
                parseQuery = parseQuery.substring(i+1); // remove email
                i = parseQuery.indexOf(","); // update comma location

                outQuery += parseQuery.substring(0, i)+"', '"; // get fname
                parseQuery = parseQuery.substring(i+1); // remove fname
                i = parseQuery.indexOf(","); // update comma location

                outQuery += parseQuery.substring(0, i)+"', '"; // get lname
                parseQuery = parseQuery.substring(i+1); // remove lname
                i = parseQuery.indexOf(","); // update comma location

                outQuery += parseQuery.substring(0, i)+"', '"; // get password
                parseQuery = parseQuery.substring(i+1); // remove password
                i = parseQuery.indexOf(","); // update comma location  

                outQuery += parseQuery+"');"; // get nickname
                
                /*
                System.out.println(parseQuery); 
                System.out.println(outQuery); 
                if (parseQuery.isEmpty()) 
                    System.out.println("parse should be correct... (debug info) "); 
                */
                sendSQLQuery.execute(outQuery); 
                // check if query was executed succesfully and give feedback... 
                // outToClient.writeBytes("feedback_here..."); 
                // we decided this is not necessary. 
                sendSQLQuery.close();
                inFromClient.close(); 
                outToClient.close(); 
                connectionSocket.close();
                return; 
            } // end new account 
            //reply = query.toUpperCase() + '\n';
            if(query.equalsIgnoreCase("DISC")){
               // we have to do sql query that we're not online anymore. connectionSocket.close() has to be done anyway. 
               System.out.println("Connection Socket Quitting");
            } 
            else {
                //outToClient.writeBytes(reply);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        
    } // end run
}
