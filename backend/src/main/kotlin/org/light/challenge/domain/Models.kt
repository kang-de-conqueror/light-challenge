package org.light.challenge.domain

import java.math.BigDecimal

enum class Department {
    SALES,
    MARKETING,
    ENGINEERING,
    FINANCE,
    HR,
    OPERATIONS
}

data class Approver(
    val id: String,
    val name: String,
    val email: String,
    val department: Department?,
    val isManager: Boolean = false
)

data class InvoiceApprovalRequest(
    val amount: BigDecimal,
    val department: Department,
    val requiresManagerApproval: Boolean
)

data class WorkflowExecutionResult(
    val request: InvoiceApprovalRequest,
    val selectedApprovers: List<Approver>,
    val notificationsSent: List<String>,
    val rulesApplied: List<String>
)

enum class ComparisonOperator {
    EQUALS,
    NOT_EQUALS,
    GREATER_THAN,
    GREATER_THAN_OR_EQUALS,
    LESS_THAN,
    LESS_THAN_OR_EQUALS
}

sealed class RuleCondition {
    abstract fun evaluate(request: InvoiceApprovalRequest): Boolean

    data class AmountCondition(
        val operator: ComparisonOperator,
        val threshold: BigDecimal
    ) : RuleCondition() {
        override fun evaluate(request: InvoiceApprovalRequest): Boolean {
            return when (operator) {
                ComparisonOperator.EQUALS -> request.amount.compareTo(threshold) == 0
                ComparisonOperator.NOT_EQUALS -> request.amount.compareTo(threshold) != 0
                ComparisonOperator.GREATER_THAN -> request.amount > threshold
                ComparisonOperator.GREATER_THAN_OR_EQUALS -> request.amount >= threshold
                ComparisonOperator.LESS_THAN -> request.amount < threshold
                ComparisonOperator.LESS_THAN_OR_EQUALS -> request.amount <= threshold
            }
        }
    }

    data class DepartmentCondition(
        val operator: ComparisonOperator,
        val department: Department
    ) : RuleCondition() {
        override fun evaluate(request: InvoiceApprovalRequest): Boolean {
            return when (operator) {
                ComparisonOperator.EQUALS -> request.department == department
                ComparisonOperator.NOT_EQUALS -> request.department != department
                else -> false
            }
        }
    }

    data class ManagerApprovalCondition(
        val requiresManagerApproval: Boolean
    ) : RuleCondition() {
        override fun evaluate(request: InvoiceApprovalRequest): Boolean {
            return request.requiresManagerApproval == requiresManagerApproval
        }
    }

    data class AndCondition(
        val conditions: List<RuleCondition>
    ) : RuleCondition() {
        override fun evaluate(request: InvoiceApprovalRequest): Boolean {
            return conditions.all { it.evaluate(request) }
        }
    }

    data class OrCondition(
        val conditions: List<RuleCondition>
    ) : RuleCondition() {
        override fun evaluate(request: InvoiceApprovalRequest): Boolean {
            return conditions.any { it.evaluate(request) }
        }
    }
}

data class WorkflowRule(
    val id: String,
    val name: String,
    val description: String,
    val condition: RuleCondition,
    val approverIds: List<String>,
    val priority: Int = 0
)

data class WorkflowConfiguration(
    val id: String,
    val name: String,
    val rules: List<WorkflowRule>
)
