package dk.westsworld.battlesnake

// This is the heart of your snake
// It defines what to do on your next move
// You get the current game state passed as a parameter, you only have to return a direction to move into
fun decideMove(request: MoveRequest): Direction {
    val head = request.you.head
    println(request)

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


//    val safeMoves = getSafeMoves(request.board, request.you)

    // we are trying to hunt for food... or go down :)
    var direction = goTowardsFood(request.you, request.board) ?: Direction.LEFT
//    if (direction == null) {
//        if (safeMoves.isNullOrEmpty()) {
//            direction = Direction.DOWN
//        } else {
//            direction = safeMoves.random()
//        }
//    }

    println("MOVE: " + direction + " @ " + (head + direction))

    return direction

    // Note: we use randomOrNull, so we don't get an exception when we are out of options
    // Rather, we move down, which will most likely kill us, but at least we do something
    //return nextMoveIsFood.randomOrNull() ?: goTowardsFood() ?: safeMoves.randomOrNull() ?: Direction.DOWN
}

fun getSafeMoves(board: Board, currentSnake: BattleSnake): List<Direction>? {
    val head = currentSnake.head
    val neck = currentSnake.body[1];

    // do not hit our own neck!
    var safeMoves = enumValues<Direction>().filter { direction ->
        val newPosition = head + direction

        newPosition != neck
    }

//    println("Neck OK moves");
//    println(safeMoves)

    // Finds moves to do, that are still on the map :)
    safeMoves = safeMoves.filter { direction ->
        // Find the next intended position
        val newPosition = head + direction

        // testing if the new position is out of bounds, of the current board
        ! isOutOfBounds(newPosition, board)
    }

//    println("Bounds OK moves");
//    println(safeMoves)

    // finds the next move, that is NOT a hazard (wall etc.)
    safeMoves = safeMoves.filter { direction ->
        // Find the next intended position
        val newPosition = head + direction

        ! isHazard(newPosition, board)
    }

//    println("Hazard OK moves");
//    println(safeMoves)

    // are we colliding with ourselves?
    safeMoves = safeMoves.filter { direction ->
        // Find the next intended position
        val newPosition = head + direction

//        println("Check colliding move @ " + direction)
        ! isCollidingWithSnake(newPosition, currentSnake, board)
    }

//    println("Colliding OK moves");
//    println(safeMoves)

    if (safeMoves.isEmpty()) {
//        println("no safe moves left, before looking at other snakes!")
        return null
    }

    // avoid other snakes at all costs!
    for (snake in board.snakes) {
//        println("check other snakes: " + snake.name)
        safeMoves = safeMoves.filter { direction ->
            // Find the next intended position
            val newPosition = head + direction

            // TODO: look ahead for the given snake, to check if the position we are choosing, could be chosen by that snake

            // checking if the new position is on an opposing snake in the game
            val validMove = !isCollidingWithSnake(newPosition, snake, board)

//            println("check for collision1 " + newPosition)
//            println("Is move valid? " + validMove)

            validMove
        }

        // handle head-to-head collisions
        safeMoves = safeMoves.filter { direction ->
            // Find the next intended position
            val newPosition = head + direction

            // if our snake is smaller than a snake close to us, do not go TOO close to it's head as it will eat us!
            var valid = true
            if (snake.head.adjacent().contains(newPosition)) {
                // if a snake is close, we might loose head to head
                valid = false
                // aha! snake is smaller, try to eat it!
                if (snake.length < currentSnake.length) {
                    valid = true
                }
            }

            println("Check for head-to-head collision @ " + newPosition)
            println("Lengths: other vs mine: " + snake.length + "," + currentSnake.length)
            println("Is snake adjacent? " + snake.head.adjacent().contains(newPosition))
            println("Is move valid? " + valid)

            valid
        }
    }

    return safeMoves
}

fun goTowardsFood(battleSnake: BattleSnake, board: Board): Direction? {
    var closetFoodPosition: Position? = null;

    for (foodPosition in board.food) {
        // no best
        if (closetFoodPosition == null) {
            closetFoodPosition = foodPosition
        }
    }

    // fetches the list of safe moves, for our snake
    val safeMoves = getSafeMoves(board, battleSnake)

//    println("Found safeMoves:")
//    println(safeMoves)

    if (safeMoves.isNullOrEmpty()) {
        println("No safe moves found in goTowardsFood().. returning UP")
        return Direction.UP
    }

    val head = battleSnake.head;

    // finds the next move, based on the closet food position.
    // we can only advance in a direction, if the move is safe to use
    if (closetFoodPosition != null && head.x < closetFoodPosition.x && safeMoves.contains(Direction.RIGHT)) {
        return Direction.RIGHT
    } else if (closetFoodPosition != null && head.x > closetFoodPosition.x && safeMoves.contains(Direction.LEFT)) {
        return Direction.LEFT
    } else if (closetFoodPosition != null && head.y < closetFoodPosition.y && safeMoves.contains(Direction.UP)) {
        return Direction.UP
    } else {
        return Direction.DOWN
    }
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
 * Checks if the given battle snake can make a food move
 */
fun hasImmediateFoodMove(battleSnake: BattleSnake, board: Board): Boolean {
    for (move in enumValues<Direction>()) {
        if (isFoodMove(battleSnake.head + move.position, board)) {
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
 * @param snake the snake to test on
 * @return Boolean
 */
fun isCollidingWithSnake(position: Position, snake: BattleSnake, board: Board): Boolean {
    var snakeBody = snake.body
    println("Check colliding with " + snake.name)
    // only remove the tail, if the body is more than one element
    if (snake.length > 1 && ! hasImmediateFoodMove(snake, board)) {
        snakeBody = snakeBody.subList(0, snake.length - 1 )
        println("subtracting snake tail!")
    }

    // Step 0: Don't let your Battlesnake move back on its own neck
    for (bodyPosition in snakeBody) {
        // if the given position is on a part of the given battleSnakes body, we are hitting the body
        if (bodyPosition == position) {
            println("New position is hitting body at " + position + " with body pos: " + bodyPosition)
            return true
        }
    }

    println("no colliding parts found!")

    return false
}

