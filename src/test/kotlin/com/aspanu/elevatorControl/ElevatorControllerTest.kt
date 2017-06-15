
package com.aspanu.elevatorControl

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

/**
 * Created by aspanu on 2017-06-15.
 */

class ElevatorControllerTest {

    @Test
    fun testElevatorController() {
        val elevatorController = ElevatorController(1)
        elevatorController.pickUp(Request(2, Direction.DOWN))
        elevatorController.step()
        val elevators = elevatorController.status()
        assertThat(elevators.size, equalTo(1))
        val elevator = elevators.elementAt(0)
        assertThat(elevator.direction, equalTo(Direction.UP))
        assertThat(elevator.floor, equalTo(1))
        assertThat(elevator.nextStop.floor, equalTo(2))
        assertThat(elevator.stops.size, equalTo(1))
        elevatorController.update(0, Request(4, Direction.NONE))
        assertThat(elevator.nextStop.floor, equalTo(2))
        assertThat(elevator.stops.size, equalTo(2))
    }
}