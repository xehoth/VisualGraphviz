/*******************************************************************************
 * Copyright (c) 2017, xehoth
 * All rights reserved.
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
 ******************************************************************************/

package cc.xehoth.graphviz.frame;

import java.awt.Container;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;

import cc.xehoth.graphviz.Main;
import cc.xehoth.graphviz.tool.Tools;

public class MainFrame extends JFrame {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private Runtime runtime;
    private Process process;
    
    private String DEFAULT_EXEC = "dot -Kdot -Tpng tmp.dot -o tmp.png";

    private Font buttonFont = new Font("Consolas", Font.PLAIN, 14);
    private Font checkBoxFont = new Font("Consolas", Font.PLAIN, 13);

    private String WINDOWS_STYLE = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";

    private Container container = null;

    private JButton export;
    private JButton paint;

    private JCheckBox directed;
    private JCheckBox weight;

    private JTextArea textArea;
    private JScrollPane scroll;

    private JLabel imageLabel;
    
    public MainFrame(int w, int h) {
        init(w, h);

        addExportButton();
        addTextArea();
        addCheckBox();
        addPaintButton();

        container.add(imageLabel);
        container.add(scroll);
        container.add(export);
        container.add(paint);
        container.add(directed);
        container.add(weight);

        runtime = Runtime.getRuntime();
        this.setAutoRequestFocus(true);
        this.setVisible(true);
        this.requestFocus();

        this.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                double scaleX = (double) getWidth() / Main.DEFAULT_WIDTH,
                        scaleY = (double) getHeight() / Main.DEFAULT_HEIGHT;
                paint.setBounds((int) (750 * scaleX), (int) (10 * scaleY), (int) (80 * scaleX), (int) (30 * scaleY));

                imageLabel.setBounds((int) (0 * scaleX), (int) (0 * scaleY), (int) (520 * scaleX),
                        (int) (680 * scaleY));
                directed.setBounds((int) (635 * scaleX), (int) (17 * scaleY), (int) (100 * scaleX),
                        (int) (20 * scaleY));
                weight.setBounds((int) (530 * scaleX), (int) (17 * scaleY), (int) (100 * scaleX), (int) (20 * scaleY));
                textArea.setBounds((int) (0 * scaleX), (int) (0 * scaleY), (int) (360 * scaleX), (int) (630 * scaleY));
                scroll.setBounds((int) (525 * scaleX), (int) (50 * scaleY), (int) (415 * scaleX), (int) (630 * scaleY));
                export.setBounds((int) (855 * scaleX), (int) (10 * scaleY), (int) (80 * scaleX), (int) (30 * scaleY));
                if (imageLabel.getIcon() != null) {
                    updateAndRepaint();
                }
            }
        });
    }

    private void addPaintButton() {
        /* paint button */
        paint = new JButton("paint");
        paint.setBounds(750, 10, 80, 30);
        paint.setFont(buttonFont);

        /* image */
        imageLabel = new JLabel();
        imageLabel.setBounds(0, 0, 520, 680);

        paint.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (!Tools.transform(directed.isSelected(), weight.isSelected(), textArea.getText(),
                            Tools.getDefaultBufferedWriter(), container))
                        return;
                    process = runtime.exec(DEFAULT_EXEC);
                    process.waitFor();
                    if (process.exitValue() != 0) {
                        Tools.showErrorDialog(container, "Invalid Input");
                        return;
                    }
                    updateAndRepaint();

                } catch (IOException | InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void addCheckBox() {
        /* isDirected */

        directed = new JCheckBox("isDirected");
        directed.setBounds(635, 17, 100, 20);
        directed.setFont(checkBoxFont);

        /* hasWight */

        weight = new JCheckBox("hasWeight");
        weight.setBounds(530, 17, 100, 20);
        weight.setFont(checkBoxFont);
    }

    private void addTextArea() {
        /* text area */
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setBounds(0, 0, 360, 630);
        textArea.setWrapStyleWord(true);
        scroll = new JScrollPane(textArea);
        scroll.setBounds(525, 50, 415, 630);

        final UndoManager undo = new UndoManager();
        textArea.getDocument().addUndoableEditListener(new UndoableEditListener() {

            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                undo.addEdit(e.getEdit());
            }
        });

        textArea.getActionMap().put("Undo", new AbstractAction("Undo") {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (undo.canUndo()) {
                    undo.undo();
                }
            }
        });

        textArea.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");

        textArea.getActionMap().put("Redo", new AbstractAction("Redo") {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (undo.canRedo()) {
                    undo.redo();
                }
            }
        });

        textArea.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
    }

    private void addExportButton() {
        /* export button */
        export = new JButton("export");
        export.setBounds(855, 10, 80, 30);
        export.setFont(buttonFont);
        export.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (!Tools.transform(directed.isSelected(), weight.isSelected(), textArea.getText(),
                            Tools.getDefaultBufferedWriter(), container))
                        return;
                    process = runtime.exec(DEFAULT_EXEC);
                    process.waitFor();
                    if (process.exitValue() != 0) {
                        Tools.showErrorDialog(container, "Invalid Input");
                        return;
                    }
                    updateAndRepaint();
                } catch (IOException | InterruptedException e2) {
                    e2.printStackTrace();
                }

                Object tmp = JOptionPane.showInputDialog(container, "Please Choose the File Type", "File Type",
                        JOptionPane.QUESTION_MESSAGE, null,
                        new String[] { "svg", "png", "gif", "ps", "ps2", "psd", "svgz", "jpg", "jpeg", "bmp", "canon",
                                "dot", "gv", "xdot", "xdot1.2", "xdot1.4", "cgimage", "cmap", "eps", "exr", "fig", "gd",
                                "gd2", "gtk", "ico", "imap", "cmapx", "imap_np", "cmapx_np", "ismap", "jp2", "jpe",
                                "json", "json0", "dot_json", "xdot_json", "pct", "pict", "pic", "plain", "plain-ext",
                                "pov", "sgi", "tga", "tif", "tiff", "tk", "vml", "vmlz", "vrml", "wbmp", "webp", "xlib",
                                "x11" },
                        "svg");
                if (tmp == null)
                    return;
                String fileType = tmp.toString();
                tmp = JOptionPane.showInputDialog(container, "Please Choose the File Type", "File Type",
                        JOptionPane.QUESTION_MESSAGE, null,
                        new String[] { "dot", "neato", "twopi", "circo", "fdp", "sfdp", "patchwork" }, "dot");
                if (tmp == null)
                    return;
                String layoutType = tmp.toString();
                try {
                    process = runtime
                            .exec("dot -K" + layoutType + " -T" + fileType + " tmp.dot -o " + " export." + fileType);
                    process.waitFor();
                    if (process.exitValue() != 0)
                        Tools.showErrorDialog(container, "Export Failed!");
                } catch (IOException | InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    protected void updateAndRepaint() {
        imageLabel.setIcon(Tools.getScaledImageIcon("tmp.png", imageLabel.getWidth(), imageLabel.getHeight()));
    }

    private void init(int w, int h) {
        this.setBounds((Toolkit.getDefaultToolkit().getScreenSize().width - w) / 2,
                (Toolkit.getDefaultToolkit().getScreenSize().height - h) / 2, w, h);
        this.setTitle("Visual Graphviz v1.02");
        this.setFont(buttonFont);
        this.setLayout(null);

        /* set close operation */
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /* set window style */
        try {
            UIManager.setLookAndFeel(WINDOWS_STYLE);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        /* this.setResizable(false); */

        container = this.getContentPane();
        container.setLayout(null);
    }
}
