package com.sevendaystominecraft.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages TV channel discovery and frame loading from the config directory.
 * Channels are subfolders of config/sevendaystominecraft/tv_videos/
 * containing numbered PNG frames: frame_001.png, frame_002.png, etc.
 */
public class TvChannelManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TV_VIDEOS_DIR = "config/sevendaystominecraft/tv_videos";

    private static final Map<String, List<int[]>> channelFrames = new ConcurrentHashMap<>();
    private static List<String> channelNames = new ArrayList<>();
    private static boolean scanned = false;

    public static void rescan() {
        channelFrames.clear();
        channelNames.clear();
        scanned = false;
        scan();
    }

    public static void scan() {
        if (scanned) return;
        scanned = true;

        Path videosDir = Minecraft.getInstance().gameDirectory.toPath().resolve(TV_VIDEOS_DIR);
        if (!Files.exists(videosDir)) {
            try {
                Files.createDirectories(videosDir);
                LOGGER.info("BZHS: Created TV videos directory at {}", videosDir);
            } catch (IOException e) {
                LOGGER.error("BZHS: Failed to create TV videos directory", e);
            }
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(videosDir)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    String name = entry.getFileName().toString();
                    loadChannel(name, entry);
                }
            }
        } catch (IOException e) {
            LOGGER.error("BZHS: Failed to scan TV videos directory", e);
        }

        Collections.sort(channelNames);
        LOGGER.info("BZHS: Found {} TV channels", channelNames.size());
    }

    private static void loadChannel(String name, Path dir) {
        List<Path> frameFiles = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.png")) {
            for (Path file : stream) {
                frameFiles.add(file);
            }
        } catch (IOException e) {
            LOGGER.error("BZHS: Failed to read channel directory: {}", name, e);
            return;
        }

        frameFiles.sort(Comparator.comparing(p -> p.getFileName().toString()));

        if (frameFiles.isEmpty()) {
            LOGGER.warn("BZHS: Channel '{}' has no PNG frames, skipping", name);
            return;
        }

        List<int[]> frames = new ArrayList<>();
        for (Path frameFile : frameFiles) {
            try {
                BufferedImage img = ImageIO.read(frameFile.toFile());
                if (img != null) {
                    int w = img.getWidth();
                    int h = img.getHeight();
                    int[] pixels = new int[w * h + 2];
                    pixels[0] = w;
                    pixels[1] = h;
                    img.getRGB(0, 0, w, h, pixels, 2, w);
                    frames.add(pixels);
                }
            } catch (IOException e) {
                LOGGER.warn("BZHS: Failed to load frame: {}", frameFile, e);
            }
        }

        if (!frames.isEmpty()) {
            channelFrames.put(name, frames);
            channelNames.add(name);
            LOGGER.info("BZHS: Loaded channel '{}' with {} frames", name, frames.size());
        }
    }

    public static List<String> getChannelNames() {
        scan();
        return Collections.unmodifiableList(channelNames);
    }

    public static boolean hasChannel(String name) {
        scan();
        return channelFrames.containsKey(name);
    }

    public static int[] getFrame(String channel, int frameIndex) {
        List<int[]> frames = channelFrames.get(channel);
        if (frames == null || frames.isEmpty()) return null;
        return frames.get(frameIndex % frames.size());
    }

    public static int getFrameCount(String channel) {
        List<int[]> frames = channelFrames.get(channel);
        return frames == null ? 0 : frames.size();
    }
}
