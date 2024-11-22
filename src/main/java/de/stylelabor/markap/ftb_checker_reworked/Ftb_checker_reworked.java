// Ftb_checker_reworked.java
package de.stylelabor.markap.ftb_checker_reworked;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

@Mod(Ftb_checker_reworked.MODID)
public class Ftb_checker_reworked {

    public static final String MODID = "ftb_checker_reworked";
    private static final Logger LOGGER = LogUtils.getLogger();
    private List<String> missingMods;

    @SuppressWarnings("removal")
    public Ftb_checker_reworked() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::clientSetup);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.CONFIG_SPEC);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        Minecraft.getInstance().execute(() -> {
            List<String> requiredMods = Config.CONFIG.mods.stream()
                    .map(mod -> mod.modId)
                    .collect(Collectors.toList());

            LOGGER.info("Required Mods: {}", requiredMods);

            missingMods = requiredMods.stream()
                    .filter(modId -> {
                        boolean isPresent = ModList.get().getModContainerById(modId).isPresent();
                        LOGGER.info("Mod ID: {}, Present: {}", modId, isPresent);
                        return !isPresent;
                    })
                    .collect(Collectors.toList());

            LOGGER.info("Missing Mods: {}", missingMods);
        });
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (missingMods != null && !missingMods.isEmpty()) {
            LOGGER.info("Displaying MissingModsScreen");
            Minecraft.getInstance().setScreen(new MissingModsScreen(missingMods));
            missingMods = null; // Ensure the screen is only displayed once
        }
    }
}