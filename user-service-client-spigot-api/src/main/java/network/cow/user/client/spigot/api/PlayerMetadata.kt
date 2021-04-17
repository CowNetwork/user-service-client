package network.cow.user.client.spigot.api

import network.cow.mooapis.user.v1.Metadata

/**
 * @author Benedikt Wüller
 */
data class PlayerMetadata(var username: String? = null, var locale: String = "en_US") {
    fun toProto() : Metadata = Metadata.newBuilder()
        .setUsername(this.username)
        .setLocale(this.locale)
        .build()
}

fun Metadata.toSpigot() = PlayerMetadata(
    if (this.hasUsername()) this.username else null,
    this.locale
)
