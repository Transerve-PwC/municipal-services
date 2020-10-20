package org.egov.ps.web.contracts;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
@Data
@Builder
@NoArgsConstructor
public class EstateModuleResponse {

	 List<EstateDemand> estateDemands = new ArrayList<EstateDemand>();
     List<EstatePayment> estatePayments = new ArrayList<EstatePayment>();
}
