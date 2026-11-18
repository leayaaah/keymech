package vn.edu.iuh.keymech.service;

import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.Deque;
import org.springframework.stereotype.Service;
import vn.edu.iuh.keymech.config.AppProperties;
import vn.edu.iuh.keymech.exception.RateLimitedException;

@Service
public class ChatRateLimiter {

    private static final String SESSION_TIMES = "chatRateTimes";
    private final int perMinute;
    private final int globalPerHour;
    private final Deque<Instant> globalTimes = new ArrayDeque<>();

    public ChatRateLimiter(AppProperties properties) {
        this.perMinute = properties.chatbot().rateLimitPerMinute();
        this.globalPerHour = properties.chatbot().rateLimitPerHourGlobal();
    }

    public synchronized void checkAndRecord(HttpSession session) {
        Instant now = Instant.now();
        @SuppressWarnings("unchecked")
        Deque<Instant> sessionTimes = (Deque<Instant>) session.getAttribute(SESSION_TIMES);
        if (sessionTimes == null) sessionTimes = new ArrayDeque<>();
        prune(sessionTimes, now.minus(1, ChronoUnit.MINUTES));
        prune(globalTimes, now.minus(1, ChronoUnit.HOURS));
        if (sessionTimes.size() >= perMinute || globalTimes.size() >= globalPerHour) {
            throw new RateLimitedException();
        }
        sessionTimes.addLast(now);
        globalTimes.addLast(now);
        session.setAttribute(SESSION_TIMES, sessionTimes);
    }

    private void prune(Deque<Instant> times, Instant cutoff) {
        while (!times.isEmpty() && times.peekFirst().isBefore(cutoff)) times.removeFirst();
    }
}
