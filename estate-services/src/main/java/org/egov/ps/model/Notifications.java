package org.egov.ps.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Notifications {
	
	@JsonProperty("moduleType")
	private String moduleType;

	@JsonProperty("applicationType")
	private String applicationType;

	@JsonProperty("state")
	private String state;

	@JsonProperty("content")
	private String content;

	@JsonProperty("modes")
	private NotificationsModes modes;
}