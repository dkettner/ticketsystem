package com.kett.TicketSystem.project.application;

import com.kett.TicketSystem.TicketSystemService;
import com.kett.TicketSystem.project.domain.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/projects")
public class ProjectController {
    private final TicketSystemService ticketSystemService;

    @Autowired
    public ProjectController(TicketSystemService ticketSystemService) {
        this.ticketSystemService = ticketSystemService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable UUID id) {
        Project project = ticketSystemService.getProjectById(id);
        return new ResponseEntity<>(project, HttpStatus.OK);
    }
}
