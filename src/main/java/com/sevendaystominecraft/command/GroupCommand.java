package com.sevendaystominecraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.sevendaystominecraft.group.Group;
import com.sevendaystominecraft.group.GroupData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class GroupCommand {

    private static final String PREFIX = "§6[Group] §r";
    private static final String SUCCESS = "§a";
    private static final String ERROR = "§c";
    private static final String INFO = "§7";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("group")
                .then(Commands.literal("create")
                        .executes(GroupCommand::createGroup))
                .then(Commands.literal("invite")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(GroupCommand::invitePlayer)))
                .then(Commands.literal("accept")
                        .executes(GroupCommand::acceptInvite))
                .then(Commands.literal("decline")
                        .executes(GroupCommand::declineInvite))
                .then(Commands.literal("leave")
                        .executes(GroupCommand::leaveGroup))
                .then(Commands.literal("disband")
                        .executes(GroupCommand::disbandGroup))
                .then(Commands.literal("list")
                        .executes(GroupCommand::listGroup))
        );
    }

    private static GroupData getGroupData(CommandContext<CommandSourceStack> ctx) {
        ServerLevel overworld = ctx.getSource().getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) return null;
        return GroupData.getOrCreate(overworld);
    }

    private static int createGroup(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR + "Must be run by a player."));
            return 0;
        }
        GroupData data = getGroupData(ctx);
        if (data == null) return 0;

        if (data.isInGroup(player.getUUID())) {
            ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR + "You are already in a group. Use /group leave first."));
            return 0;
        }

        Group group = data.createGroup(player.getUUID(), player.getName().getString());
        if (group == null) {
            ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR + "Could not create group."));
            return 0;
        }

        ctx.getSource().sendSuccess(() -> Component.literal(PREFIX + SUCCESS + "Group created! Use /group invite <player> to add members."), false);
        return 1;
    }

    private static int invitePlayer(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR + "Must be run by a player."));
            return 0;
        }
        GroupData data = getGroupData(ctx);
        if (data == null) return 0;

        if (!data.isInGroup(player.getUUID())) {
            ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR + "You are not in a group. Use /group create first."));
            return 0;
        }

        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        if (target.getUUID().equals(player.getUUID())) {
            ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR + "You cannot invite yourself."));
            return 0;
        }

        if (data.isInGroup(target.getUUID())) {
            ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR + target.getName().getString() + " is already in a group."));
            return 0;
        }

        if (!data.invite(player.getUUID(), target.getUUID())) {
            ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR + "Could not send invite."));
            return 0;
        }

        String targetName = target.getName().getString();
        String senderName = player.getName().getString();
        ctx.getSource().sendSuccess(() -> Component.literal(PREFIX + SUCCESS + "Invitation sent to " + targetName + "."), false);
        target.sendSystemMessage(Component.literal(PREFIX + INFO + senderName + " invited you to their group. Use /group accept or /group decline."));
        return 1;
    }

    private static int acceptInvite(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR + "Must be run by a player."));
            return 0;
        }
        GroupData data = getGroupData(ctx);
        if (data == null) return 0;

        if (data.isInGroup(player.getUUID())) {
            ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR + "You are already in a group. Use /group leave first."));
            return 0;
        }

        if (!data.hasPendingInvite(player.getUUID())) {
            ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR + "You have no pending group invitations."));
            return 0;
        }

        Group group = data.acceptInvite(player.getUUID(), player.getName().getString());
        if (group == null) {
            ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR + "The group no longer exists."));
            return 0;
        }

        String playerName = player.getName().getString();
        ctx.getSource().sendSuccess(() -> Component.literal(PREFIX + SUCCESS + "You joined " + group.getOwnerName() + "'s group!"), false);

        ServerPlayer owner = ctx.getSource().getServer().getPlayerList().getPlayer(group.getOwnerUUID());
        if (owner != null) {
            owner.sendSystemMessage(Component.literal(PREFIX + INFO + playerName + " joined the group."));
        }
        for (Group.Member m : group.getMembers()) {
            if (!m.uuid().equals(player.getUUID())) {
                ServerPlayer member = ctx.getSource().getServer().getPlayerList().getPlayer(m.uuid());
                if (member != null) {
                    member.sendSystemMessage(Component.literal(PREFIX + INFO + playerName + " joined the group."));
                }
            }
        }
        return 1;
    }

    private static int declineInvite(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR + "Must be run by a player."));
            return 0;
        }
        GroupData data = getGroupData(ctx);
        if (data == null) return 0;

        if (!data.declineInvite(player.getUUID())) {
            ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR + "You have no pending group invitations."));
            return 0;
        }

        ctx.getSource().sendSuccess(() -> Component.literal(PREFIX + SUCCESS + "Invitation declined."), false);
        return 1;
    }

    private static int leaveGroup(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR + "Must be run by a player."));
            return 0;
        }
        GroupData data = getGroupData(ctx);
        if (data == null) return 0;

        if (!data.isInGroup(player.getUUID())) {
            ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR + "You are not in a group."));
            return 0;
        }

        Group group = data.getGroupForPlayer(player.getUUID());
        if (group != null && group.getOwnerUUID().equals(player.getUUID())) {
            return disbandGroup(ctx);
        }

        String playerName = player.getName().getString();
        Group remainingGroup = data.leaveGroup(player.getUUID());

        ctx.getSource().sendSuccess(() -> Component.literal(PREFIX + SUCCESS + "You left the group."), false);

        if (remainingGroup != null) {
            ServerPlayer owner = ctx.getSource().getServer().getPlayerList().getPlayer(remainingGroup.getOwnerUUID());
            if (owner != null) {
                owner.sendSystemMessage(Component.literal(PREFIX + INFO + playerName + " left the group."));
            }
            for (Group.Member m : remainingGroup.getMembers()) {
                ServerPlayer member = ctx.getSource().getServer().getPlayerList().getPlayer(m.uuid());
                if (member != null) {
                    member.sendSystemMessage(Component.literal(PREFIX + INFO + playerName + " left the group."));
                }
            }
        }
        return 1;
    }

    private static int disbandGroup(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR + "Must be run by a player."));
            return 0;
        }
        GroupData data = getGroupData(ctx);
        if (data == null) return 0;

        Group group = data.getGroupForPlayer(player.getUUID());
        if (group == null) {
            ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR + "You are not in a group."));
            return 0;
        }
        if (!group.getOwnerUUID().equals(player.getUUID())) {
            ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR + "Only the group owner can disband the group. Use /group leave instead."));
            return 0;
        }

        java.util.Set<java.util.UUID> allMembers = data.disbandGroup(group.getGroupId());
        for (java.util.UUID uuid : allMembers) {
            ServerPlayer member = ctx.getSource().getServer().getPlayerList().getPlayer(uuid);
            if (member != null) {
                if (uuid.equals(player.getUUID())) {
                    member.sendSystemMessage(Component.literal(PREFIX + SUCCESS + "Group disbanded."));
                } else {
                    member.sendSystemMessage(Component.literal(PREFIX + INFO + "The group has been disbanded by the owner."));
                }
            }
        }
        return 1;
    }

    private static int listGroup(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR + "Must be run by a player."));
            return 0;
        }
        GroupData data = getGroupData(ctx);
        if (data == null) return 0;

        Group group = data.getGroupForPlayer(player.getUUID());
        if (group == null) {
            ctx.getSource().sendFailure(Component.literal(PREFIX + ERROR + "You are not in a group."));
            return 0;
        }

        StringBuilder sb = new StringBuilder(PREFIX + INFO + "Group members:\n");
        sb.append("  §e★ ").append(group.getOwnerName()).append(" §7(owner)\n");
        for (Group.Member m : group.getMembers()) {
            sb.append("  §f• ").append(m.name()).append("\n");
        }
        String text = sb.toString();
        ctx.getSource().sendSuccess(() -> Component.literal(text), false);
        return 1;
    }
}
