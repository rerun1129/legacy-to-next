package com.freightos.admin.application.notice;

import com.freightos.admin.application.notice.command.CreateNoticeCommand;
import com.freightos.admin.domain.notice.entity.Notice;
import org.springframework.stereotype.Component;

@Component
public class NoticeFactory {

    public Notice from(CreateNoticeCommand command) {
        return Notice.create(
                command.title(),
                command.content(),
                command.pinned(),
                command.active(),
                command.publishedAt(),
                command.expiresAt()
        );
    }
}
