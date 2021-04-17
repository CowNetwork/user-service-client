package network.cow.user.client.spigot

import network.cow.grape.Grape
import network.cow.user.client.spigot.api.UserServiceClient
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Benedikt Wüller
 */
class UserServicePlugin : JavaPlugin() {

    override fun onEnable() {
        val client = SpigotUserServiceClient(
            this.config.getString("service.hostname", "localhost")!!,
            this.config.getInt("service.port", 5816)
        )

        Grape.getInstance().register(UserServiceClient::class.java, client)
    }

}
