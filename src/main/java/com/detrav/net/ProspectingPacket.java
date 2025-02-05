package com.detrav.net;

import com.detrav.DetravScannerMod;
import com.detrav.gui.DetravScannerGUI;
import com.detrav.gui.textures.DetravMapTexture;
import com.detrav.proxies.ClientProxy;
import com.google.common.base.Objects;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import gregtech.api.GregTech_API;
import gregtech.api.enums.Materials;
import gregtech.api.util.GT_LanguageManager;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;

/*
//DEBUG CLASSES
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
*/

/**
 * Created by wital_000 on 20.03.2016.
 */
public class ProspectingPacket extends DetravPacket {
    public int chunkX;
    public int chunkZ;
    public int posX;
    public int posZ;
    public int size;
    public int ptype;
    public HashMap<Byte, Short>[][] map;
    public HashMap<String, Integer> ores;
    public HashMap<Short, String> metaMap;
    public static final HashMap<Integer, short[]> fluidColors = new HashMap<>();

    public int level = -1;

    public ProspectingPacket() {
    }

    public ProspectingPacket(int chunkX, int chunkZ, int posX, int posZ, int size, int ptype) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.posX = posX;
        this.posZ = posZ;
        this.size = size;
        this.ptype = ptype;
        this.map = new HashMap[(size*2+1)*16][(size*2+1)*16];
        this.ores = new HashMap<>();
        this.metaMap = new HashMap<>();
    }

    private static void addOre(ProspectingPacket packet, byte y, int i, int j, short meta) {
        String name = null;
        short[] rgba = new short[0];

        try {
            if(packet.ptype == 0 || packet.ptype == 1) {
                // Ore or Small Ore
                if (meta > 0 && (meta < 7000 || meta > 7500)) {
                    Materials tMaterial = GregTech_API.sGeneratedMaterials[meta % 1000];
                    rgba = tMaterial.getRGBA();
                    name = tMaterial.getLocalizedNameForItem(GT_LanguageManager.getTranslation("gt.blockores." + meta + ".name"));
                }
                else {
                    return;
                }
            } else if (packet.ptype == 2) {
                // Fluid
                rgba = fluidColors.get((int) meta);
                if (rgba == null) {
                    DetravScannerMod.proxy.sendPlayerExeption( "Unknown fluid ID = " + meta + " Please add to FluidColors.java!");
                    rgba = new short[]{125,123,118,0};
                }

                name = Objects.firstNonNull(
                        FluidRegistry.getFluid(meta).getLocalizedName(new FluidStack(FluidRegistry.getFluid(meta), 0)),
                        StatCollector.translateToLocal("gui.detrav.scanner.unknown_fluid")
                );
            } else if (packet.ptype == 3) {
                // Pollution
                name = StatCollector.translateToLocal("gui.detrav.scanner.pollution");
                rgba = new short[]{125,123,118,0};
            } else {
                return;
            }
        } catch (Exception ignored) {
            return;
        }
        packet.ores.put(name, ((rgba[0] & 0xFF) << 16) + ((rgba[1] & 0xFF) << 8) + ((rgba[2] & 0xFF)));
        packet.metaMap.put(meta, name);
    }

    public Object decode(ByteArrayDataInput aData) {
        try {


            ProspectingPacket packet = new ProspectingPacket(aData.readInt(), aData.readInt(), aData.readInt(), aData.readInt(), aData.readInt(), aData.readInt());
            packet.level = aData.readInt();

            int aSize = (packet.size * 2 + 1) * 16;
            int checkOut = 0;
            for (int i = 0; i < aSize; i++)
                for (int j = 0; j < aSize; j++) {
                    byte kSize = aData.readByte();
                    if (kSize == 0) continue;
                    packet.map[i][j] = new HashMap<>();
                    for (int k = 0; k < kSize; k++) {
                        final byte y = aData.readByte();
                        final short meta = aData.readShort();
                        packet.map[i][j].put(y, meta);
                        if (packet.ptype != 2 || y == 1) addOre(packet, y, i, j, meta);
                        checkOut++;
                    }
                }
            int checkOut2 = aData.readInt();
            if(checkOut != checkOut2) {
                ClientProxy.sendMessage = true;
                return null;
            }
            return packet;
        } catch (Exception ignored) {
            ignored.printStackTrace();
            ClientProxy.sendMessage = true;
        }
          return null;

    }



    @Override
    public int getPacketID() {
        return 0;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public byte[] encode() {
        ByteArrayDataOutput tOut = ByteStreams.newDataOutput(1);
        tOut.writeInt(chunkX);
        tOut.writeInt(chunkZ);
        tOut.writeInt(posX);
        tOut.writeInt(posZ);
        tOut.writeInt(size);
        tOut.writeInt(ptype);
        tOut.writeInt(level);

        int aSize = (size*2+1)*16;
        int checkOut = 0;
        for(int i =0; i<aSize; i++)
            for(int j =0; j<aSize; j++) {
                if(map[i][j]==null)
                    tOut.writeByte(0);
                else {
                    tOut.writeByte(map[i][j].keySet().size());
                    for(byte key : map[i][j].keySet()) {
                        tOut.writeByte(key);
                        tOut.writeShort(map[i][j].get(key));
                        checkOut++;
                    }
                }
            }
        tOut.writeInt(checkOut);
        return tOut.toByteArray();
    }


    @Override
    public void process() {
        DetravScannerGUI.newMap(new DetravMapTexture(this));
        DetravScannerMod.proxy.openProspectorGUI();
    }

    public void addBlock(int x, int y, int z, short metaData) {
        int aX = x - (chunkX-size)*16;
        int aZ = z - (chunkZ-size)*16;
        if(map[aX][aZ] == null) map[aX][aZ] = new HashMap<>();
        map[aX][aZ].put((byte) y, metaData);
    }

    public int getSize() {
        return (size*2+1)*16;
    }
}