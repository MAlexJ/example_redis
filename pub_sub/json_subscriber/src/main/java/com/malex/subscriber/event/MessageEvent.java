package com.malex.subscriber.event;

import java.time.LocalDateTime;

public record MessageEvent(String sender, String content, LocalDateTime timestamp) {}
