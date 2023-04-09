package com.kett.TicketSystem.notification.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kett.TicketSystem.notification.domain.Notification;
import com.kett.TicketSystem.notification.repository.NotificationRepository;
import com.kett.TicketSystem.ticket.domain.events.TicketAssignedEvent;
import com.kett.TicketSystem.user.repository.UserRepository;
import com.kett.TicketSystem.util.DummyEventListener;
import com.kett.TicketSystem.util.RestRequestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
public class NotificationControllerTests {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final DummyEventListener dummyEventListener;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final RestRequestHelper restMinion;

    private UUID userId0;
    private String userName0;
    private String userEmail0;
    private String userPassword0;
    private String jwt0;

    private UUID userId1;
    private String userName1;
    private String userEmail1;
    private String userPassword1;
    private String jwt1;

    private UUID ticketId;
    private UUID projectId;

    @Autowired
    public NotificationControllerTests(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            DummyEventListener dummyEventListener,
            ApplicationEventPublisher eventPublisher,
            NotificationService notificationService,
            NotificationRepository notificationRepository,
            UserRepository userRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.dummyEventListener = dummyEventListener;
        this.eventPublisher = eventPublisher;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.restMinion = new RestRequestHelper(mockMvc, objectMapper);
    }

    @BeforeEach
    public void buildUp() throws Exception {
        userName0 = "Peter Greene";
        userEmail0 = "etepetete.greene@gmail.com";
        userPassword0 = "MyGuitarIsMyLife1337";
        userId0 = restMinion.postUser(userName0, userEmail0, userPassword0);
        jwt0 = restMinion.authenticateUser(userEmail0, userPassword0);


        userName1 = "Julia McGonagall";
        userEmail1 = "julia.MG@hogwarts.uk";
        userPassword1 = "Leviosaaaa!__";
        userId1 = restMinion.postUser(userName1, userEmail1, userPassword1);
        jwt1 = restMinion.authenticateUser(userEmail1, userPassword1);

        ticketId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        dummyEventListener.deleteAllEvents();
        notificationRepository.deleteAll();
    }

    @AfterEach
    public void tearDown() {
        userName0 = null;
        userEmail0 = null;
        userPassword0 = null;
        userId0 = null;
        jwt0 = null;

        userName1 = null;
        userEmail1 = null;
        userPassword1 = null;
        userId1 = null;
        jwt1 = null;

        ticketId = null;
        projectId = null;

        notificationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void consumeHandleTicketAssignedEvent() throws Exception {
        eventPublisher
                .publishEvent(
                        new TicketAssignedEvent(
                                ticketId,
                                projectId,
                                userId0
                        )
                );

        // TODO: find more stable alternative for testing
        // shame: give services time to handle event
        Thread.sleep(100);

        List<Notification> notifications = notificationService.getNotificationsByRecipientId(userId0);
        assertEquals(1, notifications.size());
        Notification notification = notifications.get(0);
        assertEquals(userId0, notification.getRecipientId());
        assertEquals(false, notification.getIsRead());
        assertTrue(notification.getContent().contains(ticketId.toString()));
    }
}