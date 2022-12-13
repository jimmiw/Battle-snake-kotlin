package dk.westsworld.battlesnake

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.JsonConfiguration
import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis
import kotlin.test.*

internal class LogicKtTest {
    @Test
    fun testLogic() {
        assert(true)
    }

    private fun getSoloFoodMoveRequest(snakePosition: Position, snakeBody: List<Position>, foodList: List<Position>): MoveRequest {
        val snake = BattleSnake(
            "snake id 1",
            "snake1",
            100,
            snakeBody,
            "111",
            snakePosition,
            snakeBody.size,
            "",
            "snake squard",
            SnakeCustomization("","","")
        )

        val board = Board(11, 11, foodList, listOf(), listOf(snake))
        val game = Game(
            "game id 1",
            Ruleset(
                "Standard",
                "v1.2.3",
                RulesetSettings(
                    15,
                    1,
                    14,
                    null,
                    null
                )
            ),
            "standard",
            500,
            GameSource.LEAGUE
        )

        return MoveRequest(
            game,
            2,
            board,
            snake
        )
    }

    private fun getMoveRequest(): MoveRequest {
        val snake = BattleSnake(
            "snake id 1",
            "snake1",
            100,
            listOf(Position(5, 5), Position(5, 6)),
            "111",
            Position(5, 6),
            2,
            "",
            "snake squard",
            SnakeCustomization("","","")
        )
        val snakeTwo = BattleSnake(
            "snake id 2",
            "snake2",
            100,
            listOf(Position(9, 9), Position(9, 7)),
            "111",
            Position(9, 9),
            2,
            "",
            "snake squard 2",
            SnakeCustomization("","","")
        )
        val snakeThree = BattleSnake(
            "snake id 3",
            "snake3",
            100,
            listOf(Position(3, 6), Position(3, 7)),
            "111",
            Position(3, 6),
            2,
            "",
            "snake squard 3",
            SnakeCustomization("","","")
        )
        val snakeFour = BattleSnake(
            "snake id 4",
            "snake4",
            100,
            listOf(Position(4, 1), Position(4, 0)),
            "111",
            Position(4, 1),
            2,
            "",
            "snake squard 4",
            SnakeCustomization("","","")
        )

        val board = Board(11, 11, listOf(), listOf(), listOf(snake, snakeTwo,snakeThree,snakeFour))
        val game = Game(
            "game id 1",
            Ruleset(
                "Standard",
                "v1.2.3",
                RulesetSettings(
                    15,
                    1,
                    14,
                    null,
                    null
                )
            ),
            "standard",
            500,
            GameSource.LEAGUE
        )

        return MoveRequest(
            game,
            2,
            board,
            snake
        )
    }

    @Test
    fun testMoveRequestParsing() {
        val json = "{\"game\":{\"id\":\"b0fe08d0-65ec-40aa-9026-b1927d5a9a50\",\"ruleset\":{\"name\":\"standard\",\"version\":\"v1.2.0\",\"settings\":{\"foodSpawnChance\":15,\"minimumFood\":1,\"hazardDamagePerTurn\":14,\"hazardMap\":\"\",\"hazardMapAuthor\":\"\",\"royale\":{\"shrinkEveryNTurns\":0},\"squad\":{\"allowBodyCollisions\":false,\"sharedElimination\":false,\"sharedHealth\":false,\"sharedLength\":false}}},\"map\":\"standard\",\"timeout\":500,\"source\":\"custom\"},\"turn\":3,\"board\":{\"height\":11,\"width\":11,\"snakes\":[{\"id\":\"gs_b3WT87DmdQ3JVd6hDmDbMq76\",\"name\":\"Frank The Tank\",\"latency\":\"29\",\"health\":99,\"body\":[{\"x\":6,\"y\":1},{\"x\":6,\"y\":0},{\"x\":5,\"y\":0},{\"x\":5,\"y\":1}],\"head\":{\"x\":6,\"y\":1},\"length\":4,\"shout\":\"\",\"squad\":\"\",\"customizations\":{\"color\":\"#ffff33\",\"head\":\"whale\",\"tail\":\"dragon\"}},{\"id\":\"gs_qMGSFmXJwXHfwxByVkbJwQkb\",\"name\":\"Stupid snake (Just getting started)\",\"latency\":\"21\",\"health\":99,\"body\":[{\"x\":5,\"y\":10},{\"x\":6,\"y\":10},{\"x\":6,\"y\":9},{\"x\":5,\"y\":9}],\"head\":{\"x\":5,\"y\":10},\"length\":4,\"shout\":\"\",\"squad\":\"\",\"customizations\":{\"color\":\"#00ff00\",\"head\":\"alligator\",\"tail\":\"alligator\"}},{\"id\":\"gs_FFwqdFHRwQh8Ycv9qkwYQvf6\",\"name\":\"Mister Sneaky Pants\",\"latency\":\"28\",\"health\":97,\"body\":[{\"x\":1,\"y\":8},{\"x\":1,\"y\":7},{\"x\":1,\"y\":6}],\"head\":{\"x\":1,\"y\":8},\"length\":3,\"shout\":\"\",\"squad\":\"\",\"customizations\":{\"color\":\"#670000\",\"head\":\"sneaky\",\"tail\":\"skinny-jeans\"}}],\"food\":[{\"x\":0,\"y\":4},{\"x\":5,\"y\":5}],\"hazards\":[]},\"you\":{\"id\":\"gs_FFwqdFHRwQh8Ycv9qkwYQvf6\",\"name\":\"Mister Sneaky Pants\",\"latency\":\"28\",\"health\":97,\"body\":[{\"x\":1,\"y\":8},{\"x\":1,\"y\":7},{\"x\":1,\"y\":6}],\"head\":{\"x\":1,\"y\":8},\"length\":3,\"shout\":\"\",\"squad\":\"\",\"customizations\":{\"color\":\"#670000\",\"head\":\"sneaky\",\"tail\":\"skinny-jeans\"}}}"

        var serializer = Json {
            isLenient = true
            ignoreUnknownKeys = true
        }

        val request = serializer.decodeFromString<MoveRequest>(json)
        assertNotNull(request)
    }

    @Test
    fun testDecideMove() {

        val request = getMoveRequest()

        println(decideMove(request))
        println(decideMove(request))
//        println(decideMove(request))
//        println(decideMove(request))
    }

    @Test
    fun testFindFood() {
        var request = getSoloFoodMoveRequest(
            Position(9,5),
            listOf(Position(9,5), Position(9,5)),
            listOf()
        );

        var route = mutableListOf<Position>()
        var nextPosition: Position? = null
        val timeInMillis = measureTimeMillis {
            nextPosition = getNextMoveTowardsPosition(request.you.head, Position(10,6), route, request.board, 0)
        }
        println("route calc time: " + timeInMillis + "ms")
        println("Route length: " + route.size)
        println("Route is: " + route)
        println("suggested new position is: " + nextPosition)
        var nextDirection = request.you.head.getDirection(nextPosition ?: Position(8,5))
        println("suggested new direction is: " + nextDirection)
    }

    @Test
    fun testIsOutOfBounds() {
        val board = Board(11, 11, listOf(), listOf(), listOf())

        var position = Position(0, 0)
        assertFalse(isOutOfBounds(position, board))

        position = Position(0, -1)
        assertTrue(isOutOfBounds(position, board))

        position = Position(11, 5)
        assertTrue(isOutOfBounds(position, board))
    }

    @Test
    fun testIsHazard() {
        val board = Board(11, 11, listOf(), listOf(Position(5, 5)), listOf())
        assertFalse(isHazard(Position(1,1), board))
        assertTrue(isHazard(Position(5,5), board))
    }

    @Test
    fun testIsCollidingWithSnake() {
        val board = Board(11, 11, listOf(), listOf(), listOf())
        val battleSnake = BattleSnake(
            "1",
            "snake1",
            100,
            listOf(Position(5,5), Position(5, 6)),
            "100",
            Position(5, 5),
            2,
            "",
            "",
            SnakeCustomization("", "", "")
        )

        assertFalse(isCollidingWithSnake(Position(0, 0), battleSnake, board))
        assertFalse(isCollidingWithSnake(Position(2, 5), battleSnake, board))
        assertTrue(isCollidingWithSnake(Position(5, 5), battleSnake, board))
        // this should be false, since the snake is moving - so the tail is shifting position
        assertFalse(isCollidingWithSnake(Position(5, 6), battleSnake, board))
    }

//    fun testPositionMath() {
//        var position = Position(1,1);
//        assertEquals(Position(0, 1), position - Direction.LEFT)
//    }

    @Test
    fun testGetSafeMoves() {
        val request = getMoveRequest()

        val safeMoves = getSafeMoves(request.you, request.board, false)
        println("Test done, found safeMoves: " + safeMoves)
    }

    @Test
    fun testGetDistance() {
        // testing get distance between two points, but rounding the result to int, to make it easier to test :)
        assertEquals(3, getDistance(Position(1,1), Position(3,3)).roundToInt())

        assertEquals(1.0, getDistance(Position(1,1), Position(1,2)))
    }
}
