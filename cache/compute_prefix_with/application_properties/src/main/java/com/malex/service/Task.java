package com.malex.service;

import java.io.Serializable;
import java.time.LocalDateTime;

public record Task (String id, String name, LocalDateTime dateTime)  implements Serializable {}
