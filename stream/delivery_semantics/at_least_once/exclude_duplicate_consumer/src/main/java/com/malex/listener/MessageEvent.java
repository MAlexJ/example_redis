package com.malex.listener;

import java.time.LocalDateTime;

public record MessageEvent(String tittle, String content, LocalDateTime timestamp) {}
