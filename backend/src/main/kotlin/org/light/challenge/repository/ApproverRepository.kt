package org.light.challenge.repository

import org.light.challenge.domain.Approver
import org.light.challenge.domain.Department

interface ApproverRepository {
    fun findById(id: String): Approver?
    fun findByIds(ids: List<String>): List<Approver>
    fun findAll(): List<Approver>
    fun findByDepartment(department: Department): List<Approver>
    fun findManagers(): List<Approver>
}

class InMemoryApproverRepository : ApproverRepository {

    private val approvers = mutableMapOf<String, Approver>()

    init {
        listOf(
            Approver("emp-1", "John Smith", "john.smith@company.com", Department.SALES, isManager = false),
            Approver("emp-2", "Jane Doe", "jane.doe@company.com", Department.MARKETING, isManager = false),
            Approver("emp-3", "Bob Wilson", "bob.wilson@company.com", Department.ENGINEERING, isManager = false),
            Approver("mgr-sales", "Sales Manager", "sales.manager@company.com", Department.SALES, isManager = true),
            Approver("mgr-marketing", "Marketing Manager", "marketing.manager@company.com", Department.MARKETING, isManager = true),
            Approver("mgr-engineering", "Engineering Manager", "engineering.manager@company.com", Department.ENGINEERING, isManager = true),
            Approver("mgr-finance", "Finance Manager", "finance.manager@company.com", Department.FINANCE, isManager = true),
            Approver("vp-finance", "VP of Finance", "vp.finance@company.com", Department.FINANCE, isManager = true),
            Approver("cfo", "CFO", "cfo@company.com", Department.FINANCE, isManager = true),
            Approver("ceo", "CEO", "ceo@company.com", null, isManager = true)
        ).forEach { approvers[it.id] = it }
    }

    override fun findById(id: String): Approver? = approvers[id]

    override fun findByIds(ids: List<String>): List<Approver> =
        ids.mapNotNull { approvers[it] }

    override fun findAll(): List<Approver> = approvers.values.toList()

    override fun findByDepartment(department: Department): List<Approver> =
        approvers.values.filter { it.department == department }

    override fun findManagers(): List<Approver> =
        approvers.values.filter { it.isManager }
}
