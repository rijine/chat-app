/*
 * ChatAppServerView.java
 */

package chatappserver;

import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import java.sql.*;
import javax.swing.SwingWorker;

/**
 * The application's main frame.
 */
public class ChatAppServerView extends FrameView {

    public ChatAppServerView(SingleFrameApplication app) {
        super(app);

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = ChatAppServerApp.getApplication().getMainFrame();
            aboutBox = new ChatAppServerAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        ChatAppServerApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        loginPanel = new javax.swing.JPanel();
        lblSQLip = new javax.swing.JLabel();
        lblSQLport = new javax.swing.JLabel();
        lblDB = new javax.swing.JLabel();
        lblUsername = new javax.swing.JLabel();
        lblPassword = new javax.swing.JLabel();
        tfSQLip = new javax.swing.JTextField();
        tfSQLport = new javax.swing.JTextField();
        tfDB = new javax.swing.JTextField();
        tfUsername = new javax.swing.JTextField();
        tfPassword = new javax.swing.JPasswordField();
        btnLogin = new javax.swing.JButton();
        btnExit1 = new javax.swing.JButton();
        settingsPanel = new javax.swing.JPanel();
        lblPort = new javax.swing.JLabel();
        tfPort = new javax.swing.JTextField();
        btnStart = new javax.swing.JButton();
        btnExit2 = new javax.swing.JButton();
        lblUsersLimit = new javax.swing.JLabel();
        tfUsersLimit = new javax.swing.JTextField();
        mainPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtDebug = new javax.swing.JTextArea();

        menuBar.setName("menuBar"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(chatappserver.ChatAppServerApp.class).getContext().getResourceMap(ChatAppServerView.class);
        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(chatappserver.ChatAppServerApp.class).getContext().getActionMap(ChatAppServerView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 164, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        loginPanel.setName("loginPanel"); // NOI18N

        lblSQLip.setDisplayedMnemonic('I');
        lblSQLip.setLabelFor(tfSQLip);
        lblSQLip.setText(resourceMap.getString("lblSQLip.text")); // NOI18N
        lblSQLip.setName("lblSQLip"); // NOI18N

        lblSQLport.setDisplayedMnemonic('o');
        lblSQLport.setLabelFor(tfSQLport);
        lblSQLport.setText(resourceMap.getString("lblSQLport.text")); // NOI18N
        lblSQLport.setName("lblSQLport"); // NOI18N

        lblDB.setDisplayedMnemonic('d');
        lblDB.setLabelFor(tfDB);
        lblDB.setText(resourceMap.getString("lblDB.text")); // NOI18N
        lblDB.setName("lblDB"); // NOI18N

        lblUsername.setDisplayedMnemonic('u');
        lblUsername.setLabelFor(tfUsername);
        lblUsername.setText(resourceMap.getString("lblUsername.text")); // NOI18N
        lblUsername.setName("lblUsername"); // NOI18N

        lblPassword.setDisplayedMnemonic('p');
        lblPassword.setLabelFor(tfPassword);
        lblPassword.setText(resourceMap.getString("lblPassword.text")); // NOI18N
        lblPassword.setName("lblPassword"); // NOI18N

        tfSQLip.setText(resourceMap.getString("tfSQLip.text")); // NOI18N
        tfSQLip.setName("tfSQLip"); // NOI18N

        tfSQLport.setText(resourceMap.getString("tfSQLport.text")); // NOI18N
        tfSQLport.setName("tfSQLport"); // NOI18N

        tfDB.setText(resourceMap.getString("tfDB.text")); // NOI18N
        tfDB.setName("tfDB"); // NOI18N

        tfUsername.setText(resourceMap.getString("tfUsername.text")); // NOI18N
        tfUsername.setName("tfUsername"); // NOI18N

        tfPassword.setText(resourceMap.getString("tfPassword.text")); // NOI18N
        tfPassword.setName("tfPassword"); // NOI18N

        btnLogin.setMnemonic('l');
        btnLogin.setText(resourceMap.getString("btnLogin.text")); // NOI18N
        btnLogin.setName("btnLogin"); // NOI18N
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });

        btnExit1.setMnemonic('x');
        btnExit1.setText(resourceMap.getString("btnExit1.text")); // NOI18N
        btnExit1.setName("btnExit1"); // NOI18N
        btnExit1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExit1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout loginPanelLayout = new javax.swing.GroupLayout(loginPanel);
        loginPanel.setLayout(loginPanelLayout);
        loginPanelLayout.setHorizontalGroup(
            loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loginPanelLayout.createSequentialGroup()
                .addGap(97, 97, 97)
                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(lblSQLip)
                    .addComponent(lblDB)
                    .addComponent(lblPassword, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblUsername)
                    .addComponent(lblSQLport)
                    .addComponent(btnLogin, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tfSQLip, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
                    .addComponent(tfSQLport, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
                    .addComponent(tfDB, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
                    .addComponent(btnExit1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
                    .addComponent(tfPassword, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
                    .addComponent(tfUsername, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE))
                .addGap(102, 102, 102))
        );
        loginPanelLayout.setVerticalGroup(
            loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(loginPanelLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSQLip)
                    .addComponent(tfSQLip, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSQLport)
                    .addComponent(tfSQLport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDB)
                    .addComponent(tfDB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUsername)
                    .addComponent(tfUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPassword)
                    .addComponent(tfPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(loginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnExit1)
                    .addComponent(btnLogin))
                .addContainerGap(59, Short.MAX_VALUE))
        );

        settingsPanel.setEnabled(false);
        settingsPanel.setName("settingsPanel"); // NOI18N

        lblPort.setDisplayedMnemonic('o');
        lblPort.setLabelFor(tfPort);
        lblPort.setText(resourceMap.getString("lblPort.text")); // NOI18N
        lblPort.setName("lblPort"); // NOI18N

        tfPort.setText(resourceMap.getString("tfPort.text")); // NOI18N
        tfPort.setName("tfPort"); // NOI18N

        btnStart.setMnemonic('S');
        btnStart.setText(resourceMap.getString("btnStart.text")); // NOI18N
        btnStart.setName("btnStart"); // NOI18N
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        btnExit2.setMnemonic('x');
        btnExit2.setText(resourceMap.getString("btnExit2.text")); // NOI18N
        btnExit2.setName("btnExit2"); // NOI18N
        btnExit2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExit2ActionPerformed(evt);
            }
        });

        lblUsersLimit.setDisplayedMnemonic('m');
        lblUsersLimit.setLabelFor(tfUsersLimit);
        lblUsersLimit.setText(resourceMap.getString("lblUsersLimit.text")); // NOI18N
        lblUsersLimit.setName("lblUsersLimit"); // NOI18N

        tfUsersLimit.setText(resourceMap.getString("tfUsersLimit.text")); // NOI18N
        tfUsersLimit.setName("tfUsersLimit"); // NOI18N

        javax.swing.GroupLayout settingsPanelLayout = new javax.swing.GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, settingsPanelLayout.createSequentialGroup()
                        .addGap(66, 66, 66)
                        .addComponent(btnStart))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, settingsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblUsersLimit)
                            .addComponent(lblPort))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(tfUsersLimit)
                            .addComponent(tfPort))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addComponent(btnExit2, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(62, 62, 62))
        );
        settingsPanelLayout.setVerticalGroup(
            settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(settingsPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPort)
                    .addComponent(tfPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUsersLimit)
                    .addComponent(tfUsersLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 130, Short.MAX_VALUE)
                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnStart)
                    .addComponent(btnExit2))
                .addGap(31, 31, 31))
        );

        mainPanel.setName("mainPanel"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        txtDebug.setColumns(20);
        txtDebug.setRows(5);
        txtDebug.setName("txtDebug"); // NOI18N
        jScrollPane1.setViewportView(txtDebug);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 273, Short.MAX_VALUE)
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                .addContainerGap())
        );

        setComponent(loginPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoginActionPerformed
            try{
                Class.forName("com.mysql.jdbc.Driver");
                connectToMysql = DriverManager.getConnection("jdbc:mysql://"+tfSQLip.getText()+":"+tfSQLport.getText()+"/"+tfDB.getText(),tfUsername.getText(), String.valueOf(tfPassword.getPassword()));
                loginPanel.setVisible(false); 
                super.setComponent(settingsPanel);
            }
            catch(Exception e) {
                javax.swing.JOptionPane.showMessageDialog(super.getFrame(), "Failed to connect to database. ");
                e.printStackTrace();
            }
    }//GEN-LAST:event_btnLoginActionPerformed

    private void btnExit1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExit1ActionPerformed
            System.exit(0);
}//GEN-LAST:event_btnExit1ActionPerformed

    private void btnExit2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExit2ActionPerformed
            System.exit(0);
    }//GEN-LAST:event_btnExit2ActionPerformed

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        // listen for connections
        settingsPanel.setVisible(false);
        super.setComponent(mainPanel);
        startServerWorker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                startServer();
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        startServerWorker.execute();
    }//GEN-LAST:event_btnStartActionPerformed
    
    public void startServer() throws SQLException {
        String query;
        String reply = null;
        ServerSocket welcomeSocket=null;
        try {
            welcomeSocket = new ServerSocket(Integer.parseInt(tfPort.getText()));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        while(true) {
            Socket connectionSocket;
            try {
                connectionSocket = welcomeSocket.accept();
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                query = inFromClient.readLine();
                Statement sendSQLQuery = null;
                sendSQLQuery = connectToMysql.createStatement();
                if (query.startsWith("CHEK:")) {
                    // checks database for stuff
                    String parseQuery = query;
                    parseQuery = parseQuery.substring("CHEK:".length());
                    if (parseQuery.startsWith("nickname:")) {
                        parseQuery = parseQuery.substring("nickname:".length());
                        sendSQLQuery.executeQuery("SELECT nickname FROM user WHERE nickname = '" + parseQuery.toLowerCase() + "';");
                        ResultSet results = sendSQLQuery.getResultSet();
                        boolean found = results.next();
                        results.close();
                        sendSQLQuery.close();
                        if (found)
                            reply = "AlreadyExists\n";
                        else
                            reply = "Available\n";
                        outToClient.writeBytes(reply);
                    }
                    else if (parseQuery.startsWith("username:")) {
                        parseQuery = parseQuery.substring("username:".length());
                        sendSQLQuery.executeQuery("SELECT username FROM user WHERE username = '" + parseQuery.toLowerCase() + "';");
                        ResultSet results = sendSQLQuery.getResultSet();
                        boolean found = results.next();
                        results.close();
                        sendSQLQuery.close();
                        if (found)
                            reply = "AlreadyExists\n";
                        else
                            reply = "Available\n";
                        outToClient.writeBytes(reply);
                    }
                    else {
                        // some error about not knowing what's after the check
                    }
                    
                }
                //reply = query.toUpperCase() + '\n';
                if(query.equalsIgnoreCase("DISC")){
                   connectionSocket.close();
                   System.out.println("Connection Socket Quitting");
                } 
                else {
                    //outToClient.writeBytes(reply);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExit1;
    private javax.swing.JButton btnExit2;
    private javax.swing.JButton btnLogin;
    private javax.swing.JButton btnStart;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblDB;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblPort;
    private javax.swing.JLabel lblSQLip;
    private javax.swing.JLabel lblSQLport;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JLabel lblUsersLimit;
    private javax.swing.JPanel loginPanel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JTextField tfDB;
    private javax.swing.JPasswordField tfPassword;
    private javax.swing.JTextField tfPort;
    private javax.swing.JTextField tfSQLip;
    private javax.swing.JTextField tfSQLport;
    private javax.swing.JTextField tfUsername;
    private javax.swing.JTextField tfUsersLimit;
    private javax.swing.JTextArea txtDebug;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private Connection connectToMysql; 
    private JDialog aboutBox;
    private SwingWorker startServerWorker;
}
