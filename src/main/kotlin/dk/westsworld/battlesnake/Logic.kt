package dk.westsworld.battlesnake

var game: Game? = null
//= Game("", Ruleset("", "", RulesetSettings(1,1,1,null, null)), null, 1, null)

// This is the heart of your snake
// It defines what to do on your next move
// You get the current game state passed as a parameter, you only have to return a direction to move into
fun decideMove(request: MoveRequest): Direction {
    println(request)

    game = request.game
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


    return getMoveDirection(request.you, request.board)
}

/**
 * Checks how much space the given snake will have, if it makes the given move to the new position.
 */
fun getSpaceLeft(position: Position, board: Board, checkedPositions: MutableList<Position>): Int {
    var space = 0 // should it be 1, since we have already decided on a move?

    // is the given position is safe
    if (isSafePosition(position, board)) {
        space++
        // adding the current element to the list of positions that have already been checked
        checkedPositions.add(position)
        // test the positions next to the snake
        for (p in position.adjacent()) {
            // skip checked positions :)
            if (! checkedPositions.contains(p)) {
                // is the new position safe?
                if (isSafePosition(p, board)) {
                    space += getSpaceLeft(p, board, checkedPositions)
                }
            }
        }
    }

    return space
}

/**
 * Checking if the given position on the board is safe
 */
fun isSafePosition(position: Position, board: Board): Boolean {
    if (isOutOfBounds(position, board)) {
        return false
    }
    if (isHazard(position, board)) {
        return false
    }

    for (snake in board.snakes) {
        if (isCollidingWithSnake(position, snake, board)) {
            return false
        }
    }

    return true
}

fun getMoveDirection(battleSnake: BattleSnake, board: Board): Direction {
    // we are trying to hunt for food...
    val foodDirection = goTowardsFood(battleSnake, board)
//    var safeMoves = getSafeMoves(battleSnake, board, shouldMovesBeSafe())

    println("food direction: " + foodDirection)
//    println("safeMoves: " + safeMoves)

    // finding the optimal move, which is the one with "most space left" after the move has been done
    var bestDirection: Direction? = null
    var bestSpaceLeft = 0

//    for (move in safeMoves) {
//        println("checking safe move " + move)
//        // calculating the battle snake's head position, after the move
//        val position = battleSnake.head + move
//        // calculating how much space is left, if that move is taken
//        val spaceLeft = getSpaceLeft(position, board, mutableListOf<Position>())
//
//        println("space left is " + spaceLeft)
//
//        if (spaceLeft > bestSpaceLeft) {
//            bestSpaceLeft = spaceLeft
//            bestDirection = move
//            println("is new bestDirection: " + bestDirection + " with " + spaceLeft + " space")
//        }
//    }

    // We need to prioritize the food, so, the found food direction is calculated "again"
    if (foodDirection != null) {
        val position = battleSnake.head + foodDirection
        val foodSpaceLeft = getSpaceLeft(position, board, mutableListOf<Position>())

        // if it's a solo map, we should ALWAYS go for the food
        if (isSoloMap()) {
            bestDirection = foodDirection
            println("solo map, food direction is always best: " + foodDirection)
        } else {
            // if the food move, is the move with the move space left, take it
            if (foodSpaceLeft >= bestSpaceLeft) {
                bestDirection = foodDirection
                println("food is new bestDirection: " + foodDirection + " with " + foodSpaceLeft + " space - CHOSEN")
            } else {
                // checking if the food move, can still be used... is there enough room for the snake if it's +1 length?
                if (foodSpaceLeft + 1 > battleSnake.length) {
                    bestDirection = foodDirection
                    println("food is big enough: " + foodDirection + " with " + foodSpaceLeft + " space - has enough space for the snake, let's try it out!")
                }
            }
        }
    }

    // direction can be null, if there are no safe moves, where we have looked ahead for possible moves from other snakes
    if (bestDirection == null) {
        println("bestDirection was null, finding a less safe move with no lookahead")
        // find a safe move, but don't lookahead to see a possible dangerous situation
        var safeMoves = getSafeMoves(battleSnake, board, shouldMovesBeSafe()) // shouldMovesBeSafe() => false?
        bestDirection = safeMoves.randomOrNull() ?: Direction.DOWN
    }

    println("final MOVE is: " + bestDirection)

    return bestDirection
}

/**
 * Tries to find a killing head-to-head direction
 */
fun findPossibleHeadToHeadKillDirection(currentSnake: BattleSnake, board: Board): Direction? {
    // first finding all the safe moves
    var safeMoves = getSafeMoves(currentSnake, board, shouldMovesBeSafe())

    for (snake in board.snakes) {
        // handle head-to-head collisions
        safeMoves = safeMoves.filter { direction ->
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

    return safeMoves.randomOrNull()
}

/**
 * finds the moves that are safe to do.
 * Should this be cached?
 */
fun getSafeMoves(currentSnake: BattleSnake, board: Board, disregardSafety: Boolean): List<Direction> {
    val head = currentSnake.head
    val neck = currentSnake.body[1]

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
        return listOf<Direction>()
    }

    // avoid all snakes at all costs!
    for (snake in board.snakes) {
        safeMoves = safeMoves.filter { direction ->
            // Find the next intended position
            val newPosition = head + direction

            // checking if the new position is on an opposing snake in the game
            var validMove = !isCollidingWithSnake(newPosition, snake, board)

            // only check distance on other snakes (AND disreguardSafety is false)
            if (snake.id != currentSnake.id && ! disregardSafety) {
                // checking if the given snake is within too close of a distance of the new position
                val distance = getDistance(snake.head, newPosition)
                if (distance <= 2) {
                    println("Move " + direction + " is not valid, as it is too close to an other snake")
                    println("snake vs newPosition: " + snake.head + " & " + newPosition)
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
    var safeFoodPosition: Position? = null
    // find food, that is not close to other snakes
    for (foodPosition in board.food) {
        var enemyClosestDistance = 1000000.0
        val myDistance = getDistance(battleSnake.head, foodPosition)

        for (snake in board.snakes) {
            // skip myself
            if (snake.id == battleSnake.id) {
                continue
            }

            val distance = getDistance(snake.head, foodPosition)
            // this enemy is closer to the food, save his distance
            if (distance < enemyClosestDistance) {
                enemyClosestDistance = distance
            }
        }

        if (enemyClosestDistance > myDistance) {
            safeFoodPosition = foodPosition
        }
    }

    if (safeFoodPosition == null) {
        return null
    }

    println("safe food position is: " + safeFoodPosition)

    // using floodfill to find a path to the food
    var route = mutableListOf<Position>();
    var nextPosition = getNextMoveTowardsPosition(battleSnake.head, safeFoodPosition, route, board, 0);
    println("Route is: " + route)
    println("current head position is: " + battleSnake.head)
    println("suggested new position is: " + nextPosition)
    var nextDirection = battleSnake.head.getDirection(nextPosition ?: Position(0,0))
    println("suggested new direction is: " + nextDirection)

    return nextDirection


//    // fetches the list of safe moves, for our snake
//    val safeMoves = getSafeMoves(battleSnake, board, shouldMovesBeSafe())
//
//    if (safeMoves.isEmpty()) {
//        return null
//    }
//
//    // finds the next move, based on the closet food position.
//    // we can only advance in a direction, if the move is safe to use
//    if (battleSnake.head.x < safeFoodPosition.x && safeMoves.contains(Direction.RIGHT)) {
//        return Direction.RIGHT
//    } else if (battleSnake.head.x > safeFoodPosition.x && safeMoves.contains(Direction.LEFT)) {
//        return Direction.LEFT
//    } else if (battleSnake.head.y < safeFoodPosition.y && safeMoves.contains(Direction.UP)) {
//        return Direction.UP
//    } else if (battleSnake.head.y > safeFoodPosition.y && safeMoves.contains(Direction.DOWN)) {
//        return Direction.DOWN
//    }
//
//    return null
}

/**
 * Suggests the next direction to walk in, to get to the given position.
 * NOTE: using floodfill to calculate the next step :)
 */
fun getNextMoveTowardsPosition(currentPosition: Position, destinationPosition: Position, route: MutableList<Position>, board: Board, depth: Int): Position? {
    if (depth > 70) {
        println("stopping because of max depth!")
        return route.first()
    }

    if (currentPosition == destinationPosition) {
        // if the first possible destination is the food, return it!
        return if (route.isEmpty()) {
            destinationPosition
        } else {
            route.first()
        }
    } else {
        println("" + currentPosition + " adjacent " + currentPosition.adjacent())
        for (position in currentPosition.adjacent()) {
            if (isSafePosition(position, board)) {
                if (!route.contains(position)) {
                    // adding the current position to our route towards the destination!
                    route.add(position)

                    val nextMove = getNextMoveTowardsPosition(position, destinationPosition, route, board, depth + 1)

                    if (nextMove == null) {
                        // no route found, remove position, return null
                        route.removeLast()
                    } else {
                        return nextMove
                    }
                }
            }
        }
    }

    return null
}

private fun shouldMovesBeSafe(): Boolean {
    // ZOMG had to do a nasty switcharooooooo... if the game starts with solo, return true REVERSE!
    return ! isSoloMap()
}

private fun isSoloMap(): Boolean {
    return game?.map?.startsWith("solo") ?: false
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

