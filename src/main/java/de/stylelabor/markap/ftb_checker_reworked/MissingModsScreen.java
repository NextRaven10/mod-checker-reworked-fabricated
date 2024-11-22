// MissingModsScreen.java
package de.stylelabor.markap.ftb_checker_reworked;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class MissingModsScreen extends Screen {

    private static final Logger LOGGER = LoggerFactory.getLogger(MissingModsScreen.class);
    private final List<String> missingMods;
    private Button restartButton;
    private String modsList;

    protected MissingModsScreen(List<String> missingMods) {
        super(Component.literal("Missing Mods"));
        this.missingMods = missingMods;
    }

    // MissingModsScreen.java
    @Override
    protected void init() {
        LOGGER.info("Initializing MissingModsScreen with mods: {}", missingMods);
        int y = this.height / 4 + 24;

        if (missingMods.size() <= 5) {
            for (String modId : missingMods) {
                String formattedModName = formatModName(modId);
                int finalY = y;
                Config.CONFIG.getWebsiteDownloadLink(modId).ifPresent(link -> this.addRenderableWidget(Button.builder(
                                Component.literal("Download ").append(Component.literal(formattedModName).withStyle(style -> style.withBold(true).withColor(0xFFA500))),
                                button -> Util.getPlatform().openUri(link))
                        .bounds(this.width / 2 - 100, finalY, 200, 20)
                        .build()
                ));
                y += 24;
            }
        } else {
            // Prepare the list of missing mods as a single line of text
            modsList = missingMods.stream()
                    .map(this::formatModName)
                    .reduce((mod1, mod2) -> mod1 + ", " + mod2)
                    .orElse("");
            y += 24;
        }

        // Stop all sounds, including background music
        Minecraft.getInstance().getSoundManager().stop();

        // Add the "Download automatically" button with increased margin
        y += 24;
        this.addRenderableWidget(Button.builder(
                        Component.literal("Download automatically").withStyle(style -> style.withBold(true).withColor(0x90EE90)), // Light green color
                        button -> {
                            showMessage();
                            new Thread(() -> {
                                AtomicInteger progress = new AtomicInteger();
                                for (String modId : missingMods) {
                                    Config.CONFIG.getDirectDownloadLink(modId).ifPresent(link -> {
                                        try {
                                            downloadMod(link, modId + ".jar");
                                            progress.getAndIncrement();
                                            if (progress.get() == missingMods.size()) {
                                                Objects.requireNonNull(this.minecraft).execute(() -> restartButton.visible = true);
                                            }
                                        } catch (IOException e) {
                                            LOGGER.error("Failed to download mod: {}", modId, e);
                                        }
                                    });
                                }
                            }).start();
                        })
                .bounds(this.width / 2 - 100, y, 200, 20)
                .build()
        );

        // Initialize the restart button but set it to be invisible initially
        restartButton = Button.builder(
                        Component.literal("Restart the game to finish!").withStyle(style -> style.withColor(0xFF0000)), // Red color, not bold
                        button -> Objects.requireNonNull(this.minecraft).stop())
                .bounds(this.width / 2 - 100, y + 24, 200, 20)
                .build();
        restartButton.visible = false;
        this.addRenderableWidget(restartButton);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Draw a semi-transparent background
        guiGraphics.fill(0, 0, this.width, this.height, 0x80000000); // 50% transparent black

        // Render the title
        guiGraphics.drawCenteredString(this.font, this.title.getString(), this.width / 2, 20, 0xFFFFFF);

        // Render the list of missing mods with text wrapping
        if (missingMods.size() > 5 && modsList != null) {
            int maxWidth = this.width - 40; // 20 pixels padding on each side
            List<FormattedCharSequence> wrappedLines = this.font.split(Component.literal(modsList), maxWidth);
            int y = this.height / 4 + 24;
            for (FormattedCharSequence line : wrappedLines) {
                guiGraphics.drawCenteredString(this.font, line, this.width / 2, y, 0xFFFFFF);
                y += this.font.lineHeight + 2; // Add a small margin between lines
            }
        }

        // Render buttons and other components
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private void showMessage() {
        if (Objects.requireNonNull(this.minecraft).player != null) {
            Objects.requireNonNull(this.minecraft.player).displayClientMessage(Component.literal("Download started..."), false);
        }
    }

    private String formatModName(String modId) {
        if (modId.toLowerCase().startsWith("ftb")) {
            String rest = modId.substring(3); // Get the part after "FTB"
            return "FTB " + rest.substring(0, 1).toUpperCase() + rest.substring(1).toLowerCase();
        } else {
            return modId.substring(0, 1).toUpperCase() + modId.substring(1).toLowerCase();
        }
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
}