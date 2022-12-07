package dk.westsworld.battlesnake

// This is the heart of your snake
// It defines what to do on your next move
// You get the current game state passed as a parameter, you only have to return a direction to move into
fun decideMove(request: MoveRequest): Direction {
    val head = request.you.head

    // Find all "safe" moves to do
    // (if you do a move that is not in this list, you will lose)
    var safeMoves = enumValues<Direction>().filter { direction ->
        // Find the next intended position
        val newPosition = head + direction

        // testing if the new position is out of bounds, of the current board
        ! isOutOfBounds(newPosition, request.board)
    }

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

    // Step 0: Don't let your Battlesnake move back on its own neck

    // TODO: Step 1 - Don't hit walls.
    // Use information in the request to prevent your Battlesnake from moving beyond the boundaries of the board.

    // TODO: Step 2 - Don't hit yourself.
    // Use information in the request to prevent your Battlesnake from colliding with itself.
    // val myBody = request.you.body

    // TODO: Step 3 - Don't collide with others.
    // Use information in the request to prevent your Battlesnake from colliding with others.

    // TODO: Step 4 - Find food.
    // Use information in the request to seek out and find food.
    // Finally, choose a move from the available safe moves.
    // TODO: Step 5 - Select a move to make based on strategy, rather than random.

    // Note: we use randomOrNull, so we don't get an exception when we are out of options
    // Rather, we move down, which will most likely kill us, but at least we do something
    return safeMoves.randomOrNull() ?: Direction.DOWN
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

