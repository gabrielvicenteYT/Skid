package me.levansj01.verus.check.checks.badpackets;

import me.levansj01.verus.check.PacketCheck;
import me.levansj01.verus.check.annotation.CheckInfo;
import me.levansj01.verus.check.type.CheckType;
import me.levansj01.verus.check.version.CheckVersion;
import me.levansj01.verus.compat.VPacket;
import me.levansj01.verus.compat.packets.VPacketPlayInUseEntity;

@CheckInfo(type=CheckType.BAD_PACKETS, subType="V", friendlyName="Invalid Interact", version=CheckVersion.RELEASE, maxViolations=5, logData=true)
public class BadPacketsV extends PacketCheck {
    public void handle(VPacket vPacket, long l) {
        int n;
        if (vPacket instanceof VPacketPlayInUseEntity && (n = ((VPacketPlayInUseEntity)vPacket).getId()) < 0) {
            this.handleViolation(() -> BadPacketsV.lambda$handle$0(n));
        }
    }

    private static String lambda$handle$0(int n) {
        return String.format("E: %s", n);
    }
}
