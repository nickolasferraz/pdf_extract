package com.example.pdf_extratct.readpdf.service.excelservices;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Map;

@Service
public class SystemExcelService {

    public Workbook WriteExcel(List<Map<String,String>> rows){

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("ExcelExtract");

        if(rows==null || rows.isEmpty()){
            return workbook;
        }

        Row headerRow = sheet.createRow(0);
        int col=0;

        for(String header : rows.get(0).keySet()){
            headerRow.createCell(col++).setCellValue(header);
        }

        int rowIdx=1;

        for (Map<String,String> data : rows){
            Row row = sheet.createRow(rowIdx++);
            int colIdx=0;

            for(String value : data.values()){
                row.createCell(colIdx++).setCellValue(value);
            }
        }

        for (int i = 0; i<rows.get(0).size(); i++){
            sheet.autoSizeColumn(i);
        }


        return workbook;


    }

}
