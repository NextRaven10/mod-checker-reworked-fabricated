// Ftb_checker_reworked.java
package de.stylelabor.markap.ftb_checker_reworked;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Mod(Ftb_checker_reworked.MODID)
public class Ftb_checker_reworked {

    public static final String MODID = "ftb_checker_reworked";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Ftb_checker_reworked() {
        //noinspection removal (commented out)
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::onLoadComplete);
        MinecraftForge.EVENT_BUS.register(this);
        //noinspection removal (commented out)
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.CONFIG_SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("HELLO FROM CLIENT SETUP");
    }

    private void onLoadComplete(final FMLLoadCompleteEvent event) {
        Minecraft.getInstance().execute(() -> {
            List<String> requiredMods = Config.CONFIG.mods.stream()
                    .map(mod -> mod.modId)
                    .collect(Collectors.toList());

            LOGGER.info("Required Mods: {}", requiredMods);

            List<String> missingMods = requiredMods.stream()
                    .filter(modId -> {
                        boolean isPresent = ModList.get().getModContainerById(modId).isPresent();
                        LOGGER.info("Mod ID: {}, Present: {}", modId, isPresent);
                        return !isPresent;
                    })
                    .collect(Collectors.toList());

            LOGGER.info("Missing Mods: {}", missingMods);

            if (!missingMods.isEmpty()) {
                LOGGER.info("Scheduling MissingModsScreen display");

                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
                scheduler.schedule(() -> Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreen(new MissingModsScreen(missingMods))), 3, TimeUnit.SECONDS);
            }
        });
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("FTB CHECKER REWORKED >> HELLO FROM CLIENT SETUP");
        }
    }
}