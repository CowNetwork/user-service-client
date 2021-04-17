package network.cow.user.client.spigot

import network.cow.grape.Grape
import network.cow.user.client.spigot.api.UserServiceClient
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Benedikt Wüller
 */
class UserServicePlugin : JavaPlugin(), Listener {

    private lateinit var client: SpigotUserServiceClient

    override fun onEnable() {
        this.client = SpigotUserServiceClient(
            this.config.getString("service.hostname", "localhost")!!,
            this.config.getInt("service.port", 5816)
        )

        Grape.getInstance().register(UserServiceClient::class.java, client)
    }

    @EventHandler
    private fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        val id = event.playerProfile.id ?: return
        val name = event.playerProfile.name ?: return
        this.client.loadPlayer(name, id)
    }

    @EventHandler
    private fun onPlayerLeave(event: PlayerQuitEvent) {
        this.client.invalidate(event.player)
    }

}
