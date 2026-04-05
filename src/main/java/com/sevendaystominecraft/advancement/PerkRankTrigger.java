package com.sevendaystominecraft.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class PerkRankTrigger extends SimpleCriterionTrigger<PerkRankTrigger.TriggerInstance> {

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, String perkId, int rank) {
        this.trigger(player, instance -> instance.matches(perkId, rank));
    }

    public record TriggerInstance(
            Optional<ContextAwarePredicate> player,
            String perkId,
            int minRank
    ) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        Codec.STRING.fieldOf("perk_id").forGetter(TriggerInstance::perkId),
                        Codec.INT.optionalFieldOf("min_rank", 1).forGetter(TriggerInstance::minRank)
                ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(String perkId, int rank) {
            return this.perkId.equals(perkId) && rank >= this.minRank;
        }
    }
}
