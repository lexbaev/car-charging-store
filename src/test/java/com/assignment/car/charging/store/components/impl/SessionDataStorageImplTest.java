package com.assignment.car.charging.store.components.impl;

import com.assignment.car.charging.store.components.DataStorage;
import com.assignment.car.charging.store.domain.ChargeSession;
import com.assignment.car.charging.store.domain.CounterSummary;
import com.assignment.car.charging.store.domain.StatusEnum;
import com.assignment.car.charging.store.exceptions.ChargingSessionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for SessionDataStorageImpl class.
 *
 * @author <a href="mailto:lexbaev@gmail.com">Aliaksei Lizunou</a>
 */
class SessionDataStorageImplTest {

  private DataStorage<UUID, ChargeSession, CounterSummary> dataStorage;

  @BeforeEach
  public void init() {
    dataStorage = new SessionDataStorageImpl();
  }

  @AfterEach
  public void destroy() {
    dataStorage = null;
  }

  @Test
  void submitSession() {
    ChargeSession session = new ChargeSession("ABC-12345", LocalDateTime.now());
    assertEquals(session, dataStorage.submitSession(session));
    assertEquals(1, dataStorage.retrieveAllSessions().size());
    assertTrue(dataStorage.retrieveAllSessions().contains(session));
  }

  @Test
  void stopSession() throws ChargingSessionException {
    LocalDateTime now = LocalDateTime.now();
    ChargeSession session0 = new ChargeSession("ABC-12345", now);
    ChargeSession session1 = new ChargeSession("ABC-12345", now);
    UUID id0 = dataStorage.submitSession(session0).getId();
    dataStorage.submitSession(session1);
    assertEquals(StatusEnum.IN_PROGRESS, ((ChargeSession) dataStorage.retrieveAllSessions().toArray()[0]).getStatus());
    assertEquals(StatusEnum.IN_PROGRESS, ((ChargeSession) dataStorage.retrieveAllSessions().toArray()[1]).getStatus());

    dataStorage.stopSession(id0);

    Collection<StatusEnum> statuses = dataStorage.retrieveAllSessions().stream()
            .map(ChargeSession::getStatus).collect(Collectors.toList());
    assertTrue(statuses.contains(StatusEnum.FINISHED));
    assertTrue(statuses.contains(StatusEnum.IN_PROGRESS));
  }

  @Test
  void retrieveAllSessions() {
    LocalDateTime now = LocalDateTime.now();
    for (int i = 0; i < 3; i++) {
      dataStorage.submitSession(new ChargeSession("ABC-12345", now));
    }
    assertEquals(3, dataStorage.retrieveAllSessions().size());
  }

  @Test
  void retrieveSummarySubmittedSessions() throws ChargingSessionException {
    LocalDateTime now = LocalDateTime.now();
    dataStorage.submitSession(new ChargeSession("ABC-1", now.minusMinutes(2)));
    ChargeSession session2 = new ChargeSession("ABC-2", now.minusMinutes(3));
    dataStorage.submitSession(session2);
    dataStorage.submitSession(new ChargeSession("ABC-3", now.minusSeconds(40)));
    dataStorage.submitSession(new ChargeSession("ABC-4", now.minusSeconds(30)));
    dataStorage.submitSession(new ChargeSession("ABC-5", now.minusSeconds(20)));
    ChargeSession session6 = new ChargeSession("ABC-6", now.minusSeconds(10));
    dataStorage.submitSession(session6);
    dataStorage.stopSession(session2.getId());
    dataStorage.stopSession(session6.getId());

    assertEquals(StatusEnum.FINISHED, session2.getStatus());
    assertEquals(StatusEnum.FINISHED, session6.getStatus());

    CounterSummary counterSummary = dataStorage.retrieveSummaryLastMinuteSubmittedSessions();

    assertEquals(4, counterSummary.getStartedCount());
    assertEquals(2, counterSummary.getStoppedCount());
    assertEquals(6, counterSummary.getTotalCount());
  }

  @Test
  void sessionValidationNullSession() {
    assertThrows(ChargingSessionException.class, () -> SessionDataStorageImpl.sessionInProgressValidation(UUID.randomUUID(), null));
  }

  @Test
  void sessionValidationFinishedSession() throws ChargingSessionException {
    ChargeSession session = new ChargeSession("ABC-12345", LocalDateTime.now());
    assertEquals(session, dataStorage.submitSession(session));
    dataStorage.stopSession(session.getId());

    assertThrows(ChargingSessionException.class, () -> SessionDataStorageImpl.sessionInProgressValidation(session.getId(), session));
  }

  @Test
  void isChangedLessThanMinuteAgo() {
    LocalDateTime now = LocalDateTime.now();
    assertFalse(SessionDataStorageImpl.isChangedLessThanMinuteAgo(now, now.minusMinutes(1)));
    assertFalse(SessionDataStorageImpl.isChangedLessThanMinuteAgo(now, now.minusMinutes(2)));
    assertTrue(SessionDataStorageImpl.isChangedLessThanMinuteAgo(now, now.minusSeconds(20)));
  }
}