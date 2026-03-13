package com.sevendaystominecraft.block.workstation;

public enum WorkstationType {
    CAMPFIRE("campfire", "Campfire", true, 3, 1, 1),
    GRILL("grill", "Grill", true, 3, 1, 1),
    WORKBENCH("workbench", "Workbench", false, 4, 4, 0),
    FORGE("forge", "Forge", true, 3, 3, 1),
    CEMENT_MIXER("cement_mixer", "Cement Mixer", true, 2, 2, 1),
    CHEMISTRY_STATION("chemistry_station", "Chemistry Station", false, 4, 4, 0),
    ADVANCED_WORKBENCH("advanced_workbench", "Advanced Workbench", false, 6, 6, 0);

    private final String id;
    private final String displayName;
    private final boolean usesFuel;
    private final int inputSlots;
    private final int outputSlots;
    private final int fuelSlots;

    WorkstationType(String id, String displayName, boolean usesFuel, int inputSlots, int outputSlots, int fuelSlots) {
        this.id = id;
        this.displayName = displayName;
        this.usesFuel = usesFuel;
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
        this.fuelSlots = fuelSlots;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public boolean usesFuel() { return usesFuel; }
    public int getInputSlots() { return inputSlots; }
    public int getOutputSlots() { return outputSlots; }
    public int getFuelSlots() { return fuelSlots; }
    public int getTotalSlots() { return inputSlots + outputSlots + fuelSlots; }
}
