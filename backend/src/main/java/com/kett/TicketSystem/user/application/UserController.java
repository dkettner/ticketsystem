package com.kett.TicketSystem.user.application;

import com.kett.TicketSystem.application.TicketSystemService;
import com.kett.TicketSystem.user.application.dto.UserResponseDto;
import com.kett.TicketSystem.user.domain.exceptions.NoUserFoundException;
import com.kett.TicketSystem.user.domain.exceptions.UserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Transactional
@CrossOrigin(origins = {"http://127.0.0.1:5173"})
@RequestMapping("/users")
public class UserController {
    private final TicketSystemService ticketSystemService;

    @Autowired
    public UserController(TicketSystemService ticketSystemService) {
        this.ticketSystemService = ticketSystemService;
    }


    // endpoints

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable UUID id) {
        UserResponseDto userResponseDto = ticketSystemService.getUserById(id);
        return new ResponseEntity<>(userResponseDto, HttpStatus.OK);
    }


    // exception handlers

    @ExceptionHandler(UserException.class)
    public ResponseEntity<String> handleTicketException(UserException userException) {
        return new ResponseEntity<>(userException.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoUserFoundException.class)
    public ResponseEntity<String> handleTicketException(NoUserFoundException noUserFoundException) {
        return new ResponseEntity<>(noUserFoundException.getMessage(), HttpStatus.NOT_FOUND);
    }
}