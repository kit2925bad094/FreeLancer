package com.freelancehub.service;

import com.freelancehub.dto.MessageCreateRequest;
import com.freelancehub.exception.BadRequestException;
import com.freelancehub.exception.ResourceNotFoundException;
import com.freelancehub.model.Message;
import com.freelancehub.model.Notification;
import com.freelancehub.model.Project;
import com.freelancehub.model.User;
import com.freelancehub.repository.MessageRepository;
import com.freelancehub.repository.NotificationRepository;
import com.freelancehub.repository.ProjectRepository;
import com.freelancehub.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public MessageService(MessageRepository messageRepository,
                          ProjectRepository projectRepository,
                          UserRepository userRepository,
                          NotificationRepository notificationRepository) {
        this.messageRepository = messageRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    public Message sendMessage(MessageCreateRequest req) {
        Project project = projectRepository.findById(req.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + req.getProjectId()));

        User sender = userRepository.findById(req.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender user not found with id: " + req.getSenderId()));

        User receiver = userRepository.findById(req.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver user not found with id: " + req.getReceiverId()));

        if (req.getMessage() == null || req.getMessage().trim().isEmpty()) {
            throw new BadRequestException("Message cannot be empty");
        }

        Message msg = new Message();
        msg.setSenderId(req.getSenderId());
        msg.setReceiverId(req.getReceiverId());
        msg.setProjectId(req.getProjectId());
        msg.setMessage(req.getMessage().trim());
        msg.setIsRead(false);

        Long msgId = messageRepository.save(msg);
        msg.setId(msgId);
        msg.setSenderName(sender.getName());
        msg.setSenderRole(sender.getRole());
        msg.setReceiverName(receiver.getName());

        // Notify receiver
        Notification notif = new Notification();
        notif.setUserId(req.getReceiverId());
        notif.setTitle("New Message from " + sender.getName());
        notif.setMessage("Regarding \"" + project.getTitle() + "\": " + (msg.getMessage().length() > 50 ? msg.getMessage().substring(0, 50) + "..." : msg.getMessage()));
        notif.setType("MESSAGE");
        notificationRepository.save(notif);

        return msg;
    }

    public ArrayList<Message> getMessagesByProject(Long projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        return messageRepository.findByProjectId(projectId);
    }

    public ArrayList<Message> getMessagesByUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return messageRepository.findByUserId(userId);
    }

    public void markAsRead(Long messageId) {
        messageRepository.markAsRead(messageId);
    }
}
