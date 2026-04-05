package com.sevendaystominecraft.quest;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class QuestInstance {

    public enum State {
        ACTIVE,
        READY_TO_TURN_IN,
        COMPLETED
    }

    private final String questId;
    private final QuestDefinition definition;
    private int progress;
    private State state;

    public QuestInstance(QuestDefinition definition) {
        this.questId = UUID.randomUUID().toString().substring(0, 8);
        this.definition = definition;
        this.progress = 0;
        this.state = State.ACTIVE;
    }

    private QuestInstance(String questId, QuestDefinition definition, int progress, State state) {
        this.questId = questId;
        this.definition = definition;
        this.progress = progress;
        this.state = state;
    }

    public String getQuestId() { return questId; }
    public QuestDefinition getDefinition() { return definition; }
    public int getProgress() { return progress; }
    public State getState() { return state; }

    public void incrementProgress(int amount) {
        if (state != State.ACTIVE) return;
        progress = Math.min(progress + amount, definition.getTargetCount());
        if (progress >= definition.getTargetCount()) {
            state = State.READY_TO_TURN_IN;
        }
    }

    public void setProgress(int value) {
        if (state != State.ACTIVE && state != State.READY_TO_TURN_IN) return;
        progress = Math.min(value, definition.getTargetCount());
        if (progress >= definition.getTargetCount()) {
            state = State.READY_TO_TURN_IN;
        } else if (state == State.READY_TO_TURN_IN) {
            state = State.ACTIVE;
        }
    }

    public void markCompleted() {
        state = State.COMPLETED;
    }

    public boolean isComplete() {
        return progress >= definition.getTargetCount();
    }

    public String getProgressText() {
        return progress + "/" + definition.getTargetCount();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("questId", questId);
        tag.put("definition", definition.save());
        tag.putInt("progress", progress);
        tag.putString("state", state.name());
        return tag;
    }

    public static QuestInstance load(CompoundTag tag) {
        String questId = tag.getString("questId");
        QuestDefinition definition = QuestDefinition.load(tag.getCompound("definition"));
        int progress = tag.getInt("progress");
        State state;
        try {
            state = State.valueOf(tag.getString("state"));
        } catch (IllegalArgumentException e) {
            state = State.ACTIVE;
        }
        return new QuestInstance(questId, definition, progress, state);
    }
}
