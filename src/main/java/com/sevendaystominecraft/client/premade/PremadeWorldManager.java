package com.sevendaystominecraft.client.premade;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sevendaystominecraft.SevenDaysToMinecraft;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtAccounter;

public class PremadeWorldManager {

    private static final Gson GSON = new Gson();
    private static final String BUNDLED_PATH = "data/sevendaystominecraft/premade_worlds";
    private static final String EXTERNAL_DIR_NAME = "premade_worlds";

    private static List<PremadeWorldInfo> cachedWorlds = null;

    public static List<PremadeWorldInfo> getAvailableWorlds() {
        if (cachedWorlds == null) {
            cachedWorlds = scanWorlds();
        }
        return cachedWorlds;
    }

    public static void invalidateCache() {
        cachedWorlds = null;
    }

    private static List<PremadeWorldInfo> scanWorlds() {
        List<PremadeWorldInfo> worlds = new ArrayList<>();
        worlds.addAll(scanBundledWorlds());
        worlds.addAll(scanExternalWorlds());
        return Collections.unmodifiableList(worlds);
    }

    private static List<PremadeWorldInfo> scanBundledWorlds() {
        List<PremadeWorldInfo> worlds = new ArrayList<>();
        try {
            URI uri = PremadeWorldManager.class.getClassLoader().getResource(BUNDLED_PATH).toURI();
            Path bundledRoot;
            FileSystem fs = null;
            if (uri.getScheme().equals("jar")) {
                try {
                    fs = FileSystems.getFileSystem(uri);
                } catch (FileSystemNotFoundException e) {
                    fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
                }
                bundledRoot = fs.getPath("/" + BUNDLED_PATH);
            } else {
                bundledRoot = Path.of(uri);
            }

            try (Stream<Path> dirs = Files.list(bundledRoot)) {
                dirs.filter(Files::isDirectory).forEach(dir -> {
                    Path infoFile = dir.resolve("world_info.json");
                    if (Files.exists(infoFile)) {
                        PremadeWorldInfo info = readWorldInfo(infoFile, dir.getFileName().toString(), PremadeWorldInfo.PremadeWorldSource.BUNDLED);
                        if (info != null) {
                            worlds.add(info);
                        }
                    }
                });
            }
        } catch (Exception e) {
            SevenDaysToMinecraft.LOGGER.warn("BZHS: Could not scan bundled premade worlds: {}", e.getMessage());
        }
        return worlds;
    }

    private static List<PremadeWorldInfo> scanExternalWorlds() {
        List<PremadeWorldInfo> worlds = new ArrayList<>();
        Path externalDir = Minecraft.getInstance().gameDirectory.toPath().resolve(EXTERNAL_DIR_NAME);
        if (!Files.isDirectory(externalDir)) {
            return worlds;
        }
        try (Stream<Path> dirs = Files.list(externalDir)) {
            dirs.filter(Files::isDirectory).forEach(dir -> {
                Path infoFile = dir.resolve("world_info.json");
                if (Files.exists(infoFile)) {
                    PremadeWorldInfo info = readWorldInfo(infoFile, dir.getFileName().toString(), PremadeWorldInfo.PremadeWorldSource.EXTERNAL);
                    if (info != null) {
                        worlds.add(info);
                    }
                }
            });
        } catch (Exception e) {
            SevenDaysToMinecraft.LOGGER.warn("BZHS: Could not scan external premade worlds: {}", e.getMessage());
        }
        return worlds;
    }

    private static PremadeWorldInfo readWorldInfo(Path infoFile, String folderId, PremadeWorldInfo.PremadeWorldSource source) {
        try (Reader reader = Files.newBufferedReader(infoFile)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            String name = json.has("name") ? json.get("name").getAsString() : folderId;
            String description = json.has("description") ? json.get("description").getAsString() : "";
            return new PremadeWorldInfo(folderId, name, description, source, infoFile.getParent().toString());
        } catch (Exception e) {
            SevenDaysToMinecraft.LOGGER.warn("BZHS: Failed to read world_info.json in {}: {}", folderId, e.getMessage());
            return null;
        }
    }

    public static boolean copyPremadeWorld(PremadeWorldInfo world, String worldName) {
        Path savesDir = Minecraft.getInstance().gameDirectory.toPath().resolve("saves").resolve(worldName);
        if (Files.exists(savesDir)) {
            SevenDaysToMinecraft.LOGGER.warn("BZHS: Save directory already exists: {}", savesDir);
            return false;
        }

        try {
            if (world.source() == PremadeWorldInfo.PremadeWorldSource.BUNDLED) {
                return copyBundledWorld(world, savesDir);
            } else {
                return copyExternalWorld(world, savesDir);
            }
        } catch (Exception e) {
            SevenDaysToMinecraft.LOGGER.error("BZHS: Failed to copy premade world '{}': {}", world.name(), e.getMessage());
            return false;
        }
    }

    private static boolean copyBundledWorld(PremadeWorldInfo world, Path targetDir) throws Exception {
        URI uri = PremadeWorldManager.class.getClassLoader()
                .getResource(BUNDLED_PATH + "/" + world.id()).toURI();

        Path sourceRoot;
        FileSystem fs = null;
        if (uri.getScheme().equals("jar")) {
            try {
                fs = FileSystems.getFileSystem(uri);
            } catch (FileSystemNotFoundException e) {
                fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
            }
            sourceRoot = fs.getPath("/" + BUNDLED_PATH + "/" + world.id());
        } else {
            sourceRoot = Path.of(uri);
        }

        copyDirectory(sourceRoot, targetDir);
        updateLevelName(targetDir, targetDir.getFileName().toString());
        return true;
    }

    private static boolean copyExternalWorld(PremadeWorldInfo world, Path targetDir) throws Exception {
        Path sourceDir = Path.of(world.path());
        copyDirectory(sourceDir, targetDir);
        updateLevelName(targetDir, targetDir.getFileName().toString());
        return true;
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        Files.createDirectories(target);
        try (Stream<Path> walk = Files.walk(source)) {
            walk.forEach(sourcePath -> {
                try {
                    Path relative = source.relativize(sourcePath);
                    Path targetPath = target.resolve(relative.toString());
                    if (Files.isDirectory(sourcePath)) {
                        Files.createDirectories(targetPath);
                    } else {
                        Files.createDirectories(targetPath.getParent());
                        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }

    private static void updateLevelName(Path worldDir, String newName) {
        Path levelDat = worldDir.resolve("level.dat");
        if (!Files.exists(levelDat)) {
            SevenDaysToMinecraft.LOGGER.warn("BZHS: No level.dat found in premade world, skipping name update");
            return;
        }
        try {
            CompoundTag root = NbtIo.readCompressed(levelDat, NbtAccounter.unlimitedHeap());
            if (root.contains("Data")) {
                CompoundTag data = root.getCompound("Data");
                data.putString("LevelName", newName);
                NbtIo.writeCompressed(root, levelDat);
                SevenDaysToMinecraft.LOGGER.info("BZHS: Updated level.dat LevelName to '{}'", newName);
            } else {
                SevenDaysToMinecraft.LOGGER.warn("BZHS: level.dat missing Data compound, skipping name update");
            }
        } catch (Exception e) {
            SevenDaysToMinecraft.LOGGER.warn("BZHS: Failed to update LevelName in level.dat: {}", e.getMessage());
        }
    }
}
