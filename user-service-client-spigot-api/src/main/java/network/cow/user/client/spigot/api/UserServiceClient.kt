package network.cow.user.client.spigot.api

import network.cow.grape.Service
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * @author Benedikt Wüller
 */
interface UserServiceClient : Service {

    fun getPlayerId(player: Player) : CompletableFuture<UUID>

    fun getPlayerIds(vararg players: Player) : CompletableFuture<Map<Player, UUID>>

    fun getMetadata(player: Player) : CompletableFuture<PlayerMetadata>

    fun updateMetadata(player: Player, metadata: PlayerMetadata) : CompletableFuture<Void>

}
