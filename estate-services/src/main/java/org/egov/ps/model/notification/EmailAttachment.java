package org.egov.ps.model.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@Builder
@NoArgsConstructor
public class EmailAttachment {
	private String name;
	private String url;
	private String mimeType;
}
