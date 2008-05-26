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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    
    /*
     *   sendMessageToServer takes two Strings as parameters, The first one is a special code, the second is the request body. 
     * This function is used to communicate to the server special requests, like create new account, etc. 
     * A socket is opened, the message is sent, the reply is obtained from the server, then the socket is closed. 
     * This function is not used for normal chat sessions, since they will use the ChatHandler instead for a persistant connection. 
     * The function returns a String, which is the reply obtained from the server. 
     */
    public String sendMessageToServer(String code, String message) throws FileNotFoundException, IOException {
        String query; 
        String reply = null; 
        String hostname;
        int port;
        
        BufferedReader inputStream = new BufferedReader(new FileReader("settings.ini"));    // takes the server IP and port from the settings.ini file at the client side. 
        hostname = inputStream.readLine();
        port = Integer.parseInt(inputStream.readLine());
        inputStream.close();
        
        Socket clientSocket = null;
        try {
            clientSocket = new Socket(hostname, port);      // creates a new socket. 
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream()); 
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 
            query = code + ":" + message;  
            outToServer.writeBytes(query + '\n');   // send the data which is obtained through the parameters to the server, in the format specified in the previous line. 
            reply = inFromServer.readLine();        // wait for server to reply. 
        } catch (UnknownHostException ex) {
            javax.swing.JOptionPane.showMessageDialog(super.getFrame(), "Unknown hostname.", "Error!", javax.swing.JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (IOException ex) {
            javax.swing.JOptionPane.showMessageDialog(super.getFrame(), "Could not connect to host.", "Error!", javax.swing.JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        try {
            clientSocket.close();        // close socket 
        } catch (IOException ex) {
            javax.swing.JOptionPane.showMessageDialog(super.getFrame(), "Could not close socket.", "Error!", javax.swing.JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        /* for "code" == "CHEK"
         * query = "CHEK:username:some_username"
         * query = "CHEK:nickname:some_nickname"
         */
        return reply;                       // return the reply obtained from the server. 
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
    
/*
 * This function takes a String, and returns a String which is the md5 hash of the input. 
 * It is used to encrypt the passwords before sending it to the server. This provides security against evesdroppers, but also renders the server unable to know what the user password is. 
 * Therefore, the server administrator cannot access user accounts, since the passwords  will also be stored in the database as md5 hashes. 
 */
private String MD5Hash(String Input)
    {
         String pass = Input;
         
         StringBuffer hexString = new StringBuffer();
        byte[] defaultBytes = pass.getBytes();
        try{
         MessageDigest algorithm = MessageDigest.getInstance("MD5");
         algorithm.reset();
         algorithm.update(defaultBytes);
         byte messageDigest[] = algorithm.digest();

         for (int i=0;i<messageDigest.length;i++) {
            String hex = Integer.toHexString(0xFF & messageDigest[i]); 
            if(hex.length()==1)
            hexString.append('0');

            hexString.append(hex);
     }
     pass = hexString+"";
    }
    catch(NoSuchAlgorithmException nsae){
        System.out.println("Coulnd't encrypt password... "); 
      }
    return hexString.toString(); 
}

 // <editor-fold defaultstate="collapsed" desc="etc">
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        lblUserLogin = new javax.swing.JLabel();
        lblPassLogin = new javax.swing.JLabel();
        tfUsernameLogin = new javax.swing.JTextField();
        lblExUser = new javax.swing.JLabel();
        btntLogin = new javax.swing.JButton();
        btntNewAcc = new javax.swing.JButton();
        btntGuest = new javax.swing.JButton();
        tfPassLogin = new javax.swing.JPasswordField();
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
        tfUserName = new javax.swing.JTextField();
        lblUnameCheck = new javax.swing.JLabel();
        lblPass = new javax.swing.JLabel();
        tfPassword = new javax.swing.JPasswordField();
        lblRePass = new javax.swing.JLabel();
        tfPassword2 = new javax.swing.JPasswordField();
        lblPassCheck = new javax.swing.JLabel();
        lblEmail = new javax.swing.JLabel();
        tfEmail = new javax.swing.JTextField();
        lblEmailCheck = new javax.swing.JLabel();
        lblNickName = new javax.swing.JLabel();
        tfNickName = new javax.swing.JTextField();
        lblNickCheck = new javax.swing.JLabel();
        lblFName = new javax.swing.JLabel();
        tfFName = new javax.swing.JTextField();
        lblFNameCheck = new javax.swing.JLabel();
        lblLName = new javax.swing.JLabel();
        tfLName = new javax.swing.JTextField();
        lblLNameCheck = new javax.swing.JLabel();
        btnBack = new javax.swing.JButton();
        btnSubmit = new javax.swing.JButton();
        PanChat = new javax.swing.JPanel();
        lblWelcome = new javax.swing.JLabel();
        tfSend = new javax.swing.JTextField();
        scrollUsers = new javax.swing.JScrollPane();
        tblUsers = new javax.swing.JTable();
        tbsChan = new javax.swing.JTabbedPane();
        scrollChans = new javax.swing.JScrollPane();
        txtMessages = new javax.swing.JTextPane();

        mainPanel.setName("mainPanel"); // NOI18N

        lblUserLogin.setDisplayedMnemonic('u');
        lblUserLogin.setLabelFor(tfUsernameLogin);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(chatclient.ChatClientApp.class).getContext().getResourceMap(ChatClientView.class);
        lblUserLogin.setText(resourceMap.getString("lblUserLogin.text")); // NOI18N
        lblUserLogin.setName("lblUserLogin"); // NOI18N

        lblPassLogin.setDisplayedMnemonic('p');
        lblPassLogin.setLabelFor(tfPassLogin);
        lblPassLogin.setText(resourceMap.getString("lblPassLogin.text")); // NOI18N
        lblPassLogin.setName("lblPassLogin"); // NOI18N

        tfUsernameLogin.setText(resourceMap.getString("tfUsernameLogin.text")); // NOI18N
        tfUsernameLogin.setName("tfUsernameLogin"); // NOI18N

        lblExUser.setText(resourceMap.getString("lblExUser.text")); // NOI18N
        lblExUser.setName("lblExUser"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(chatclient.ChatClientApp.class).getContext().getActionMap(ChatClientView.class, this);
        btntLogin.setAction(actionMap.get("showChatBox")); // NOI18N
        btntLogin.setMnemonic('g');
        btntLogin.setText(resourceMap.getString("btntLogin.text")); // NOI18N
        btntLogin.setName("btntLogin"); // NOI18N
        btntLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btntLoginActionPerformed(evt);
            }
        });

        btntNewAcc.setMnemonic('n');
        btntNewAcc.setText(resourceMap.getString("btntNewAcc.text")); // NOI18N
        btntNewAcc.setName("btntNewAcc"); // NOI18N
        btntNewAcc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btntNewAccActionPerformed(evt);
            }
        });

        btntGuest.setMnemonic('s');
        btntGuest.setText(resourceMap.getString("btntGuest.text")); // NOI18N
        btntGuest.setName("btntGuest"); // NOI18N

        tfPassLogin.setText(resourceMap.getString("tfPassLogin.text")); // NOI18N
        tfPassLogin.setName("tfPassLogin"); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPassLogin)
                    .addComponent(lblUserLogin))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblExUser)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(tfPassLogin, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                            .addComponent(tfUsernameLogin, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btntLogin))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btntNewAcc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btntGuest))))
                .addContainerGap(26, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(lblExUser)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUserLogin)
                    .addComponent(tfUsernameLogin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btntNewAcc))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPassLogin)
                    .addComponent(tfPassLogin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btntGuest))
                .addGap(9, 9, 9)
                .addComponent(btntLogin)
                .addContainerGap(116, Short.MAX_VALUE))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setMnemonic('f');
        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

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
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setMnemonic('h');
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
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 171, Short.MAX_VALUE)
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
        PanNewUser.setLayout(new java.awt.GridBagLayout());

        lblUsername.setDisplayedMnemonic('u');
        lblUsername.setLabelFor(tfUserName);
        lblUsername.setText(resourceMap.getString("lblUsername.text")); // NOI18N
        lblUsername.setName("lblUsername"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        PanNewUser.add(lblUsername, gridBagConstraints);

        tfUserName.setText(resourceMap.getString("tfUserName.text")); // NOI18N
        tfUserName.setName("tfUserName"); // NOI18N
        tfUserName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfUserNameFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        PanNewUser.add(tfUserName, gridBagConstraints);

        lblUnameCheck.setIcon(resourceMap.getIcon("lblUnameCheck.icon")); // NOI18N
        lblUnameCheck.setText(resourceMap.getString("lblUnameCheck.text")); // NOI18N
        lblUnameCheck.setName("lblUnameCheck"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        PanNewUser.add(lblUnameCheck, gridBagConstraints);

        lblPass.setDisplayedMnemonic('p');
        lblPass.setLabelFor(tfPassword);
        lblPass.setText(resourceMap.getString("lblPass.text")); // NOI18N
        lblPass.setName("lblPass"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        PanNewUser.add(lblPass, gridBagConstraints);

        tfPassword.setText(resourceMap.getString("tfPassword.text")); // NOI18N
        tfPassword.setName("tfPassword"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        PanNewUser.add(tfPassword, gridBagConstraints);

        lblRePass.setDisplayedMnemonic('a');
        lblRePass.setLabelFor(tfPassword2);
        lblRePass.setText(resourceMap.getString("lblRePass.text")); // NOI18N
        lblRePass.setName("lblRePass"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        PanNewUser.add(lblRePass, gridBagConstraints);

        tfPassword2.setText(resourceMap.getString("tfPassword2.text")); // NOI18N
        tfPassword2.setName("tfPassword2"); // NOI18N
        tfPassword2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfPassword2FocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        PanNewUser.add(tfPassword2, gridBagConstraints);

        lblPassCheck.setIcon(resourceMap.getIcon("lblPassCheck.icon")); // NOI18N
        lblPassCheck.setName("lblPassCheck"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        PanNewUser.add(lblPassCheck, gridBagConstraints);

        lblEmail.setDisplayedMnemonic('e');
        lblEmail.setLabelFor(tfEmail);
        lblEmail.setText(resourceMap.getString("lblEmail.text")); // NOI18N
        lblEmail.setName("lblEmail"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        PanNewUser.add(lblEmail, gridBagConstraints);

        tfEmail.setText(resourceMap.getString("tfEmail.text")); // NOI18N
        tfEmail.setName("tfEmail"); // NOI18N
        tfEmail.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfEmailFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        PanNewUser.add(tfEmail, gridBagConstraints);

        lblEmailCheck.setIcon(resourceMap.getIcon("lblEmailCheck.icon")); // NOI18N
        lblEmailCheck.setName("lblEmailCheck"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        PanNewUser.add(lblEmailCheck, gridBagConstraints);

        lblNickName.setDisplayedMnemonic('n');
        lblNickName.setLabelFor(tfNickName);
        lblNickName.setText(resourceMap.getString("lblNickName.text")); // NOI18N
        lblNickName.setName("lblNickName"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        PanNewUser.add(lblNickName, gridBagConstraints);

        tfNickName.setText(resourceMap.getString("tfNickName.text")); // NOI18N
        tfNickName.setName("tfNickName"); // NOI18N
        tfNickName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfNickNameFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        PanNewUser.add(tfNickName, gridBagConstraints);

        lblNickCheck.setIcon(resourceMap.getIcon("lblNickCheck.icon")); // NOI18N
        lblNickCheck.setName("lblNickCheck"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        PanNewUser.add(lblNickCheck, gridBagConstraints);

        lblFName.setDisplayedMnemonic('i');
        lblFName.setLabelFor(tfFName);
        lblFName.setText(resourceMap.getString("lblFName.text")); // NOI18N
        lblFName.setName("lblFName"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        PanNewUser.add(lblFName, gridBagConstraints);

        tfFName.setText(resourceMap.getString("tfFName.text")); // NOI18N
        tfFName.setName("tfFName"); // NOI18N
        tfFName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfFNameFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        PanNewUser.add(tfFName, gridBagConstraints);

        lblFNameCheck.setIcon(resourceMap.getIcon("lblFNameCheck.icon")); // NOI18N
        lblFNameCheck.setName("lblFNameCheck"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        PanNewUser.add(lblFNameCheck, gridBagConstraints);

        lblLName.setDisplayedMnemonic('l');
        lblLName.setLabelFor(tfLName);
        lblLName.setText(resourceMap.getString("lblLName.text")); // NOI18N
        lblLName.setName("lblLName"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        PanNewUser.add(lblLName, gridBagConstraints);

        tfLName.setText(resourceMap.getString("tfLName.text")); // NOI18N
        tfLName.setName("tfLName"); // NOI18N
        tfLName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfLNameFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        PanNewUser.add(tfLName, gridBagConstraints);

        lblLNameCheck.setIcon(resourceMap.getIcon("lblLNameCheck.icon")); // NOI18N
        lblLNameCheck.setName("lblLNameCheck"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        PanNewUser.add(lblLNameCheck, gridBagConstraints);

        btnBack.setMnemonic('b');
        btnBack.setText(resourceMap.getString("btnBack.text")); // NOI18N
        btnBack.setMargin(new java.awt.Insets(1, 14, 2, 14));
        btnBack.setName("btnBack"); // NOI18N
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 8);
        PanNewUser.add(btnBack, gridBagConstraints);

        btnSubmit.setMnemonic('r');
        btnSubmit.setText(resourceMap.getString("btnSubmit.text")); // NOI18N
        btnSubmit.setMargin(new java.awt.Insets(1, 18, 2, 18));
        btnSubmit.setName("btnSubmit"); // NOI18N
        btnSubmit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSubmitActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(5, 4, 0, 0);
        PanNewUser.add(btnSubmit, gridBagConstraints);

        PanChat.setName("PanChat"); // NOI18N

        lblWelcome.setText(resourceMap.getString("lblWelcome.text")); // NOI18N
        lblWelcome.setName("lblWelcome"); // NOI18N

        tfSend.setText(resourceMap.getString("tfSend.text")); // NOI18N
        tfSend.setName("tfSend"); // NOI18N
        tfSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tfSendActionPerformed(evt);
            }
        });
        tfSend.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfSendKeyTyped(evt);
            }
        });

        scrollUsers.setName("scrollUsers"); // NOI18N

        tblUsers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Image", "UserList"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblUsers.setName("tblUsers"); // NOI18N
        tblUsers.setShowHorizontalLines(false);
        tblUsers.setShowVerticalLines(false);
        scrollUsers.setViewportView(tblUsers);
        tblUsers.getColumnModel().getColumn(0).setResizable(false);
        tblUsers.getColumnModel().getColumn(0).setPreferredWidth(10);

        tbsChan.setName("tbsChan"); // NOI18N

        scrollChans.setName("scrollChans"); // NOI18N

        txtMessages.setFocusable(false);
        txtMessages.setName("txtMessages"); // NOI18N
        scrollChans.setViewportView(txtMessages);

        tbsChan.addTab(resourceMap.getString("scrollChans.TabConstraints.tabTitle"), scrollChans); // NOI18N

        javax.swing.GroupLayout PanChatLayout = new javax.swing.GroupLayout(PanChat);
        PanChat.setLayout(PanChatLayout);
        PanChatLayout.setHorizontalGroup(
            PanChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanChatLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PanChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PanChatLayout.createSequentialGroup()
                        .addGroup(PanChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tbsChan, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
                            .addComponent(tfSend, javax.swing.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scrollUsers, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblWelcome))
                .addContainerGap())
        );
        PanChatLayout.setVerticalGroup(
            PanChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PanChatLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblWelcome)
                .addGap(18, 18, 18)
                .addGroup(PanChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollUsers, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
                    .addGroup(PanChatLayout.createSequentialGroup()
                        .addComponent(tbsChan, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(tfSend, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void btntNewAccActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btntNewAccActionPerformed
       mainPanel.setVisible(false);                 // close the mainpanel
       super.setComponent(PanNewUser);      // open the newuser panel. 
       clearNewUserFields();                          // we clear the fields if we want to create another account later. 
       PanNewUser.setVisible(true);               // display the newuser panel. 
}//GEN-LAST:event_btntNewAccActionPerformed
    private boolean checkFields() { 
        if (!bEmail || !bPass || !bNick || !bUname || !bFName || !bLName) {
            return false;
        }
        return true; // if all fields have been filled legally, return true. (ie allow user to be created) 
    }
    private void btnSubmitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitActionPerformed
        if (tfUserName.getText().isEmpty() || tfEmail.getText().isEmpty() || 
               tfFName.getText().isEmpty() || tfLName.getText().isEmpty() ||
               tfNickName.getText().isEmpty() || 
               String.valueOf(tfPassword.getPassword()).isEmpty() || String.valueOf(tfPassword2.getPassword()).isEmpty()) 
            javax.swing.JOptionPane.showMessageDialog(super.getFrame(), "All fields are required. ", "Error!", javax.swing.JOptionPane.ERROR_MESSAGE); // if a field is left empty, do not allow to send new user request. 
        else if (checkFields()) { // if they're not empty, check if they're legal. if they are legal, proceed and request the new user account. 
            try {
                sendMessageToServer("NEWA", tfUserName.getText() + "," + tfEmail.getText() + "," + tfFName.getText() + "," + tfLName.getText() + "," + MD5Hash(String.valueOf(tfPassword.getPassword())) + "," + tfNickName.getText());
                // ask user if you want to login directly... 
                int choice = javax.swing.JOptionPane.showConfirmDialog(super.getFrame(), "User " + tfUserName.getText() + " created.\nDo you want to login?", "Success!", javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE);
                if (choice == 0) { // i want to login
                    try {
                        String reply = sendMessageToServer("LOGN", tfUserName.getText() + "," + MD5Hash(String.valueOf(tfPassword.getPassword())));

                        if (reply.equals("ERR1")) {
                            javax.swing.JOptionPane.showMessageDialog(super.getFrame(), "User already logged in...");
                            PanNewUser.setVisible(false);
                            super.setComponent(mainPanel);
                            mainPanel.setVisible(true);                     // go back to main menu.  (login failed)
                        } else if (reply.equals("ERR2")) {
                            javax.swing.JOptionPane.showMessageDialog(super.getFrame(), "Invalid username/password...");
                            PanNewUser.setVisible(false);
                            super.setComponent(mainPanel);
                            mainPanel.setVisible(true);                     // go back to main menu (login failed)
                        } else if (reply.equals("SUCC")) {
                            PanNewUser.setVisible(false);
                            super.setComponent(PanChat);
                            PanChat.setVisible(true);                        // proceed to chat session
                            ChatClientChatHandler.connect(tfUserName.getText()); // initiate persistant connection for chatting. 
                        } else {
                            System.out.println("Invalid error code..."); // the server returned an invalid reply... (shoulnd't happen) 
                        }
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(ChatClientView.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(ChatClientView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                else { // i want to go back to the main menu (i don't want to login) 
                    PanNewUser.setVisible(false);
                    super.setComponent(mainPanel);
                    mainPanel.setVisible(true);         // go back to main menu
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ChatClientView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ChatClientView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else { // checkFields() returned false, at least one of the users is illegal. (Either it used invalid characters, or the server reported that it already exists, and duplicates are not allowed)
            javax.swing.JOptionPane.showMessageDialog(null, "Fix the errors (red X)", "Error!", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
}//GEN-LAST:event_btnSubmitActionPerformed

    private void tfPassword2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfPassword2FocusLost
        if (!String.valueOf(tfPassword.getPassword()).equals(String.valueOf(tfPassword2.getPassword()))) {
            lblPassCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/redX.png")));
            lblPassCheck.setToolTipText("Passwords do not match");
            bPass = false;
        }
        else if (tfPassword.getPassword().length < 6) {
            lblPassCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/redX.png")));
            lblPassCheck.setToolTipText("Password must be at least 6 characters long.");
            bPass = false;
        }
        else { // legal password
            lblPassCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/greenTick.png")));
            lblPassCheck.setToolTipText(null);
            bPass = true; // set as a valid password. 
        }
        lblPassCheck.setVisible(true);
}//GEN-LAST:event_tfPassword2FocusLost

    private void tfUserNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfUserNameFocusLost
        String allowableCharacters = "[A-Za-z0-9]*";
        Pattern regex = Pattern.compile(allowableCharacters);
        if (regex.matcher(tfUserName.getText()).matches()) {
            try {
                if (tfUserName.getText().isEmpty()) {
                    lblUnameCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/redX.png")));
                    lblUnameCheck.setToolTipText("Username cannot be emtpy");
                    bUname = false;
                }
                else { // check if the username already exists on the server side. This is done on focusLost for the username field. 
                    if (sendMessageToServer("CHEK", "username:" + tfUserName.getText()).equals("Available")) { // if the reply is "available", ie it's an available username. 
                        lblUnameCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/greenTick.png")));
                        lblUnameCheck.setToolTipText(null);
                        bUname = true; // set as a valid username. 
                    }
                    else {
                        lblUnameCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/redX.png")));
                        lblUnameCheck.setToolTipText("Username already exists");
                        bUname = false;
                    }
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ChatClientView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ChatClientView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else { // illegal characters. Allowed characters are alphanumeric characters. 
            lblUnameCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/redX.png")));
            lblUnameCheck.setToolTipText("Illegal characters inputted");
            bUname = false;
        }
        lblUnameCheck.setVisible(true);
}//GEN-LAST:event_tfUserNameFocusLost

    private void settingsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsMenuItemActionPerformed
        // Open Settings window
}//GEN-LAST:event_settingsMenuItemActionPerformed

    private void tfEmailFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfEmailFocusLost
        Pattern regex = Pattern.compile("^(([A-Za-z0-9]+_+)|([A-Za-z0-9]+\\-+)|([A-Za-z0-9]+\\.+)|([A-Za-z0-9]+\\++))*[A-Za-z0-9]+@((\\w+\\-+)|(\\w+\\.))*\\w{1,63}\\.[a-zA-Z]{2,6}$");
        if(regex.matcher(tfEmail.getText()).matches()) {
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
}//GEN-LAST:event_tfEmailFocusLost

    private void tfNickNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfNickNameFocusLost
        String allowableCharacters = "[A-Za-z0-9]*";
        Pattern regex = Pattern.compile(allowableCharacters);
        if (regex.matcher(tfNickName.getText()).matches()) {
            try {
                if (tfNickName.getText().isEmpty()) {
                    lblNickCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/redX.png")));
                    lblNickCheck.setToolTipText("Nickname cannot be empty");
                    bNick = false;
                }
                else {
                    if (sendMessageToServer("CHEK", "nickname:" + tfNickName.getText()).equals("Available")) {
                        lblNickCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/greenTick.png")));
                        lblNickCheck.setToolTipText(null);
                        bNick = true;
                    }
                    else {
                        lblNickCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/redX.png")));
                        lblNickCheck.setToolTipText("Nickname already in use");
                        bNick = false;
                    }
                }
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
        }
        lblNickCheck.setVisible(true);
}//GEN-LAST:event_tfNickNameFocusLost

    private void tfFNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfFNameFocusLost
        String allowableCharacters = "[A-Za-z0-9]*";
        Pattern regex = Pattern.compile(allowableCharacters);
        if (!regex.matcher(tfFName.getText()).matches() || tfFName.getText().isEmpty()) {
            lblFNameCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/redX.png")));
            if (tfFName.getText().isEmpty())
                lblFNameCheck.setToolTipText("First name cannot be empty"); 
            else lblFNameCheck.setToolTipText("Illegal characters inputted");
            bFName= false;
        }
        else {
            lblFNameCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/greenTick.png")));
            lblFNameCheck.setToolTipText(null);
            bFName= true;
        }
        lblFNameCheck.setVisible(true);
}//GEN-LAST:event_tfFNameFocusLost

    private void tfLNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfLNameFocusLost
        String allowableCharacters = "[A-Za-z0-9]*";
        Pattern regex = Pattern.compile(allowableCharacters);
        if (!regex.matcher(tfLName.getText()).matches() || tfLName.getText().isEmpty()) {
            lblLNameCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/redX.png")));
            if (tfLName.getText().isEmpty())
                lblLNameCheck.setToolTipText("Last name cannot be empty"); 
            else lblLNameCheck.setToolTipText("Illegal characters inputted");
            bLName = false;
        }
        else {
            lblLNameCheck.setIcon(new ImageIcon(getClass().getClassLoader().getResource("chatclient/resources/icons/greenTick.png")));
            lblLNameCheck.setToolTipText(null);
            bLName = true;
        }
        lblFNameCheck.setVisible(true);
}//GEN-LAST:event_tfLNameFocusLost

    private void btntLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btntLoginActionPerformed
        try {
            String reply = sendMessageToServer("LOGN", tfUsernameLogin.getText() + "," + MD5Hash(String.valueOf(tfPassLogin.getPassword())));
            
            if (reply.equals("ERR1")) {
                javax.swing.JOptionPane.showMessageDialog(super.getFrame(), "User already logged in...");
            } else if (reply.equals("ERR2")) {
                javax.swing.JOptionPane.showMessageDialog(super.getFrame(), "Invalid username/password...");
            } else if (reply.equals("SUCC")) {
                mainPanel.setVisible(false);
                super.setComponent(PanChat);
                PanChat.setVisible(true);                                                   // view chat panel
                ChatClientChatHandler.connect(tfUsernameLogin.getText()); // on login, we establish a persistant connection for chatting purposes. 
            } else {
                System.out.println("Invalid error code...");
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ChatClientView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ChatClientView.class.getName()).log(Level.SEVERE, null, ex);
        }
}//GEN-LAST:event_btntLoginActionPerformed
/*
  * This function simply clears the fields in the new user account. This is required in case we want to create another user, or go back and then go forward again, etc. 
 *  It also clears the icons. 
 */
    private void clearNewUserFields() {
        String empty = ""; 
        tfUserName.setText(empty); tfPassword.setText(empty); tfPassword2.setText(empty); tfNickName.setText(empty); tfEmail.setText(empty); tfFName.setText(empty); tfLName.setText(empty); 
        lblUnameCheck.setIcon(null); lblPassCheck.setIcon(null); lblEmailCheck.setIcon(null); lblNickCheck.setIcon(null); lblFNameCheck.setIcon(null); lblLNameCheck.setIcon(null);
    }
    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        PanNewUser.setVisible(false);
        super.setComponent(mainPanel);
        mainPanel.setVisible(true); // simply go back to main menu
    }//GEN-LAST:event_btnBackActionPerformed
// </editor-fold>    
    /*
     * This function is called on Enter pressed in the chatting text field. 
     * It sends the text in the chatting text field to the server. 
     * It also modifies it accordingly if it's normal text or a special command. 
     */
    private void tfSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfSendActionPerformed
        try {
            String toSend = tfSend.getText(); 
            
            if (toSend.isEmpty())
                return; 
            if (toSend.startsWith("//")) {
                toSend = '-'+toSend.substring(1);
                ChatClientChatHandler.send(toSend + '\n');
            }
            else if (!toSend.startsWith("/")) { /// if it's a command, send it without the prefix "-". 
                toSend = '-'+toSend; 
                ChatClientChatHandler.send(toSend + '\n');
            }
            else { // it's a command
                // If we have time, change this so that it sends and waits for a reply from server in a seperate thread
                if (toSend.equalsIgnoreCase("/DISC")) {
                    tfSend.setEnabled(false); 
                    ChatClientChatHandler.disconnect();
                }
                else if (toSend.toUpperCase().startsWith("/SEND ") || 
                         toSend.substring(0, "/NICK ".length()).equalsIgnoreCase("/NICK ")) {
                    ChatClientChatHandler.send(toSend + '\n');
                }
                else {
                    txtMessages.setText(txtMessages.getText() + "Invalid Command\n"); // display "invalid command" in the main text area. 
                }
            }
            tfSend.setText(""); // clear text field
        } catch (IOException ex) {
            Logger.getLogger(ChatClientView.class.getName()).log(Level.SEVERE, null, ex);
        }
}//GEN-LAST:event_tfSendActionPerformed

    // this function simply sends the text if it's larger than 1024 characters. this is used to help avoid spamming by writing huge amounts of text.
    // however it will need more tweaking to really avoid spamming. 
    private void tfSendKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfSendKeyTyped
        if (tfSend.getText().length() >= 1024) {
            tfSendActionPerformed(null);
        }
}//GEN-LAST:event_tfSendKeyTyped

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        ChatClientChatHandler.disconnect();
        System.exit(0); 
    }//GEN-LAST:event_exitMenuItemActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PanChat;
    private javax.swing.JPanel PanNewUser;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnSubmit;
    private javax.swing.JButton btntGuest;
    private javax.swing.JButton btntLogin;
    private javax.swing.JButton btntNewAcc;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblEmailCheck;
    private javax.swing.JLabel lblExUser;
    private javax.swing.JLabel lblFName;
    private javax.swing.JLabel lblFNameCheck;
    private javax.swing.JLabel lblLName;
    private javax.swing.JLabel lblLNameCheck;
    private javax.swing.JLabel lblNickCheck;
    private javax.swing.JLabel lblNickName;
    private javax.swing.JLabel lblPass;
    private javax.swing.JLabel lblPassCheck;
    private javax.swing.JLabel lblPassLogin;
    private javax.swing.JLabel lblRePass;
    private javax.swing.JLabel lblUnameCheck;
    private javax.swing.JLabel lblUserLogin;
    private javax.swing.JLabel lblUsername;
    private javax.swing.JLabel lblWelcome;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JScrollPane scrollChans;
    private javax.swing.JScrollPane scrollUsers;
    private javax.swing.JMenuItem settingsMenuItem;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    public static javax.swing.JTable tblUsers;
    private javax.swing.JTabbedPane tbsChan;
    private javax.swing.JTextField tfEmail;
    private javax.swing.JTextField tfFName;
    private javax.swing.JTextField tfLName;
    private javax.swing.JTextField tfNickName;
    private javax.swing.JPasswordField tfPassLogin;
    private javax.swing.JPasswordField tfPassword;
    private javax.swing.JPasswordField tfPassword2;
    private javax.swing.JTextField tfSend;
    private javax.swing.JTextField tfUserName;
    private javax.swing.JTextField tfUsernameLogin;
    public static javax.swing.JTextPane txtMessages;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
    private JDialog settingsBox;
    private boolean bEmail = false, bUname = false, bPass = false, bNick = false, bFName = false, bLName = false;
}