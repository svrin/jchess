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

import jchess.gui.GameTab;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Class representing the game interface which is seen by a player and where are
 * lockated available for player opptions, current games and where can he start
 * a new game (load it or save it)
 */
public class GUIUtils {

    private static final Properties configFile = GUIUtils.getConfigFile();
    public GameTab game;


    private GUIUtils() {
        this.game = new GameTab();

        // this.drawGUI();
    }/*--endOf-GUI--*/


    private static GUIUtils instance;


    public static GUIUtils getInstance() {
        if (GUIUtils.instance == null) {
            GUIUtils.instance = new GUIUtils();
        }

        return GUIUtils.instance;
    }

    /*
     * Method load image by a given name with extension
     *
     * @name : strings of image to load for ex. "chessboard.jpg"
     *
     * @returns : image or null if cannot load
     */


    public static BufferedImage loadImage(String name) {
        if (configFile == null) {
            return null;
        }
        BufferedImage img = null;
        URL url = null;
        try {
            String imageLink = "theme/" + configFile.getProperty("THEME", "default") + "/images/" + name;
            System.out.println(configFile.getProperty("THEME"));
            url = Application.class.getResource(imageLink);
            img = ImageIO.read(url);

        } catch (Exception e) {
            System.out.println("some error loading image!");
            e.printStackTrace();
        }
        return img;
    }/*--endOf-loadImage--*/

    public static boolean themeIsValid(String name) {
        return true;
    }

    public static String getJarPath() {
        String path = GUIUtils.class.getProtectionDomain().getCodeSource()
                      .getLocation().getFile();
        path = path.replaceAll(
                   "[a-zA-Z0-9%!@#$%^&*\\(\\)\\[\\]\\{\\}\\.\\,\\s]+\\.jar", "");
        int lastSlash = path.lastIndexOf(File.separator);
        if (path.length() - 1 == lastSlash) {
            path = path.substring(0, lastSlash);
        }
        path = path.replace("%20", " ");
        return path;
    }

    public static Properties getConfigFile() {
        Properties confFile = new Properties();

        // Try to locate config.txt
        File outFile = new File("config.txt");
        if (!outFile.exists() && Application.class.getResource("config.txt") != null) {
            outFile = new File(Application.class.getResource("config.txt").getFile());
        } else {
            return confFile;
        }

        // Read it
        try {
            FileInputStream configurationFile = new FileInputStream(outFile);
            confFile.load(configurationFile);
        } catch (java.io.IOException exc) {
            System.err.print("Error loading configuration file");
        }
        return confFile;
    }
}