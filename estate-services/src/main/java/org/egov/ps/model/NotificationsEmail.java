package org.egov.ps.model;

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
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NotificationsEmail {

	private boolean enabled;

	private String html;

	private String to;

	private String subject;

	public boolean isValid() {
		return html != null && to != null && subject != null;
	}
}
