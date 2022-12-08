package dk.westsworld.battlesnake

// This is the heart of your snake
// It defines what to do on your next move
// You get the current game state passed as a parameter, you only have to return a direction to move into
fun decideMove(request: MoveRequest): Direction {
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
    val foodDirection: Direction? = goTowardsFood(request.you, request.board)
//    val killingDirection: Direction? = findPossibleHeadToHeadKillDirection(request.you, request.board)
    val safeMoves = getSafeMoves(request.you, request.board)
    println("safeMoves: " + safeMoves)
    // picking a single safe move to use
    val safeMoveDirection = safeMoves?.randomOrNull()
    println("food direction: " + foodDirection)
    println("safe move direction: " + safeMoveDirection)

    val direction = /*killingDirection ?:*/ foodDirection ?: safeMoveDirection ?: Direction.DOWN

    println("MOVE: " + direction)

    return direction

    // Note: we use randomOrNull, so we don't get an exception when we are out of options
    // Rather, we move down, which will most likely kill us, but at least we do something
    //return nextMoveIsFood.randomOrNull() ?: goTowardsFood() ?: safeMoves.randomOrNull() ?: Direction.DOWN
}

/**
 * Tries to find a killing head-to-head direction
 */
fun findPossibleHeadToHeadKillDirection(currentSnake: BattleSnake, board: Board): Direction? {
    // first finding all the safe moves
    var safeMoves = getSafeMoves(currentSnake, board);

    for (snake in board.snakes) {
        // handle head-to-head collisions
        safeMoves = safeMoves?.filter { direction ->
            // Find the next intended position
            val newPosition = currentSnake.head + direction

            // if our snake is smaller than a snake close to us, do not go TOO close to it's head as it will eat us!
            var valid = true
            if (snake.head.adjacent().contains(newPosition)) {
                // if a snake is close, we might lose head to head
                valid = false
                // aha! snake is smaller, try to eat it!
                if (snake.length < currentSnake.length) {
                    valid = true
                }
            }

            // Only print out this, if the move is valid :)
            if (valid) {
                println("Check for head-to-head collision against " + snake.name + " @ " + newPosition)
                println("Lengths: other vs mine: " + snake.length + "," + currentSnake.length)
                println("Is snake adjacent? " + snake.head.adjacent().contains(newPosition))
                println("Direction " + direction)
                println("Is move valid? " + valid)
            }

            valid
        }
    }

    return safeMoves?.randomOrNull()
}

/**
 * finds the moves that are safe to do.
 * Should this be cached?
 */
fun getSafeMoves(currentSnake: BattleSnake, board: Board): List<Direction>? {
    val head = currentSnake.head
    val neck = currentSnake.body[1];

    // do not hit our own neck!
    var safeMoves = enumValues<Direction>().filter { direction ->
        val newPosition = head + direction

        newPosition != neck
    }

    // Finds moves to do, that are still on the map :)
    safeMoves = safeMoves.filter { direction ->
        // Find the next intended position
        val newPosition = head + direction

        // testing if the new position is out of bounds, of the current board
        ! isOutOfBounds(newPosition, board)
    }

    // finds the next move, that is NOT a hazard (wall etc.)
    safeMoves = safeMoves.filter { direction ->
        // Find the next intended position
        val newPosition = head + direction

        ! isHazard(newPosition, board)
    }

    if (safeMoves.isEmpty()) {
        println("no safe moves left, before looking at other snakes!")
        return null
    }

    // avoid all snakes at all costs!
    for (snake in board.snakes) {
        safeMoves = safeMoves.filter { direction ->
            // Find the next intended position
            val newPosition = head + direction

            // checking if the new position is on an opposing snake in the game
            var validMove = !isCollidingWithSnake(newPosition, snake, board)

            // only check distance on other snakes :)
            if (snake.id != currentSnake.id) {
                // checking if the given snake is within too close of a distance of the new position
                val distance = getDistance(snake.head, newPosition)
                if (distance <= 1.5) {
                    println("Move " + direction + " is not valid, as it is too close to an other snake")
                    println("points: " + snake.head + " & " + newPosition)
                    println("distance: " + distance)
                    validMove = false
                }
            }

            validMove
        }
    }

    return safeMoves
}

/**
 * Tries to find a direction to go for food!
 * @param battleSnake the snake who is looking for food
 * @param board the current game board
 * @return The direction to choose or null
 */
fun goTowardsFood(battleSnake: BattleSnake, board: Board): Direction? {
    var closetFoodPosition: Position? = null;

    for (foodPosition in board.food) {
        // no current best, just grab this food position
        if (closetFoodPosition == null) {
            closetFoodPosition = foodPosition
        } else {
            val newDistance = getDistance(battleSnake.head, foodPosition)
            val currentDistance = getDistance(battleSnake.head, closetFoodPosition)

            if (newDistance < currentDistance) {
                closetFoodPosition = foodPosition
            }
        }
    }

    // no food found... return early
    if (closetFoodPosition == null) {
        return null
    }

    // fetches the list of safe moves, for our snake
    val safeMoves = getSafeMoves(battleSnake, board)

    if (safeMoves.isNullOrEmpty()) {
        return null
    }

    // finds the next move, based on the closet food position.
    // we can only advance in a direction, if the move is safe to use
    if (battleSnake.head.x < closetFoodPosition.x && safeMoves.contains(Direction.RIGHT)) {
        return Direction.RIGHT
    } else if (battleSnake.head.x > closetFoodPosition.x && safeMoves.contains(Direction.LEFT)) {
        return Direction.LEFT
    } else if (battleSnake.head.y < closetFoodPosition.y && safeMoves.contains(Direction.UP)) {
        return Direction.UP
    } else if (battleSnake.head.y > closetFoodPosition.y && safeMoves.contains(Direction.DOWN)) {
        return Direction.DOWN
    }

    return null
}

/**
 * Calculating the distance between two points in a 2D matrix
 * @param position1
 * @param position2
 * @return the distance between the two given points
 */
fun getDistance(position1: Position, position2: Position): Double {
    val xPos = (position2.x - position1.x)
    val yPos = (position2.y - position1.y)
    return Math.sqrt(Math.pow(xPos.toDouble(), 2.0) + Math.pow(yPos.toDouble(), 2.0))
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
//    println("Check colliding with " + snake.name)
    // only remove the tail, if the body is more than one element
    if (snake.length > 1 && ! hasImmediateFoodMove(snake, board)) {
        snakeBody = snakeBody.subList(0, snake.length - 1 )
//        println("subtracting snake tail!")
    }

    // Step 0: Don't let your Battlesnake move back on its own neck
    for (bodyPosition in snakeBody) {
        // if the given position is on a part of the given battleSnakes body, we are hitting the body
        if (bodyPosition == position) {
//            println("New position is hitting body at " + position + " with body pos: " + bodyPosition)
            return true
        }
    }

    return false
}

