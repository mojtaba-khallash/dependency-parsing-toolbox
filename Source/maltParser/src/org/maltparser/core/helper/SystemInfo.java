package org.maltparser.core.helper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import org.maltparser.core.exception.MaltChainedException;
import org.maltparser.core.options.OptionManager;

/**
 *
 *
 * @author Johan Hall
 */
public class SystemInfo {

    private static SystemInfo uniqueInstance = new SystemInfo();
    private static String version;
    private static String buildDate;
    private static Attributes manifestAttributes;
    private static File maltJarPath;

    private SystemInfo() {
        String separator = File.separator;
        if (separator.equals("\\")) {
            separator = "\\\\";
        }

        try {
            getManifestInfo();

            String maltJarSimpleName = "malt.jar";
            String maltJarVersionName = (version != null && version.length() > 0) ? "malt-" + version + ".jar" : "";
            Pattern MALTJAR = Pattern.compile("^.*malt[^" + separator + "]*\\.jar$");

            String[] jarfiles = System.getProperty("java.class.path").split(File.pathSeparator);
            for (int i = 0; i < jarfiles.length; i++) {
                if (jarfiles[i].endsWith(maltJarSimpleName) || jarfiles[i].endsWith(maltJarVersionName)) {
                    maltJarPath = new File(new File(jarfiles[i]).getAbsolutePath());
                }
            }
            if (maltJarPath == null || maltJarPath.length() == 0) {
                for (int i = 0; i < jarfiles.length; i++) {
                    if (MALTJAR.matcher(jarfiles[i]).matches()) {
                        maltJarPath = new File(new File(jarfiles[i]).getAbsolutePath());
                    }
                }
            }
            if (maltJarPath == null || maltJarPath.length() == 0) {
                String codeBasePath = SystemInfo.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                if (codeBasePath.endsWith(maltJarSimpleName) || codeBasePath.endsWith(maltJarVersionName)) {
                    maltJarPath = new File(new File(codeBasePath).getAbsolutePath());
                }
                if (maltJarPath == null || maltJarPath.length() == 0) {
                    if (MALTJAR.matcher(codeBasePath).matches()) {
                        maltJarPath = new File(new File(codeBasePath).getAbsolutePath());
                    }
                }
            }
        } catch (MaltChainedException e) {
            if (SystemLogger.logger().isDebugEnabled()) {
                SystemLogger.logger().debug("", e);
            } else {
                SystemLogger.logger().error(e.getMessageChain());
            }
            System.exit(1);
        }
    }

    /**
     * Returns a reference to the single instance.
     */
    public static SystemInfo instance() {
        return uniqueInstance;
    }

    /**
     * Returns the application header
     *
     * @return the application header
     */
    public static String header() {
        StringBuilder sb = new StringBuilder();
        sb.append("-----------------------------------------------------------------------------\n")
          .append("                          MaltParser ").append(version).append("                             \n")
          .append("-----------------------------------------------------------------------------\n")
          .append("         MALT (Models and Algorithms for Language Technology) Group          \n")
          .append("             Vaxjo University and Uppsala University                         \n")
          .append("                             Sweden                                          \n")
          .append("-----------------------------------------------------------------------------\n");
        return sb.toString();
    }

    /**
     * Returns a short version of the help
     *
     * @return a short version of the help
     */
    public static String shortHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n")
          .append("Usage: \n")
          .append("   java -jar maltparser-").append(version).append(".jar -f <path to option file> <options>\n")
          .append("   java -jar maltparser-").append(version).append(".jar -h for more help and options\n\n")
          .append(OptionManager.instance().getOptionDescriptions().toStringOptionGroup("system"))
          .append("Documentation: docs/index.html\n");
        return sb.toString();
    }

    /**
     * Returns a set of attributes present in the jar manifest file
     *
     * @return a set of attributes present in the jar manifest file
     */
    public static Attributes getManifestAttributes() {
        return manifestAttributes;
    }

    /**
     * Returns the version number as string
     *
     * @return the version number as string
     */
    public static String getVersion() {
        return version;
    }

    /**
     * Returns the build date
     *
     * @return the build date
     */
    public static String getBuildDate() {
        return buildDate;
    }

    public static File getMaltJarPath() {
        return maltJarPath;
    }

    /**
     * Loads the manifest attributes from the manifest in the jar-file
     *
     * @throws MaltChainedException
     */
    private void getManifestInfo() throws MaltChainedException {
        try {
            URL codeBase = SystemInfo.class.getProtectionDomain().getCodeSource().getLocation();
            if (codeBase != null && codeBase.getPath().endsWith(".jar")) {
                JarFile jarfile = new JarFile(URLDecoder.decode(codeBase.getPath(), java.nio.charset.Charset.defaultCharset().name()));
                Manifest manifest = jarfile.getManifest();
                Attributes manifestAttributes = manifest.getMainAttributes();
                version = manifestAttributes.getValue("Implementation-Version");
                buildDate = manifestAttributes.getValue("Build-Date");
            }
        } catch (IOException e) {
            version = "";
            buildDate = "Not available";
            e.printStackTrace();
        }
    }
}