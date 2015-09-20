package gg.uhc.matchpost.chat;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ChatSender {

    protected final Constructor packetConstructor;
    protected final Field componentsField;
    protected final Field playerConnectionField;
    protected final Method sendPacketMethod;
    protected final Method getHandleMethod;

    public ChatSender(Plugin plugin) throws ReflectiveOperationException {
        String packageName = plugin.getServer().getClass().getPackage().getName();
        String version = packageName.substring(packageName.lastIndexOf(".") + 1);

        Class packetPlayOutClass = Class.forName("net.minecraft.server." + version + ".PacketPlayOutChat");
        packetConstructor = packetPlayOutClass.getConstructor();
        componentsField = packetPlayOutClass.getDeclaredField("components");

        getHandleMethod = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer").getDeclaredMethod("getHandle");
        playerConnectionField = Class.forName("net.minecraft.server." + version + ".EntityPlayer").getDeclaredField("playerConnection");
        sendPacketMethod = Class.forName("net.minecraft.server." + version + ".PlayerConnection").getDeclaredMethod("sendPacket", Class.forName("net.minecraft.server." + version + ".Packet"));
    }

    public void sendChat(BaseComponent content, Player player) {
        try {
            Object packet = packetConstructor.newInstance();
            componentsField.set(packet, new BaseComponent[]{content});

            sendPacketMethod.invoke(playerConnectionField.get(getHandleMethod.invoke(player)), packet);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
