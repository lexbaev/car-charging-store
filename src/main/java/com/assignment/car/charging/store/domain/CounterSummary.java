package com.assignment.car.charging.store.domain;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * Entity for summary charging sessions for the last minute.
 *
 * @author <a href="mailto:lexbaev@gmail.com">Aliaksei Lizunou</a>
 */
public class CounterSummary {

  private long totalCount;

  private long startedCount;

  private long stoppedCount;

  public CounterSummary(long totalCount, long startedCount, long stoppedCount) {
    this.totalCount = totalCount;
    this.startedCount = startedCount;
    this.stoppedCount = stoppedCount;
  }

  @JsonGetter("totalCount")
  public long getTotalCount() {
    return totalCount;
  }

  @JsonGetter("startedCount")
  public long getStartedCount() {
    return startedCount;
  }

  @JsonGetter("stoppedCount")
  public long getStoppedCount() {
    return stoppedCount;
  }
}
