/* 
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Oleg Kurbatov (o.v.kurbatov@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.firmata4j.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.firmata4j.IODevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays pins states of {@link IODevice}.
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public class JPinboard extends JPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(JPinboard.class);

    public JPinboard(IODevice model) {
        JPanel anchor = new JPanel();
        GridBagLayout layout = new GridBagLayout();
        anchor.setLayout(layout);
        int preferredHeight = 70;
        GridBagConstraints constraints = new GridBagConstraints();
        for (int i = 0; i < model.getPinsCount(); i++) {
            JPin pin = new JPin(model.getPin(i));
            constraints = new GridBagConstraints();
            constraints.gridy = 0;
            constraints.weightx = 1;
            constraints.anchor = GridBagConstraints.CENTER;
            constraints.fill = GridBagConstraints.NONE;
            layout.setConstraints(pin, constraints);
            anchor.add(pin);
            constraints = new GridBagConstraints();
            constraints.gridy = 1;
            constraints.anchor = GridBagConstraints.CENTER;
            constraints.fill = GridBagConstraints.NONE;
            JLabel label = new JLabel(String.valueOf(i));
            layout.setConstraints(label, constraints);
            anchor.add(label);
        }
        if (model.getEncoderCount()>0) {
            preferredHeight += 70;
            for (int i=0; i<model.getEncoderCount(); i++) {
                JEncoder encoder = new JEncoder(model.getEncoder(i));
                constraints = new GridBagConstraints();
                constraints.gridy = 2;
                constraints.weightx = 1;
                constraints.fill = GridBagConstraints.HORIZONTAL;
                layout.setConstraints(encoder, constraints);
                anchor.add(encoder);
                constraints = new GridBagConstraints();
                constraints.gridy = 3;
                constraints.anchor = GridBagConstraints.CENTER;
                constraints.fill = GridBagConstraints.NONE;
                JLabel label = new JLabel(String.valueOf(i));
                layout.setConstraints(label, constraints);
                anchor.add(label);
            }
        }
        JScrollPane scroll = new JScrollPane(anchor, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setPreferredSize(new java.awt.Dimension(70*model.getPinsCount(), preferredHeight));
        GridBagLayout mainLayout = new GridBagLayout();
        this.setLayout(mainLayout);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        mainLayout.setConstraints(scroll, constraints);
        this.add(scroll);
        this.doLayout();
    }
    
}
