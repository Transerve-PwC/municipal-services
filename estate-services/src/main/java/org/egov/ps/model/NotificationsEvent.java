package org.egov.ps.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NotificationsEvent {

	@JsonProperty("enabled")
	private boolean enabled;

	@JsonProperty("to")
	private String to;
	
	@JsonProperty("trigger")
	private String trigger;
	
	public boolean isValid() {
		return to != null;
	}
}
