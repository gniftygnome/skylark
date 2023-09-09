package net.gnomecraft.skylark.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record TeamDescription(String name, TEAM_TYPES type, Text displayName, BlockPos spawnPos, List<ServerPlayerEntity> members) {
    public enum TEAM_TYPES { DEFAULT, MINECRAFT, PLAYER; }

    public TeamDescription {
        members = new ArrayList<>(members);
        members.sort(Comparator.comparing(PlayerEntity::getEntityName));
    }

    public List<ServerPlayerEntity> members() {
        return List.copyOf(members);
    }

    public boolean addMember(ServerPlayerEntity member) {
        return members.add(member);
    }

    public boolean removeMember(ServerPlayerEntity member) {
        return members.remove(member);
    }
}
