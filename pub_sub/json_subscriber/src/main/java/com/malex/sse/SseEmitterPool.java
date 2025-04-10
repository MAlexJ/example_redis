package com.malex.sse;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/*
 * Manages multiple SSE (Server-Sent Events) connections (emitters).
 *
 * This class handles active SSE connections and allows sending data to all connected clients.
 * It also implements the cleanup of inactive connections.
 */
@Component
public class SseEmitterPool {

  // List of all active SSE connections
  private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

  /*
   * Adds a new SSE connection to the pool.
   *
   * When a client connects, a new SseEmitter object is created.
   * The logic for cleaning up the connection upon completion, timeout, or error is also set up here.
   *
   * @return the new SseEmitter, which will be used to send events to the client.
   */
  public SseEmitter addEmitter() {
    // Create a new SseEmitter with infinite timeout
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

    // Completion handler (called when the client closes the connection)
    emitter.onCompletion(() -> emitters.remove(emitter));

    // Timeout handler (called when the connection does not respond within a certain period)
    emitter.onTimeout(() -> emitters.remove(emitter));

    // Error handler (called when an error occurs while sending data)
    emitter.onError(e -> emitters.remove(emitter));

    // Add the new emitter to the pool of active connections
    emitters.add(emitter);

    // Return the new emitter
    return emitter;
  }

  /*
   * Sends data to all connected SSE clients.
   *
   * This method is used to send data to all active SSE connections.
   * For example, when new data
   * (like a message from Redis) is received, it sends the data to all clients.
   *
   * @param data the data to be sent to all clients (e.g., message from Redis).
   */
  public void sendToAll(Object data) {
    // Iterate over all active connections
    Iterator<SseEmitter> iterator = emitters.iterator();
    while (iterator.hasNext()) {
      SseEmitter emitter = iterator.next();
      try {
        // Send data to the client via SSE
        emitter.send(data);
      } catch (Exception e) {
        // If an error occurs, remove the emitter from the pool
        iterator.remove(); // Using iterator to safely remove elements during iteration
      }
    }
  }

  /*
   * Cleans up all inactive connections.
   *
   * This method can be called periodically to remove old or inactive connections.
   */
  public void cleanUp() {
    Iterator<SseEmitter> iterator = emitters.iterator();
    while (iterator.hasNext()) {
      SseEmitter emitter = iterator.next();
      try {
        // Try to send an empty message to check if the connection is still alive
        emitter.send(""); // This step ensures that the connection is alive
      } catch (Exception e) {
        // If an error occurs (meaning the connection is closed), remove it from the pool
        iterator.remove();
      }
    }
  }
}
