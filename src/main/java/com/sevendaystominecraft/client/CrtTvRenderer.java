package com.sevendaystominecraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.sevendaystominecraft.SevenDaysToMinecraft;
import com.sevendaystominecraft.block.CrtTvBlock;
import com.sevendaystominecraft.block.CrtTvBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CrtTvRenderer implements BlockEntityRenderer<CrtTvBlockEntity> {

    private static final Map<String, DynamicTexture> dynamicTextures = new HashMap<>();
    private static final Map<String, ResourceLocation> textureLocations = new HashMap<>();
    private static final Random staticRandom = new Random();

    private static final int SCREEN_WIDTH = 64;
    private static final int SCREEN_HEIGHT = 48;

    // Static noise texture
    private static DynamicTexture staticTexture;
    private static ResourceLocation staticTextureLocation;
    private static int staticTickCounter = 0;

    public CrtTvRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CrtTvBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.isPlaying()) return;

        String channel = blockEntity.getChannel();
        ResourceLocation texLoc;

        if ("__static__".equals(channel)) {
            texLoc = getOrCreateStaticTexture();
            updateStaticNoise();
        } else if (TvChannelManager.hasChannel(channel)) {
            texLoc = getOrCreateChannelTexture(channel, blockEntity.getFrameIndex());
        } else {
            texLoc = getOrCreateStaticTexture();
            updateStaticNoise();
        }

        if (texLoc == null) return;

        Direction facing = blockEntity.getBlockState().getValue(CrtTvBlock.FACING);

        poseStack.pushPose();

        // Position the screen quad on the front face of the TV
        poseStack.translate(0.5, 0.5, 0.5);

        float rotation = switch (facing) {
            case SOUTH -> 180f;
            case WEST -> 270f;
            case EAST -> 90f;
            default -> 0f;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        // Move to the front face position
        // Screen is at Z=2/16 from block origin, centered, from Y=1 to Y=9 (in block units)
        poseStack.translate(0, 0, -0.375);

        float screenWidth = 10f / 16f;   // 10 pixels wide (from x=3 to x=13)
        float screenHeight = 8f / 16f;   // 8 pixels tall (from y=1 to y=9)

        // Position bottom-left of quad
        poseStack.translate(-screenWidth / 2, -screenHeight / 2 - 0.0625f, 0);

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(texLoc));

        // Render the screen quad
        float w = screenWidth;
        float h = screenHeight;

        consumer.addVertex(matrix, 0, 0, 0)
                .setColor(255, 255, 255, 255)
                .setUv(0, 1)
                .setOverlay(packedOverlay)
                .setLight(15728880)
                .setNormal(0, 0, -1);

        consumer.addVertex(matrix, w, 0, 0)
                .setColor(255, 255, 255, 255)
                .setUv(1, 1)
                .setOverlay(packedOverlay)
                .setLight(15728880)
                .setNormal(0, 0, -1);

        consumer.addVertex(matrix, w, h, 0)
                .setColor(255, 255, 255, 255)
                .setUv(1, 0)
                .setOverlay(packedOverlay)
                .setLight(15728880)
                .setNormal(0, 0, -1);

        consumer.addVertex(matrix, 0, h, 0)
                .setColor(255, 255, 255, 255)
                .setUv(0, 0)
                .setOverlay(packedOverlay)
                .setLight(15728880)
                .setNormal(0, 0, -1);

        // Add scanline overlay effect
        renderScanlines(poseStack, bufferSource, packedOverlay, w, h);

        poseStack.popPose();
    }

    private void renderScanlines(PoseStack poseStack, MultiBufferSource bufferSource,
                                 int packedOverlay, float w, float h) {
        // Render subtle horizontal scanlines by drawing semi-transparent dark lines
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer lineConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(
                ResourceLocation.withDefaultNamespace("textures/misc/white.png")));

        float lineSpacing = h / 12f;

        for (int i = 0; i < 12; i++) {
            float y = i * lineSpacing;
            float lineH = lineSpacing * 0.3f;

            lineConsumer.addVertex(matrix, 0, y, -0.001f)
                    .setColor(0, 0, 0, 40)
                    .setUv(0, 0)
                    .setOverlay(packedOverlay)
                    .setLight(15728880)
                    .setNormal(0, 0, -1);

            lineConsumer.addVertex(matrix, w, y, -0.001f)
                    .setColor(0, 0, 0, 40)
                    .setUv(1, 0)
                    .setOverlay(packedOverlay)
                    .setLight(15728880)
                    .setNormal(0, 0, -1);

            lineConsumer.addVertex(matrix, w, y + lineH, -0.001f)
                    .setColor(0, 0, 0, 40)
                    .setUv(1, 1)
                    .setOverlay(packedOverlay)
                    .setLight(15728880)
                    .setNormal(0, 0, -1);

            lineConsumer.addVertex(matrix, 0, y + lineH, -0.001f)
                    .setColor(0, 0, 0, 40)
                    .setUv(0, 1)
                    .setOverlay(packedOverlay)
                    .setLight(15728880)
                    .setNormal(0, 0, -1);
        }
    }

    private ResourceLocation getOrCreateStaticTexture() {
        if (staticTexture == null) {
            staticTexture = new DynamicTexture(SCREEN_WIDTH, SCREEN_HEIGHT, false);
            staticTextureLocation = ResourceLocation.fromNamespaceAndPath(
                    SevenDaysToMinecraft.MOD_ID, "dynamic/crt_static");
            net.minecraft.client.Minecraft.getInstance().getTextureManager()
                    .register(staticTextureLocation, staticTexture);
        }
        return staticTextureLocation;
    }

    private void updateStaticNoise() {
        staticTickCounter++;
        if (staticTickCounter < 2) return; // Update every other tick for performance
        staticTickCounter = 0;

        if (staticTexture == null) return;

        var pixels = staticTexture.getPixels();
        if (pixels == null) return;

        for (int y = 0; y < SCREEN_HEIGHT; y++) {
            for (int x = 0; x < SCREEN_WIDTH; x++) {
                int gray = staticRandom.nextInt(200) + 30;
                // Slight blue tint for CRT feel
                int r = (int) (gray * 0.9f);
                int g = (int) (gray * 0.95f);
                int b = gray;
                pixels.setPixel(x, y, 0xFF000000 | (b << 16) | (g << 8) | r);
            }
        }

        staticTexture.upload();
    }

    private ResourceLocation getOrCreateChannelTexture(String channel, int frameIndex) {
        String key = channel + "_tex";

        if (!dynamicTextures.containsKey(key)) {
            DynamicTexture tex = new DynamicTexture(SCREEN_WIDTH, SCREEN_HEIGHT, false);
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(
                    SevenDaysToMinecraft.MOD_ID, "dynamic/crt_channel_" + channel.toLowerCase().replaceAll("[^a-z0-9_]", "_"));
            net.minecraft.client.Minecraft.getInstance().getTextureManager().register(loc, tex);
            dynamicTextures.put(key, tex);
            textureLocations.put(key, loc);
        }

        DynamicTexture tex = dynamicTextures.get(key);
        int[] frameData = TvChannelManager.getFrame(channel, frameIndex);
        if (frameData != null && tex.getPixels() != null) {
            int srcW = frameData[0];
            int srcH = frameData[1];

            var pixels = tex.getPixels();
            for (int dy = 0; dy < SCREEN_HEIGHT; dy++) {
                for (int dx = 0; dx < SCREEN_WIDTH; dx++) {
                    int sx = dx * srcW / SCREEN_WIDTH;
                    int sy = dy * srcH / SCREEN_HEIGHT;
                    int srcIdx = 2 + sy * srcW + sx;
                    if (srcIdx < frameData.length) {
                        int argb = frameData[srcIdx];
                        int a = (argb >> 24) & 0xFF;
                        int r = (argb >> 16) & 0xFF;
                        int g = (argb >> 8) & 0xFF;
                        int b = argb & 0xFF;
                        pixels.setPixel(dx, dy, (a << 24) | (b << 16) | (g << 8) | r);
                    }
                }
            }
            tex.upload();
        }

        return textureLocations.get(key);
    }

    public static void cleanup() {
        if (staticTexture != null) {
            staticTexture.close();
            staticTexture = null;
        }
        for (DynamicTexture tex : dynamicTextures.values()) {
            tex.close();
        }
        dynamicTextures.clear();
        textureLocations.clear();
    }
}
