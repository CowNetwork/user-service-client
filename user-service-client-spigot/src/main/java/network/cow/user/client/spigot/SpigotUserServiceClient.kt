package network.cow.user.client.spigot

import io.grpc.ManagedChannelBuilder
import network.cow.mooapis.user.v1.GetPlayerRequest
import network.cow.mooapis.user.v1.GetPlayersRequest
import network.cow.mooapis.user.v1.PlayerIdentifier
import network.cow.mooapis.user.v1.UpdatePlayerMetadataRequest
import network.cow.mooapis.user.v1.UserServiceGrpc
import network.cow.user.client.spigot.api.PlayerMetadata
import network.cow.user.client.spigot.api.UserServiceClient
import network.cow.user.client.spigot.api.toSpigot
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import network.cow.mooapis.user.v1.Player as GrpcPlayer

/**
 * @author Benedikt Wüller
 */
class SpigotUserServiceClient(host: String, port: Int) : UserServiceClient {

    companion object {
        private const val TYPE = "minecraft"
    }

    private val executor = Executors.newCachedThreadPool()

    private val stub = UserServiceGrpc.newBlockingStub(ManagedChannelBuilder.forAddress(host, port).build())

    private fun createPlayerIdentifier(player: Player) = PlayerIdentifier.newBuilder().setType(TYPE).setId(player.uniqueId.toString()).build()

    private fun getPlayer(player: Player) : CompletableFuture<GrpcPlayer> {
        val future = CompletableFuture<GrpcPlayer>()
        this.executor.submit {
            val result = this.stub.getPlayer(GetPlayerRequest.newBuilder().setIdentifier(this.createPlayerIdentifier(player)).build())
            future.complete(result.player)
        }
        return future
    }

    override fun getPlayerId(player: Player): CompletableFuture<UUID> = this.getPlayer(player).thenApply { UUID.fromString(it.id) }

    override fun getPlayerIds(vararg players: Player): CompletableFuture<Map<Player, UUID>> {
        val future = CompletableFuture<Map<Player, UUID>>()
        this.executor.submit {
            val identifiers = players.map(this::createPlayerIdentifier)
            val result = this.stub.getPlayers(GetPlayersRequest.newBuilder().addAllIdentifiers(identifiers).build())

            val ids = mutableMapOf<Player, UUID>()
            result.playersList.forEachIndexed { index, player -> ids[players[index]] = UUID.fromString(player.id) }
            future.complete(ids)
        }
        return future
    }

    override fun getMetadata(player: Player): CompletableFuture<PlayerMetadata> = this.getPlayer(player).thenApply { it.metadata.toSpigot() }

    override fun updateMetadata(player: Player, metadata: PlayerMetadata): CompletableFuture<Void> = this.getPlayerId(player).thenAcceptAsync({
        this.stub.updatePlayerMetadata(UpdatePlayerMetadataRequest.newBuilder().setMetadata(metadata.toProto()).build())
    }, this.executor)

}
