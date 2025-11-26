package org.light.challenge.service

import org.light.challenge.domain.Approver
import org.light.challenge.domain.InvoiceApprovalRequest
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

interface NotificationService {
    fun sendApprovalNotification(approver: Approver, request: InvoiceApprovalRequest): String
    fun sendBulkNotifications(approvers: List<Approver>, request: InvoiceApprovalRequest): List<String>
}

class MockNotificationService : NotificationService {

    override fun sendApprovalNotification(approver: Approver, request: InvoiceApprovalRequest): String {
        val message = buildNotificationMessage(approver, request)

        println("sending approval via Slack")
        println("  -> To: ${approver.name} (${approver.email})")
        println("  -> Message: $message")

        logger.info { "Notification sent to ${approver.name}: $message" }

        return "Slack notification sent to ${approver.name}"
    }

    override fun sendBulkNotifications(approvers: List<Approver>, request: InvoiceApprovalRequest): List<String> {
        return approvers.map { approver ->
            sendApprovalNotification(approver, request)
        }
    }

    private fun buildNotificationMessage(approver: Approver, request: InvoiceApprovalRequest): String {
        return "Hi ${approver.name}, Invoice approval required: Amount=\$${request.amount}, " +
               "Department=${request.department}, " +
               "Manager Approval Required=${request.requiresManagerApproval}"
    }
}
