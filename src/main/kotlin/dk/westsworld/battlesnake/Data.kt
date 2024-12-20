package dk.westsworld.battlesnake

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// This file contains data structures that you get from the api
// You can read more about the api here: https://docs.battlesnake.com/references/api

@Serializable
data class BotInfo(
    @SerialName("apiversion") val apiVersion: String = "1",
    val author: String,
    val color: String,
    val head: String,
    val tail: String,
    val version: String
)

@Serializable
data class StartRequest(
    val game: Game,
    val turn: Int,
    val board: Board,
    val you: BattleSnake
)

typealias MoveRequest = StartRequest

@Serializable
data class MoveResponse(
    val shout: String?,
    val move: Direction
)

typealias EndRequest = StartRequest

@Serializable
data class Game(
    val id: String,
    val ruleset: Ruleset,
    val map: String? = null,
    val timeout: Long,
    val source: GameSource? = null
)

@Serializable
data class Ruleset(
    val name: String,
    val version: String,
    val settings: RulesetSettings
)

@Serializable
data class RulesetSettings(
    val foodSpawnChance: Int,
    val minimumFood: Int,
    val hazardDamagePerTurn: Int,
    val royale: RoyaleSettings?,
    val squad: SquadSettings?
)

@Serializable
data class SquadSettings(
    val allowBodyCollisions: Boolean,
    val sharedElimination: Boolean,
    val sharedHealth: Boolean,
    val sharedLength: Boolean
)

@Serializable
data class RoyaleSettings(val shrinkEveryNTurns: Int)

@Serializable
enum class GameSource {
    @SerialName("tournament") TOURNAMENT,
    @SerialName("league") LEAGUE,
    @SerialName("arena") ARENA,
    @SerialName("challenge") CHALLENGE,
    @SerialName("custom") CUSTOM,
    @SerialName("") CLI_ENGINE,
    @SerialName("ladder") LADDER
}

@Serializable
data class Board(
    val height: Int,
    val width: Int,
    val food: List<Position>,
    val hazards: List<Position>,
    val snakes: List<BattleSnake>
)

@Serializable
data class BattleSnake(
    val id: String,
    val name: String,
    val health: Int,
    val body: List<Position>,
    val latency: String,
    val head: Position,
    val length: Int,
    val shout: String,
    val squad: String,
    val customizations: SnakeCustomization
)

@Serializable
data class SnakeCustomization(
    val color: String,
    val head: String,
    val tail: String
)