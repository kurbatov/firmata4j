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

package org.firmata4j;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jssc.SerialPortList;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.ssd1306.SSD1306;
import org.firmata4j.ssd1306.MonochromeCanvas;

/**
 * Example of usage of an i2c device.
 *
 * @author Oleg Kurbatov &lt;o.v.kurbatov@gmail.com&gt;
 */
public class I2CExample {

    private static final JFrame INITIALIZATION_FRAME = new JFrame();

    public static void main(String[] args) throws IOException, InterruptedException {
        try { // set look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(I2CExample.class.getName()).log(Level.SEVERE, "Cannot load system look and feel.", ex);
        }
        // requesting a user to define the port name
        String port = requestPort();
        final IODevice device = new FirmataDevice(port);
        showInitializationMessage();
        device.start();
        try {
            device.ensureInitializationIsDone();
        } catch (InterruptedException e) {
            JOptionPane.showMessageDialog(INITIALIZATION_FRAME, e.getMessage(), "Connection error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        I2CDevice i2cDevice = device.getI2CDevice((byte) 0x3C);
        SSD1306.Size size = requestSize();
        final SSD1306 display = new SSD1306(i2cDevice, size);
        display.init();
        int x = (size.width - 75) / 2;
        int y = (size.height - 16) / 2;
        display.getCanvas().drawImage(x, y, ImageIO.read(I2CExample.class.getResource("/img/firmata4j.png")), true, true);
        display.display();
        hideInitializationWindow();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame mainFrame = new JFrame("SSD1306 Example");
                GridBagLayout layout = new GridBagLayout();
                mainFrame.setLayout(layout);
                GridBagConstraints constraints = new GridBagConstraints();
                constraints.gridx = 0;
                constraints.gridy = 0;
                constraints.fill = GridBagConstraints.BOTH;
                constraints.weightx = 1;
                constraints.weighty = 1;
                JButton clearButton = new JButton("Clear");
                clearButton.addActionListener(new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        display.clear();
                    }
                });
                layout.setConstraints(clearButton, constraints);
                mainFrame.add(clearButton);

                constraints = new GridBagConstraints();
                constraints.gridx = 0;
                constraints.gridy = 1;
                constraints.fill = GridBagConstraints.BOTH;
                constraints.weightx = 1;
                constraints.weighty = 1;
                JButton drawLinesButton = new JButton("Draw Lines");
                drawLinesButton.addActionListener(new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        drawLines(display);
                    }
                });
                layout.setConstraints(drawLinesButton, constraints);
                mainFrame.add(drawLinesButton);

                constraints = new GridBagConstraints();
                constraints.gridx = 0;
                constraints.gridy = 2;
                constraints.fill = GridBagConstraints.BOTH;
                constraints.weightx = 1;
                constraints.weighty = 1;
                JButton drawRectButton = new JButton("Draw Rect");
                drawRectButton.addActionListener(new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        drawRect(display);
                    }
                });
                layout.setConstraints(drawRectButton, constraints);
                mainFrame.add(drawRectButton);

                constraints = new GridBagConstraints();
                constraints.gridx = 0;
                constraints.gridy = 3;
                constraints.fill = GridBagConstraints.BOTH;
                constraints.weightx = 1;
                constraints.weighty = 1;
                JButton drawCircleButton = new JButton("Draw Circle");
                drawCircleButton.addActionListener(new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        drawCircle(display);
                    }
                });
                layout.setConstraints(drawCircleButton, constraints);
                mainFrame.add(drawCircleButton);

                mainFrame.pack();
                mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                mainFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        try {
                            display.turnOff();
                            device.stop();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        super.windowClosing(e);
                    }
                });
                mainFrame.setVisible(true);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static String requestPort() {
        JComboBox<String> portNameSelector = new JComboBox<>();
        portNameSelector.setModel(new DefaultComboBoxModel<String>());
        String[] portNames = SerialPortList.getPortNames();
        for (String portName : portNames) {
            portNameSelector.addItem(portName);
        }
        if (portNameSelector.getItemCount() == 0) {
            JOptionPane.showMessageDialog(null, "Cannot find any serial port", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.add(new JLabel("Port "));
        panel.add(portNameSelector);
        if (JOptionPane.showConfirmDialog(null, panel, "Select the port", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            return portNameSelector.getSelectedItem().toString();
        } else {
            System.exit(0);
        }
        return "";
    }

    private static SSD1306.Size requestSize() {
        JComboBox<SSD1306.Size> sizeSelector = new JComboBox<>();
        sizeSelector.setModel(new DefaultComboBoxModel<SSD1306.Size>());
        sizeSelector.addItem(SSD1306.Size.SSD1306_128_64);
        sizeSelector.addItem(SSD1306.Size.SSD1306_128_32);
        sizeSelector.addItem(SSD1306.Size.SSD1306_96_16);
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.add(new JLabel("Size "));
        panel.add(sizeSelector);
        if (JOptionPane.showConfirmDialog(null, panel, "Select size of display", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            return (SSD1306.Size) sizeSelector.getSelectedItem();
        } else {
            System.exit(0);
        }
        return null;
    }

    private static void showInitializationMessage() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    JFrame frame = INITIALIZATION_FRAME;
                    frame.setUndecorated(true);
                    JLabel label = new JLabel("Connecting to device");
                    label.setHorizontalAlignment(JLabel.CENTER);
                    frame.add(label);
                    frame.pack();
                    frame.setSize(frame.getWidth() + 40, frame.getHeight() + 40);
                    Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
                    int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
                    int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
                    frame.setLocation(x, y);
                    frame.setVisible(true);
                }
            });
        } catch (InterruptedException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void hideInitializationWindow() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    INITIALIZATION_FRAME.setVisible(false);
                    INITIALIZATION_FRAME.dispose();
                }
            });
        } catch (InterruptedException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void drawLines(SSD1306 ssd1306) {
        ssd1306.clear();
        MonochromeCanvas display = ssd1306.getCanvas();
        try {
            for (int i = 0; i < display.getWidth(); i += 4) {
                display.drawLine(0, 0, i, display.getHeight() - 1);
                ssd1306.display();
            }
            for (int i = 0; i < display.getHeight(); i += 4) {
                display.drawLine(0, 0, display.getWidth() - 1, i);
                ssd1306.display();
            }
            Thread.sleep(250);
            ssd1306.clear();
            for (int i = 0; i < display.getWidth(); i += 4) {
                display.drawLine(0, display.getHeight() - 1, i, 0);
                ssd1306.display();
            }
            for (int i = display.getHeight() - 1; i >= 0; i -= 4) {
                display.drawLine(0, display.getHeight() - 1, display.getWidth() - 1, i);
                ssd1306.display();
            }
            Thread.sleep(250);
            ssd1306.clear();
            for (int i = display.getWidth() - 1; i >= 0; i -= 4) {
                display.drawLine(display.getWidth() - 1, display.getHeight() - 1, i, 0);
                ssd1306.display();
            }
            for (int i = display.getHeight() - 1; i >= 0; i -= 4) {
                display.drawLine(display.getWidth() - 1, display.getHeight() - 1, 0, i);
                ssd1306.display();
            }
            Thread.sleep(250);

            ssd1306.clear();
            for (int i = 0; i < display.getHeight(); i += 4) {
                display.drawLine(display.getWidth() - 1, 0, 0, i);
                ssd1306.display();
            }
            for (int i = 0; i < display.getWidth(); i += 4) {
                display.drawLine(display.getWidth() - 1, 0, i, display.getHeight() - 1);
                ssd1306.display();
            }
            Thread.sleep(250);
        } catch (InterruptedException e) {
            return;
        }
    }

    private static void drawRect(SSD1306 ssd1306) {
        ssd1306.clear();
        MonochromeCanvas display = ssd1306.getCanvas();
        for (int i = 0; i < display.getHeight() / 2; i += 2) {
            display.drawRect(i, i, display.getWidth() - 2 * i, display.getHeight() - 2 * i);
            ssd1306.display();
        }
    }
    
    private static void drawCircle(SSD1306 ssd1306) {
        ssd1306.clear();
        MonochromeCanvas display = ssd1306.getCanvas();
        for (int i = 0; i < display.getHeight(); i += 2) {
            display.drawCircle(display.getWidth() / 2, display.getHeight() / 2, i);
            ssd1306.display();
        }
    }

}
