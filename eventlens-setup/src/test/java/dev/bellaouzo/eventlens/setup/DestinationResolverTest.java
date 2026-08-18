package dev.bellaouzo.eventlens.setup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DestinationResolverTest {

    @TempDir
    Path temp;

    @Test
    void paperUsesPluginsFolderWhenSelected() {
        Path plugins = temp.resolve("plugins");
        assertEquals(plugins, DestinationResolver.payloadDirectory(SetupTarget.PAPER, plugins));
        assertEquals(temp, DestinationResolver.serverRoot(plugins));
    }

    @Test
    void paperCreatesPluginsUnderServerRoot() {
        assertEquals(temp.resolve("plugins"), DestinationResolver.payloadDirectory(SetupTarget.PAPER, temp));
        assertEquals(temp, DestinationResolver.serverRoot(temp));
    }

    @Test
    void clientUsesModsFolderWhenSelected() {
        Path mods = temp.resolve("mods");
        assertEquals(mods, DestinationResolver.payloadDirectory(SetupTarget.NEOFORGE, mods));
    }

    @Test
    void prismInstanceWalksUpToInstanceCfg() throws Exception {
        Path instance = temp.resolve("MyPack");
        Path minecraft = instance.resolve(".minecraft");
        Path mods = minecraft.resolve("mods");
        Files.createDirectories(mods);
        Files.writeString(instance.resolve("instance.cfg"), "name=MyPack\n");

        assertEquals(instance, DestinationResolver.prismInstanceRoot(mods).orElseThrow());
        assertEquals(mods, DestinationResolver.payloadDirectory(SetupTarget.FABRIC, mods));
        assertTrue(DestinationResolver.describe(SetupTarget.FORGE, instance).contains("Prism/MultiMC"));
    }
}
