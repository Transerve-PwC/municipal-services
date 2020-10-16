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
public class NotificationsEmail {

	@JsonProperty("enabled")
	private boolean enabled;

	@JsonProperty("html")
	private String html;

	@JsonProperty("to")
	private String to;
}
