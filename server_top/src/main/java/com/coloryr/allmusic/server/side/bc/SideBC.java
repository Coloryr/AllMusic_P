package com.coloryr.allmusic.server.side.bc;

import com.coloryr.allmusic.server.AllMusicBC;
import com.coloryr.allmusic.server.core.AllMusic;
import com.coloryr.allmusic.server.core.objs.enums.HudType;
import com.coloryr.allmusic.server.core.utils.HudUtils;
import com.coloryr.allmusic.server.core.music.play.PlayMusic;
import com.coloryr.allmusic.server.core.objs.config.SaveObj;
import com.coloryr.allmusic.server.core.objs.music.MusicObj;
import com.coloryr.allmusic.server.core.objs.music.SongInfoObj;
import com.coloryr.allmusic.server.core.objs.enums.ComType;
import com.coloryr.allmusic.server.core.side.ISide;
import com.coloryr.allmusic.server.core.sql.IEconomy;
import com.coloryr.allmusic.server.side.bc.event.MusicAddEvent;
import com.coloryr.allmusic.server.side.bc.event.MusicPlayEvent;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

public class SideBC extends ISide implements IEconomy {
    public static final Set<Server> TopServers = new CopyOnWriteArraySet<>();

    public static final Map<String, Integer> SendToBackend = new ConcurrentHashMap<>();

    public static void sendAllToServer(Server server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeInt(0);
        if (PlayMusic.nowPlayMusic == null)
            out.writeUTF(AllMusic.getMessage().papi.emptyMusic);
        else {
            if (AllMusic.getConfig().messageLimit
                    && PlayMusic.nowPlayMusic.getName().length() > AllMusic.getConfig().messageLimitSize) {
                out.writeUTF(PlayMusic.nowPlayMusic.getName()
                        .substring(0, AllMusic.getConfig().messageLimitSize));
            } else {
                out.writeUTF(PlayMusic.nowPlayMusic.getName());
            }
        }

        server.sendData(AllMusic.channelBC, out.toByteArray());

        out = ByteStreams.newDataOutput();
        out.writeInt(1);
        if (PlayMusic.nowPlayMusic == null)
            out.writeUTF("");
        else
            out.writeUTF(PlayMusic.nowPlayMusic.getAl());
        server.sendData(AllMusic.channelBC, out.toByteArray());

        out = ByteStreams.newDataOutput();
        out.writeInt(2);
        if (PlayMusic.nowPlayMusic == null)
            out.writeUTF("");
        else
            out.writeUTF(PlayMusic.nowPlayMusic.getAlia());
        server.sendData(AllMusic.channelBC, out.toByteArray());

        out = ByteStreams.newDataOutput();
        out.writeInt(3);
        if (PlayMusic.nowPlayMusic == null)
            out.writeUTF("");
        else
            out.writeUTF(PlayMusic.nowPlayMusic.getAuthor());
        server.sendData(AllMusic.channelBC, out.toByteArray());

        out = ByteStreams.newDataOutput();
        out.writeInt(4);
        if (PlayMusic.nowPlayMusic == null)
            out.writeUTF("");
        else
            out.writeUTF(PlayMusic.nowPlayMusic.getCall());
        server.sendData(AllMusic.channelBC, out.toByteArray());

        out = ByteStreams.newDataOutput();
        out.writeInt(5);
        out.writeInt(PlayMusic.getListSize());
        server.sendData(AllMusic.channelBC, out.toByteArray());

        out = ByteStreams.newDataOutput();
        out.writeInt(6);
        out.writeUTF(PlayMusic.getAllList());
        server.sendData(AllMusic.channelBC, out.toByteArray());
    }

    public static void sendLyricToServer(Server server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeInt(7);
        if (PlayMusic.lyric == null)
            out.writeUTF("");
        else
            out.writeUTF(PlayMusic.lyric.getLyric());
        server.sendData(AllMusic.channelBC, out.toByteArray());

        out = ByteStreams.newDataOutput();
        out.writeInt(8);
        if (PlayMusic.lyric == null || PlayMusic.lyric.getTlyric() == null)
            out.writeUTF("");
        else
            out.writeUTF(PlayMusic.lyric.getTlyric());
        server.sendData(AllMusic.channelBC, out.toByteArray());

        out = ByteStreams.newDataOutput();
        out.writeInt(9);
        out.writeBoolean(PlayMusic.lyric != null && PlayMusic.lyric.getTlyric() != null);
        server.sendData(AllMusic.channelBC, out.toByteArray());

        out = ByteStreams.newDataOutput();
        out.writeInt(10);
        if (PlayMusic.lyric == null || PlayMusic.lyric.getKly() == null)
            out.writeUTF("");
        else
            out.writeUTF(PlayMusic.lyric.getTlyric());
        server.sendData(AllMusic.channelBC, out.toByteArray());

        out = ByteStreams.newDataOutput();
        out.writeInt(11);
        out.writeBoolean(PlayMusic.lyric != null && PlayMusic.lyric.getKly() != null);
        server.sendData(AllMusic.channelBC, out.toByteArray());
    }

    @Override
    public int getAllPlayer() {
        return ProxyServer.getInstance().getOnlineCount();
    }

    @Override
    public void sendHudLyric(String data) {
        try {
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                if (ok(player))
                    continue;
                SaveObj obj = HudUtils.get(player.getName());
                if (!obj.lyric.enable)
                    continue;
                ByteBuf buf = Unpooled.buffer();
                buf.writeByte(ComType.LYRIC.ordinal());
                writeString(buf, data);
                send(player, buf);
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌词发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendHudInfo(String data) {
        try {
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                if (ok(player))
                    continue;
                SaveObj obj = HudUtils.get(player.getName());
                if (!obj.info.enable)
                    continue;
                ByteBuf buf = Unpooled.buffer();
                buf.writeByte(ComType.INFO.ordinal());
                writeString(buf, data);
                send(player, buf);
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌词信息发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendHudList(String data) {
        try {
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                if (ok(player))
                    continue;
                String name = player.getName();
                SaveObj obj = HudUtils.get(name);
                if (!obj.list.enable)
                    continue;
                ByteBuf buf = Unpooled.buffer();
                buf.writeByte(ComType.LIST.ordinal());
                writeString(buf, data);
                send(player, buf);
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌曲列表发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendHudUtilsAll() {
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            try {
                SaveObj obj = HudUtils.get(player.getName());
                String data = AllMusic.gson.toJson(obj);
                ByteBuf buf = Unpooled.buffer();
                buf.writeByte(ComType.HUD.ordinal());
                writeString(buf, data);
                send(player, buf);
            } catch (Exception e1) {
                AllMusic.log.warning("§d[AllMusic]§c数据发送发生错误");
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void sendBar(String data) {
        TextComponent message = new TextComponent(data);
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            try {
                player.sendMessage(ChatMessageType.ACTION_BAR, message);
            } catch (Exception e1) {
                AllMusic.log.warning("§d[AllMusic]§c数据发送发生错误");
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void sendMusic(String url) {
        try {
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                String server = player.getServer() == null ? null : player.getServer().getInfo().getName();
                if (AllMusic.isOK(player.getName(), server, false))
                    continue;
                ByteBuf buf = Unpooled.buffer();
                buf.writeByte(ComType.PLAY.ordinal());
                writeString(buf, url);
                send(player, buf);
                AllMusic.addNowPlayPlayer(player.getName());
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌曲指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    protected void topSendMusic(String player, String url) {
        try {
            ProxiedPlayer player1 = ProxyServer.getInstance().getPlayer(player);
            if (player1 == null)
                return;
            String server = player1.getServer() == null ? null : player1.getServer().getInfo().getName();
            if (AllMusic.isOK(player1.getName(), server, false))
                return;

            ByteBuf buf = Unpooled.buffer();
            buf.writeByte(ComType.PLAY.ordinal());
            writeString(buf, url);
            send(player1, buf);
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌曲指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendHudPos(String player) {
        try {
            ProxiedPlayer player1 = ProxyServer.getInstance().getPlayer(player);
            if (player1 == null)
                return;
            String server = player1.getServer() == null ? null : player1.getServer().getInfo().getName();
            if (AllMusic.isOK(player1.getName(), server, false))
                return;

            SaveObj obj = HudUtils.get(player);
            String data = AllMusic.gson.toJson(obj);
            ByteBuf buf = Unpooled.buffer();
            buf.writeByte(ComType.HUD.ordinal());
            writeString(buf, data);
            send(player1, buf);
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌曲指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendHud(String name, HudType pos, String data) {
        try {
            if (pos == HudType.PIC) {
                return;
            }
            ProxiedPlayer player1 = ProxyServer.getInstance().getPlayer(name);
            if (player1 == null)
                return;
            if (ok(player1))
                return;

            ByteBuf buf = Unpooled.buffer();
            switch (pos) {
                case INFO:
                    buf.writeByte(ComType.INFO.ordinal());
                    break;
                case LIST:
                    buf.writeByte(ComType.LIST.ordinal());
                    break;
                case LYRIC:
                    buf.writeByte(ComType.LYRIC.ordinal());
                    break;
            }
            writeString(buf, data);

            send(player1, buf);
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌曲指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendPic(String url) {
        try {
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                if (ok(player))
                    continue;
                String name = player.getName();
                SaveObj obj = HudUtils.get(name);
                if (!obj.pic.enable)
                    continue;
                ByteBuf buf = Unpooled.buffer();
                buf.writeByte(ComType.IMG.ordinal());
                writeString(buf, url);
                send(player, buf);
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c图片指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendPic(String player, String url) {
        try {
            ProxiedPlayer player1 = ProxyServer.getInstance().getPlayer(player);
            if (player1 == null)
                return;
            if (ok(player1))
                return;
            ByteBuf buf = Unpooled.buffer();
            buf.writeByte(ComType.IMG.ordinal());
            writeString(buf, url);
            send(player1, buf);
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c图片指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void sendPos(String player, int pos) {
        try {
            ProxiedPlayer player1 = ProxyServer.getInstance().getPlayer(player);
            if (player1 == null)
                return;
            if (ok(player1))
                return;
            ByteBuf buf = Unpooled.buffer();
            buf.writeByte(ComType.POS.ordinal());
            buf.writeInt(pos);
            send(player1, buf);
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c歌曲位置指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    protected void topSendStop() {
        try {
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                ByteBuf buf = Unpooled.buffer();
                buf.writeByte(ComType.STOP.ordinal());
                send(player, buf);
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c停止指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    protected void topSendStop(String name) {
        try {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(name);
            if (player == null)
                return;
            ByteBuf buf = Unpooled.buffer();
            buf.writeByte(ComType.STOP.ordinal());
            send(player, buf);
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c停止指令发送出错");
            e.printStackTrace();
        }
    }

    @Override
    public void clearHud(String name) {
        try {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(name);
            if (player == null)
                return;
            ByteBuf buf = Unpooled.buffer();
            buf.writeByte(ComType.CLEAR.ordinal());
            send(player, buf);
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c清空Hud发生出错");
            e.printStackTrace();
        }
    }

    @Override
    public void clearHud() {
        try {
            Collection<ProxiedPlayer> values = ProxyServer.getInstance().getPlayers();
            for (ProxiedPlayer player : values) {
                ByteBuf buf = Unpooled.buffer();
                buf.writeByte(ComType.CLEAR.ordinal());
                send(player, buf);
            }
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c清空Hud发生出错");
            e.printStackTrace();
        }
    }

    @Override
    public void bq(String data) {
        if (AllMusic.getConfig().messageLimit
                && data.length() > AllMusic.getConfig().messageLimitSize) {
            data = data.substring(0, AllMusic.getConfig().messageLimitSize - 1) + "...";
        }
        TextComponent message = new TextComponent(data);
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if (ok(player))
                continue;
            player.sendMessage(message);
        }
    }

    @Override
    public void bqRun(String message, String end, String command) {
        TextComponent send = new TextComponent(message);
        TextComponent endtext = new TextComponent(end);
        endtext.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        send.addExtra(endtext);
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if (ok(player))
                continue;
            player.sendMessage(send);
        }
    }

    @Override
    public void bqt(String data) {
        this.bq(data);
    }

    @Override
    public boolean needPlay() {
        int online = 0;
        for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
            if (AllMusic.getConfig().muteServer.contains(server.getName()))
                continue;
            for (ProxiedPlayer player : server.getPlayers())
                if (!AllMusic.getConfig().mutePlayer.contains(player.getName()))
                    online++;
        }
        return online > 0;
    }

    @Override
    public void sendMessaget(Object obj, String message) {
        CommandSender sender = (CommandSender) obj;
        sender.sendMessage(new TextComponent(message));
    }

    @Override
    public void sendMessage(Object obj, String message) {
        CommandSender sender = (CommandSender) obj;
        sender.sendMessage(new TextComponent(message));
    }

    @Override
    public void sendMessageRun(Object obj, String message, String end, String command) {
        CommandSender sender = (CommandSender) obj;
        TextComponent send = new TextComponent(message);
        TextComponent endtext = new TextComponent(end);
        endtext.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        send.addExtra(endtext);
        sender.sendMessage(send);
    }

    @Override
    public void sendMessageSuggest(Object obj, String message, String end, String command) {
        CommandSender sender = (CommandSender) obj;
        TextComponent send = new TextComponent(message);
        TextComponent endtext = new TextComponent(end);
        endtext.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
        send.addExtra(endtext);
        sender.sendMessage(send);
    }

    @Override
    public void runTask(Runnable run) {
        ProxyServer.getInstance().getScheduler().runAsync(AllMusicBC.plugin, run);
    }

    @Override
    public void reload() {
        new AllMusic().init(AllMusicBC.plugin.getDataFolder());
    }

    @Override
    public boolean checkPermission(String player, String permission) {
        if (AllMusic.getConfig().adminList.contains(player))
            return false;
        ProxiedPlayer player1 = ProxyServer.getInstance().getPlayer(player);
        if (player1 == null)
            return true;
        return !player1.hasPermission(permission);
    }

    @Override
    public void runTask(Runnable run, int delay) {
        ProxyServer.getInstance().getScheduler()
                .schedule(AllMusicBC.plugin, run, delay, TimeUnit.MICROSECONDS);
    }

    @Override
    public void updateInfo() {
        for (Server server : TopServers) {
            if (server.isConnected()) {
                sendAllToServer(server);
            } else {
                TopServers.remove(server);
            }
        }
    }

    @Override
    public void updateLyric() {
        for (Server server : TopServers) {
            if (server.isConnected()) {
                sendLyricToServer(server);
            } else {
                TopServers.remove(server);
            }
        }
    }

    @Override
    public void ping() {
        Iterator<Server> iterator = TopServers.iterator();
        while (iterator.hasNext()) {
            Server server = iterator.next();
            if (server.isConnected()) {
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeInt(200);
                server.sendData(AllMusic.channelBC, out.toByteArray());
            } else {
                iterator.remove();
            }
        }
    }

    @Override
    public List<String> getPlayerList() {
        List<String> list = new ArrayList<>();
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            list.add(player.getName().toLowerCase(Locale.ROOT));
        }

        return list;
    }

    @Override
    public boolean onMusicPlay(SongInfoObj obj) {
        MusicPlayEvent event = new MusicPlayEvent(obj);
        ProxyServer.getInstance().getPluginManager().callEvent(event);
        return event.isCancel();
    }

    @Override
    public boolean onMusicAdd(Object obj, MusicObj music) {
        MusicAddEvent event = new MusicAddEvent(music, (CommandSender) obj);
        ProxyServer.getInstance().getPluginManager().callEvent(event);
        return event.isCancel();
    }

    private void send(ProxiedPlayer players, ByteBuf data) {
        if (players == null)
            return;
        try {
            runTask(() -> players.sendData(AllMusic.channel, data.array()));
        } catch (Exception e) {
            AllMusic.log.warning("§d[AllMusic]§c数据发送发生错误");
            e.printStackTrace();
        }
    }

    private boolean ok(ProxiedPlayer player) {
        String server = player.getServer() == null ? null : player.getServer().getInfo().getName();
        return AllMusic.isOK(player.getName(), server, true);
    }

    @Override
    public boolean check(String name, int cost) {
        return topEconomy(name, cost, 12);
    }

    @Override
    public boolean cost(String name, int cost) {
        return topEconomy(name, cost, 13);
    }

    private boolean topEconomy(String name, int cost, int type) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeInt(type);
        String uuid;
        do {
            uuid = UUID.randomUUID().toString();
        } while (SendToBackend.containsKey(uuid));

        SendToBackend.put(uuid, -1);
        String server = AllMusic.getConfig().economy.backend;
        Server toServer = null;
        for (Server connection : TopServers) {
            if (connection.getInfo().getName().equalsIgnoreCase(server)) {
                toServer = connection;
            }
        }
        if (toServer == null) {
            AllMusic.log.warning("§d[AllMusic]§c没有找到目标服务器");
            return false;
        }

        out.writeUTF(uuid);
        out.write(cost);
        out.writeUTF(name);

        toServer.sendData(AllMusic.channelBC, out.toByteArray());

        Integer res;

        int count = 0;

        do {
            try {
                res = SendToBackend.get(uuid);
                if (res == null)
                    return false;
                else if (res == -1) {
                    Thread.sleep(1);
                    count++;
                } else if (res == 0) {
                    AllMusic.log.warning("§d[AllMusic]§c后端经济插件错误");
                    SendToBackend.remove(uuid);
                    return false;
                } else if (res == 1) {
                    SendToBackend.remove(uuid);
                    return false;
                } else if (res == 2) {
                    SendToBackend.remove(uuid);
                    return true;
                }
            } catch (Exception e) {
                AllMusic.log.warning("§d[AllMusic]§c经济数据发送错误");
                e.printStackTrace();
            }
        } while (count < 100);

        AllMusic.log.warning("§d[AllMusic]§c经济数据请求超时");

        return false;
    }

    private static void writeString(ByteBuf buf, String text) {
        byte[] temp = text.getBytes(StandardCharsets.UTF_8);
        buf.writeInt(temp.length);
        buf.writeBytes(temp);
    }
}