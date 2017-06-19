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

package cc.xehoth.graphviz.tool;

import java.awt.Component;
import java.awt.Image;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class Tools {

    public static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;

    public static BufferedWriter getDefaultBufferedWriter() throws IOException {
        return new BufferedWriter(new FileWriter("tmp.dot"), DEFAULT_BUFFER_SIZE);
    }

    public static void showErrorDialog(Component container, String message) {
        JOptionPane.showMessageDialog(container, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean transform(boolean isDirected, boolean hasWeight, String content, BufferedWriter bw,
            Component container) throws IOException {
        bw.write(isDirected ? "digraph G {\n" : "graph G {\n");
        String[] split = content.split("\\s+");
        if (hasWeight) {
            if (split.length % 3 != 0) {
                showErrorDialog(container, "Invalid Input");
                bw.flush();
                bw.close();
                bw = null;
                return false;
            }
            if (isDirected) {
                for (int i = 0; i < split.length; i += 3) {
                    bw.write("    ");
                    bw.write(split[i]);
                    bw.write(" -> ");
                    bw.write(split[i + 1]);
                    bw.write(" [label = ");
                    bw.write(split[i + 2]);
                    bw.write("];\n");
                }
            } else {
                for (int i = 0; i < split.length; i += 3) {
                    bw.write("    ");
                    bw.write(split[i]);
                    bw.write(" -- ");
                    bw.write(split[i + 1]);
                    bw.write(" [label = ");
                    bw.write(split[i + 2]);
                    bw.write("];\n");
                }
            }
        } else {
            if (split.length % 2 != 0) {
                showErrorDialog(container, "Invalid Input");
                bw.flush();
                bw.close();
                bw = null;
                return false;
            }
            if (isDirected) {
                for (int i = 0; i < split.length; i += 2) {
                    bw.write("    ");
                    bw.write(split[i]);
                    bw.write(" -> ");
                    bw.write(split[i + 1]);
                    bw.write(";\n");
                }
            } else {
                for (int i = 0; i < split.length; i += 2) {
                    bw.write("    ");
                    bw.write(split[i]);
                    bw.write(" -- ");
                    bw.write(split[i + 1]);
                    bw.write(";\n");
                }
            }
        }
        bw.write("}");
        bw.flush();
        bw.close();
        bw = null;
        return true;
    }

    public static ImageIcon getScaledImageIcon(String path, int w, int h) {
        Image img = new ImageIcon("tmp.png").getImage();
        img.flush();
        
        int x = img.getWidth(null);
        int y = img.getHeight(null);
        double scale = Math.min((double) (w) / x, (double) (h) / y);
        x = (int) (x * scale);
        y = (int) (y * scale);
        return new ImageIcon(img.getScaledInstance(x, y, Image.SCALE_SMOOTH));
    }
}
