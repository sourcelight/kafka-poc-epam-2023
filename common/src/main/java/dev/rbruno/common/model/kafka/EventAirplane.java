package dev.rbruno.common.model.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Riccardo_Bruno
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventAirplane {
  private String event;
}
