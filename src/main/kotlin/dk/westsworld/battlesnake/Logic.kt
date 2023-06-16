package dk.westsworld.battlesnake

import kotlin.system.measureTimeMillis

var game: Game? = null
val minimumDistanceToSnakeHeads = 2.0

// This is the heart of your snake
// It defines what to do on your next move
// You get the current game state passed as a parameter, you only have to return a direction to move into
fun decideMove(request: MoveRequest): Direction {
//    println(request)

    game = request.game

    var direction = Direction.UP
    val time = measureTimeMillis {
        direction = getMoveDirection(request.you, request.board)
    }

    println("time taken: " + time + "ms")
    println("final MOVE is: " + direction)

    return direction
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

    // checking if we are colliding with any snake on the board
    if (isCollidingWithASnake(position, board)) {
        return false
    }

    return true
}

fun getMoveDirection(battleSnake: BattleSnake, board: Board): Direction {
    // we are trying to hunt for food...
    val foodDirection = if (shouldFindFood(battleSnake, board)) {
        goTowardsFood(battleSnake, board)
    } else {
        null
    }

    var safeMoves = getSafeMoves(battleSnake, board, areThereOtherSnakes())

    // finding the optimal move, which is the one with "most space left" after the move has been done
    var bestDirection: Direction? = null
    var bestSpaceLeft = 0
    // finds the best direction
    for (move in safeMoves) {
        // calculating the battle snake's head position, after the move
        val position = battleSnake.head + move
        // calculating how much space is left, if we are using the new potision
        val spaceLeft = getSpaceLeft(position, board, mutableListOf<Position>())
        val distanceToSnakeOnBestMove = getDistanceToClosestSnake(position, battleSnake, board.snakes)

        // checking the amount of space left on the new move AND the distance to the nearest snake.
        if (spaceLeft > bestSpaceLeft && distanceToSnakeOnBestMove > minimumDistanceToSnakeHeads) {
            bestSpaceLeft = spaceLeft
            bestDirection = move
        }
    }

    println("food direction " + foodDirection)
    println("best direction " + bestDirection)

    // if it's a solo map, we should ALWAYS go for the food
    if (isSoloMap()) {
        if (foodDirection != null) {
            println("solo map, food direction is always best: " + foodDirection)
            return foodDirection
        }

        // no food? go for the best direction (with a fallback to direction down)
        return bestDirection ?: (safeMoves.randomOrNull() ?: Direction.DOWN)
    }

    // We need to prioritize the food, so, the found food direction is calculated "again"
    // ZOMG remember to not take a food move, if it's NOT safe... :D
    if (foodDirection != null && safeMoves.contains(foodDirection)) {
        println("entering ZOMG foodsection")
        val suggestedFoodPosition = battleSnake.head + foodDirection
        val foodSpaceLeft = getSpaceLeft(suggestedFoodPosition, board, mutableListOf<Position>())

        // only take a food move, if it's safe :)
        val distanceToFood = getDistanceToClosestSnake(suggestedFoodPosition, battleSnake, board.snakes)
        println("distance to closest snake is " + distanceToFood + ", should be more than " + minimumDistanceToSnakeHeads)
        if (distanceToFood > minimumDistanceToSnakeHeads) {
            // if the food move, is the move with the most space left, take it
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

    // check if we can kill a smaller snake in "the next move"
//    val killMove = findPossibleHeadToHeadKillDirection(battleSnake, board)
//    // if we can kill a snake, go for it
//    if (killMove != null) {
//        println("killMove available! " + killMove)
//        return killMove
//    }

    // direction can be null, if there are no safe moves, where we have looked ahead for possible moves from other snakes
    if (bestDirection == null) {
        println("bestDirection was null, finding a less safe move with no lookahead")
        // find a safe move, but don't lookahead to see a possible dangerous situation
        safeMoves = getSafeMoves(battleSnake, board, false) // shouldMovesBeSafe() => false?
        bestDirection = safeMoves.randomOrNull() ?: Direction.DOWN // TODO: handle NEVER eating our neck. Use isNeckPosition()
    }

    return bestDirection
}

/**
 * Finds the closest distance to the other snakes
 */
fun getDistanceToClosestSnake(position: Position, snake: BattleSnake, snakes: List<BattleSnake>): Double {
    var closestDistance = 100000.0

    // avoid all snakes at all costs!
    for (otherSnake in snakes) {
        if (otherSnake.id != snake.id) {
            val distance = getDistance(otherSnake.head, position)
            closestDistance = kotlin.math.min(distance, closestDistance)
        }
    }

    return closestDistance
}

/**
 * Tests if the snake should go for food!
 */
fun shouldFindFood(battleSnake: BattleSnake, board: Board): Boolean {
    if (isSoloMap() ) {
        return true
    }

    // always go for food!
    return true

    // we want to be the largest snake on the board
    var largestSnake = battleSnake.length
    for (snake in board.snakes) {
        // skip self!
        if (snake.id != battleSnake.id) {
            if (snake.length > largestSnake) {
                largestSnake = snake.length
            }
        }
    }

    // never be the smallest!
    if (battleSnake.length < largestSnake) {
        return true
    } else if (battleSnake.health > 30) {
        return false
    }

    return true
}

/**
 * Tries to find a killing head-to-head direction
 */
fun findPossibleHeadToHeadKillDirection(currentSnake: BattleSnake, board: Board): Direction? {
    // first finding all the safe moves, disregarding safety towards other snakes!
    var safeMoves = getSafeMoves(currentSnake, board, true)

    for (snake in board.snakes) {
        // are we checking against our own snake?
        if (snake.id != currentSnake.id) {
            println("kill check on snake: " + snake.name)
            // handle head-to-head collisions
            safeMoves = safeMoves.filter { direction ->
                // Find the next intended position
                val newPosition = currentSnake.head + direction

                // NOTE: if our snake is smaller than a snake close to us, do not go TOO close to it's head as it will eat us!
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
                    println("snake adjacent: " + snake.head.adjacent())
                    println("Direction " + direction)
                    println("Is move valid? " + valid)
                }

                valid
            }
        }
    }

    println("Found kill moves: " + safeMoves)

    return safeMoves.randomOrNull()
}

/**
 * Fetches the given snake's neck position
 * @param currentSnake the snake we are checking
 * @return the given snake's neck position
 */
fun getNeckPosition(currentSnake: BattleSnake): Position {
    return currentSnake.body[1]
}

/**
 * Checks if the given position, is the position of the given snakes neck.
 * @param snake the snake to check
 * @param position the position to check
 * @return true if the position is the snake's neck, else false
 */
fun isNeckPosition(snake: BattleSnake, position: Position): Boolean {
    return getNeckPosition(snake) == position
}

/**
 * finds the moves that are safe to do.
 * Should this be cached?
 */
fun getSafeMoves(currentSnake: BattleSnake, board: Board, lookAheadForSnakes: Boolean): List<Direction> {
    val head = currentSnake.head

    // do not hit our own neck!
    var safeMoves = enumValues<Direction>().filter { direction ->
        val newPosition = head + direction

        ! isNeckPosition(currentSnake, newPosition)
    }
    println("safeMoves0 !neck: " + safeMoves);
    // Finds moves to do, that are still on the map :)
    safeMoves = safeMoves.filter { direction ->
        // Find the next intended position
        val newPosition = head + direction

        // testing if the new position is out of bounds, of the current board
        ! isOutOfBounds(newPosition, board)
    }
    println("safeMoves1 !oob: " + safeMoves);

    // only check for hazards, if we have a map with hazards and there are any hazards :)
    if (game?.map == "royale" && !board.hazards.isEmpty()) {
        // finding damage per turn (if any), converting from nullable int, to regular int
        val damagePerTurn: Int = if (game?.ruleset?.settings?.hazardDamagePerTurn != null) {
            game?.ruleset?.settings?.hazardDamagePerTurn!!
        } else {
            0
        }

        // finds the next move, that is NOT a hazard (wall etc.)
        safeMoves = safeMoves.filter { direction ->
            // Find the next intended position
            val newPosition = head + direction

            var isHazardMove = isHazard(newPosition, board)

            // if we have a hazard move, check if we can survive it!
            if (isHazardMove && currentSnake.health > damagePerTurn) {
                isHazardMove = false
            }

            isHazardMove
        }
        println("safeMoves2 !hazards: " + safeMoves);
    }
    // making sure we are not hitting a snake on an existing position
    safeMoves = safeMoves.filter { direction ->
        // Find the next intended position
        val newPosition = head + direction

        ! isCollidingWithASnake(newPosition, board)
    }
    println("safeMoves3 !snakes: " + safeMoves);
    if (safeMoves.isEmpty()) {
        println("no safe moves left, before disregardSafety checks")
        return listOf<Direction>()
    }

    // if there are other snakes on the map, we should check the distance to the other snakes' heads, to be safer
    if (lookAheadForSnakes) {
        safeMoves = safeMoves.filter { direction ->
            // Find the next intended position
            val newPosition = head + direction

            // checking if the new position is on an opposing snake in the game
            var validMove = true //!isCollidingWithSnake(newPosition, snake, board)

            if (getDistanceToClosestSnake(newPosition, currentSnake, board.snakes) < minimumDistanceToSnakeHeads) {
                validMove = false
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
    var distanceToFood = 100000.0
    for (foodPosition in board.food) {
        val distance = getDistance(battleSnake.head, foodPosition)

        if (distance < distanceToFood) {
            distanceToFood = distance
            safeFoodPosition = foodPosition
        }
    }

    if (safeFoodPosition == null) {
        return null
    }


    // using floodfill to find a path to the food
    var route = mutableListOf<Position>()
    println("safe food position is: " + safeFoodPosition)

    // when solo mapping, allow 100 moves as "ok" :)
    val maxDepth = if (isSoloMap()) {
        100
    } else {
        // calculate the "best length" to the food
        val distance = (getDistance(battleSnake.head, safeFoodPosition) * 1.5).toInt()
        // adding a maximum distance, just to help out the processing
        if (distance > 20 ) 20 else distance
    }

    println("max depth is: " + maxDepth)
    println("current head position is: " + battleSnake.head)

    var nextPosition = getNextMoveTowardsPosition(battleSnake.head, safeFoodPosition, route, board, maxDepth)
    println("Route is: " + route)
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
fun getNextMoveTowardsPosition(currentPosition: Position, destinationPosition: Position, route: MutableList<Position>, board: Board, maxDepth: Int): Position? {
    if (maxDepth < 0) {
        return null
    }
    println("getNextMoveTowardsPosition() " + currentPosition + " -> " + destinationPosition)
    println("getNextMoveTowardsPosition() route: " + route)
    if (currentPosition == destinationPosition) {
        // if the first possible destination is the desired destination, return it!
        return if (route.isEmpty()) {
            destinationPosition
        } else {
            route.first()
        }
    } else {
        // sorting the adjacent positions, so we might get a better path
        val adjacentPositions = currentPosition.adjacent()
//        println("not sorted: " + adjacentPositions)
        var adjacentPositionsSorted = adjacentPositions.toMutableList()
        adjacentPositionsSorted.sortBy { getDistance(it, destinationPosition) }
//        println("sorted: " + adjacentPositionsSorted)

        for (position in adjacentPositionsSorted) {
            if (isSafePosition(position, board)) {
                if (!route.contains(position)) {
                    // adding the current position to our route towards the destination!
                    route.add(position)

                    val nextMove = getNextMoveTowardsPosition(position, destinationPosition, route, board, maxDepth - 1)

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

private fun areThereOtherSnakes(): Boolean {
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
    return Math.ceil(Math.sqrt(Math.pow(xPos.toDouble(), 2.0) + Math.pow(yPos.toDouble(), 2.0)))
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
    if (game?.map == "arcade_maze") {
        return false
    }

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
 * Checks if the given position is colliding with a snake (body/head/tail) on the board
 * @param position the position to check.
 * @param board the current game board.
 * @return Boolean
 */
fun isCollidingWithASnake(position: Position, board: Board): Boolean {
    for (snake in board.snakes) {
        if (isCollidingWithSnake(position, snake, board)) {
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

    // only remove the tail, if the body is more than one element
    if (snake.length > 1 && ! hasImmediateFoodMove(snake, board)) {
        snakeBody = snakeBody.subList(0, snake.length - 1 )
    }

    return snakeBody.contains(position)
}

