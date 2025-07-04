package com.myworkmanagement.company.controller;

import com.myworkmanagement.company.dto.ProjectDTO;
import com.myworkmanagement.company.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Project", description = "Project management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/companies/{companyId}/projects")
    @Operation(summary = "Get all projects for a company", description = "Retrieves a paginated list of projects for a specific company")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved projects"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Company not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<ProjectDTO>> getAllProjectsByCompanyId(
            @Parameter(description = "Unique identifier of the company", required = true, example = "1") @PathVariable Long companyId,
            @Parameter(description = "Pagination parameters (page, size, sort)", required = false) Pageable pageable) {
        return ResponseEntity.ok(projectService.getAllProjectsByCompanyId(companyId, pageable));
    }

    @GetMapping("/projects")
    @Operation(summary = "Get all projects for the authenticated user", description = "Retrieves a paginated list of all projects for the currently logged-in user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved projects"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<ProjectDTO>> getAllProjectsForUser(
            @Parameter(description = "Pagination parameters (page, size, sort)", required = false) Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        return ResponseEntity.ok(projectService.getAllProjectsByUserEmail(userEmail, pageable));
    }

    @GetMapping("/companies/{companyId}/projects/{projectId}")
    @Operation(summary = "Get project by ID", description = "Retrieves a project by its ID for a specific company")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved project"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Company or project not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ProjectDTO> getProjectById(
            @Parameter(description = "Unique identifier of the company", required = true, example = "1") @PathVariable Long companyId,
            @Parameter(description = "Unique identifier of the project", required = true, example = "1") @PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProjectById(companyId, projectId));
    }

    @PostMapping("/companies/{companyId}/projects")
    @Operation(summary = "Create new project", description = "Creates a new project for a company")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Project created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Company not found"),
        @ApiResponse(responseCode = "409", description = "Project with same name already exists for this company"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectDTO> createProject(
            @Parameter(description = "Unique identifier of the company", required = true, example = "1") @PathVariable Long companyId,
            @Parameter(description = "Project data", required = true, schema = @Schema(implementation = ProjectDTO.class))
            @Valid @RequestBody ProjectDTO projectDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        projectDTO.setUserEmail(userEmail);
        return new ResponseEntity<>(projectService.createProject(companyId, projectDTO), HttpStatus.CREATED);
    }

    @PutMapping("/companies/{companyId}/projects/{projectId}")
    @Operation(summary = "Update project", description = "Updates an existing project for a company")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Project updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Company or project not found"),
        @ApiResponse(responseCode = "409", description = "Project with same name already exists for this company"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectDTO> updateProject(
            @Parameter(description = "Unique identifier of the company", required = true, example = "1") @PathVariable Long companyId,
            @Parameter(description = "Unique identifier of the project", required = true, example = "1") @PathVariable Long projectId,
            @Parameter(description = "Updated project data", required = true, schema = @Schema(implementation = ProjectDTO.class))
            @Valid @RequestBody ProjectDTO projectDTO) {
        return ResponseEntity.ok(projectService.updateProject(companyId, projectId, projectDTO));
    }

    @DeleteMapping("/companies/{companyId}/projects/{projectId}")
    @Operation(summary = "Delete project", description = "Deletes a project by its ID for a specific company")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Company or project not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "Unique identifier of the company", required = true, example = "1") @PathVariable Long companyId,
            @Parameter(description = "Unique identifier of the project", required = true, example = "1") @PathVariable Long projectId) {
        projectService.deleteProject(companyId, projectId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/companies/{companyId}/projects/search")
    @Operation(summary = "Search projects for a company", description = "Searches projects by name or description for a specific company with pagination support")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved projects"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Company not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<ProjectDTO>> searchProjects(
            @Parameter(description = "Unique identifier of the company", required = true, example = "1") @PathVariable Long companyId,
            @Parameter(description = "Search term to filter projects by name or description", required = true, example = "website") @RequestParam String searchTerm,
            @Parameter(description = "Pagination parameters (page, size, sort)", required = false) Pageable pageable) {
        return ResponseEntity.ok(projectService.searchProjects(companyId, searchTerm, pageable));
    }

    @GetMapping("/companies/{companyId}/projects/name/{projectName}")
    @Operation(summary = "Get project by name", description = "Retrieves a project by its name for a specific company")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved project"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Company or project not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ProjectDTO> getProjectByName(
            @Parameter(description = "Unique identifier of the company", required = true, example = "1") @PathVariable Long companyId,
            @Parameter(description = "Name of the project", required = true, example = "Website Redesign") @PathVariable String projectName) {
        return ResponseEntity.ok(projectService.getProjectByName(companyId, projectName));
    }
} 