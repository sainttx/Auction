package com.sainttx.auction.command.subcommand;

import com.sainttx.auction.api.AuctionsAPI;
import com.sainttx.auction.command.AuctionSubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Handles the /auction reload command for the auction plugin
 */
public class ReloadCommand extends AuctionSubCommand {

    public ReloadCommand() {
        super("auctions.reload", "reload", "r", "rel");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        AuctionsAPI.getMessageHandler().sendMessage(plugin.getMessage("messages.pluginReloaded"), sender);
        plugin.reloadConfig();
        plugin.loadConfig();
        return false;
    }
}
