package com.aspanu.elevatorControl

import java.util.*

/**
 * Created by aspanu on 2017-06-06
 */

// Status of elevator
data class Elevator(val id: Int, var floor: Int, var direction: Direction, val stops: MutableList<Request> = mutableListOf(), var nextStop: Request)

data class Request (val floor: Int, val direction: Direction)

enum class Direction {
    UP,
    DOWN,
    NONE; // I.e. this will be used as a drop off location
}

class ElevatorController(numElevators: Int) {

    val elevators: MutableSet<Elevator> = mutableSetOf()
    val requestQueue: Queue<Request> = LinkedList<Request>()

    init {
        for (i in 0..numElevators-1) {
            elevators.add(Elevator(id = i, floor = 0, direction = Direction.NONE, nextStop = Request(floor = 0, direction = Direction.NONE)))
        }
    }

    fun status() = elevators

    fun pickUp(request: Request) {
        distributeToElevatorsOrQueue(request)
    }

    private fun distributeToElevatorsOrQueue(request: Request) {
        // Right now, I am just filtering this by the same direction
        // TODO: Add a 'distance' concept here in this filter
        val elevatorsInSameDirection = elevators.filter { it.direction == request.direction }
        if (elevatorsInSameDirection.isEmpty()) requestQueue.add(request)
        // TODO: Be more intelligent about adding an additional stop to specific elevators
        else elevatorsInSameDirection.first().stops.add(request)
    }

    fun up

}
