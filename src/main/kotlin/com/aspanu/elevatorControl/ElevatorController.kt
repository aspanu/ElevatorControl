package com.aspanu.elevatorControl

import com.aspanu.elevatorControl.Direction.*
import java.lang.Math.abs
import java.util.*

/**
 * Created by aspanu on 2017-06-06
 */

/**
 * An elevator containing its status and the information it needs to guide itself.
 */
data class Elevator(val id: Int, var floor: Int, var direction: Direction, val stops: MutableList<Request> = mutableListOf(), var nextStop: Request) {

    /**
     * If the elevator is at a rest or the next stop is closer than the current next stop, we'll go there next,
     * otherwise, queue it up. Note that we assume that the direction of this request is the same as our direction
     * of movement
     */
    fun add(request: Request) {
        if (direction == NONE || distance(request) < distance(nextStop)) {
            nextStop = request
            direction = getDirectionForRequest(nextStop) // Elevator will start or continue moving in this direction
        }
        stops.add(request)
    }

    private fun distance(request: Request): Int {
        return abs(request.floor - floor)
    }

    fun moveTimeUnits(numberOfTimeUnits: Int) {
        for (i in 0..numberOfTimeUnits - 1) {
            moveOneTimeUnit()
        }
    }

    private fun moveOneTimeUnit() {
        if (nextStop.floor == floor) {
            serviceStop()
        }
        moveOneFloorIfNecessary()
    }

    private fun moveOneFloorIfNecessary() {
        if (direction == UP) floor++
        else if (direction == DOWN) floor--
    }

    /**
     * Each time we stop, we will find the stop with the next smallest distance in the direction that we are moving
     * and keep going. Note: the 'in the direction we are moving' should not matter: we should only accept updates or
     * pickup requests in the same direction we are moving anyway.
     */
    private fun serviceStop() {
        stops.remove(nextStop)
        if (stops.size == 0) {
            nextStop = Request(floor, NONE)
            direction = NONE // No need to keep going
            return
        } else {
            nextStop = stops.filter { it.direction == NONE || direction == it.direction }.minBy { distance(it) }!!
            direction = getDirectionForRequest(nextStop)
        }
    }

    fun getDirectionForRequest(request: Request): Direction {
        if (request.floor > floor) return UP
        else if (request.floor < floor) return DOWN
        return NONE
    }
}

/**
 * The request data class encompasses both the concept of a pick-up request and drop-off request
 * Future work can differentiate between the two since they have slightly different data requirements
 */
data class Request (val floor: Int, val direction: Direction)

/**
 * This enum holds both the concept of the direction of movement and the direction of a request
 * This overloading is not great and should be pulled apart a little bit in future work
 */
enum class Direction {
    UP,
    DOWN,
    NONE, // The elevator is not currently moving
}

class ElevatorController(numElevators: Int) {

    val elevators: MutableSet<Elevator> = mutableSetOf()
    val requestQueue: Queue<Request> = LinkedList<Request>()

    init {
        for (i in 0..numElevators-1) {
            elevators.add(Elevator(id = i, floor = 0, direction = NONE, nextStop = Request(floor = 0, direction = NONE)))
        }
    }

    fun status() = elevators

    fun pickUp(request: Request) {
        distributeToElevatorsOrQueue(request)
    }

    private fun distributeToElevatorsOrQueue(request: Request) {

        if (distributeRequestToStoppedElevators(request)) return

        val elevatorsInSameDirection = elevators.filter {
            it.direction == it.getDirectionForRequest(request) && it.direction == request.direction
        }
        if (elevatorsInSameDirection.isEmpty()) requestQueue.add(request)
        else elevatorsInSameDirection.minBy { it.stops.size }!!.add(request) // Add the stop to the elevator with the fewest current stops already moving
    }

    private fun distributeRequestToStoppedElevators(request: Request): Boolean {
        val stoppedElevators = elevators.filter { it.direction == NONE }
        if (stoppedElevators.isEmpty()) return false
        stoppedElevators.first().add(request) // Assign to an elevator
        return true
    }

    /**
     * I'm not sure if I understand what the 'update' function is supposed to represent: an external forcible update
     * to an elevator's status (i.e. overwrite the status of a specific elevator to be something else), a callback from
     * each elevator to report that they have finished their work (which is not necessary with the current architecture),
     * or a way to simulate a person pressing a button inside of the elevator. I have assumed that it is the last which
     * we are attempting to build here.
     *
     * return: This returns a boolean if the update is not accepted. For example, the passenger pushed a button in the
     * wrong direction (that is, they called an elevator to go 'up' and instead pushed a floor which would cause the elevator
     * to go in the 'down' direction. In this case, the update is rejected, similar to real elevators.
     */
    fun update(elevatorId: Int, request: Request): Boolean {
        if (elevatorId >= elevators.size) {
            return false
        }
        // TODO: Add a check to see if this request can be accepted
        val elevator = elevators.elementAt(elevatorId)
        if (elevator.getDirectionForRequest(request) != elevator.direction) return false
        elevator.add(request)
        return true
    }

    /**
     * The step function will allow all elevators to move forward one time step
     * One time step is assumed to mean a whole number movement of a floor
     * Drop offs and pickups are assumed to be instant (this is definitely not a correct assumption, but makes things easier)
     * After every step, the queue will be attempted to be added to elevators
     */
    fun step() {
        elevators.forEach { it.moveTimeUnits(1) }
        val copyQueue = requestQueue.toCollection(LinkedList<Request>())
        requestQueue.clear()
        copyQueue.forEach { distributeToElevatorsOrQueue(it) }
    }


}
