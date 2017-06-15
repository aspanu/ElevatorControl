
package com.aspanu.elevatorControl

import com.aspanu.elevatorControl.Direction.*
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.greaterThan
import org.junit.Test

/**
 * Created by aspanu on 2017-06-15.
 */

class ElevatorControllerTest {

    @Test
    fun testElevatorController() {
        val elevatorController = ElevatorController(1)
        elevatorController.pickUp(Request(2, DOWN))
        elevatorController.step()
        val elevators = elevatorController.status()
        assertThat(elevators.size, equalTo(1))
        val elevator = elevators.elementAt(0)
        assertThat(elevator.direction, equalTo(UP))
        assertThat(elevator.floor, equalTo(1))
        assertThat(elevator.nextStop.floor, equalTo(2))
        assertThat(elevator.stops.size, equalTo(1))
        elevatorController.update(0, Request(4, NONE))
        assertThat(elevator.nextStop.floor, equalTo(2))
        assertThat(elevator.stops.size, equalTo(2))
    }

    @Test
    fun testElevatorEventualConsistency() {
        val elevatorController = ElevatorController(4)
        elevatorController.pickUp(Request(2, UP))
        elevatorController.pickUp(Request(3, UP))
        elevatorController.pickUp(Request(4, UP))
        elevatorController.pickUp(Request(5, UP))
        elevatorController.pickUp(Request(2, DOWN))
        elevatorController.pickUp(Request(3, DOWN))
        elevatorController.pickUp(Request(4, DOWN))
        elevatorController.pickUp(Request(5, DOWN))
        elevatorController.pickUp(Request(8, UP))
        elevatorController.pickUp(Request(9, UP))
        elevatorController.pickUp(Request(10, UP))
        elevatorController.pickUp(Request(11, UP))

        for (i in 0..100) {
            elevatorController.step()
        }

        for (elevator in elevatorController.elevators) {
            assertThat(elevator.direction, equalTo(NONE))
            assertThat(elevator.stops.size, equalTo(0))
            assertThat(elevator.nextStop, equalTo(Request(elevator.floor, NONE)))
            assertThat(elevator.floor, greaterThan(0))
        }

        assertThat(elevatorController.requestQueue.size, equalTo(0))
    }

}