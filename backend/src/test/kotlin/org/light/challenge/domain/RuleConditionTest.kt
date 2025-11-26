package org.light.challenge.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal

class RuleConditionTest {

    @Test
    fun `AmountCondition GREATER_THAN evaluates correctly`() {
        val condition = RuleCondition.AmountCondition(ComparisonOperator.GREATER_THAN, BigDecimal("1000"))
        val requestHigh = InvoiceApprovalRequest(BigDecimal("1500"), Department.SALES, false)
        val requestLow = InvoiceApprovalRequest(BigDecimal("500"), Department.SALES, false)
        val requestEqual = InvoiceApprovalRequest(BigDecimal("1000"), Department.SALES, false)

        assertTrue(condition.evaluate(requestHigh))
        assertFalse(condition.evaluate(requestLow))
        assertFalse(condition.evaluate(requestEqual))
    }

    @Test
    fun `AmountCondition LESS_THAN_OR_EQUALS evaluates correctly`() {
        val condition = RuleCondition.AmountCondition(ComparisonOperator.LESS_THAN_OR_EQUALS, BigDecimal("1000"))
        val requestHigh = InvoiceApprovalRequest(BigDecimal("1500"), Department.SALES, false)
        val requestLow = InvoiceApprovalRequest(BigDecimal("500"), Department.SALES, false)
        val requestEqual = InvoiceApprovalRequest(BigDecimal("1000"), Department.SALES, false)

        assertFalse(condition.evaluate(requestHigh))
        assertTrue(condition.evaluate(requestLow))
        assertTrue(condition.evaluate(requestEqual))
    }

    @Test
    fun `AmountCondition EQUALS evaluates correctly`() {
        val condition = RuleCondition.AmountCondition(ComparisonOperator.EQUALS, BigDecimal("1000"))
        val requestEqual = InvoiceApprovalRequest(BigDecimal("1000"), Department.SALES, false)
        val requestNotEqual = InvoiceApprovalRequest(BigDecimal("999"), Department.SALES, false)

        assertTrue(condition.evaluate(requestEqual))
        assertFalse(condition.evaluate(requestNotEqual))
    }

    @Test
    fun `AmountCondition NOT_EQUALS evaluates correctly`() {
        val condition = RuleCondition.AmountCondition(ComparisonOperator.NOT_EQUALS, BigDecimal("1000"))
        val requestEqual = InvoiceApprovalRequest(BigDecimal("1000"), Department.SALES, false)
        val requestNotEqual = InvoiceApprovalRequest(BigDecimal("999"), Department.SALES, false)

        assertFalse(condition.evaluate(requestEqual))
        assertTrue(condition.evaluate(requestNotEqual))
    }

    @Test
    fun `AmountCondition GREATER_THAN_OR_EQUALS evaluates correctly`() {
        val condition = RuleCondition.AmountCondition(ComparisonOperator.GREATER_THAN_OR_EQUALS, BigDecimal("1000"))
        val requestHigh = InvoiceApprovalRequest(BigDecimal("1500"), Department.SALES, false)
        val requestEqual = InvoiceApprovalRequest(BigDecimal("1000"), Department.SALES, false)
        val requestLow = InvoiceApprovalRequest(BigDecimal("500"), Department.SALES, false)

        assertTrue(condition.evaluate(requestHigh))
        assertTrue(condition.evaluate(requestEqual))
        assertFalse(condition.evaluate(requestLow))
    }

    @Test
    fun `AmountCondition LESS_THAN evaluates correctly`() {
        val condition = RuleCondition.AmountCondition(ComparisonOperator.LESS_THAN, BigDecimal("1000"))
        val requestLow = InvoiceApprovalRequest(BigDecimal("500"), Department.SALES, false)
        val requestEqual = InvoiceApprovalRequest(BigDecimal("1000"), Department.SALES, false)

        assertTrue(condition.evaluate(requestLow))
        assertFalse(condition.evaluate(requestEqual))
    }

    @Test
    fun `DepartmentCondition EQUALS evaluates correctly`() {
        val condition = RuleCondition.DepartmentCondition(ComparisonOperator.EQUALS, Department.SALES)
        val requestSales = InvoiceApprovalRequest(BigDecimal("500"), Department.SALES, false)
        val requestMarketing = InvoiceApprovalRequest(BigDecimal("500"), Department.MARKETING, false)

        assertTrue(condition.evaluate(requestSales))
        assertFalse(condition.evaluate(requestMarketing))
    }

    @Test
    fun `DepartmentCondition NOT_EQUALS evaluates correctly`() {
        val condition = RuleCondition.DepartmentCondition(ComparisonOperator.NOT_EQUALS, Department.SALES)
        val requestSales = InvoiceApprovalRequest(BigDecimal("500"), Department.SALES, false)
        val requestMarketing = InvoiceApprovalRequest(BigDecimal("500"), Department.MARKETING, false)

        assertFalse(condition.evaluate(requestSales))
        assertTrue(condition.evaluate(requestMarketing))
    }

    @Test
    fun `DepartmentCondition with unsupported operator returns false`() {
        val condition = RuleCondition.DepartmentCondition(ComparisonOperator.GREATER_THAN, Department.SALES)
        val request = InvoiceApprovalRequest(BigDecimal("500"), Department.SALES, false)

        assertFalse(condition.evaluate(request))
    }

    @Test
    fun `ManagerApprovalCondition evaluates correctly when true`() {
        val condition = RuleCondition.ManagerApprovalCondition(true)
        val requestWithApproval = InvoiceApprovalRequest(BigDecimal("500"), Department.SALES, true)
        val requestWithoutApproval = InvoiceApprovalRequest(BigDecimal("500"), Department.SALES, false)

        assertTrue(condition.evaluate(requestWithApproval))
        assertFalse(condition.evaluate(requestWithoutApproval))
    }

    @Test
    fun `ManagerApprovalCondition evaluates correctly when false`() {
        val condition = RuleCondition.ManagerApprovalCondition(false)
        val requestWithApproval = InvoiceApprovalRequest(BigDecimal("500"), Department.SALES, true)
        val requestWithoutApproval = InvoiceApprovalRequest(BigDecimal("500"), Department.SALES, false)

        assertFalse(condition.evaluate(requestWithApproval))
        assertTrue(condition.evaluate(requestWithoutApproval))
    }

    @Test
    fun `AndCondition evaluates correctly when all conditions are true`() {
        val condition = RuleCondition.AndCondition(
            listOf(
                RuleCondition.AmountCondition(ComparisonOperator.GREATER_THAN, BigDecimal("1000")),
                RuleCondition.ManagerApprovalCondition(true)
            )
        )
        val request = InvoiceApprovalRequest(BigDecimal("1500"), Department.SALES, true)

        assertTrue(condition.evaluate(request))
    }

    @Test
    fun `AndCondition evaluates correctly when one condition is false`() {
        val condition = RuleCondition.AndCondition(
            listOf(
                RuleCondition.AmountCondition(ComparisonOperator.GREATER_THAN, BigDecimal("1000")),
                RuleCondition.ManagerApprovalCondition(true)
            )
        )
        val request = InvoiceApprovalRequest(BigDecimal("1500"), Department.SALES, false)

        assertFalse(condition.evaluate(request))
    }

    @Test
    fun `OrCondition evaluates correctly when one condition is true`() {
        val condition = RuleCondition.OrCondition(
            listOf(
                RuleCondition.AmountCondition(ComparisonOperator.GREATER_THAN, BigDecimal("1000")),
                RuleCondition.ManagerApprovalCondition(true)
            )
        )
        val request = InvoiceApprovalRequest(BigDecimal("500"), Department.SALES, true)

        assertTrue(condition.evaluate(request))
    }

    @Test
    fun `OrCondition evaluates correctly when no conditions are true`() {
        val condition = RuleCondition.OrCondition(
            listOf(
                RuleCondition.AmountCondition(ComparisonOperator.GREATER_THAN, BigDecimal("1000")),
                RuleCondition.ManagerApprovalCondition(true)
            )
        )
        val request = InvoiceApprovalRequest(BigDecimal("500"), Department.SALES, false)

        assertFalse(condition.evaluate(request))
    }
}

