package org.light.challenge.repository

import org.light.challenge.domain.*
import java.math.BigDecimal

interface WorkflowRuleRepository {
    fun findById(id: String): WorkflowRule?
    fun findAll(): List<WorkflowRule>
    fun getActiveConfiguration(): WorkflowConfiguration
}

class InMemoryWorkflowRuleRepository : WorkflowRuleRepository {

    private val rules: List<WorkflowRule>

    init {
        rules = buildWorkflowRules()
    }

    private fun buildWorkflowRules(): List<WorkflowRule> {
        return listOf(
            WorkflowRule(
                id = "rule-1",
                name = "High Amount with Manager Approval",
                description = "Invoices over \$1000 requiring manager approval go to CFO",
                condition = RuleCondition.AndCondition(
                    listOf(
                        RuleCondition.AmountCondition(ComparisonOperator.GREATER_THAN, BigDecimal("1000")),
                        RuleCondition.ManagerApprovalCondition(true)
                    )
                ),
                approverIds = listOf("cfo"),
                priority = 1
            ),
            WorkflowRule(
                id = "rule-2",
                name = "High Amount without Manager Approval",
                description = "Invoices over \$1000 not requiring manager approval go to VP of Finance",
                condition = RuleCondition.AndCondition(
                    listOf(
                        RuleCondition.AmountCondition(ComparisonOperator.GREATER_THAN, BigDecimal("1000")),
                        RuleCondition.ManagerApprovalCondition(false)
                    )
                ),
                approverIds = listOf("vp-finance"),
                priority = 2
            ),
            WorkflowRule(
                id = "rule-3",
                name = "Low Amount with Manager Approval",
                description = "Invoices \$1000 or less requiring manager approval go to department manager",
                condition = RuleCondition.AndCondition(
                    listOf(
                        RuleCondition.AmountCondition(ComparisonOperator.LESS_THAN_OR_EQUALS, BigDecimal("1000")),
                        RuleCondition.ManagerApprovalCondition(true)
                    )
                ),
                approverIds = listOf("mgr-sales", "mgr-marketing", "mgr-engineering", "mgr-finance"),
                priority = 3
            ),
            WorkflowRule(
                id = "rule-4",
                name = "Low Amount without Manager Approval",
                description = "Invoices \$1000 or less not requiring manager approval go to employee",
                condition = RuleCondition.AndCondition(
                    listOf(
                        RuleCondition.AmountCondition(ComparisonOperator.LESS_THAN_OR_EQUALS, BigDecimal("1000")),
                        RuleCondition.ManagerApprovalCondition(false)
                    )
                ),
                approverIds = listOf("emp-1", "emp-2", "emp-3"),
                priority = 4
            )
        )
    }

    override fun findById(id: String): WorkflowRule? = rules.find { it.id == id }

    override fun findAll(): List<WorkflowRule> = rules

    override fun getActiveConfiguration(): WorkflowConfiguration {
        return WorkflowConfiguration(
            id = "invoice-approval-workflow",
            name = "Invoice Approval Workflow",
            rules = rules
        )
    }
}
