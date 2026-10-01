package com.freelancehub.controller;

import com.freelancehub.dto.ApiResponse;
import com.freelancehub.dto.MessageCreateRequest;
import com.freelancehub.model.Message;
import com.freelancehub.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Message>> sendMessage(@Valid @RequestBody MessageCreateRequest request) {
        Message msg = messageService.sendMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message sent successfully", msg));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<ArrayList<Message>>> getByProject(@PathVariable Long projectId) {
        ArrayList<Message> messages = messageService.getMessagesByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success("Project messages retrieved", messages));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<ArrayList<Message>>> getByUser(@PathVariable Long userId) {
        ArrayList<Message> messages = messageService.getMessagesByUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User messages retrieved", messages));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        messageService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Message marked as read"));
    }
}
