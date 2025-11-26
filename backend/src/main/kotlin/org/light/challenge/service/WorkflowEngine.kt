package org.light.challenge.service

import org.light.challenge.domain.*
import org.light.challenge.repository.ApproverRepository
import org.light.challenge.repository.WorkflowRuleRepository
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class WorkflowEngine(
    private val workflowRuleRepository: WorkflowRuleRepository,
    private val approverRepository: ApproverRepository,
    private val notificationService: NotificationService
) {

    fun executeWorkflow(request: InvoiceApprovalRequest): WorkflowExecutionResult {
        logger.info { "Executing workflow for request: $request" }

        val configuration = workflowRuleRepository.getActiveConfiguration()
        val matchingRules = findMatchingRules(request, configuration.rules)

        logger.info { "Found ${matchingRules.size} matching rules" }

        val selectedApprovers = determineApprovers(request, matchingRules)

        logger.info { "Selected ${selectedApprovers.size} approvers: ${selectedApprovers.map { it.name }}" }

        val notifications = notificationService.sendBulkNotifications(selectedApprovers, request)

        return WorkflowExecutionResult(
            request = request,
            selectedApprovers = selectedApprovers,
            notificationsSent = notifications,
            rulesApplied = matchingRules.map { it.name }
        )
    }

    private fun findMatchingRules(request: InvoiceApprovalRequest, rules: List<WorkflowRule>): List<WorkflowRule> {
        return rules
            .filter { rule -> rule.condition.evaluate(request) }
            .sortedBy { it.priority }
    }

    private fun determineApprovers(request: InvoiceApprovalRequest, matchingRules: List<WorkflowRule>): List<Approver> {
        if (matchingRules.isEmpty()) {
            logger.warn { "No matching rules found for request: $request" }
            return emptyList()
        }

        val primaryRule = matchingRules.first()
        val approverIds = primaryRule.approverIds

        val allApprovers = approverRepository.findByIds(approverIds)

        return if (primaryRule.name.contains("Department Manager", ignoreCase = true) ||
                   primaryRule.name.contains("Low Amount with Manager", ignoreCase = true)) {
            allApprovers.filter { approver ->
                approver.isManager && approver.department == request.department
            }.ifEmpty {
                allApprovers.filter { it.isManager }.take(1)
            }
        } else if (primaryRule.name.contains("Employee", ignoreCase = true) ||
                   primaryRule.name.contains("Low Amount without Manager", ignoreCase = true)) {
            allApprovers.filter { approver ->
                !approver.isManager && approver.department == request.department
            }.ifEmpty {
                allApprovers.filter { !it.isManager }.take(1)
            }
        } else {
            allApprovers
        }
    }
}
