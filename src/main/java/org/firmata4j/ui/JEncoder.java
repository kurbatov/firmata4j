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

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.firmata4j.Encoder;
import org.firmata4j.EncoderEventListener;
import org.firmata4j.IOEvent;
import org.firmata4j.Pin;

/**
 * Displays representation of an {@link Encoder}.
 *
 * @author Jeffrey Kuhn &lt;drjrkuhn@gmail.com&gt;
 */
public class JEncoder extends JTextField implements EncoderEventListener {

    private Encoder model;
    private JPopupMenu attachMenu;
    private Runnable refreshRoutine = new Runnable() {
        @Override
        public void run() {
            refreshText();
        }
    };

    static {
        ClassLoader classLoader = JEncoder.class.getClassLoader();
    }

    public JEncoder(Encoder encoder) {
        setHorizontalAlignment(JLabel.CENTER);
        attachMenu = new JPopupMenu(String.valueOf(encoder.getIndex()));
        setModel(encoder);
        this.setEnabled(false);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
//                if (e.getClickCount() == 2) {
//                    if (model.getMode() == Pin.Mode.OUTPUT) {
//                        long newValue = (model.getValue() == 0 ? 1 : 0);
//                        try {
//                            model.setValue(newValue);
//                        } catch (IOException ex) {
//                            JOptionPane.showMessageDialog(JEncoder.this, ex.getLocalizedMessage(), "", JOptionPane.ERROR_MESSAGE);
//                        }
//                    }
//                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    attachMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    attachMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    @Override
    public void onPositionChange(IOEvent event) {
        SwingUtilities.invokeLater(refreshRoutine);
    }

    @Override
    public void onAttach(IOEvent event) {
        SwingUtilities.invokeLater(refreshRoutine);
        System.out.print(DeviceReport.formatEncoderList(model.getDevice()));
    }

    @Override
    public void onDetach(IOEvent event) {
        SwingUtilities.invokeLater(refreshRoutine);
        System.out.print(DeviceReport.formatEncoderList(model.getDevice()));
    }
    
    public final void setModel(final Encoder model) {
        if (this.model != null) {
            this.model.removeEventListener(this);
            attachMenu.removeAll();
        }
        this.model = model;
        Action action;
        action = new AbstractAction("Attach") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inputValue = JOptionPane.showInputDialog("Enter two comma separated encoder pins", "2, 3");
                String[] pinValues = inputValue.split(",");
                List<Pin> pins = new ArrayList<Pin>(pinValues.length);
                for (String val : pinValues) {
                    int pinId = Integer.parseInt(val.trim());
                    if (pinId >= 0 && pinId < model.getDevice().getPinsCount()) {
                        pins.add(model.getDevice().getPin(pinId));
                    }
                }
                if (pins.size() != 2) {
                    JOptionPane.showMessageDialog(JEncoder.this, "Invalid encoder pins "+inputValue,"",JOptionPane.ERROR_MESSAGE);
                }
                try {
                    model.attach(pins.get(0), pins.get(1));
                } catch (IOException | IllegalArgumentException | IllegalStateException ex) {
                    Logger.getLogger(JEncoder.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(JEncoder.this, ex.getLocalizedMessage(), "", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        attachMenu.add(action);
        action = new AbstractAction("Detach") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    model.detach();
                } catch (IOException | IllegalArgumentException | IllegalStateException ex) {
                    Logger.getLogger(JEncoder.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(JEncoder.this, ex.getLocalizedMessage(), "", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        attachMenu.add(action);
        action = new AbstractAction("Reset") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    model.resetPosition();
                } catch (IOException | IllegalArgumentException | IllegalStateException ex) {
                    Logger.getLogger(JEncoder.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(JEncoder.this, ex.getLocalizedMessage(), "", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        attachMenu.add(action);

        model.addEventListener(this);
        refreshText();
    }
    
    private void refreshText() {
        if (model.isAttached()) {
            setText(String.valueOf(model.getPosition()));
        } else {
            setText("detached");
        }
    }

}
