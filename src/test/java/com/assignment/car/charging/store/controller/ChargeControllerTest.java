package com.assignment.car.charging.store.controller;

import com.assignment.car.charging.store.components.SessionDataStorage;
import com.assignment.car.charging.store.domain.ChargeSession;
import com.assignment.car.charging.store.domain.CounterSummary;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.CollectionUtils.arrayToList;

/**
 * Test for ChargeController class.
 *
 * @author <a href="mailto:lexbaev@gmail.com">Aliaksei Lizunou</a>
 */
@RunWith(SpringRunner.class)
@WebMvcTest(ChargeController.class)
class ChargeControllerTest {

  @MockBean
  SessionDataStorage sessionDataStorage;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void submitSession() throws Exception {
    ChargeSession session = new ChargeSession("ABC-12345", LocalDateTime.now());
    String initialString = "{\"stationId\": \"ABC-12345\"}";
    InputStream inputStream = new ByteArrayInputStream(initialString.getBytes());
    when(sessionDataStorage.submitSession(session)).thenReturn(session);
    mockMvc.perform(post("/chargingSessions").content(IOUtils.toByteArray(inputStream))
            .characterEncoding(String.valueOf(StandardCharsets.UTF_8))
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print()).andExpect(status().isOk());
  }

  @Test
  void submitSessionException() throws Exception {
    ChargeSession session = new ChargeSession("ABC-12345", LocalDateTime.now());
    String initialString = "{\"station\": \"ABC-12345\"}";
    InputStream inputStream = new ByteArrayInputStream(initialString.getBytes());
    when(sessionDataStorage.submitSession(session)).thenReturn(session);
    mockMvc.perform(post("/chargingSessions").content(IOUtils.toByteArray(inputStream))
            .characterEncoding(String.valueOf(StandardCharsets.UTF_8))
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print()).andExpect(status().is4xxClientError());
  }

  @Test
  void stopSession() throws Exception {
    ChargeSession session = new ChargeSession("ABC-12345", LocalDateTime.now());
    when(sessionDataStorage.stopSession(session.getId())).thenReturn(session);
    mockMvc.perform(put("/chargingSessions/{id}", session.getId()))
            .andDo(print()).andExpect(status().isOk());
  }

  @Test
  void retrieveAllSessions() throws Exception{
    ChargeSession session0 = new ChargeSession("ABC-12345", LocalDateTime.now());
    ChargeSession session1 = new ChargeSession("ABC-12345", LocalDateTime.now());
    ChargeSession session2 = new ChargeSession("ABC-12345", LocalDateTime.now());
    List<ChargeSession> chargeSessionList = arrayToList(new ChargeSession[] {session0, session1, session2});

    when(sessionDataStorage.retrieveAllSessions()).thenReturn(chargeSessionList);
    mockMvc.perform(get("/chargingSessions")).andDo(print()).andExpect(status().isOk());
  }

  @Test
  void retrieveSummarySubmittedSessions() throws Exception {
    when(sessionDataStorage.retrieveSummaryLastMinuteSubmittedSessions()).thenReturn(new CounterSummary(5, 4, 1));
    mockMvc.perform(get("/chargingSessions/summary")).andDo(print()).andExpect(status().isOk());
  }
}