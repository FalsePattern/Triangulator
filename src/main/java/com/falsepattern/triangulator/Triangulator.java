package com.falsepattern.triangulator;

import com.falsepattern.lib.config.ConfigurationManager;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MODID,
     version = Tags.VERSION,
     name = Tags.MODNAME,
     acceptedMinecraftVersions = "[1.7.10]",
     dependencies = "required-after:falsepatternlib@[0.5.0,);")
public class Triangulator {
    public static Logger triLog = LogManager.getLogger(Tags.MODNAME);

    public Triangulator() {
        triLog.info("Skidaddle skidoodle, your quad is now a noodle!");
    }

    @SneakyThrows
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        ConfigurationManager.registerConfig(TriConfig.class);
    }
}
