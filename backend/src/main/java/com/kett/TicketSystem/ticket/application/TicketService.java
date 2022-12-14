package com.kett.TicketSystem.ticket.application;

import com.kett.TicketSystem.membership.domain.events.MembershipDeletedEvent;
import com.kett.TicketSystem.phase.domain.events.NewTicketAssignedToPhaseEvent;
import com.kett.TicketSystem.project.domain.events.ProjectDeletedEvent;
import com.kett.TicketSystem.common.exceptions.ImpossibleException;
import com.kett.TicketSystem.ticket.domain.Ticket;
import com.kett.TicketSystem.ticket.domain.events.TicketCreatedEvent;
import com.kett.TicketSystem.ticket.domain.exceptions.NoTicketFoundException;
import com.kett.TicketSystem.ticket.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public TicketService(TicketRepository ticketRepository, ApplicationEventPublisher eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.eventPublisher = eventPublisher;
    }


    // create

    public Ticket addTicket(Ticket ticket, UUID postingUserId) {
        Ticket initializedTicket = ticketRepository.save(ticket);
        eventPublisher.publishEvent(new TicketCreatedEvent(initializedTicket.getId(), initializedTicket.getProjectId(), postingUserId));
        return initializedTicket;
    }


    // read

    public Ticket getTicketById(UUID id) throws NoTicketFoundException {
        return ticketRepository
                .findById(id)
                .orElseThrow(() -> new NoTicketFoundException("could not find ticket with id: " + id));
    }

    public List<Ticket> getTicketsByPhaseId(UUID phaseId) throws NoTicketFoundException {
        List<Ticket> tickets = ticketRepository.findByPhaseId(phaseId);
        if (tickets.isEmpty()) {
            throw new NoTicketFoundException("could not find tickets with phaseId: " + phaseId);
        }
        return tickets;
    }

    public List<Ticket> getTicketsByProjectId(UUID projectId) throws NoTicketFoundException {
        List<Ticket> tickets = ticketRepository.findByProjectId(projectId);
        if (tickets.isEmpty()) {
            throw new NoTicketFoundException("could not find tickets with projectId: " + projectId);
        }
        return tickets;
    }

    public List<Ticket> getTicketsByAssigneeId(UUID assigneeId) throws NoTicketFoundException {
        List<Ticket> tickets = ticketRepository.findByAssigneeIdsContaining(assigneeId);
        if (tickets.isEmpty()) {
            throw new NoTicketFoundException("could not find tickets with assigneeId: " + assigneeId);
        }
        return tickets;
    }

    public UUID getProjectIdByTicketId(UUID ticketId) throws NoTicketFoundException {
        return this.getTicketById(ticketId).getProjectId();
    }

    public boolean hasTicketsWithPhaseId(UUID phaseId) {
        return ticketRepository.existsByPhaseIdEquals(phaseId);
    }


    // update

    public void patchTicket(
            UUID id,
            String title,
            String description,
            LocalDateTime dueTime,
            UUID phaseId,
            List<UUID> assigneeIds
    ) throws NoTicketFoundException {
        Ticket ticket = this.getTicketById(id);

        if (title != null) {
            ticket.setTitle(title);
        }
        if (description != null) {
            ticket.setDescription(description);
        }
        if (dueTime != null) {
            ticket.setDueTime(dueTime);
        }
        if (phaseId != null) {
            ticket.setPhaseId(phaseId);
        }
        if (assigneeIds != null) {
            ticket.setAssigneeIds(assigneeIds);
        }

        ticketRepository.save(ticket);
    }


    // delete

    public void deleteTicketById(UUID id) throws NoTicketFoundException {
        Long numOfDeletedTickets = ticketRepository.removeById(id);

        if (numOfDeletedTickets == 0) {
            throw new NoTicketFoundException("could not delete because there was no ticket with id: " + id);
        } else if (numOfDeletedTickets > 1) {
            throw new ImpossibleException(
                    "!!! This should not happen. " +
                    "Multiple tickets were deleted when deleting tickets with id: " + id
            );
        }
    }

    public void deleteTicketsByProjectId(UUID projectId) {
        ticketRepository.deleteByProjectId(projectId);
    }


    // event listeners

    @EventListener
    @Async
    public void handleProjectDeletedEvent(ProjectDeletedEvent projectDeletedEvent) {
        this.deleteTicketsByProjectId(projectDeletedEvent.getProjectId());
    }

    @EventListener
    @Async
    public void handleMembershipDeletedEvent(MembershipDeletedEvent membershipDeletedEvent) {
        List<Ticket> tickets =
                ticketRepository
                        .findByProjectId(membershipDeletedEvent.getProjectId())
                        .stream()
                        .filter(ticket -> ticket.isAssignee(membershipDeletedEvent.getUserId()))
                        .toList();

        tickets.forEach(ticket -> ticket.removeAssignee(membershipDeletedEvent.getUserId()));
        ticketRepository.saveAll(tickets);
    }

    @EventListener
    @Async
    public void handleNewTicketAssignedToPhaseEvent(NewTicketAssignedToPhaseEvent newTicketAssignedToPhaseEvent) {
        Ticket ticket = this.getTicketById(newTicketAssignedToPhaseEvent.getTicketId());
        ticket.setPhaseId(newTicketAssignedToPhaseEvent.getPhaseId());
        ticketRepository.save(ticket);
    }
}
