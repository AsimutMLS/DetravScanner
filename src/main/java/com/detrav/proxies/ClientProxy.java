package com.detrav.proxies;

import com.detrav.DetravScannerMod;
import com.detrav.enums.Textures01;
import com.detrav.gui.DetravScannerGUI;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by wital_000 on 19.03.2016.
 */
public class ClientProxy extends CommonProxy {
    public static volatile boolean sendMessage = false;


    public ClientProxy()
    {
        int test = Textures01.mTextures.length;
    }

    @Override
    public void onPostLoad() {
        super.onPostLoad();
    }
    @Override
    public void onLoad()
    {
        super.onLoad();
    }
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (sendMessage &&  Minecraft.getMinecraft().thePlayer != null) {
                Minecraft.getMinecraft().thePlayer.sendChatMessage("DetravScannerMod: " + "NotFound");
                sendMessage = false;
            }
        }
    }

    public void openProspectorGUI()
    {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        player.openGui(DetravScannerMod.instance, DetravScannerGUI.GUI_ID, player.worldObj, (int)player.posX, (int)player.posY, (int)player.posZ);
    }
    @Override
    public void onPreInit()
    {
        super.onPreInit();
        FMLCommonHandler.instance().bus().register(this);
    }



    @Override
    public void sendPlayerExeption(String s) {
        Minecraft.getMinecraft().thePlayer.sendChatMessage("DetravScannerMod: " + s);
    }
}
