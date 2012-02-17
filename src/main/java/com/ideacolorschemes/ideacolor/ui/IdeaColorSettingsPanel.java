package com.ideacolorschemes.ideacolor.ui;

import com.intellij.ide.BrowserUtil;
import com.intellij.ui.HyperlinkAdapter;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class IdeaColorSettingsPanel extends IdeaColorSettingsPanelUtil {
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
        signupTextField.setText(noticeTest());
        signupTextField.setBackground(panel.getBackground());
        signupTextField.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        testButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkCredentialsAction();
            }
        });
    }

    public JComponent getPanel() {
        return panel;
    }

    @Override
    public String getUserId() {
        return userIdTextField.getText().trim();
    }
    
    public void setUserId(final String userId) {
        userIdTextField.setText(userId);
    }

    @Override
    public String getKey() {
        return keyTextField.getText().trim();
    }
    
    public void setKey(final String key) {
        keyTextField.setText(key);
    }
}
