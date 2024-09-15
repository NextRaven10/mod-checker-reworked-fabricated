// MissingModsScreen.java
package de.stylelabor.markap.ftb_checker_reworked;

import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MissingModsScreen extends Screen {

    private static final Logger LOGGER = LoggerFactory.getLogger(MissingModsScreen.class);
    private final List<String> missingMods;

    protected MissingModsScreen(List<String> missingMods) {
        super(Component.literal("Missing FTB Mods"));
        this.missingMods = missingMods;
    }

    @Override
    protected void init() {
        LOGGER.info("Initializing MissingModsScreen with mods: {}", missingMods);
        int y = this.height / 4 + 24;
        for (String modId : missingMods) {
            this.addRenderableWidget(Button.builder(Component.literal("Download " + modId).withStyle(style -> style.withColor(0xFFA500)), button -> Util.getPlatform().openUri("https://modrinth.com/mod/" + modId)).bounds(this.width / 2 - 100, y, 200, 20).build());
            y += 24;
        }

        // Add the "Download automatically" button with increased margin
        y += 24;
        this.addRenderableWidget(Button.builder(Component.literal("Download automatically"), button -> {
            for (String modId : missingMods) {
                Config.CONFIG.getDirectDownloadLink(modId).ifPresent(link -> {
                    try {
                        downloadMod(link, modId + ".jar");
                    } catch (IOException e) {
                        LOGGER.error("Failed to download mod: {}", modId, e);
                    }
                });
            }
        }).bounds(this.width / 2 - 100, y, 200, 20).build());
    }

    private void downloadMod(String urlString, String fileName) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        Path modsFolder = Paths.get("mods");
        if (!Files.exists(modsFolder)) {
            Files.createDirectories(modsFolder);
        }

        Path filePath = modsFolder.resolve(fileName);
        try (InputStream in = connection.getInputStream()) {
            Files.copy(in, filePath);
            LOGGER.info("Downloaded mod to: {}", filePath);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title.getString(), this.width / 2, 20, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}