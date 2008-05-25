/*
 * ChatClientSettingsBox.java
 *
 * Created on May 15, 2008, 9:03 PM
 */

package chatclient;

import java.io.*;
import org.jdesktop.application.Action;

public class ChatClientSettingsBox extends javax.swing.JDialog {
    
    /** Creates new form ChatClientSettingsBox */
    public ChatClientSettingsBox(java.awt.Frame parent) throws IOException {
        super(parent);
        initComponents();
        File testFile = new File("settings.ini");
        if (!testFile.exists()) {
            PrintWriter outputStream = new PrintWriter(new FileWriter("settings.ini"));
            outputStream.println("localhost");
            outputStream.println("55555");
            outputStream.flush();
            outputStream.close();
        }
        
        BufferedReader inputStream = new BufferedReader(new FileReader("settings.ini"));
        txtServer.setText(inputStream.readLine());
        ftfPort.setText(inputStream.readLine());
        inputStream.close();
        getRootPane().setDefaultButton(cancelButton);
    }
    @Action public void closeSettingsBox() {
        setVisible(false);
    }

    @Action public void cancelSettingsBox() throws FileNotFoundException, IOException {
        BufferedReader inputStream = new BufferedReader(new FileReader("settings.ini"));
        txtServer.setText(inputStream.readLine());
        ftfPort.setText(inputStream.readLine());
        inputStream.close();
        closeSettingsBox();
    }
    
    @Action public void defaultSettingsBox() throws FileNotFoundException, IOException {
        txtServer.setText("localhost");
        ftfPort.setText("55555");
    }
        
    @Action public void saveSettings() throws IOException {
        PrintWriter outputStream = new PrintWriter(new FileWriter("settings.ini"));
        outputStream.println(txtServer.getText());
        outputStream.println(ftfPort.getText());
        outputStream.flush();
        outputStream.close();
        closeSettingsBox();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cancelButton = new javax.swing.JButton();
        lblServer = new javax.swing.JLabel();
        lblPort = new javax.swing.JLabel();
        txtServer = new javax.swing.JTextField();
        defaultButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        ftfPort = new javax.swing.JFormattedTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(chatclient.ChatClientApp.class).getContext().getResourceMap(ChatClientSettingsBox.class);
        setTitle(resourceMap.getString("settingsBox.title")); // NOI18N
        setModal(true);
        setName("settingsBox"); // NOI18N
        setResizable(false);

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(chatclient.ChatClientApp.class).getContext().getActionMap(ChatClientSettingsBox.class, this);
        cancelButton.setAction(actionMap.get("cancelSettingsBox")); // NOI18N
        cancelButton.setText(resourceMap.getString("cancelButton.text")); // NOI18N
        cancelButton.setName("cancelButton"); // NOI18N

        lblServer.setText(resourceMap.getString("lblServer.text")); // NOI18N
        lblServer.setName("lblServer"); // NOI18N

        lblPort.setText(resourceMap.getString("lblPort.text")); // NOI18N
        lblPort.setName("lblPort"); // NOI18N

        txtServer.setText(resourceMap.getString("txtServer.text")); // NOI18N
        txtServer.setName("txtServer"); // NOI18N

        defaultButton.setAction(actionMap.get("defaultSettingsBox")); // NOI18N
        defaultButton.setText(resourceMap.getString("defaultButton.text")); // NOI18N
        defaultButton.setName("defaultButton"); // NOI18N

        saveButton.setAction(actionMap.get("saveSettings")); // NOI18N
        saveButton.setText(resourceMap.getString("saveButton.text")); // NOI18N
        saveButton.setName("saveButton"); // NOI18N

        ftfPort.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        ftfPort.setText(resourceMap.getString("ftfPort.text")); // NOI18N
        ftfPort.setName("ftfPort"); // NOI18N
        ftfPort.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                ftfPortFocusLost(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblServer)
                            .addComponent(lblPort))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(ftfPort)
                            .addComponent(txtServer, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(defaultButton, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveButton)))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblServer)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblPort))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ftfPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(177, 177, 177)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(defaultButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void ftfPortFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_ftfPortFocusLost
        if (ftfPort.getText().length() > 5)
            ftfPort.setText(ftfPort.getText().substring(0, 5));
    }//GEN-LAST:event_ftfPortFocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton defaultButton;
    private javax.swing.JFormattedTextField ftfPort;
    private javax.swing.JLabel lblPort;
    private javax.swing.JLabel lblServer;
    private javax.swing.JButton saveButton;
    private javax.swing.JTextField txtServer;
    // End of variables declaration//GEN-END:variables
}
