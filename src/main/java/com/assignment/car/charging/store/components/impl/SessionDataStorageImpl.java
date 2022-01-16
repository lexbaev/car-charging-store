package com.assignment.car.charging.store.components.impl;

import com.assignment.car.charging.store.components.SessionDataStorage;
import com.assignment.car.charging.store.domain.ChargeSession;
import com.assignment.car.charging.store.domain.CounterSummary;
import com.assignment.car.charging.store.domain.StatusEnum;
import com.assignment.car.charging.store.exceptions.ChargingSessionException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of charging sessions data storage based on ConcurrentHashMap.
 *
 * @author <a href="mailto:lexbaev@gmail.com">Aliaksei Lizunou</a>
 */
@Component
public class SessionDataStorageImpl implements SessionDataStorage {

  /**
   * Charging sessions storage map.
   */
  private final Map<UUID, ChargeSession> chargeSessionMap = new ConcurrentHashMap<>();

  /**
   * Map of charging session updates.
   */
  private final Map<Integer, ChargeSession> chargeSessionUpdatesMap = new ConcurrentHashMap<>();

  /**
   * Counter of the charging session updates.
   * We make an assumption that total amount of charging sessions will never exceed 2^30.
   */
  private final AtomicInteger updatesAmount = new AtomicInteger(0);

  @Override
  public ChargeSession submitSession(ChargeSession session) {
    chargeSessionUpdatesMap.put(updatesAmount.incrementAndGet(), session.clone());
    chargeSessionMap.put(session.getId(), session);
    return session;
  }

  @Override
  public ChargeSession stopSession(UUID id) throws ChargingSessionException {
    ChargeSession session = chargeSessionMap.get(id);
    sessionInProgressValidation(id, session);
    LocalDateTime now = LocalDateTime.now();
    ChargeSession clonedSession = session.clone();
    chargeSessionUpdatesMap.put(updatesAmount.incrementAndGet(), clonedSession);
    clonedSession.stopSession(now);
    session.stopSession(now);
    return session;
  }

  @Override
  public Collection<ChargeSession> retrieveAllSessions() {
    return chargeSessionMap.values();
  }

  @Override
  public CounterSummary retrieveSummaryLastMinuteSubmittedSessions() {
    LocalDateTime now = LocalDateTime.now();
    int startedCount = 0, stoppedCount = 0, index = chargeSessionUpdatesMap.keySet().size();
    ChargeSession session = chargeSessionUpdatesMap.get(index);

    while (session != null) {
      if (isChangedLessThanMinuteAgo(now, session.getStoppedAt())) {
        stoppedCount++;
      } else if (isChangedLessThanMinuteAgo(now, session.getStartedAt())) {
        startedCount++;
      } else {
        break;
      }
      session = chargeSessionUpdatesMap.get(--index);
    }
    return new CounterSummary(startedCount + stoppedCount, startedCount, stoppedCount);
  }

  /**
   * Validate the charging session is not null and the session has not been finished.
   *
   * @param id
   * @param session
   * @throws ChargingSessionException
   */
  protected static void sessionInProgressValidation(UUID id, ChargeSession session) throws ChargingSessionException {
    if (session == null) {
      throw new ChargingSessionException(String.format("Charging session with id: %s is not found", id), HttpStatus.BAD_REQUEST.value());
    } else if (session.getStatus() == StatusEnum.FINISHED) {
      throw new ChargingSessionException(String.format("Charging session with id: %s has already been finished", id), HttpStatus.BAD_REQUEST.value());
    }
  }

  /**
   * Returns true if loggedTime value is not earlier than one minute ago.
   *
   * @param now
   * @param loggedTime
   * @return
   */
  protected static boolean isChangedLessThanMinuteAgo(LocalDateTime now, LocalDateTime loggedTime) {
    return loggedTime != null && loggedTime.isAfter(now.minusMinutes(1L));
  }
}
