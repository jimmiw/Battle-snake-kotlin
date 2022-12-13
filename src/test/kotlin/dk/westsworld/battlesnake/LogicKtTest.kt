package dk.westsworld.battlesnake

import kotlin.math.roundToInt
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
            nextPosition = getNextMoveTowardsPosition(request.you.head, Position(10,5), route, request.board, 0)
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
