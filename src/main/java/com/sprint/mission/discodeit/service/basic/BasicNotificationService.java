package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.exception.notification.NotificationForbiddenException;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicNotificationService implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @PreAuthorize("principal.userDto.id == #receiverId")
    @Override
    public List<NotificationDto> findAllByReceiverId(UUID receiverId) {
        log.debug("알림 목록 조회 시작: receiverId={}", receiverId);
        List<NotificationDto> notifications = notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(
                receiverId)
            .stream()
            .map(notificationMapper::toDto)
            .toList();
        log.info("알림 목록 조회 완료: receiverId={}, 조회된 항목 수={}", receiverId, notifications.size());
        return notifications;
    }

    @PreAuthorize("principal.userDto.id == #receiverId")
    @Transactional
    @Override
    public void delete(UUID notificationId, UUID receiverId) {
        log.debug("알림 삭제 시작: id={}, receiverId={}", notificationId, receiverId);
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> NotificationNotFoundException.withId(notificationId));
        if (!notification.getReceiverId().equals(receiverId)) {
            log.warn("알림 삭제 권한 없음: id={}, receiverId={}", notificationId, receiverId);
            throw NotificationForbiddenException.withId(notificationId, receiverId);
        }
        notificationRepository.delete(notification);
    }
} 