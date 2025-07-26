package com.malexj.producer;

import java.time.LocalDateTime;

public record MessageEvent(String title, String content, LocalDateTime timestamp) {}
