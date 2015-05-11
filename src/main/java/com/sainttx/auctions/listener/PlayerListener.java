package com.sainttx.auctions.listener;

import com.sainttx.auctions.AuctionPlugin;
import com.sainttx.auctions.api.Auction;
import com.sainttx.auctions.api.AuctionsAPI;
import com.sainttx.auctions.api.reward.Reward;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Monitors specific events for the auction plugin
 */
public class PlayerListener implements Listener {

    private AuctionPlugin plugin;

    public PlayerListener(AuctionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    /**
     * Responsible for giving the players back items that were unable to be
     * returned at a previous time
     */
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Reward reward = plugin.getOfflineReward(player.getUniqueId());

        if (reward != null) {
            reward.giveItem(player);
            AuctionsAPI.getMessageHandler().sendMessage(player, plugin.getMessage("messages.savedItemReturn"));
            plugin.removeOfflineReward(player.getUniqueId());
        }
    }

    @EventHandler(ignoreCancelled = true)
    /**
     * Cancels a players command if they're auctioning
     */
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().split(" ")[0];
        if (plugin.getConfig().getBoolean("general.blockCommands.ifAuctioning", false)
                && plugin.getConfig().isList("general.blockedCommands")
                && plugin.getConfig().getStringList("general.blockedCommands").contains(command.toLowerCase())) {
            Player player = event.getPlayer();
            Auction auction = AuctionsAPI.getAuctionManager().getCurrentAuction();

            if (AuctionsAPI.getAuctionManager().hasActiveAuction(player)) {
                event.setCancelled(true);
                AuctionsAPI.getMessageHandler().sendMessage(player, plugin.getMessage("messages.error.cantUseCommandWhileAuctioning"));
            } else if (plugin.getConfig().getBoolean("general.blockedCommands.ifQueued", false)
                    && AuctionsAPI.getAuctionManager().hasAuctionInQueue(player)) {
                event.setCancelled(true);
                AuctionsAPI.getMessageHandler().sendMessage(player, plugin.getMessage("messages.error.cantUseCommandWhileQueued"));
            } else if (plugin.getConfig().getBoolean("general.blockCommands.ifTopBidder", false)
                    && auction != null && player.getUniqueId().equals(auction.getTopBidder())) {
                event.setCancelled(true);
                AuctionsAPI.getMessageHandler().sendMessage(player, plugin.getMessage("messages.error.cantUseCommandWhileTopBidder"));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        World target = event.getTo().getWorld();

        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL
                && plugin.getConfig().isList("general.disabledWorlds")
                && plugin.getConfig().getStringList("general.disabledWorlds").contains(target.getName())) {
            if (AuctionsAPI.getAuctionManager().hasActiveAuction(player)
                    || AuctionsAPI.getAuctionManager().hasAuctionInQueue(player)) {
                event.setCancelled(true);
                AuctionsAPI.getMessageHandler().sendMessage(player, plugin.getMessage("messages.error.cantTeleportToDisabledWorld"));
            } else {
                Auction auction = AuctionsAPI.getAuctionManager().getCurrentAuction();

                if (auction != null && player.getUniqueId().equals(auction.getTopBidder())) {
                    event.setCancelled(true);
                    AuctionsAPI.getMessageHandler().sendMessage(player, plugin.getMessage("messages.error.cantTeleportToDisabledWorld"));
                }
            }
        }
    }
}