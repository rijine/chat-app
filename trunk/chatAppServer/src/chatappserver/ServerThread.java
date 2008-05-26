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
            
            String username = parseQuery.substring(0, i);

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

            ChatAppServerView.txtDebug.setText(ChatAppServerView.txtDebug.getText() + username + " has just been created.\n");
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
                    ChatAppServerView.txtDebug.setText(ChatAppServerView.txtDebug.getText() + parseQuery + " has logged in.\n");
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
                    results.next();
                    reply += results.getString("nickname") + ",";
                }
                else 
                    break;
            }
            results.close();
            outToClient.writeBytes(reply + "\n");
            //outToClient.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void chat(String username) throws SQLException, IOException {
        Statement sendSQLQuery = null;
        sendSQLQuery = connectToMysql.createStatement();
        BufferedReader inFromClient = null;
        
        this.setName(username);
        ChatAppServerView.llThreads.add(this, this.getId());

        // register this thread with this username. 
        sendSQLQuery.execute("INSERT into threadlookup(username, threadid, ip) Values('"+username+"', '"+this.getId()+ "', '"+ connectionSocket.getInetAddress() +"');");
        // Insert the user into the channel
        sendSQLQuery.execute("INSERT into chan_main(usernames) Values('"+username+"');");
        ChatAppServerView.txtDebug.setText(ChatAppServerView.txtDebug.getText() + username + " is in chat.\n");
        
        // Update UserList from server button
        ChatAppServerUserList.updateUserList(connectToMysql);
        
        ResultSet results = null;
        String nickname = null;
        
        sendSQLQuery.execute("UPDATE user SET loggedin = '1' WHERE username = '" + username + "';");
        
        // On login update everyone's user list
        sendSQLQuery.executeQuery("SELECT * FROM chan_main;");
        results = sendSQLQuery.getResultSet();
        String usernames = "";
        String processUname = "";
        // Get the list of all the usernames
        while (results.next()) {
            usernames += results.getString("usernames").toLowerCase() + ",";
        }
        
        // Conver username to nickname
        sendSQLQuery.executeQuery("SELECT nickname FROM user WHERE username = '" + username.toLowerCase() + "';");
        results = sendSQLQuery.getResultSet();
        if (results.next())
            nickname = results.getString("nickname");
        results.close();
        
        // Get threadid of each user, update the userlist and broadcast that a user joined the channel
        while (!usernames.isEmpty()) {
            processUname = usernames.substring(0, usernames.indexOf(","));
            usernames = usernames.substring(usernames.indexOf(",") + 1);
            if (!processUname.isEmpty()) {
                sendSQLQuery.executeQuery("SELECT threadid FROM threadlookup WHERE username = '" + processUname + "';");
                results = sendSQLQuery.getResultSet();
                results.next();
                if (ChatAppServerView.llThreads.find(Long.parseLong(results.getString("threadid"))) != null) {
                    try {
                        ChatAppServerView.llThreads.find(Long.parseLong(results.getString("threadid"))).updateList("UPDA:main");
                        ServerThread.sleep(100);
                        ChatAppServerView.llThreads.find(Long.parseLong(results.getString("threadid"))).send(nickname + " has joined the channel.\n");
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
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
                    sendSQLQuery.executeQuery("SELECT * FROM chan_main;");
                    results = sendSQLQuery.getResultSet();
                    usernames = "";
                    processUname = "";
                    // Get the list of all the usernames
                    while (results.next()) {
                        usernames += results.getString("usernames").toLowerCase() + ",";
                    }
                    // Get threadid of each user
                    while (!usernames.isEmpty()) {
                        processUname = usernames.substring(0, usernames.indexOf(","));
                        usernames = usernames.substring(usernames.indexOf(",") + 1);
                        if (!processUname.isEmpty()) {
                            sendSQLQuery.executeQuery("SELECT threadid FROM threadlookup WHERE username = '" + processUname + "';");
                            results = sendSQLQuery.getResultSet();
                            results.next();
                            if (ChatAppServerView.llThreads.find(Long.parseLong(results.getString("threadid"))) != null)
                                ChatAppServerView.llThreads.find(Long.parseLong(results.getString("threadid"))).send(nickname + ": " + message + "\n");
                        }
                    }
                    results.close();   
                }
                else { // message is a special command, treat it accordingly. 
                    if (message.equalsIgnoreCase("/DISC")) {
                        sendSQLQuery.execute("DELETE FROM threadlookup WHERE threadid = '"+ this.getId() +"';");
                        sendSQLQuery.execute("DELETE FROM chan_main WHERE usernames = '"+ username +"';");
                        sendSQLQuery.execute("UPDATE user SET loggedin = '0' WHERE username = '" + username + "';");
                        sendSQLQuery.executeQuery("SELECT * FROM chan_main;");
                        // Update UserList from server button
                        ChatAppServerUserList.updateUserList(connectToMysql);
                        results = sendSQLQuery.getResultSet();
                        // Get the list of all the usernames
                        while (results.next()) {
                            usernames += results.getString("usernames").toLowerCase() + ",";
                        }

                        // Conver username to nickname
                        sendSQLQuery.executeQuery("SELECT nickname FROM user WHERE username = '" + username.toLowerCase() + "';");
                        results = sendSQLQuery.getResultSet();
                        if (results.next())
                            nickname = results.getString("nickname");
                        results.close();

                        // Get threadid of each user, update the userlist and broadcast that a user quit the channel
                        while (!usernames.isEmpty()) {
                            processUname = usernames.substring(0, usernames.indexOf(","));
                            usernames = usernames.substring(usernames.indexOf(",") + 1);
                            if (!processUname.isEmpty()) {
                                sendSQLQuery.executeQuery("SELECT threadid FROM threadlookup WHERE username = '" + processUname + "';");
                                results = sendSQLQuery.getResultSet();
                                results.next();
                                if (ChatAppServerView.llThreads.find(Long.parseLong(results.getString("threadid"))) != null) {
                                    try {
                                        ChatAppServerView.llThreads.find(Long.parseLong(results.getString("threadid"))).updateList("UPDA:main");
                                        ServerThread.sleep(100);
                                        ChatAppServerView.llThreads.find(Long.parseLong(results.getString("threadid"))).send(nickname + " has quit the channel.\n");
                                    } catch (InterruptedException ex) {
                                        Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                        }
                        results.close();
        
                        break; 
                    }
                    else if (message.toUpperCase().startsWith("/SEND ")){
                        String nick_target = message.substring("/SEND ".length());
                        sendSQLQuery.executeQuery("SELECT ip FROM threadlookup WHERE username = (SELECT username FROM user WHERE nickname = '" + nick_target + "');");
                        results = sendSQLQuery.getResultSet();
                        if (results.next()) {
                            outToClient.writeBytes("/SEND "+results.getString("ip")+'\n');
                        } else {
                            outToClient.writeBytes("/SEND ERR "+nick_target+'\n');
                        }
                    }
                    else if (message.equalsIgnoreCase("/PING")) {
                    }
                    else if (message.substring(0, "/WHOIS ".length()).equalsIgnoreCase("/WHOIS ")) {
                        String reply = null;
                        sendSQLQuery.executeQuery("SELECT * FROM user WHERE nickname = '" + message.substring("/WHOIS ".length()) + "';");
                        results = sendSQLQuery.getResultSet();
                        boolean found = results.next();
                        if (found) {
                            reply = "/WHOIS:Nickname: " + results.getString("nickname") +
                                        "\nFirst Name: " + results.getString("fname") +
                                        "\nLast Name: " + results.getString("lname") +
                                        "\nE-mail: " + results.getString("email");
                            if (results.getString("loggedin").equals("0"))
                                reply +="\nOnline: No\n"; 
                            else 
                                reply +="\nOnline: Yes\n"; 
                                        
                        } else {
                            reply = "/WHOIS:NOTFOUND\n";
                        }
                        outToClient.writeBytes(reply);     
                        results.close();
                    }
                    else if (message.equalsIgnoreCase("/TIME")) {
                    }
                    else if (message.substring(0, "/NICK ".length()).equalsIgnoreCase("/NICK ")) {
                        String reply = null;
                        sendSQLQuery.executeQuery("SELECT nickname FROM user WHERE nickname = '" + message.substring("/NICK ".length()) + "';");
                        results = sendSQLQuery.getResultSet();
                        boolean found = results.next();
                        results.close();
                        if (found) {
                            reply = "/NICK:/EXISTS\n";
                        } else {
                            reply = "/NICK:/SUCC:" + message.substring("/NICK ".length()) + "\n";
                            String old_nick = nickname;
                            ChatAppServerView.txtDebug.setText(ChatAppServerView.txtDebug.getText() + old_nick + " is now known as " + message.substring("/NICK ".length()) + ".\n");
                            sendSQLQuery.execute("UPDATE user SET nickname = '" + message.substring("/NICK ".length()) + "' WHERE nickname = '" + nickname + "';");
                            sendSQLQuery.executeQuery("SELECT * FROM chan_main;");
                            results = sendSQLQuery.getResultSet();
                            // Get the list of all the usernames
                            while (results.next()) {
                                usernames += results.getString("usernames").toLowerCase() + ",";
                            }

                            // Conver username to nickname
                            sendSQLQuery.executeQuery("SELECT nickname FROM user WHERE username = '" + username.toLowerCase() + "';");
                            results = sendSQLQuery.getResultSet();
                            if (results.next())
                                nickname = results.getString("nickname");
                            results.close();

                            // Get threadid of each user, update the userlist and broadcast that a user quit the channel
                            while (!usernames.isEmpty()) {
                                processUname = usernames.substring(0, usernames.indexOf(","));
                                usernames = usernames.substring(usernames.indexOf(",") + 1);
                                if (!processUname.isEmpty()) {
                                    sendSQLQuery.executeQuery("SELECT threadid FROM threadlookup WHERE username = '" + processUname + "';");
                                    results = sendSQLQuery.getResultSet();
                                    results.next();
                                    if (ChatAppServerView.llThreads.find(Long.parseLong(results.getString("threadid"))) != null) {
                                        ChatAppServerView.llThreads.find(Long.parseLong(results.getString("threadid"))).updateList("UPDA:main");
                                        ServerThread.sleep(100);
                                        ChatAppServerView.llThreads.find(Long.parseLong(results.getString("threadid"))).send(old_nick + " is now known as " + message.substring("/NICK ".length()) + ".\n");
                                    }
                                }
                            }
                            results.close();
                        }
                        outToClient.writeBytes(reply);
                    }
                    else {
                        // invalid command
                        System.out.println("Invalid command. "); 
                    }
                }
            }
            outToClient.close();
            ChatAppServerView.txtDebug.setText(ChatAppServerView.txtDebug.getText() + username + " has disconnected.\n");
            System.out.println("Disconnecting..."); // debug
            //outToClient.writeBytes("DISC"); // tell client we're discing
        } catch (InterruptedException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
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
    
    private void send(String message) throws IOException {
        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        outToClient.writeBytes(message);
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
