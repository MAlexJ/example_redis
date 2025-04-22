package com.malex.service;

import java.time.LocalDateTime;

public record Task(String id, String name, LocalDateTime timestamp) {}
