package com.inkzzz.bungee.staffchat;

import com.google.common.collect.Sets;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Luke Denham on 29/05/2016.
 */
public final class StaffChatPlugin extends Plugin
{

    protected String message = "&8[&c&lSTAFF&8] {player} &8[&7{server}&8]&7: &d{message}";
    protected String toggleMessage = "&cYou have &7{toggle} &cthe staff chat.";

    @Override
    public final void onEnable()
    {

        if(!getDataFolder().exists())
        {
            getDataFolder().mkdir();
        }

        File file = new File(getDataFolder(), "config.yml");

        try
        {
            if(!file.exists())
            {
                if(file.createNewFile())
                {
                    Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
                    if(configuration.get("Message") == null)
                    {
                        configuration.set("Message", this.message);
                        ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, file);
                    }
                    else
                    {
                        this.message = configuration.getString("Message");
                    }
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        StaffChatCommand staffChatCommand = new StaffChatCommand(this);

        getProxy().getPluginManager().registerCommand(this, staffChatCommand);
        getProxy().getPluginManager().registerListener(this, staffChatCommand);
    }

    private class StaffChatCommand extends Command implements Listener
    {

        private final StaffChatPlugin plugin;
        private final Set<UUID> toggled;

        public StaffChatCommand(StaffChatPlugin plugin)
        {
            super("staffchat", "staffchat.use");
            this.plugin = plugin;
            this.toggled = Sets.newHashSet();
        }

        @Override
        public void execute(CommandSender sender, String[] args)
        {

            if(!(sender instanceof ProxiedPlayer))
            {
                return;
            }

            final ProxiedPlayer player = (ProxiedPlayer) sender;

            if(args.length == 1 && args[0].equalsIgnoreCase("toggle"))
            {
                if(getToggled().contains(player.getUniqueId()))
                {
                    getToggled().remove(player.getUniqueId());
                }
                else
                {
                    getToggled().add(player.getUniqueId());
                }
                player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', this.plugin.toggleMessage.replace("{toggle}", (getToggled().contains(player.getUniqueId()) ? "joined" : "left")))));
                return;
            }

            String message = getMessage(args, 0);
            String server = player.getServer().getInfo().getName();

            getProxy().getPlayers().stream().filter(proxiedPlayer -> proxiedPlayer.hasPermission("staffchat.use")).forEach(proxiedPlayer ->
            {
                proxiedPlayer.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', this.plugin.message.replace("{player}", player.getName()).replace("{server}", server).replace("{message}", message))));
            });

        }

        @EventHandler
        public final void onEvent(final ChatEvent event)
        {
            final ProxiedPlayer player = (ProxiedPlayer) event.getSender();
            if(getToggled().contains(player.getUniqueId()))
            {
                event.setCancelled(true);

                String message = event.getMessage();
                String server = player.getServer().getInfo().getName();

                getProxy().getPlayers().stream().filter(proxiedPlayer -> proxiedPlayer.hasPermission("staffchat.use")).forEach(proxiedPlayer ->
                {
                    proxiedPlayer.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', this.plugin.message.replace("{player}", player.getName()).replace("{server}", server).replace("{message}", message))));
                });
            }
        }

        @EventHandler
        public final void onEvent(final PlayerDisconnectEvent event)
        {
            final ProxiedPlayer player = event.getPlayer();
            if(getToggled().contains(player.getUniqueId()))
            {
                getToggled().remove(player.getUniqueId());
            }
        }

        private String getMessage(String[] args, int x)
        {
            StringBuilder stringBuilder = new StringBuilder();
            for(int i = x; i < args.length; i++)
            {
                stringBuilder.append(args[i]).append( x >= args.length - 1 ? "" : " " );
            }
            return stringBuilder.toString();
        }

        private Set<UUID> getToggled()
        {
            return this.toggled;
        }

    }

}
