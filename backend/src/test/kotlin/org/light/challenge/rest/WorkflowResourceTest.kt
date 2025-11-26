package org.light.challenge.rest

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.light.challenge.domain.Department
import java.math.BigDecimal

class WorkflowResourceTest {

    private val resource = WorkflowResource()

    @Test
    fun `executeWorkflow with high amount and manager approval returns CFO`() {
        val request = WorkflowExecuteRequest(
            amount = BigDecimal("1500"),
            department = "SALES",
            requiresManagerApproval = true
        )

        val response = resource.executeWorkflow(request)

        assertEquals(200, response.status)
        val body = response.entity as WorkflowExecuteResponse
        assertTrue(body.success)
        assertEquals(1, body.selectedApprovers.size)
        assertEquals("CFO", body.selectedApprovers[0].name)
        assertTrue(body.rulesApplied.contains("High Amount with Manager Approval"))
    }

    @Test
    fun `executeWorkflow with high amount without manager approval returns VP of Finance`() {
        val request = WorkflowExecuteRequest(
            amount = BigDecimal("1500"),
            department = "ENGINEERING",
            requiresManagerApproval = false
        )

        val response = resource.executeWorkflow(request)

        assertEquals(200, response.status)
        val body = response.entity as WorkflowExecuteResponse
        assertTrue(body.success)
        assertEquals(1, body.selectedApprovers.size)
        assertEquals("VP of Finance", body.selectedApprovers[0].name)
        assertTrue(body.rulesApplied.contains("High Amount without Manager Approval"))
    }

    @Test
    fun `executeWorkflow with low amount and manager approval returns department manager`() {
        val request = WorkflowExecuteRequest(
            amount = BigDecimal("500"),
            department = "MARKETING",
            requiresManagerApproval = true
        )

        val response = resource.executeWorkflow(request)

        assertEquals(200, response.status)
        val body = response.entity as WorkflowExecuteResponse
        assertTrue(body.success)
        assertEquals(1, body.selectedApprovers.size)
        assertEquals("Marketing Manager", body.selectedApprovers[0].name)
        assertTrue(body.rulesApplied.contains("Low Amount with Manager Approval"))
    }

    @Test
    fun `executeWorkflow with low amount without manager approval returns employee`() {
        val request = WorkflowExecuteRequest(
            amount = BigDecimal("500"),
            department = "SALES",
            requiresManagerApproval = false
        )

        val response = resource.executeWorkflow(request)

        assertEquals(200, response.status)
        val body = response.entity as WorkflowExecuteResponse
        assertTrue(body.success)
        assertEquals(1, body.selectedApprovers.size)
        assertTrue(body.rulesApplied.contains("Low Amount without Manager Approval"))
    }

    @Test
    fun `executeWorkflow with invalid department returns bad request`() {
        val request = WorkflowExecuteRequest(
            amount = BigDecimal("500"),
            department = "INVALID_DEPT",
            requiresManagerApproval = false
        )

        val response = resource.executeWorkflow(request)

        assertEquals(400, response.status)
        val body = response.entity as ErrorResponse
        assertFalse(body.success)
        assertEquals("INVALID_REQUEST", body.error)
    }

    @Test
    fun `executeWorkflow with boundary amount 1000 with manager approval goes to department manager`() {
        val request = WorkflowExecuteRequest(
            amount = BigDecimal("1000"),
            department = "SALES",
            requiresManagerApproval = true
        )

        val response = resource.executeWorkflow(request)

        assertEquals(200, response.status)
        val body = response.entity as WorkflowExecuteResponse
        assertTrue(body.success)
        assertTrue(body.rulesApplied.contains("Low Amount with Manager Approval"))
    }

    @Test
    fun `executeWorkflow with boundary amount 1001 with manager approval goes to CFO`() {
        val request = WorkflowExecuteRequest(
            amount = BigDecimal("1001"),
            department = "SALES",
            requiresManagerApproval = true
        )

        val response = resource.executeWorkflow(request)

        assertEquals(200, response.status)
        val body = response.entity as WorkflowExecuteResponse
        assertTrue(body.success)
        assertEquals("CFO", body.selectedApprovers[0].name)
        assertTrue(body.rulesApplied.contains("High Amount with Manager Approval"))
    }

    @Test
    fun `getDepartments returns all departments`() {
        val response = resource.getDepartments()

        assertEquals(200, response.status)
        @Suppress("UNCHECKED_CAST")
        val body = response.entity as Map<String, List<String>>
        val departments = body["departments"]!!
        
        assertEquals(Department.values().size, departments.size)
        assertTrue(departments.contains("SALES"))
        assertTrue(departments.contains("MARKETING"))
        assertTrue(departments.contains("ENGINEERING"))
        assertTrue(departments.contains("FINANCE"))
    }
}

