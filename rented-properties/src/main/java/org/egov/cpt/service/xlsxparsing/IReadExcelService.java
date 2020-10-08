package org.egov.cpt.service.xlsxparsing;

import java.io.File;
import java.io.InputStream;

import org.egov.cpt.models.RentDemandResponse;

public interface IReadExcelService {
    public RentDemandResponse getDatafromExcel(File file, int sheetIndex);

    public RentDemandResponse getDatafromExcel(InputStream file, int sheetIndex);
}