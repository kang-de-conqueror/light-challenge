package org.light.challenge.rest

import org.light.challenge.domain.Department
import org.light.challenge.repository.InMemoryApproverRepository
import org.light.challenge.repository.InMemoryWorkflowRuleRepository
import org.light.challenge.service.MockNotificationService
import org.light.challenge.service.WorkflowEngine
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

@Path("/workflow")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class WorkflowResource {

    private val approverRepository = InMemoryApproverRepository()
    private val workflowRuleRepository = InMemoryWorkflowRuleRepository()
    private val notificationService = MockNotificationService()
    private val workflowEngine = WorkflowEngine(
        workflowRuleRepository,
        approverRepository,
        notificationService
    )

    @POST
    @Path("/execute")
    fun executeWorkflow(request: WorkflowExecuteRequest): Response {
        logger.info { "Received workflow execution request: $request" }

        return try {
            val domainRequest = request.toDomain()
            val result = workflowEngine.executeWorkflow(domainRequest)
            val response = WorkflowExecuteResponse.fromDomain(result)

            Response.ok(response).build()
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Invalid request: ${e.message}" }
            Response.status(Response.Status.BAD_REQUEST)
                .entity(ErrorResponse(
                    error = "INVALID_REQUEST",
                    message = e.message ?: "Invalid request parameters"
                ))
                .build()
        } catch (e: Exception) {
            logger.error(e) { "Error executing workflow: ${e.message}" }
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(ErrorResponse(
                    error = "INTERNAL_ERROR",
                    message = "An error occurred while processing the workflow"
                ))
                .build()
        }
    }

    @GET
    @Path("/departments")
    fun getDepartments(): Response {
        val departments = Department.values().map { it.name }
        return Response.ok(mapOf("departments" to departments)).build()
    }
}
