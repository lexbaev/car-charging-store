package com.assignment.car.charging.store.components;

import com.assignment.car.charging.store.domain.ChargeSession;
import com.assignment.car.charging.store.domain.CounterSummary;
import java.util.UUID;

/**
 * Charging sessions data storage interface.
 *
 * @author <a href="mailto:lexbaev@gmail.com">Aliaksei Lizunou</a>
 */
public interface SessionDataStorage extends DataStorage<UUID, ChargeSession, CounterSummary> {
}
