package org.egov.cpt.service;

import java.io.File;

import org.egov.cpt.models.RentDemandResponse;

public interface IReadExcelService {
    public RentDemandResponse getDatafromExcel(File file, int sheetIndex);
}