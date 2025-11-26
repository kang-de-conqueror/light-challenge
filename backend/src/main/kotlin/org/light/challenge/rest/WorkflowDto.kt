package org.light.challenge.rest

import com.fasterxml.jackson.annotation.JsonProperty
import org.light.challenge.domain.*
import java.math.BigDecimal

data class WorkflowExecuteRequest(
    @JsonProperty("amount")
    val amount: BigDecimal,

    @JsonProperty("department")
    val department: String,

    @JsonProperty("requiresManagerApproval")
    val requiresManagerApproval: Boolean
) {
    fun toDomain(): InvoiceApprovalRequest {
        val dept = try {
            Department.valueOf(department.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid department: $department. Valid values: ${Department.values().joinToString()}")
        }

        return InvoiceApprovalRequest(
            amount = amount,
            department = dept,
            requiresManagerApproval = requiresManagerApproval
        )
    }
}

data class ApproverDto(
    @JsonProperty("id")
    val id: String,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("email")
    val email: String,

    @JsonProperty("department")
    val department: String?,

    @JsonProperty("isManager")
    val isManager: Boolean
) {
    companion object {
        fun fromDomain(approver: Approver): ApproverDto {
            return ApproverDto(
                id = approver.id,
                name = approver.name,
                email = approver.email,
                department = approver.department?.name,
                isManager = approver.isManager
            )
        }
    }
}

data class WorkflowExecuteResponse(
    @JsonProperty("success")
    val success: Boolean,

    @JsonProperty("amount")
    val amount: BigDecimal,

    @JsonProperty("department")
    val department: String,

    @JsonProperty("requiresManagerApproval")
    val requiresManagerApproval: Boolean,

    @JsonProperty("selectedApprovers")
    val selectedApprovers: List<ApproverDto>,

    @JsonProperty("notificationsSent")
    val notificationsSent: List<String>,

    @JsonProperty("rulesApplied")
    val rulesApplied: List<String>,

    @JsonProperty("message")
    val message: String
) {
    companion object {
        fun fromDomain(result: WorkflowExecutionResult): WorkflowExecuteResponse {
            return WorkflowExecuteResponse(
                success = true,
                amount = result.request.amount,
                department = result.request.department.name,
                requiresManagerApproval = result.request.requiresManagerApproval,
                selectedApprovers = result.selectedApprovers.map { ApproverDto.fromDomain(it) },
                notificationsSent = result.notificationsSent,
                rulesApplied = result.rulesApplied,
                message = "Workflow executed successfully. ${result.selectedApprovers.size} approver(s) notified."
            )
        }
    }
}

data class ErrorResponse(
    @JsonProperty("success")
    val success: Boolean = false,

    @JsonProperty("error")
    val error: String,

    @JsonProperty("message")
    val message: String
)
