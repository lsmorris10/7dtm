package com.sevendaystominecraft.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class MagazineReadTrigger extends SimpleCriterionTrigger<MagazineReadTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, String seriesId, int issue, boolean mastery) {
        this.trigger(player, instance -> instance.matches(seriesId, issue, mastery));
    }

    public record TriggerInstance(
            Optional<ContextAwarePredicate> player,
            String seriesId,
            int issue,
            boolean requireMastery
    ) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        Codec.STRING.fieldOf("series_id").forGetter(TriggerInstance::seriesId),
                        Codec.INT.optionalFieldOf("issue", 0).forGetter(TriggerInstance::issue),
                        Codec.BOOL.optionalFieldOf("require_mastery", false).forGetter(TriggerInstance::requireMastery)
                ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(String seriesId, int issue, boolean mastery) {
            if (!this.seriesId.equals(seriesId)) return false;
            if (this.requireMastery) return mastery;
            if (this.issue > 0) return issue >= this.issue;
            return true;
        }
    }
}
