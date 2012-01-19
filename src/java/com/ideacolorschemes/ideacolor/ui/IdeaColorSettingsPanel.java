package com.ideacolorschemes.ideacolor.ui;

import com.ideacolorschemes.ideacolor.SiteUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.HyperlinkAdapter;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class IdeaColorSettingsPanel {
    private JTextField userIdTextField;
    private JTextField keyTextField;
    private JPanel panel;
    private JTextPane signupTextField;
    private JButton testButton;

    public IdeaColorSettingsPanel() {
        signupTextField.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            public void hyperlinkActivated(HyperlinkEvent e) {
                BrowserUtil.launchBrowser(e.getURL().toExternalForm());
            }
        });
        signupTextField.setText(
          "<html>Do not have an account at ideacolorscheme.com? <a href=\"http://localhost:8080\">Sign up</a></html>"
        );
        signupTextField.setBackground(panel.getBackground());
        signupTextField.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        testButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(SiteUtil.checkCredentials(getUserId(), getKey(), ProjectManager.getInstance().getDefaultProject())) {
                    Messages.showInfoMessage("Connection successful", "Success");
                } else {
                    Messages.showErrorDialog("Cannot login to the ideacolorscheme using given credentials", "Failure");
                }
            }
        });
    }

    public JComponent getPanel() {
        return panel;
    }

    public String getUserId() {
        return userIdTextField.getText().trim();
    }
    
    public void setUserId(final String userId) {
        userIdTextField.setText(userId);
    }
    
    public String getKey() {
        return keyTextField.getText().trim();
    }
    
    public void setKey(final String key) {
        keyTextField.setText(key);
    }
}
