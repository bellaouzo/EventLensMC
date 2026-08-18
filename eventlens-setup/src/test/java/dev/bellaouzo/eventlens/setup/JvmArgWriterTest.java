package dev.bellaouzo.eventlens.setup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JvmArgWriterTest {

    @TempDir
    Path temp;

    @Test
    void quotesPathsWithSpaces() {
        String arg = JvmArgWriter.javaAgentArgument(Path.of("C:/My Agents/eventlens-agent-1.12.0.jar"));
        assertEquals("-javaagent:\"C:/My Agents/eventlens-agent-1.12.0.jar\"", arg);
    }

    @Test
    void writesAndReplacesUserJvmArgs() throws Exception {
        Path file = temp.resolve("user_jvm_args.txt");
        String first = "-javaagent:C:/old/eventlens-agent-1.11.0.jar";
        JvmArgWriter.patchUserJvmArgs(file, first);
        assertTrue(Files.readString(file).contains(first));

        String next = "-javaagent:C:/new/eventlens-agent-1.12.0.jar";
        JvmArgWriter.patchUserJvmArgs(file, next);
        String text = Files.readString(file);
        assertTrue(text.contains(next));
        assertFalse(text.contains("1.11.0"));
    }

    @Test
    void insertsAfterJavaWithoutTouchingClickText() throws Exception {
        Path script = temp.resolve("start.bat");
        Files.writeString(
                script,
                """
                @echo off
                echo click a block
                java -Xmx2G -jar paper.jar nogui
                """);
        String arg = "-javaagent:C:/server/eventlens-agent-1.12.0.jar";
        assertTrue(JvmArgWriter.patchStartScript(script, arg));
        String text = Files.readString(script);
        assertTrue(text.contains("echo click a block"));
        assertTrue(text.contains("java " + arg + " -Xmx2G"));
    }

    @Test
    void replacesExistingAgentInStartScript() {
        String updated = JvmArgWriter.insertIntoJavaCommand(
                "java -javaagent:C:/old/eventlens-agent-1.11.0.jar -jar paper.jar",
                "-javaagent:C:/new/eventlens-agent-1.12.0.jar");
        assertEquals("java -javaagent:C:/new/eventlens-agent-1.12.0.jar -jar paper.jar", updated);
    }

    @Test
    void patchesPrismJvmArgs() throws Exception {
        Path cfg = temp.resolve("instance.cfg");
        Files.writeString(cfg, "name=Pack\nJvmArgs=-Xmx4G\n");
        String arg = "-javaagent:C:/agents/eventlens-client-agent-1.12.0.jar";
        assertTrue(JvmArgWriter.patchPrismInstance(cfg, arg));
        String text = Files.readString(cfg);
        assertTrue(text.contains("OverrideJavaArgs=true"));
        assertTrue(text.contains("JvmArgs=-Xmx4G " + arg));
    }
}
