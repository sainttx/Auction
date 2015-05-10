package com.sainttx.auction.command.subcommand;

import com.sainttx.auction.api.Auction;
import com.sainttx.auction.api.AuctionManager;
import com.sainttx.auction.api.AuctionsAPI;
import com.sainttx.auction.api.messages.MessageHandler;
import com.sainttx.auction.command.AuctionSubCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles the /auction bid command for the auction plugin
 */
public class BidCommand extends AuctionSubCommand {

    public BidCommand() {
        super("auctions.bid", "bid", "b");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        AuctionManager manager = AuctionsAPI.getAuctionManager();
        MessageHandler handler = manager.getMessageHandler();
        Auction auction = manager.getCurrentAuction();

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can place bids on auctions");
        } else if (args.length < 2 && !plugin.getConfig().getBoolean("auctionSettings.canBidAutomatically", true)) {
            handler.sendMessage(sender, plugin.getMessage("messages.error.bidSyntax"));
        } else if (auction == null) {
            handler.sendMessage(sender, plugin.getMessage("messages.error.noCurrentAuction"));
        } else {
            Player player = (Player) sender;

            double bid;

            try {
                bid = args.length < 2
                        ? auction.getTopBid() + auction.getBidIncrement()
                        : Double.parseDouble(args[1]);
            } catch (NumberFormatException ex) {
                handler.sendMessage(sender, plugin.getMessage("messages.error.invalidNumberEntered"));
                return true;
            }

            if (plugin.getConfig().isList("general.disabledWorlds")
                    && plugin.getConfig().getStringList("general.disabledWorlds").contains(player.getWorld().getName())) {
                handler.sendMessage(player, plugin.getMessage("messages.error.cantUsePluginInWorld"));
            } else if (handler.isIgnoring(player.getUniqueId())) {
                handler.sendMessage(player, plugin.getMessage("messages.error.currentlyIgnoring")); // player is ignoring
            } else if (auction.getOwner().equals(player.getUniqueId())) {
                handler.sendMessage(player, plugin.getMessage("messages.error.bidOnOwnAuction")); // cant bid on own auction
            } else if (bid < auction.getTopBid() + auction.getBidIncrement()) {
                handler.sendMessage(player, plugin.getMessage("messages.error.bidTooLow")); // the bid wasnt enough
            } else if (plugin.getEconomy().getBalance(player) < bid) {
                handler.sendMessage(player, plugin.getMessage("messages.error.insufficientBalance")); // insufficient funds
            } else if (player.getUniqueId().equals(auction.getTopBidder())) {
                handler.sendMessage(player, plugin.getMessage("messages.error.alreadyTopBidder")); // already top bidder
            } else {
                if (auction.getTopBidder() != null) { // give the old winner their money back
                    OfflinePlayer oldPlayer = Bukkit.getOfflinePlayer(auction.getTopBidder());
                    plugin.getEconomy().depositPlayer(oldPlayer, auction.getTopBid());
                }

                // place the bid
                auction.placeBid(player, bid);
                plugin.getEconomy().withdrawPlayer(player, bid);
            }
        }

        return true;
    }
}
