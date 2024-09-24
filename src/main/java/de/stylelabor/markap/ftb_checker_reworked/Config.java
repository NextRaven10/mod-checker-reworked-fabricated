// Config.java
package de.stylelabor.markap.ftb_checker_reworked;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = Ftb_checker_reworked.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    public static final ForgeConfigSpec CONFIG_SPEC;
    public static final Config CONFIG;

    static {
        final Pair<Config, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Config::new);
        CONFIG_SPEC = specPair.getRight();
        CONFIG = specPair.getLeft();
    }

    public List<ModConfig> mods;

    public Config(ForgeConfigSpec.Builder builder) {
        loadModsFromFile();
    }

    private void loadModsFromFile() {
        try {
            Path path = Paths.get("config/ftb_checker_reworked.json");
            if (!Files.exists(path)) {
                createDefaultConfigFile();
            }
            try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(path))) {
                Type modConfigListType = new TypeToken<List<ModConfig>>() {}.getType();
                mods = new Gson().fromJson(reader, modConfigListType);
                if (mods == null) {
                    mods = Collections.emptyList();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            mods = Collections.emptyList();
        }
    }

    private void createDefaultConfigFile() throws IOException {
        List<ModConfig> defaultMods = List.of(
                new ModConfig("ftbchunks", "https://www.curseforge.com/api/v1/mods/314906/files/5378090/download", "https://www.curseforge.com/minecraft/mc-mods/ftb-chunks-forge"),
                new ModConfig("ftbquests", "https://www.curseforge.com/api/v1/mods/289412/files/5543955/download", "https://www.curseforge.com/minecraft/mc-mods/ftb-quests-forge"),
                new ModConfig("ftblibrary", "https://www.curseforge.com/api/v1/mods/404465/files/5567591/download", "https://www.curseforge.com/minecraft/mc-mods/ftb-library"),
                new ModConfig("ftbteams", "https://www.curseforge.com/api/v1/mods/404468/files/5267190/download", "https://www.curseforge.com/minecraft/mc-mods/ftb-teams"),
                new ModConfig("ftbultimine", "https://www.curseforge.com/api/v1/mods/386134/files/5363345/download", "https://www.curseforge.com/minecraft/mc-mods/ftb-ultimine")
        );
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(Paths.get("config/ftb_checker_reworked.json")))) {
            gson.toJson(defaultMods, writer);
        }
    }

    public static class ModConfig {
        public String modId;
        public String directDownloadLink;
        public String websiteDownloadLink;

        public ModConfig(String modId, String directDownloadLink, String websiteDownloadLink) {
            this.modId = modId;
            this.directDownloadLink = directDownloadLink;
            this.websiteDownloadLink = websiteDownloadLink;
        }
    }

    public Optional<String> getDirectDownloadLink(String modId) {
        return mods.stream()
                .filter(mod -> mod.modId.equals(modId))
                .map(mod -> mod.directDownloadLink)
                .findFirst();
    }

    public Optional<String> getWebsiteDownloadLink(String modId) {
        return mods.stream()
                .filter(mod -> mod.modId.equals(modId))
                .map(mod -> mod.websiteDownloadLink)
                .findFirst();
    }
}