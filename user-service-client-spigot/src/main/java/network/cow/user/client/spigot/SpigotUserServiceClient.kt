package network.cow.user.client.spigot

import io.grpc.ManagedChannelBuilder
import network.cow.mooapis.user.v1.GetPlayerRequest
import network.cow.mooapis.user.v1.PlayerIdentifier
import network.cow.mooapis.user.v1.UpdatePlayerMetadataRequest
import network.cow.mooapis.user.v1.UserServiceGrpc
import network.cow.user.client.spigot.api.PlayerMetadata
import network.cow.user.client.spigot.api.UserServiceClient
import network.cow.user.client.spigot.api.toSpigot
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/**
 * @author Benedikt Wüller
 */
class SpigotUserServiceClient(host: String, port: Int) : UserServiceClient {

    companion object {
        private const val TYPE = "minecraft"
    }

    private val executor = Executors.newSingleThreadExecutor()

    private val stub = UserServiceGrpc.newBlockingStub(ManagedChannelBuilder.forAddress(host, port).build())

    private val idCache = ConcurrentHashMap<UUID, UUID>()
    private val metadataCache = ConcurrentHashMap<UUID, PlayerMetadata>()

    fun loadPlayer(playerId: UUID) {
        val grpcPlayer = this.stub.getPlayer(GetPlayerRequest.newBuilder()
            .setIdentifier(PlayerIdentifier.newBuilder().setId(playerId.toString()).setType(TYPE).build())
            .build()
        ).player

        synchronized(this.idCache) {
            this.idCache[playerId] = UUID.fromString(grpcPlayer.id)
        }

        synchronized(this.metadataCache) {
            this.metadataCache[playerId] = grpcPlayer.metadata.toSpigot()
        }
    }

    private fun getMetadata(player: Player) : PlayerMetadata = synchronized(this.metadataCache) {
        this.metadataCache[player.uniqueId] ?: error("The player has not been loaded yet.")
    }

    override fun getPlayerId(player: Player): UUID = synchronized(this.idCache) {
        this.idCache[player.uniqueId] ?: error("The player has not been loaded yet.")
    }

    override fun getLocale(player: Player): String = this.getMetadata(player).locale

    override fun updateLocale(player: Player, locale: String) {
        this.executor.submit {
            val id = this.getPlayerId(player)

            val metadata = synchronized(this.metadataCache) {
                val metadata = this.getMetadata(player)
                metadata.locale = locale
                return@synchronized metadata
            }

            this.stub.updatePlayerMetadata(UpdatePlayerMetadataRequest.newBuilder().setPlayerId(id.toString()).setMetadata(metadata.toProto()).build())
        }
    }

    fun invalidate(player: Player) {
        synchronized(this.idCache) {
            this.idCache.remove(player.uniqueId)
        }

        synchronized(this.metadataCache) {
            this.metadataCache.remove(player.uniqueId)
        }
    }

}
