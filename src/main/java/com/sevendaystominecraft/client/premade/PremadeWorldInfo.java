package com.sevendaystominecraft.client.premade;

public record PremadeWorldInfo(String id, String name, String description, PremadeWorldSource source, String path) {

    public enum PremadeWorldSource {
        BUNDLED,
        EXTERNAL
    }
}
