package org.light.challenge.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.light.challenge.domain.Approver
import org.light.challenge.domain.Department
import org.light.challenge.domain.InvoiceApprovalRequest
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.math.BigDecimal

class NotificationServiceTest {

    private val notificationService = MockNotificationService()

    @Test
    fun `sendApprovalNotification returns correct message format`() {
        val approver = Approver("cfo", "CFO", "cfo@company.com", Department.FINANCE, true)
        val request = InvoiceApprovalRequest(BigDecimal("1500"), Department.SALES, true)

        val result = notificationService.sendApprovalNotification(approver, request)

        assertEquals("Slack notification sent to CFO", result)
    }

    @Test
    fun `sendApprovalNotification prints to console`() {
        val approver = Approver("cfo", "CFO", "cfo@company.com", Department.FINANCE, true)
        val request = InvoiceApprovalRequest(BigDecimal("1500"), Department.SALES, true)

        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            notificationService.sendApprovalNotification(approver, request)
            val output = outputStream.toString()

            assertTrue(output.contains("sending approval via Slack"))
            assertTrue(output.contains("CFO"))
            assertTrue(output.contains("cfo@company.com"))
        } finally {
            System.setOut(originalOut)
        }
    }

    @Test
    fun `sendBulkNotifications sends to all approvers`() {
        val approvers = listOf(
            Approver("cfo", "CFO", "cfo@company.com", Department.FINANCE, true),
            Approver("vp-finance", "VP of Finance", "vp@company.com", Department.FINANCE, true)
        )
        val request = InvoiceApprovalRequest(BigDecimal("1500"), Department.SALES, true)

        val results = notificationService.sendBulkNotifications(approvers, request)

        assertEquals(2, results.size)
        assertEquals("Slack notification sent to CFO", results[0])
        assertEquals("Slack notification sent to VP of Finance", results[1])
    }

    @Test
    fun `sendBulkNotifications returns empty list for empty approvers`() {
        val request = InvoiceApprovalRequest(BigDecimal("1500"), Department.SALES, true)

        val results = notificationService.sendBulkNotifications(emptyList(), request)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `notification message contains correct details`() {
        val approver = Approver("emp-1", "John Smith", "john@company.com", Department.SALES, false)
        val request = InvoiceApprovalRequest(BigDecimal("500.50"), Department.MARKETING, false)

        val originalOut = System.out
        val outputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(outputStream))

        try {
            notificationService.sendApprovalNotification(approver, request)
            val output = outputStream.toString()

            assertTrue(output.contains("500.50"))
            assertTrue(output.contains("MARKETING"))
            assertTrue(output.contains("Manager Approval Required=false"))
        } finally {
            System.setOut(originalOut)
        }
    }
}

