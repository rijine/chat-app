/*
 * ChatAppServerUserList.java
 *
 * Created on May 26, 2008, 1:04 PM
 */

package chatappserver;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.jdesktop.application.Action;

public class ChatAppServerUserList extends javax.swing.JDialog {

    /** Creates new form ChatAppServerUserList */
    public ChatAppServerUserList(java.awt.Frame parent) {
        super(parent);
        initComponents();
        getRootPane().setDefaultButton(closeButton);
    }
    
    @Action public void closeUserList() {
        setVisible(false);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        closeButton = new javax.swing.JButton();
        usersScrollPane = new javax.swing.JScrollPane();
        listUsers = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(chatappserver.ChatAppServerApp.class).getContext().getActionMap(ChatAppServerUserList.class, this);
        closeButton.setAction(actionMap.get("closeUserList")); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(chatappserver.ChatAppServerApp.class).getContext().getResourceMap(ChatAppServerUserList.class);
        closeButton.setText(resourceMap.getString("closeButton.text")); // NOI18N
        closeButton.setName("closeButton"); // NOI18N

        usersScrollPane.setName("usersScrollPane"); // NOI18N

        listUsers.setName("listUsers"); // NOI18N
        usersScrollPane.setViewportView(listUsers);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(163, 163, 163)
                        .addComponent(closeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(usersScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(usersScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(closeButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    public static void updateUserList(java.sql.Connection connectToMysql) throws SQLException {
            Statement sendSQLQuery = null;
            ResultSet results = null;
            sendSQLQuery = connectToMysql.createStatement();
            sendSQLQuery.executeQuery("SELECT username FROM threadlookup");
            results = sendSQLQuery.getResultSet();
            listUsers.setText(null);
            while(results.next()) {
                listUsers.setText(listUsers.getText() + results.getString("username") + "\n");
            }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private static javax.swing.JTextPane listUsers;
    private javax.swing.JScrollPane usersScrollPane;
    // End of variables declaration//GEN-END:variables
    
}
