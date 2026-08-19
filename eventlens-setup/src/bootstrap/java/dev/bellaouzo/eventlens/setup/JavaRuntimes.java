package dev.bellaouzo.eventlens.setup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** Java 8-safe helpers so the setup jar can start when Windows opens it with Java 8. */
final class JavaRuntimes {

    static final int MIN_MAJOR = 21;

    private static final String JAVAW_EXE = "javaw.exe";
    private static final String JAVA_EXE = "java.exe";

    private JavaRuntimes() {}

    static int runtimeMajor() {
        return parseSpecification(System.getProperty("java.specification.version", "0"));
    }

    static boolean isNewEnough(int major) {
        return major >= MIN_MAJOR;
    }

    static int parseSpecification(String spec) {
        if (spec == null) {
            return 0;
        }
        String value = spec.trim();
        if (value.startsWith("1.")) {
            return leadingNumber(value.substring(2));
        }
        return leadingNumber(value);
    }

    static int parseVersionOutput(String text) {
        if (text == null) {
            return 0;
        }
        int start = text.indexOf('"');
        if (start < 0) {
            return 0;
        }
        int end = text.indexOf('"', start + 1);
        if (end <= start) {
            return 0;
        }
        return parseSpecification(text.substring(start + 1, end));
    }

    static File findNewerJavaw() {
        List<File> candidates = new ArrayList<File>();
        addIfPresent(candidates, javawInHome(System.getenv("JAVA_HOME")));
        String programFiles = System.getenv("ProgramFiles");
        if (programFiles != null) {
            scanInstallRoot(candidates, new File(programFiles, "Eclipse Adoptium"));
            scanInstallRoot(candidates, new File(programFiles, "Microsoft"));
            scanInstallRoot(candidates, new File(programFiles, "Java"));
            scanInstallRoot(candidates, new File(programFiles, "Temurin"));
            scanInstallRoot(candidates, new File(programFiles, "Amazon Corretto"));
            scanInstallRoot(candidates, new File(programFiles, "Eclipse Foundation"));
            scanInstallRoot(candidates, new File(programFiles, "BellSoft"));
            scanInstallRoot(candidates, new File(programFiles, "Zulu"));
        }
        File currentHome = new File(System.getProperty("java.home", ""));
        for (int i = 0; i < candidates.size(); i++) {
            File javaw = candidates.get(i);
            if (isUnder(javaw, currentHome)) {
                continue;
            }
            if (isNewEnough(probeMajor(javaw))) {
                return javaw;
            }
        }
        return null;
    }

    static String currentRuntimeLabel() {
        return "Java "
                + runtimeMajor()
                + " ("
                + System.getProperty("java.version", "unknown")
                + " from "
                + System.getProperty("java.home", "unknown")
                + ")";
    }

    static String requiredRuntimeMessage() {
        return "EventLens setup needs Java 21 or newer.\n\n"
                + "On Windows, \"Java SE Platform Binary\" is usually Oracle Java 8 and cannot open this jar.\n"
                + "Use \"OpenJDK Platform Binary\" (Eclipse Temurin, Microsoft Build of OpenJDK, or similar).\n\n"
                + "Right-click the setup jar → Open with → OpenJDK Platform Binary.\n"
                + "Or install Java 21+ from https://adoptium.net/ and try again.\n\n"
                + "This computer is running "
                + currentRuntimeLabel()
                + ".";
    }

    private static int leadingNumber(String value) {
        int end = 0;
        while (end < value.length() && Character.isDigit(value.charAt(end))) {
            end++;
        }
        if (end == 0) {
            return 0;
        }
        return Integer.parseInt(value.substring(0, end));
    }

    private static void addIfPresent(List<File> candidates, File javaw) {
        if (javaw != null && javaw.isFile()) {
            candidates.add(javaw);
        }
    }

    private static File javawInHome(String home) {
        if (home == null || home.trim().isEmpty()) {
            return null;
        }
        File bin = new File(home, "bin");
        File javaw = new File(bin, JAVAW_EXE);
        if (javaw.isFile()) {
            return javaw;
        }
        File java = new File(bin, JAVA_EXE);
        if (java.isFile()) {
            return java;
        }
        File posix = new File(bin, "java");
        return posix.isFile() ? posix : null;
    }

    private static void scanInstallRoot(List<File> candidates, File root) {
        File[] children = root.isDirectory() ? root.listFiles() : null;
        if (children == null) {
            return;
        }
        for (int i = 0; i < children.length; i++) {
            addIfPresent(candidates, javawInHome(children[i].getAbsolutePath()));
        }
    }

    private static boolean isUnder(File file, File home) {
        try {
            return file.getCanonicalPath().startsWith(home.getCanonicalPath());
        } catch (IOException ignored) {
            return false;
        }
    }

    private static int probeMajor(File javaw) {
        String exe = javaw.getAbsolutePath();
        if (exe.toLowerCase().endsWith(JAVAW_EXE)) {
            exe = exe.substring(0, exe.length() - JAVAW_EXE.length()) + JAVA_EXE;
        }
        try {
            Process process = new ProcessBuilder(exe, "-version")
                    .redirectErrorStream(true)
                    .start();
            String output = readFully(process.getInputStream());
            process.waitFor();
            return parseVersionOutput(output);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return 0;
        } catch (IOException ignored) {
            return 0;
        }
    }

    private static String readFully(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) >= 0) {
            out.write(buffer, 0, read);
        }
        return out.toString("UTF-8");
    }
}
