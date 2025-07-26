package com.malex.listener;

import java.time.LocalDateTime;

public record MessageEvent(String title, String content, LocalDateTime timestamp) {}
