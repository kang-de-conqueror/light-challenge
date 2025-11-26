package org.light.challenge.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.light.challenge.domain.*
import org.light.challenge.repository.ApproverRepository
import org.light.challenge.repository.WorkflowRuleRepository
import java.math.BigDecimal

class WorkflowEngineTest {

    private lateinit var workflowRuleRepository: WorkflowRuleRepository
    private lateinit var approverRepository: ApproverRepository
    private lateinit var notificationService: NotificationService
    private lateinit var workflowEngine: WorkflowEngine

    private val cfo = Approver("cfo", "CFO", "cfo@company.com", Department.FINANCE, true)
    private val vpFinance = Approver("vp-finance", "VP of Finance", "vp@company.com", Department.FINANCE, true)
    private val salesManager = Approver("mgr-sales", "Sales Manager", "sales@company.com", Department.SALES, true)
    private val salesEmployee = Approver("emp-1", "John Smith", "john@company.com", Department.SALES, false)

    @BeforeEach
    fun setup() {
        workflowRuleRepository = mockk()
        approverRepository = mockk()
        notificationService = mockk()
        workflowEngine = WorkflowEngine(workflowRuleRepository, approverRepository, notificationService)
    }

    private fun setupWorkflowConfiguration() {
        val rules = listOf(
            WorkflowRule(
                id = "rule-1",
                name = "High Amount with Manager Approval",
                description = "CFO approval",
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
                description = "VP approval",
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
                description = "Manager approval",
                condition = RuleCondition.AndCondition(
                    listOf(
                        RuleCondition.AmountCondition(ComparisonOperator.LESS_THAN_OR_EQUALS, BigDecimal("1000")),
                        RuleCondition.ManagerApprovalCondition(true)
                    )
                ),
                approverIds = listOf("mgr-sales", "mgr-marketing"),
                priority = 3
            ),
            WorkflowRule(
                id = "rule-4",
                name = "Low Amount without Manager Approval",
                description = "Employee approval",
                condition = RuleCondition.AndCondition(
                    listOf(
                        RuleCondition.AmountCondition(ComparisonOperator.LESS_THAN_OR_EQUALS, BigDecimal("1000")),
                        RuleCondition.ManagerApprovalCondition(false)
                    )
                ),
                approverIds = listOf("emp-1", "emp-2"),
                priority = 4
            )
        )

        every { workflowRuleRepository.getActiveConfiguration() } returns WorkflowConfiguration(
            id = "test-config",
            name = "Test Configuration",
            rules = rules
        )
    }

    @Test
    fun `high amount with manager approval routes to CFO`() {
        setupWorkflowConfiguration()
        every { approverRepository.findByIds(listOf("cfo")) } returns listOf(cfo)
        every { notificationService.sendBulkNotifications(listOf(cfo), any()) } returns listOf("Notified CFO")

        val request = InvoiceApprovalRequest(BigDecimal("1500"), Department.SALES, true)
        val result = workflowEngine.executeWorkflow(request)

        assertEquals(1, result.selectedApprovers.size)
        assertEquals("CFO", result.selectedApprovers[0].name)
        assertEquals("High Amount with Manager Approval", result.rulesApplied[0])
        verify { notificationService.sendBulkNotifications(listOf(cfo), request) }
    }

    @Test
    fun `high amount without manager approval routes to VP of Finance`() {
        setupWorkflowConfiguration()
        every { approverRepository.findByIds(listOf("vp-finance")) } returns listOf(vpFinance)
        every { notificationService.sendBulkNotifications(listOf(vpFinance), any()) } returns listOf("Notified VP")

        val request = InvoiceApprovalRequest(BigDecimal("1500"), Department.SALES, false)
        val result = workflowEngine.executeWorkflow(request)

        assertEquals(1, result.selectedApprovers.size)
        assertEquals("VP of Finance", result.selectedApprovers[0].name)
        assertEquals("High Amount without Manager Approval", result.rulesApplied[0])
    }

    @Test
    fun `low amount with manager approval routes to department manager`() {
        setupWorkflowConfiguration()
        every { approverRepository.findByIds(listOf("mgr-sales", "mgr-marketing")) } returns listOf(salesManager)
        every { notificationService.sendBulkNotifications(listOf(salesManager), any()) } returns listOf("Notified Manager")

        val request = InvoiceApprovalRequest(BigDecimal("500"), Department.SALES, true)
        val result = workflowEngine.executeWorkflow(request)

        assertEquals(1, result.selectedApprovers.size)
        assertEquals("Sales Manager", result.selectedApprovers[0].name)
        assertEquals("Low Amount with Manager Approval", result.rulesApplied[0])
    }

    @Test
    fun `low amount without manager approval routes to employee`() {
        setupWorkflowConfiguration()
        every { approverRepository.findByIds(listOf("emp-1", "emp-2")) } returns listOf(salesEmployee)
        every { notificationService.sendBulkNotifications(listOf(salesEmployee), any()) } returns listOf("Notified Employee")

        val request = InvoiceApprovalRequest(BigDecimal("500"), Department.SALES, false)
        val result = workflowEngine.executeWorkflow(request)

        assertEquals(1, result.selectedApprovers.size)
        assertEquals("John Smith", result.selectedApprovers[0].name)
        assertEquals("Low Amount without Manager Approval", result.rulesApplied[0])
    }
}

