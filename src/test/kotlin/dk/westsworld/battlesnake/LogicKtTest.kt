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
    fun testMoveFindFoodParsing() {
        val gameLogic = Logic()
        val serializer = Json {
            isLenient = true
            ignoreUnknownKeys = true
        }

        var request = serializer.decodeFromString<MoveRequest>("{\"game\":{\"id\":\"0f9c724c-d87e-4eb7-b999-4e2b18b6a4e3\",\"ruleset\":{\"name\":\"standard\",\"version\":\"v1.2.0\",\"settings\":{\"foodSpawnChance\":15,\"minimumFood\":1,\"hazardDamagePerTurn\":14,\"hazardMap\":\"\",\"hazardMapAuthor\":\"\",\"royale\":{\"shrinkEveryNTurns\":0},\"squad\":{\"allowBodyCollisions\":false,\"sharedElimination\":false,\"sharedHealth\":false,\"sharedLength\":false}}},\"map\":\"standard\",\"timeout\":500,\"source\":\"custom\"},\"turn\":0,\"board\":{\"height\":11,\"width\":11,\"snakes\":[{\"id\":\"gs_wdk9FjyWh7f4xqVdTwyXKBPW\",\"name\":\"Mister Sneaky Pants\",\"latency\":\"\",\"health\":100,\"body\":[{\"x\":1,\"y\":9},{\"x\":1,\"y\":9},{\"x\":1,\"y\":9}],\"head\":{\"x\":1,\"y\":9},\"length\":3,\"shout\":\"\",\"squad\":\"\",\"customizations\":{\"color\":\"#670000\",\"head\":\"sneaky\",\"tail\":\"skinny-jeans\"}},{\"id\":\"gs_ttpTVyXb7MCkvjGqwTcbYwtP\",\"name\":\"Stupid snake (Just getting started)\",\"latency\":\"\",\"health\":100,\"body\":[{\"x\":1,\"y\":1},{\"x\":1,\"y\":1},{\"x\":1,\"y\":1}],\"head\":{\"x\":1,\"y\":1},\"length\":3,\"shout\":\"\",\"squad\":\"\",\"customizations\":{\"color\":\"#00ff00\",\"head\":\"alligator\",\"tail\":\"alligator\"}},{\"id\":\"gs_gySFSpV4drjrQjmDxSYCW9yC\",\"name\":\"Frank The Tank\",\"latency\":\"\",\"health\":100,\"body\":[{\"x\":9,\"y\":9},{\"x\":9,\"y\":9},{\"x\":9,\"y\":9}],\"head\":{\"x\":9,\"y\":9},\"length\":3,\"shout\":\"\",\"squad\":\"\",\"customizations\":{\"color\":\"#ffff33\",\"head\":\"whale\",\"tail\":\"dragon\"}}],\"food\":[{\"x\":2,\"y\":10},{\"x\":2,\"y\":0},{\"x\":10,\"y\":8},{\"x\":5,\"y\":5}],\"hazards\":[]},\"you\":{\"id\":\"gs_wdk9FjyWh7f4xqVdTwyXKBPW\",\"name\":\"Mister Sneaky Pants\",\"latency\":\"\",\"health\":100,\"body\":[{\"x\":1,\"y\":9},{\"x\":1,\"y\":9},{\"x\":1,\"y\":9}],\"head\":{\"x\":1,\"y\":9},\"length\":3,\"shout\":\"\",\"squad\":\"\",\"customizations\":{\"color\":\"#670000\",\"head\":\"sneaky\",\"tail\":\"skinny-jeans\"}}}")
        var direction = gameLogic.decideMove(request)
        println("Move 1: " + direction)
//        assertEquals(Direction.UP, direction)

        request = serializer.decodeFromString<MoveRequest>("{\"game\":{\"id\":\"0f9c724c-d87e-4eb7-b999-4e2b18b6a4e3\",\"ruleset\":{\"name\":\"standard\",\"version\":\"v1.2.0\",\"settings\":{\"foodSpawnChance\":15,\"minimumFood\":1,\"hazardDamagePerTurn\":14,\"hazardMap\":\"\",\"hazardMapAuthor\":\"\",\"royale\":{\"shrinkEveryNTurns\":0},\"squad\":{\"allowBodyCollisions\":false,\"sharedElimination\":false,\"sharedHealth\":false,\"sharedLength\":false}}},\"map\":\"standard\",\"timeout\":500,\"source\":\"custom\"},\"turn\":1,\"board\":{\"height\":11,\"width\":11,\"snakes\":[{\"id\":\"gs_wdk9FjyWh7f4xqVdTwyXKBPW\",\"name\":\"Mister Sneaky Pants\",\"latency\":\"32\",\"health\":99,\"body\":[{\"x\":1,\"y\":10},{\"x\":1,\"y\":9},{\"x\":1,\"y\":9}],\"head\":{\"x\":1,\"y\":10},\"length\":3,\"shout\":\"Hello, world!\",\"squad\":\"\",\"customizations\":{\"color\":\"#670000\",\"head\":\"sneaky\",\"tail\":\"skinny-jeans\"}},{\"id\":\"gs_ttpTVyXb7MCkvjGqwTcbYwtP\",\"name\":\"Stupid snake (Just getting started)\",\"latency\":\"25\",\"health\":99,\"body\":[{\"x\":2,\"y\":1},{\"x\":1,\"y\":1},{\"x\":1,\"y\":1}],\"head\":{\"x\":2,\"y\":1},\"length\":3,\"shout\":\"\",\"squad\":\"\",\"customizations\":{\"color\":\"#00ff00\",\"head\":\"alligator\",\"tail\":\"alligator\"}},{\"id\":\"gs_gySFSpV4drjrQjmDxSYCW9yC\",\"name\":\"Frank The Tank\",\"latency\":\"237\",\"health\":99,\"body\":[{\"x\":9,\"y\":8},{\"x\":9,\"y\":9},{\"x\":9,\"y\":9}],\"head\":{\"x\":9,\"y\":8},\"length\":3,\"shout\":\"\",\"squad\":\"\",\"customizations\":{\"color\":\"#ffff33\",\"head\":\"whale\",\"tail\":\"dragon\"}}],\"food\":[{\"x\":2,\"y\":10},{\"x\":2,\"y\":0},{\"x\":10,\"y\":8},{\"x\":5,\"y\":5}],\"hazards\":[]},\"you\":{\"id\":\"gs_wdk9FjyWh7f4xqVdTwyXKBPW\",\"name\":\"Mister Sneaky Pants\",\"latency\":\"32\",\"health\":99,\"body\":[{\"x\":1,\"y\":10},{\"x\":1,\"y\":9},{\"x\":1,\"y\":9}],\"head\":{\"x\":1,\"y\":10},\"length\":3,\"shout\":\"Hello, world!\",\"squad\":\"\",\"customizations\":{\"color\":\"#670000\",\"head\":\"sneaky\",\"tail\":\"skinny-jeans\"}}}")
        direction = gameLogic.decideMove(request)
        println("Move 2: " + direction)
//        assertEquals(Direction.RIGHT, direction)
    }

    @Test
    fun testMoveFindFoodParsing2() {
        val serializer = Json {
            isLenient = true
            ignoreUnknownKeys = true
        }
        val gameLogic = Logic()

        var request = serializer.decodeFromString<MoveRequest>("{\"game\":{\"id\":\"d4d793b4-830c-4257-b2a4-2adaa2f41688\",\"ruleset\":{\"name\":\"standard\",\"version\":\"cli\",\"settings\":{\"foodSpawnChance\":15,\"minimumFood\":1,\"hazardDamagePerTurn\":14,\"hazardMap\":\"\",\"hazardMapAuthor\":\"\",\"royale\":{\"shrinkEveryNTurns\":25},\"squad\":{\"allowBodyCollisions\":false,\"sharedElimination\":false,\"sharedHealth\":false,\"sharedLength\":false}}},\"map\":\"standard\",\"timeout\":2000,\"source\":\"\"},\"turn\":6,\"board\":{\"height\":11,\"width\":11,\"snakes\":[{\"id\":\"e423aef6-6a4c-4853-80ea-769063173020\",\"name\":\"Frankies\",\"latency\":\"21\",\"health\":96,\"body\":[{\"x\":6,\"y\":4},{\"x\":7,\"y\":4},{\"x\":8,\"y\":4},{\"x\":9,\"y\":4}],\"head\":{\"x\":6,\"y\":4},\"length\":4,\"shout\":\"\",\"squad\":\"\",\"customizations\":{\"color\":\"#FFFF33\",\"head\":\"whale\",\"tail\":\"dragon\"}},{\"id\":\"ffb518c0-a8fe-4a58-8693-d7662bff9015\",\"name\":\"Fredeberg\",\"latency\":\"67\",\"health\":96,\"body\":[{\"x\":4,\"y\":6},{\"x\":3,\"y\":6},{\"x\":2,\"y\":6},{\"x\":1,\"y\":6}],\"head\":{\"x\":4,\"y\":6},\"length\":4,\"shout\":\"\",\"squad\":\"\",\"customizations\":{\"color\":\"#00FF00\",\"head\":\"alligator\",\"tail\":\"alligator\"}},{\"id\":\"a48cbdb2-6875-4059-ba7a-f86c6a34544a\",\"name\":\"Jimseloms\",\"latency\":\"4\",\"health\":96,\"body\":[{\"x\":6,\"y\":6},{\"x\":6,\"y\":7},{\"x\":6,\"y\":8},{\"x\":6,\"y\":9}],\"head\":{\"x\":6,\"y\":6},\"length\":4,\"shout\":\"\",\"squad\":\"\",\"customizations\":{\"color\":\"#670000\",\"head\":\"sneaky\",\"tail\":\"skinny-jeans\"}}],\"food\":[{\"x\":5,\"y\":5}],\"hazards\":[]},\"you\":{\"id\":\"a48cbdb2-6875-4059-ba7a-f86c6a34544a\",\"name\":\"Jimseloms\",\"latency\":\"0\",\"health\":96,\"body\":[{\"x\":6,\"y\":6},{\"x\":6,\"y\":7},{\"x\":6,\"y\":8},{\"x\":6,\"y\":9}],\"head\":{\"x\":6,\"y\":6},\"length\":4,\"shout\":\"\",\"squad\":\"\",\"customizations\":{\"color\":\"#670000\",\"head\":\"sneaky\",\"tail\":\"skinny-jeans\"}}}")
        var direction = gameLogic.decideMove(request)
        println("Move 1: " + direction)
//        assertEquals(Direction.UP, direction)

        request = serializer.decodeFromString<MoveRequest>("{\"game\":{\"id\":\"d4d793b4-830c-4257-b2a4-2adaa2f41688\",\"ruleset\":{\"name\":\"standard\",\"version\":\"cli\",\"settings\":{\"foodSpawnChance\":15,\"minimumFood\":1,\"hazardDamagePerTurn\":14,\"hazardMap\":\"\",\"hazardMapAuthor\":\"\",\"royale\":{\"shrinkEveryNTurns\":25},\"squad\":{\"allowBodyCollisions\":false,\"sharedElimination\":false,\"sharedHealth\":false,\"sharedLength\":false}}},\"map\":\"standard\",\"timeout\":2000,\"source\":\"\"},\"turn\":7,\"board\":{\"height\":11,\"width\":11,\"snakes\":[{\"id\":\"e423aef6-6a4c-4853-80ea-769063173020\",\"name\":\"Frankies\",\"latency\":\"26\",\"health\":95,\"body\":[{\"x\":5,\"y\":4},{\"x\":6,\"y\":4},{\"x\":7,\"y\":4},{\"x\":8,\"y\":4}],\"head\":{\"x\":5,\"y\":4},\"length\":4,\"shout\":\"\",\"squad\":\"\",\"customizations\":{\"color\":\"#FFFF33\",\"head\":\"whale\",\"tail\":\"dragon\"}},{\"id\":\"ffb518c0-a8fe-4a58-8693-d7662bff9015\",\"name\":\"Fredeberg\",\"latency\":\"67\",\"health\":95,\"body\":[{\"x\":5,\"y\":6},{\"x\":4,\"y\":6},{\"x\":3,\"y\":6},{\"x\":2,\"y\":6}],\"head\":{\"x\":5,\"y\":6},\"length\":4,\"shout\":\"\",\"squad\":\"\",\"customizations\":{\"color\":\"#00FF00\",\"head\":\"alligator\",\"tail\":\"alligator\"}},{\"id\":\"a48cbdb2-6875-4059-ba7a-f86c6a34544a\",\"name\":\"Jimseloms\",\"latency\":\"4\",\"health\":95,\"body\":[{\"x\":6,\"y\":5},{\"x\":6,\"y\":6},{\"x\":6,\"y\":7},{\"x\":6,\"y\":8}],\"head\":{\"x\":6,\"y\":5},\"length\":4,\"shout\":\"\",\"squad\":\"\",\"customizations\":{\"color\":\"#670000\",\"head\":\"sneaky\",\"tail\":\"skinny-jeans\"}}],\"food\":[{\"x\":5,\"y\":5},{\"x\":1,\"y\":4}],\"hazards\":[]},\"you\":{\"id\":\"a48cbdb2-6875-4059-ba7a-f86c6a34544a\",\"name\":\"Jimseloms\",\"latency\":\"0\",\"health\":95,\"body\":[{\"x\":6,\"y\":5},{\"x\":6,\"y\":6},{\"x\":6,\"y\":7},{\"x\":6,\"y\":8}],\"head\":{\"x\":6,\"y\":5},\"length\":4,\"shout\":\"\",\"squad\":\"\",\"customizations\":{\"color\":\"#670000\",\"head\":\"sneaky\",\"tail\":\"skinny-jeans\"}}}")
        direction = gameLogic.decideMove(request)
        println("Move 2: " + direction)
//        assertEquals(Direction.RIGHT, direction)
    }

    @Test
    fun testDecideMove() {

        val request = getMoveRequest()
        val gameLogic = Logic()

        println(gameLogic.decideMove(request))
        println(gameLogic.decideMove(request))
//        println(gameLogic.decideMove(request))
//        println(gameLogic.decideMove(request))
    }

    @Test
    fun testFindFood() {
        val gameLogic = Logic()
        var request = getSoloFoodMoveRequest(
            Position(9,5),
            listOf(Position(9,5), Position(9,5)),
            listOf()
        );

        var route = mutableListOf<Position>()
        var nextPosition: Position? = null
        val timeInMillis = measureTimeMillis {
            nextPosition = gameLogic.getNextMoveTowardsPosition(request.you.head, Position(10,6), route, request.board, 0)
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
        val gameLogic = Logic()
        val board = Board(11, 11, listOf(), listOf(), listOf())

        var position = Position(0, 0)
        assertFalse(gameLogic.isOutOfBounds(position, board))

        position = Position(0, -1)
        assertTrue(gameLogic.isOutOfBounds(position, board))

        position = Position(11, 5)
        assertTrue(gameLogic.isOutOfBounds(position, board))
    }

    @Test
    fun testIsHazard() {
        val gameLogic = Logic()
        val board = Board(11, 11, listOf(), listOf(Position(5, 5)), listOf())
        assertFalse(gameLogic.isHazard(Position(1,1), board))
        assertTrue(gameLogic.isHazard(Position(5,5), board))
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

        val gameLogic = Logic()

        assertFalse(gameLogic.isCollidingWithSnake(Position(0, 0), battleSnake, board))
        assertFalse(gameLogic.isCollidingWithSnake(Position(2, 5), battleSnake, board))
        assertTrue(gameLogic.isCollidingWithSnake(Position(5, 5), battleSnake, board))
        // this should be false, since the snake is moving - so the tail is shifting position
        assertFalse(gameLogic.isCollidingWithSnake(Position(5, 6), battleSnake, board))
    }

//    fun testPositionMath() {
//        var position = Position(1,1);
//        assertEquals(Position(0, 1), position - Direction.LEFT)
//    }

    @Test
    fun testGetSafeMoves() {
        val request = getMoveRequest()
        val gameLogic = Logic()

        val safeMoves = gameLogic.getSafeMoves(request.you, request.board, false)
        println("Test done, found safeMoves: " + safeMoves)
    }

    @Test
    fun testGetDistance() {
        val gameLogic = Logic()
        // testing get distance between two points, but rounding the result to int, to make it easier to test :)
        assertEquals(3, gameLogic.getDistance(Position(1,1), Position(3,3)).roundToInt())

        assertEquals(1.0, gameLogic.getDistance(Position(1,1), Position(1,2)))
        assertEquals(2.0, gameLogic.getDistance(Position(1,9), Position(2,10)))

        println("test distance: " + gameLogic.getDistance(Position(8,6), Position(7, 5)))
    }
}
