/*
 * ChatClientView.java
 */

package chatclient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Pattern;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * The application's main frame.
 */
public class ChatClientView extends FrameView {
    public ChatClientView(SingleFrameApplication app) {
        super(app);

        initComponents();
        
        lblUnameCheck.setVisible(false);
        lblEmailCheck.setVisible(false);
        lblPassCheck.setVisible(false);
        lblNickCheck.setVisible(false);
        
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
            JFrame mainFrame = ChatClientApp.getApplication().getMainFrame();
            aboutBox = new ChatClientAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        ChatClientApp.getApplication().show(aboutBox);
    }
    
    public boolean sendMessageToServer(String code, String message) throws FileNotFoundException, IOException {
        String query; 
        String reply = null; 
        String hostname;
        int port;
        
        BufferedReader inputStream = new BufferedReader(new FileReader("settings.ini"));
        hostname = inputStream.readLine();
        port = Integer.parseInt(inputStream.readLine());
        inputStream.close();
        
        Socket clientSocket = null;
        try {
            clientSocket = new Socket(hostname, port);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream()); 
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 
            query = code + ":" + message; 
            outToServer.writeBytes(query + '\n');
            reply = inFromServer.readLine(); 
        } catch (UnknownHostException ex) {
            javax.swing.JOptionPane.showMessageDialog(super.getFrame(), "Unknown hostname.");
            ex.printStackTrace();
        } catch (IOException ex) {
            javax.swing.JOptionPane.showMessageDialog(super.getFrame(), "Could not connect to host.");
            ex.printStackTrace();
        }
        try {
            clientSocket.close();  
        } catch (IOException ex) {
            javax.swing.JOptionPane.showMessageDialog(super.getFrame(), "Could not close socket.");
            ex.printStackTrace();
        }
        /* for "code" == "CHEK"
         * query = "CHEK:username:some_username"
         * query = "CHEK:nickname:some_nickname"
         */
        if (code.equals("CHEK")) {
            if (reply.equals("AlreadyExists"))
                return false;
            else if (reply.equals("Available"))
                return true;
        }
        return false;
    }
    
    @Action
    public void showSettingsBox() throws IOException {
        if (settingsBox == null) {
            JFrame mainFrame = ChatClientApp.getApplication().getMainFrame();
            settingsBox = new ChatClientSettingsBox(mainFrame);
            settingsBox.setLocationRelativeTo(mainFrame);
        }
        ChatClientApp.getApplication().show(settingsBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        lblUser = new javax.swing.JLabel();
        lblPassword = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        lblExUser = new javax.swing.JLabel();
        butLogin = new javax.swing.JButton();
        butNewAcc = new javax.swing.JButton();
        butGuest = new javax.swing.JButton();
        txtPass = new javax.swing.JPasswordField();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        settingsMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        PanNewUser = new javax.swing.JPanel();
        lblUsername = new javax.swing.JLabel();
        lblPass = new javax.swing.JLabel();
        lblRePass = new javax.swing.JLabel();
        lblEmail = new javax.swing.JLabel();
        txtUserName = new javax.swing.JTextField();
        txtEmail = new javax.swing.JTextField();
        txtPassword2 = new javax.swing.JPasswordField();
        txtPassword = new javax.swing.JPasswordField();
        btnSubmit = new javax.swing.JButton();
        lblNickName = new javax.swing.JLabel();
        txtNickName = new javax.swing.JTextField();
        lblFName = new javax.swing.JLabel();
        lblLName = new javax.swing.JLabel();
        txtFName = new javax.swing.JTextField();
        txtLName = new javax.swing.JTextField();
        lblUnameCheck = new javax.swing.JLabel();
        lblPassCheck = new javax.swing.JLabel();
        lblEmailCheck = new javax.swing.JLabel();
        lblNickCheck = new javax.swing.JLabel();
        PanChat = new javax.swing.JPanel();
        lblWelcome = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        txtSend = new javax.swing.JTextField();

        mainPanel.setName("mainPanel"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(chatclient.ChatClientApp.class).getContext().getResourceMap(ChatClientView.class);
        lblUser.setText(resourceMap.getString("lblUser.text")); // NOI18N
        lblUser.setName("lblUser"); // NOI18N

        lblPassword.setText(resourceMap.getString("lblPassword.text")); // NOI18N
        lblPassword.setName("lblPassword"); // NOI18N

        jTextField1.setText(resourceMap.getString("jTextField1.text")); // NOI18N
        jTextField1.setName("jTextField1"); // NOI18N

        lblExUser.setText(resourceMap.getString("lblExUser.text")); // NOI18N
        lblExUser.setName("lblExUser"); // NOI18N

        butLogin.setText(resourceMap.getString("butLogin.text")); // NOI18N
        butLogin.setName("butLogin"); // NOI18N

        butNewAcc.setText(resourceMap.getString("butNewAcc.text")); // NOI18N
        butNewAcc.setName("butNewAcc"); // NOI18N
        butNewAcc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butNewAccActionPerformed(evt);
            }
        });

        butGuest.setText(resourceMap.getString("butGuest.text")); // NOI18N
        butGuest.setName("butGuest"); // NOI18N

        txtPass.setText(resourceMap.getString("txtPass.text")); // NOI18N
        txtPass.setName("txtPass"); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPassword)
                    .addComponent(lblUser))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblExUser)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(txtPass, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(butLogin))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(butNewAcc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(butGuest))))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(lblExUser)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUser)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(butNewAcc))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPassword)
                    .addComponent(txtPass, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(butGuest))
                .addGap(9, 9, 9)
                .addComponent(butLogin)
                .addContainerGap(118, Short.MAX_VALUE))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(chatclient.ChatClientApp.class).getContext().getActionMap(ChatClientView.class, this);
        settingsMenuItem.setAction(actionMap.get("showSettingsBox")); // NOI18N
        settingsMenuItem.setText(resourceMap.getString("settingsMenuItem.text")); // NOI18N
        settingsMenuItem.setName("settingsMenuItem"); // NOI18N
        settingsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(settingsMenuItem);

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
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 167, Short.MAX_VALUE)
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

        PanNewUser.setName("PanNewUser"); // NOI18N
        PanNewUser.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblUsername.setText(resourceMap.getString("lblUsername.text")); // NOI18N
        lblUsername.setName("lblUsername"); // NOI18N
        PanNewUser.add(lblUsername, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 20, -1, -1));

        lblPass.setText(resourceMap.getString("lblPass.text")); // NOI18N
        lblPass.setName("lblPass"); // NOI18N
        PanNewUser.add(lblPass, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, -1, -1));

        lblRePass.setText(resourceMap.getString("lblRePass.text")); // NOI18N
        lblRePass.setName("lblRePass"); // NOI18N
        PanNewUser.add(lblRePass, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, -1, -1));

        lblEmail.setText(resourceMap.getString("lblEmail.text")); // NOI18N
        lblEmail.setName("lblEmail"); // NOI18N
        PanNewUser.add(lblEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 80, -1, -1));

        txtUserName.setText(resourceMap.getString("txtUserName.text")); // NOI18N
        txtUserName.setName("txtUserName"); // NOI18N
        txtUserName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtUserNameFocusLost(evt);
            }
        });
        PanNewUser.add(txtUserName, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 20, 130, -1));

        txtEmail.setText(resourceMap.getString("txtEmail.text")); // NOI18N
        txtEmail.setName("txtEmail"); // NOI18N
        txtEmail.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtEmailFocusLost(evt);
            }
        });
        PanNewUser.add(txtEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 80, 130, 20));

        txtPassword2.setText(resourceMap.getString("txtPassword2.text")); // NOI18N
        txtPassword2.setName("txtPassword2"); // NOI18N
        txtPassword2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtPassword2FocusLost(evt);
            }
        });
        PanNewUser.add(txtPassword2, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 60, 130, 20));

        txtPassword.setText(resourceMap.getString("txtPassword.text")); // NOI18N
        txtPassword.setName("txtPassword"); // NOI18N
        PanNewUser.add(txtPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 40, 130, -1));

        btnSubmit.setText(resourceMap.getString("btnSubmit.text")); // NOI18N
        btnSubmit.setName("btnSubmit"); // NOI18N
        btnSubmit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSubmitActionPerformed(evt);
            }
        });
        PanNewUser.add(btnSubmit, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 220, -1, -1));

        lblNickName.setText(resourceMap.getString("lblNickName.text")); // NOI18N
        lblNickName.setName("lblNickName"); // NOI18N
        PanNewUser.add(lblNickName, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 100, -1, -1));

        txtNickName.setText(resourceMap.getString("txtNickName.text")); // NOI18N
        txtNickName.setName("txtNickName"); // NOI18N
        txtNickName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtNickNameFocusLost(evt);
            }
        });
        PanNewUser.add(txtNickName, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 100, 130, -1));

        lblFName.setText(resourceMap.getString("lblFName.text")); // NOI18N
        lblFName.setName("lblFName"); // NOI18N
        PanNewUser.add(lblFName, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, -1, -1));

        lblLName.setText(resourceMap.getString("lblLName.text")); // NOI18N
        lblLName.setName("lblLName"); // NOI18N
        PanNewUser.add(lblLName, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, -1, -1));

        txtFName.setText(resourceMap.getString("txtFName.text")); // NOI18N
        txtFName.setName("txtFName"); // NOI18N
        PanNewUser.add(txtFName, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 120, 130, -1));

        txtLName.setText(resourceMap.getString("txtLName.text")); // NOI18N
        txtLName.setName("txtLName"); // NOI18N
        PanNewUser.add(txtLName, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 140, 130, -1));

        lblUnameCheck.setIcon(resourceMap.getIcon("lblUnameCheck.icon")); // NOI18N
        lblUnameCheck.setText(resourceMap.getString("lblUnameCheck.text")); // NOI18N
        lblUnameCheck.setName("lblUnameCheck"); // NOI18N
        PanNewUser.add(lblUnameCheck, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 20, -1, -1));

        lblPassCheck.setIcon(resourceMap.getIcon("lblPassCheck.icon")); // NOI18N
        lblPassCheck.setName("lblPassCheck"); // NOI18N
        PanNewUser.add(lblPassCheck, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 60, -1, -1));

        lblEmailCheck.setIcon(resourceMap.getIcon("lblEmailCheck.icon")); // NOI18N
        lblEmailCheck.setName("lblEmailCheck"); // NOI18N
        PanNewUser.add(lblEmailCheck, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 80, -1, -1));

        lblNickCheck.setIcon(resourceMap.getIcon("lblNickCheck.icon")); // NOI18N
        lblNickCheck.setName("lblNickCheck"); // NOI18N
        PanNewUser.add(lblNickCheck, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 100, -1, -1));

        PanChat.setName("PanChat"); // NOI18N

        lblWelcome.setText(resourceMap.getString("lblWelcome.text")); // NOI18N
        lblWelcome.setName("lblWelcome"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList1.setName("jList1"); // NOI18N
        jScrollPane1.setViewportView(jList1);

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jTextPane1.setName("jTextPane1"); // NOI18N
        jScrollPane2.setViewportView(jTextPane1);

        txtSend.setText(resourceMap.getString("txtSend.text")); // NOI18N
        txtSend.setName("txtSend"); // NOI18N

        javax.swing.GroupLayout PanChatLayout = new javax.swing.GroupLayout(PanChat);
        PanChat.setLayout(PanChatLayout);
        PanChatLayout.setHorizontalGroup(
            PanChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanChatLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PanChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblWelcome)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                    .addComponent(txtSend, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        PanChatLayout.setVerticalGroup(
            PanChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PanChatLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblWelcome)
                .addGap(18, 18, 18)
                .addGroup(PanChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, PanChatLayout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 253, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 116, Short.MAX_VALUE)
                        .addComponent(txtSend, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(28, 28, 28))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void butNewAccActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butNewAccActionPerformed
       mainPanel.setVisible(false);
       super.setComponent(PanNewUser);
    }//GEN-LAST:event_butNewAccActionPerformed
    private boolean checkFields() {
        if (!bEmail || !bPass || !bNick || !bUname) {
            return false;
        }
        return true;
    }
    private void btnSubmitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitActionPerformed
        if (txtUserName.getText().isEmpty() || txtEmail.getText().isEmpty() || 
               txtFName.getText().isEmpty() || txtLName.getText().isEmpty() ||
               txtNickName.getText().isEmpty() || 
               String.valueOf(txtPassword.getPassword()).isEmpty() || String.valueOf(txtPassword2.getPassword()).isEmpty()) 
            javax.swing.JOptionPane.showMessageDialog(super.getFrame(), "All fields are required. ");
        else if (checkFields()) {
            try {
                sendMessageToServer("NEWA", txtUserName.getText() + "," + txtEmail.getText() + "," + txtFName.getText() + "," + txtLName.getText() + "," + String.valueOf(txtPassword.getPassword()) + "," + txtNickName.getText());
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ChatClientView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ChatClientView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else {
            javax.swing.JOptionPane.showMessageDialog(null, "Fix the errors (red X)");
        }
}//GEN-LAST:event_btnSubmitActionPerformed

    private void txtPassword2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtPassword2FocusLost
        if (!String.valueOf(txtPassword.getPassword()).equals(String.valueOf(txtPassword2.getPassword()))) {
            lblPassCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/redX.png")));
            lblPassCheck.setToolTipText("Passwords do not match");
            bPass = false;
        }
        else if (txtPassword.getPassword().length < 6) {
            lblPassCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/redX.png")));
            lblPassCheck.setToolTipText("Password must be at least 6 characters long.");
            bPass = false;
        }
        else {
            lblPassCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/greenTick.png")));
            lblPassCheck.setToolTipText(null);
            bPass = true;
        }
        lblPassCheck.setVisible(true);
    }//GEN-LAST:event_txtPassword2FocusLost

    private void txtUserNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtUserNameFocusLost
        String allowableCharacters = "[A-Za-z0-9]*";
        Pattern regex = Pattern.compile(allowableCharacters);
        if (regex.matcher(txtUserName.getText()).matches()) {
            try {
                if (sendMessageToServer("CHEK", "username:" + txtUserName.getText())) {
                    lblUnameCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/greenTick.png")));
                    lblUnameCheck.setToolTipText(null);
                    bUname = true;
                }
                else {
                    lblUnameCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/redX.png")));
                    lblUnameCheck.setToolTipText("Username already exists");
                    bUname = false;
                }
                lblUnameCheck.setVisible(true);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ChatClientView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ChatClientView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else {
            lblUnameCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/redX.png")));
            lblUnameCheck.setToolTipText("Illegal characters inputted");
            bUname = false;
            lblUnameCheck.setVisible(true);
        }
    }//GEN-LAST:event_txtUserNameFocusLost

    private void settingsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsMenuItemActionPerformed
        // Open Settings window
}//GEN-LAST:event_settingsMenuItemActionPerformed

    private void txtEmailFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtEmailFocusLost
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader("regex.txt"));
            Pattern regex = Pattern.compile(r.readLine());
            if(regex.matcher(txtEmail.getText()).matches()) {
                lblEmailCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/greenTick.png")));
                lblEmailCheck.setToolTipText(null);
                bEmail = true;
            }
            else {
                lblEmailCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/redX.png")));
                lblEmailCheck.setToolTipText("Invalid email address");
                bEmail = false;
            }
            lblEmailCheck.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                    r.close();
            } catch(IOException e) {
                    e.printStackTrace();
            }
        }
    }//GEN-LAST:event_txtEmailFocusLost

    private void txtNickNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtNickNameFocusLost
        String allowableCharacters = "[A-Za-z0-9]*";
        Pattern regex = Pattern.compile(allowableCharacters);
        if (regex.matcher(txtNickName.getText()).matches()) {
            try {
                if (sendMessageToServer("CHEK", "nickname:" + txtNickName.getText())) {
                    lblNickCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/greenTick.png")));
                    lblNickCheck.setToolTipText(null);
                    bNick = true;
                }
                else {
                    lblNickCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/redX.png")));
                    lblNickCheck.setToolTipText("Nickname already in use");
                    bNick = false;
                }
                lblNickCheck.setVisible(true);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ChatClientView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ChatClientView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else {
            lblNickCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/redX.png")));
            lblNickCheck.setToolTipText("Illegal characters inputted");
            bNick = false;
            lblNickCheck.setVisible(true);
        }
    }//GEN-LAST:event_txtNickNameFocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PanChat;
    private javax.swing.JPanel PanNewUser;
    private javax.swing.JButton btnSubmit;
    private javax.swing.JButton butGuest;
    private javax.swing.JButton butLogin;
    private javax.swing.JButton butNewAcc;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblEmailCheck;
    private javax.swing.JLabel lblExUser;
    private javax.swing.JLabel lblFName;
    private javax.swing.JLabel lblLName;
    private javax.swing.JLabel lblNickCheck;
    private javax.swing.JLabel lblNickName;
    private javax.swing.JLabel lblPass;
    private javax.swing.JLabel lblPassCheck;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblRePass;
    private javax.swing.JLabel lblUnameCheck;
    private javax.swing.JLabel lblUser;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JMenuItem settingsMenuItem;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtFName;
    private javax.swing.JTextField txtLName;
    private javax.swing.JTextField txtNickName;
    private javax.swing.JPasswordField txtPass;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JPasswordField txtPassword2;
    private javax.swing.JTextField txtSend;
    private javax.swing.JTextField txtUserName;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
    private JDialog settingsBox;
    private boolean bEmail = false, bUname = false, bPass = false, bNick = false;
}
