package dk.westsworld.battlesnake

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LogicKtTest {
    @Test
    fun testLogic() {
        assert(true)
    }

    @Test
    fun testDecideMove() {
        val board = Board(11, 11, listOf(), listOf(), listOf())
        var game = Game(
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
        );

        var snake = BattleSnake(
            "snake id 1",
            "snake name",
            100,
            listOf(Position(5, 5), Position(5, 6)),
            "111",
            Position(5, 6),
            2,
            "",
            "snake squard",
            SnakeCustomization("","","")
        )

        var request = MoveRequest(
            game,
            2,
            board,
            snake
        )

        println(decideMove(request));
        println(decideMove(request));
        println(decideMove(request));
        println(decideMove(request));
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
}
