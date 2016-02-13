/* 
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.main.managers.library;

import delfos.ConsoleParameters;
import delfos.common.Global;
import delfos.main.Main;
import delfos.main.managers.CaseUseMode;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * @version 21-oct-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class Version extends CaseUseMode {

    public static final String PRINT_VERSION_PARAMETER = "--version";

    public static Version getInstance() {
        return VersionHolder.INSTANCE;
    }

    private static class VersionHolder {

        private static final Version INSTANCE = new Version();
    }

    public Version() {
    }

    @Override
    public String getModeParameter() {
        return PRINT_VERSION_PARAMETER;
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        String versionfinal = getVersionfinal(Main.class);
        Global.showln("Compile timestamp: " + versionfinal);
    }

    public static String getVersionfinal(Class classe) {

        String version;
        String shortClassName = classe.getName().substring(classe.getName().lastIndexOf(".") + 1);
        try {
            ClassLoader cl = classe.getClassLoader();
            String threadContexteClass = classe.getName().replace('.', '/');
            URL url = cl.getResource(threadContexteClass + ".class");
            if (url == null) {
                version = shortClassName + " $ (no manifest)";
            } else {
                String path = url.getPath();
                String jarExt = ".jar";
                int index = path.indexOf(jarExt);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                if (index != -1) {
                    String jarPath = path.substring(0, index + jarExt.length());
                    File file = new File(jarPath);
                    String jarVersion = file.getName();
                    try (JarFile jarFile = new JarFile(new File(new URI(jarPath)))) {
                        JarEntry entry = jarFile.getJarEntry("META-INF/MANIFEST.MF");
                        version = jarVersion.substring(0, jarVersion.length()
                                - jarExt.length()) + " $ "
                                + sdf.format(new java.util.Date(entry.getTime()));
                    }
                } else {
                    File file = new File(path);
                    version = sdf.format(new java.util.Date(file.lastModified()));
                }
            }
        } catch (IOException | URISyntaxException e) {
            version = e.toString();
        }
        return version;
    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
