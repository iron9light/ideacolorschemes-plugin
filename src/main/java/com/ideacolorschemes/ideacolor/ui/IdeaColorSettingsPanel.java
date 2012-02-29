/*
 * Copyright 2012 IL <iron9light AT gmali DOT com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
