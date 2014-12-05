/*
#    This program is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Authors:
 * Mateusz Sławomir Lach ( matlak, msl )
 * Damian Marciniak
 */
package jchess;

import jchess.resources.strings.StringResources;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

public class ThemeChooseWindow extends JDialog implements ActionListener,
    ListSelectionListener {

    public String result;
    JList themesList;
    ImageIcon themePreview;
    GridBagLayout gbl;
    GridBagConstraints gbc;
    JButton themePreviewButton;
    JButton okButton;
    String basedir = GUI.getJarPath() + File.separator + "jchess";

    ThemeChooseWindow(Frame parent) throws Exception {
        super(parent);
        File dir = new File(basedir + File.separator + "theme");

        if (dir.exists()) {
            File[] files = dir.listFiles();
            this.setTitle(StringResources.MAIN.getString("choose_theme_window_title"));
            Dimension winDim = new Dimension(550, 230);
            this.setMinimumSize(winDim);
            this.setMaximumSize(winDim);
            this.setSize(winDim);
            this.setResizable(false);
            this.setLayout(null);
            this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            String[] dirNames = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                dirNames[i] = files[i].getName();
            }
            this.themesList = new JList(dirNames);
            this.themesList.setLocation(new Point(10, 10));
            this.themesList.setSize(new Dimension(100, 120));
            this.add(this.themesList);
            this.themesList.setSelectionMode(0);
            this.themesList.addListSelectionListener(this);
            Properties prp = GUI.getConfigFile();

            this.gbl = new GridBagLayout();
            this.gbc = new GridBagConstraints();
            try {
                this.themePreview = new ImageIcon(GUI.loadImage("Preview.png"));
            } catch (java.lang.NullPointerException exc) {
                System.out.println("Cannot find preview image: " + exc);
                this.themePreview = new ImageIcon(
                    Application.class.getResource("theme/noPreview.png"));
                return;
            }
            this.result = "";
            this.themePreviewButton = new JButton(this.themePreview);
            this.themePreviewButton.setLocation(new Point(110, 10));
            this.themePreviewButton.setSize(new Dimension(420, 120));
            this.add(this.themePreviewButton);
            this.okButton = new JButton("OK");
            this.okButton.setLocation(new Point(175, 140));
            this.okButton.setSize(new Dimension(200, 50));
            this.add(this.okButton);
            this.okButton.addActionListener(this);
            this.setModal(true);
        } else {
            throw new Exception(
                    StringResources.MAIN.getString("error_when_creating_theme_config_window"));
        }

    }

    @Override
    public void valueChanged(ListSelectionEvent event) {
        String element = this.themesList.getModel()
                         .getElementAt(this.themesList.getSelectedIndex()).toString();
        String path = basedir + File.separator + "theme/";
        System.out.println(path + element + "/images/Preview.png");
        this.themePreview = new ImageIcon(path + element
                                          + "/images/Preview.png");
        this.themePreviewButton.setIcon(this.themePreview);
    }

    /**
     * Method wich is changing a pawn into queen, rook, bishop or knight
     *
     *            Capt information about performed action
     */
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == this.okButton) {
            Properties prp = GUI.getConfigFile();
            int element = this.themesList.getSelectedIndex();
            String name = this.themesList.getModel().getElementAt(element)
                          .toString();
            if (GUI.themeIsValid(name)) {
                prp.setProperty("THEME", name);
                try {
                    FileOutputStream fOutStr = new FileOutputStream("config.txt");
                    prp.store(fOutStr, null);
                    fOutStr.flush();
                    fOutStr.close();
                } catch (java.io.IOException exc) {
                }
                JOptionPane.showMessageDialog(this,
                        StringResources.MAIN.getString("changes_visible_after_restart"));
                this.setVisible(false);

            }
            System.out.print(prp.getProperty("THEME"));
        }
    }
}
