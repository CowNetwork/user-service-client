package network.cow.user.client.spigot.api

import network.cow.grape.Service
import org.bukkit.entity.Player
import java.util.UUID

/**
 * @author Benedikt Wüller
 */
interface UserServiceClient : Service {

    fun getPlayerId(player: Player) : UUID

    fun getLocale(player: Player) : String

    fun updateLocale(player: Player, locale: String)

}
