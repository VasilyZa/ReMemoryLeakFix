package ca.fxco.memoryleakfix;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;

public class MemoryLeakFixExpectPlatform {

    public static boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public static int compareMinecraftToVersion(String version) {
        try {
            return FabricLoader.getInstance().getModContainer("minecraft").get().getMetadata().getVersion().compareTo(Version.parse(version));
        } catch (VersionParsingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getMappingType() {
        return "fabric";
    }

    public static boolean isDevEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
