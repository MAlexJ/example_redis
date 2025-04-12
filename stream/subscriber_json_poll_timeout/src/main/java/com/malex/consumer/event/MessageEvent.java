package com.malex.consumer.event;

import java.time.LocalDateTime;

public record MessageEvent(String sender, String content, LocalDateTime timestamp) {
}
