package com.malex.subsctiber.event;

import java.time.LocalDateTime;

public record MessageEvent(String sender, String content, LocalDateTime timestamp) {}
