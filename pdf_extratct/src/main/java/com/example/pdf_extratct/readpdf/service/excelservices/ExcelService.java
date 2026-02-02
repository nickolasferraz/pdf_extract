package com.example.pdf_extratct.readpdf.service.excelservices;

import com.example.pdf_extratct.readpdf.service.excelservices.execeptions.GenerateExcelExeception;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

@Service
public class ExcelService {

    private final SystemExcelService systemExcelService ;

    public ExcelService(SystemExcelService systemExcelService) {
        this.systemExcelService = systemExcelService;
    }

    public byte[] generateExcel(List<Map<String, String>> rows) throws GeneralSecurityException {

        Workbook workbook = systemExcelService.WriteExcel(rows);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new GenerateExcelExeception("Erro ao gerar Excel", e);
        }
    }
}
