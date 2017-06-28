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
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;

import cc.xehoth.graphviz.Main;
import cc.xehoth.graphviz.config.Config;
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

    private Container container = null;

    private JButton export;
    private JButton paint;

    private JCheckBox directed;
    private JCheckBox weight;

    private JTextArea textArea;
    private JScrollPane scroll;

    private JLabel imageLabel;

    private Insets DEFAULT_INSETS = new Insets(0, 0, 0, 0);

    private boolean isPainted = true;
    private boolean[] isKeyPressed = new boolean[257];

    private boolean isClosing = false;
    private boolean isChanged = true;
    private boolean isPaintError = false;
    private boolean isUpdated = false;
    private boolean firstPainted = false;
    private boolean isCompiled = false;
    private boolean needPaint = false;

    private Config config = null;

    private Thread paintThread = new Thread(new Runnable() {

        @Override
        public void run() {
            try {
                while (!isClosing) {
                    if (!isPainted && !isPaintError) {
                        doPaint();
                        if (!isPaintError)
                            firstPainted = true;
                        isUpdated = false;
                        isPainted = true;
                        System.out.println("paint");
                    }
                    Thread.sleep(100);

                }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        }

    });

    private Thread checkTextAreaChangeThread = new Thread(new Runnable() {

        @Override
        public void run() {
            try {
                while (!isClosing) {
                    if (!isChanged && needPaint && textArea.getDocument().getLength() != 0) {
                        isPainted = false;
                        needPaint = false;
                    }
                    for (int i = 0; i < 7;) {
                        isChanged = false;
                        Thread.sleep(100);
                        i++;
                        if (isChanged)
                            i = 0;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });

    private Thread updateThread = new Thread(new Runnable() {

        @Override
        public void run() {
            while (!isClosing) {
                try {
                    if (!isUpdated && firstPainted && isPainted && isCompiled) {
                        updateAndRepaint();
                        isUpdated = true;
                    }
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

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

        try {
            config = Config.getData();
        } catch (ClassNotFoundException | IOException e1) {
            Tools.showErrorDialog(null, "Error During Initialization!");
            e1.printStackTrace();
        }

        if (config == null) {
            Tools.showErrorDialog(null, "Error During Initialization!");
            System.exit(0);
        }

        directed.setSelected(config.isDirected());
        weight.setSelected(config.isHasWeight());
        if (config.getContent() != null) {
            textArea.setText(config.getContent());
        }

        paintThread.start();
        updateThread.start();
        if (config.isAutoPainting()) {
            checkTextAreaChangeThread.start();
        } else {
            checkTextAreaChangeThread = null;
        }

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                isClosing = true;
                try {
                    if (config.isSaveLastData()) {
                        config.setDirected(directed.isSelected());
                        config.setHasWeight(weight.isSelected());
                        config.setContent(textArea.getText());
                    } else {
                        config.setDirected(false);
                        config.setHasWeight(false);
                        config.setContent(null);
                    }
                    Config.saveData(config);
                } catch (IOException e1) {
                    Tools.showErrorDialog(container, "Can't save your data!");
                    e1.printStackTrace();
                }
            }
        });

        this.setVisible(true);
        this.requestFocus();

        this.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                double scaleX = (double) getWidth() / Main.DEFAULT_WIDTH,
                        scaleY = (double) getHeight() / Main.DEFAULT_HEIGHT;
                paint.setBounds((int) (760 * scaleX), (int) (10 * scaleY), (int) (80 * scaleX), (int) (30 * scaleY));
                paint.setFont(new Font(buttonFont.getName(), buttonFont.getStyle(),
                        (int) (buttonFont.getSize() * Math.min(scaleX, scaleY))));

                imageLabel.setBounds((int) (0 * scaleX), (int) (0 * scaleY), (int) (520 * scaleX),
                        (int) (680 * scaleY));
                directed.setBounds((int) (635 * scaleX), (int) (17 * scaleY), (int) (125 * scaleX),
                        (int) (20 * scaleY));
                weight.setBounds((int) (530 * scaleX), (int) (17 * scaleY), (int) (100 * scaleX), (int) (20 * scaleY));
                weight.setFont(new Font(checkBoxFont.getName(), checkBoxFont.getStyle(),
                        (int) (checkBoxFont.getSize() * Math.min(scaleX, scaleY))));
                directed.setFont(new Font(checkBoxFont.getName(), checkBoxFont.getStyle(),
                        (int) (checkBoxFont.getSize() * Math.min(scaleX, scaleY))));
                textArea.setBounds((int) (0 * scaleX), (int) (0 * scaleY), (int) (360 * scaleX), (int) (630 * scaleY));
                scroll.setBounds((int) (525 * scaleX), (int) (50 * scaleY), (int) (415 * scaleX), (int) (630 * scaleY));
                export.setBounds((int) (855 * scaleX), (int) (10 * scaleY), (int) (80 * scaleX), (int) (30 * scaleY));
                export.setFont(new Font(buttonFont.getName(), buttonFont.getStyle(),
                        (int) (buttonFont.getSize() * Math.min(scaleX, scaleY))));
                if (imageLabel.getIcon() != null) {
                    isUpdated = false;
                }
            }
        });
    }

    private void addPaintButton() {
        /* paint button */
        paint = new JButton("paint");
        paint.setBounds(760, 10, 80, 30);
        paint.setFont(buttonFont);
        paint.setMargin(DEFAULT_INSETS);

        /* image */
        imageLabel = new JLabel();
        imageLabel.setBounds(0, 0, 520, 680);

        paint.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                isPainted = false;
            }
        });
    }

    protected void doPaint() throws IOException, InterruptedException {
        isCompiled = false;
        if (!Tools.transform(directed.isSelected(), weight.isSelected(), textArea.getText(),
                Tools.getDefaultBufferedWriter(), container)) {
            isPaintError = true;
            return;
        }
        isCompiled = true;
        process = runtime.exec(DEFAULT_EXEC);
        process.waitFor();
        if (process.exitValue() != 0) {
            Tools.showErrorDialog(container, "Invalid Input");
            isPaintError = true;
            return;
        }
        isPainted = true;
    }

    private void addCheckBox() {
        /* isDirected */

        directed = new JCheckBox("isDirected");
        directed.setBounds(635, 17, 125, 20);
        directed.setFont(checkBoxFont);
        directed.setMargin(DEFAULT_INSETS);

        /* hasWight */

        weight = new JCheckBox("hasWeight");
        weight.setBounds(530, 17, 100, 20);
        weight.setFont(checkBoxFont);
        weight.setMargin(DEFAULT_INSETS);
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
        textArea.requestFocus();
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                isKeyPressed[e.getKeyCode()] = true;
                if (isKeyPressed[KeyEvent.VK_CONTROL] && isKeyPressed[KeyEvent.VK_P]) {
                    isPainted = false;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                isKeyPressed[e.getKeyCode()] = false;
            }
        });

        textArea.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                isPainted = isChanged = needPaint = true;
                isPaintError = false;
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                isPainted = isChanged = needPaint = true;
                isPaintError = false;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                isPainted = isChanged = needPaint = true;
                isPaintError = false;
            }
        });
    }

    private void addExportButton() {
        /* export button */
        export = new JButton("export");
        export.setBounds(855, 10, 80, 30);
        export.setFont(buttonFont);
        export.setMargin(DEFAULT_INSETS);

        export.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    doExport();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    protected void doExport() throws IOException {
        if (!Tools.transform(directed.isSelected(), weight.isSelected(), textArea.getText(),
                Tools.getDefaultBufferedWriter(), container)) {
            isPaintError = true;
            return;
        }
        Object tmp = JOptionPane.showInputDialog(container, "Please Choose the File Type", "File Type",
                JOptionPane.QUESTION_MESSAGE, null,
                new String[] { "svg", "png", "gif", "ps", "ps2", "psd", "pdf", "svgz", "jpg", "jpeg", "bmp", "canon", "dot",
                        "gv", "xdot", "xdot1.2", "xdot1.4", "cgimage", "cmap", "eps", "exr", "fig", "gd", "gd2", "gtk",
                        "ico", "imap", "cmapx", "imap_np", "cmapx_np", "ismap", "jp2", "jpe", "json", "json0",
                        "dot_json", "xdot_json", "pct", "pict", "pic", "plain", "plain-ext", "pov", "sgi", "tga", "tif",
                        "tiff", "tk", "vml", "vmlz", "vrml", "wbmp", "webp", "xlib", "x11" },
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
            process = runtime.exec("dot -K" + layoutType + " -T" + fileType + " tmp.dot -o " + " export." + fileType);
            process.waitFor();
            if (process.exitValue() != 0)
                Tools.showErrorDialog(container, "Export Failed!");
        } catch (IOException | InterruptedException e1) {
            e1.printStackTrace();
        }
    }

    protected void updateAndRepaint() {
        imageLabel.setIcon(Tools.getScaledImageIcon("tmp.png", imageLabel.getWidth(), imageLabel.getHeight()));
    }

    private void init(int w, int h) {
        this.setBounds((Toolkit.getDefaultToolkit().getScreenSize().width - w) / 2,
                (Toolkit.getDefaultToolkit().getScreenSize().height - h) / 2, w, h);
        this.setTitle("Visual Graphviz v1.04");
        this.setFont(buttonFont);
        this.setLayout(null);

        /* set close operation */
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /* set window style */
        try {
            /* UIManager.setLookAndFeel(WINDOWS_STYLE); */
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        /* this.setResizable(false); */

        container = this.getContentPane();
        container.setLayout(null);
    }
}
