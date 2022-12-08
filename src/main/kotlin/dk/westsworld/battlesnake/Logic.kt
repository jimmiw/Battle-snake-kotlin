package dk.westsworld.battlesnake

// This is the heart of your snake
// It defines what to do on your next move
// You get the current game state passed as a parameter, you only have to return a direction to move into
fun decideMove(request: MoveRequest): Direction {
    val head = request.you.head

//    // Finds moves to do, that are still on the map :)
//    var movesAvailable = enumValues<Direction>().filter { direction ->
//        // Find the next intended position
//        val newPosition = head + direction
//
//        // testing if the new position is out of bounds, of the current board
//        ! isOutOfBounds(newPosition, request.board)
//    }
//
//    // finds the next move, that is NOT a hazard (wall etc.)
//    movesAvailable = movesAvailable.filter { direction ->
//        // Find the next intended position
//        val newPosition = head + direction
//
//        ! isHazard(newPosition, request.board)
//    }
//
//    var bestScore = -10000;
//    var bestMove = Direction.DOWN;
//
//    for (move in movesAvailable) {
//        val score = minimax(move, request.board)
//        if (score > bestScore) {
//            bestScore = score
//            bestMove = move
//        }
//    }
//
//    return bestMove


    // Finds moves to do, that are still on the map :)
    var safeMoves = enumValues<Direction>().filter { direction ->
        // Find the next intended position
        val newPosition = head + direction

        // testing if the new position is out of bounds, of the current board
        ! isOutOfBounds(newPosition, request.board)
    }

    // finds the next move, that is NOT a hazard (wall etc.)
    safeMoves = safeMoves.filter { direction ->
        // Find the next intended position
        val newPosition = head + direction

        ! isHazard(newPosition, request.board)
    }

    safeMoves = safeMoves.filter { direction ->
        // Find the next intended position
        val newPosition = head + direction

        ! isCollidingWithSnake(newPosition, request.you)
    }

    // avoid other snakes at all costs!
    for (snake in request.board.snakes) {
        safeMoves = safeMoves.filter { direction ->
            // Find the next intended position
            val newPosition = head + direction
            // checking if the new position is on an opposing snake in the game
            !isCollidingWithSnake(newPosition, snake)
        }
    }

    // find fruits! so we can live long and be long!!!!
    val foodMoves = safeMoves.filter { direction ->
        // Find the next intended position
        val newPosition = head + direction

        isFoodMove(newPosition, request.board)
    }

    // Note: we use randomOrNull, so we don't get an exception when we are out of options
    // Rather, we move down, which will most likely kill us, but at least we do something
    return foodMoves.randomOrNull() ?: safeMoves.randomOrNull() ?: Direction.DOWN
}

fun minimax(move: Direction, board: Board): Int {
    return 0
}

/**
 * CAN the next move be a move with FOOD?
 * @param position the new position to check
 * @param board the current game board
 * @return Boolean true if the given position contains food
 */
fun isFoodMove(position: Position, board: Board): Boolean {
    // handle no food on the board
    if (board.food.isEmpty()) {
        return false
    }

    for (food in board.food) {
        if (food == position) {
            return true
        }
    }

    return false
}

/**
 * Tests if the given position is out of bounds on the given board
 * @param position the position to check.
 * @param board the current game board.
 * @return Boolean
 */
fun isOutOfBounds(position: Position, board: Board): Boolean {
    if (position.x < 0 || position.y < 0) {
        return true
    } else if (position.x >= board.width || position.y >= board.height) {
        return true
    }
    // seems we are in bounds :)
    return false
}

/**
 * Tests if the given position is a wall OR any other hazard on the board
 * @param position the position to check.
 * @param board the current game board.
 * @return Boolean
 */
fun isHazard(position: Position, board: Board): Boolean {
    for (hazard in board.hazards) {
        if (position == hazard) {
            return true
        }
    }

    return false
}

/**
 * Tests if the given position is hitting anywhere on the given snake body
 * @param position the position to check for collision
 * @param battleSnake the snake to test on
 * @return Boolean
 */
fun isCollidingWithSnake(position: Position, battleSnake: BattleSnake): Boolean {
    // Step 0: Don't let your Battlesnake move back on its own neck
    for (bodyPosition in battleSnake.body) {
        // if the given position is on a part of the given battleSnakes body, we are hitting the body
        if (bodyPosition == position) {
            return true
        }
    }

    return false
}

