package com.example.pdf_extratct.readpdf.service.facade;

import com.example.pdf_extratct.loginpage.jobs.JobService;
import com.example.pdf_extratct.loginpage.jobs.ProcessingJobEntity;
import com.example.pdf_extratct.loginpage.jobs.ProcessingJobService;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import com.example.pdf_extratct.readpdf.dto.PdfProcessingRequest;
import com.example.pdf_extratct.readpdf.service.aiservices.AiResponseParserService;
import com.example.pdf_extratct.readpdf.service.aiservices.strategy.AiExtractionContext;
import com.example.pdf_extratct.readpdf.service.aiservices.strategy.AiExtractionFactory;
import com.example.pdf_extratct.readpdf.service.aiservices.strategy.AiExtractionStrategy;
import com.example.pdf_extratct.readpdf.service.excelservices.ExcelService;
import com.example.pdf_extratct.readpdf.service.pdfservices.PdfTextAggregationService;
import com.example.pdf_extratct.security.redis.quota_usage.IpBlockService;
import com.example.pdf_extratct.uploadfiles.storage.service.StorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdfProcessingFacadeServiceTest {

    @Mock private AiExtractionFactory aiFactory;
    @Mock private AiResponseParserService aiparserService;
    @Mock private ExcelService excelService;
    @Mock private StorageService storageService;
    @Mock private PdfTextAggregationService pdfTextAggregationService;
    @Mock private ProcessingJobService processingJobService;
    @Mock private JobService queryJobService;
    @Mock private IpBlockService ipBlockService;

    @InjectMocks
    private PdfProcessingFacadeService facadeService;

    @Test
    @DisplayName("Fluxo padrão: Deve processar PDF e retornar bytes do Excel")
    void shouldProcessPdfSuccessfully() throws Exception {
        // Arrange
        PdfProcessingRequest request = new PdfProcessingRequest(
                "header1", "test.pdf", 1024L, new UserEntity(), "127.0.0.1"
        );
        
        ProcessingJobEntity mockJob = new ProcessingJobEntity();
        mockJob.setJobId("job-123");

        when(pdfTextAggregationService.aggreateFullText()).thenReturn("Texto longo o suficiente para não precisar de OCR...");
        when(pdfTextAggregationService.countTotalPages()).thenReturn(2);
        when(queryJobService.createJob(any(), any(), anyLong(), anyInt(), any())).thenReturn(mockJob);
        
        AiExtractionStrategy mockStrategy = mock(AiExtractionStrategy.class);
        when(aiFactory.getStrategy(anyInt())).thenReturn(mockStrategy);
        when(mockStrategy.extract(any(), any())).thenReturn("[{\"col\": \"val\"}]");
        
        when(aiparserService.parseRows(anyString())).thenReturn(List.of(Map.of("col", "val")));
        when(excelService.generateExcel(anyList())).thenReturn(new byte[]{1, 2, 3});

        // Act
        byte[] result = facadeService.processPdfToExcel(request);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.length);
        verify(processingJobService).completeJob(eq("job-123"), anyInt(), anyInt());
        verify(storageService).deleteAll();
    }

    @Test
    @DisplayName("Falha na IA: Deve marcar job como falho e reembolsar uso se anônimo")
    void shouldHandleFailureAndRefundIfAnonymous() throws Exception {
        // Arrange
        PdfProcessingRequest request = new PdfProcessingRequest(
                "header1", "test.pdf", 1024L, null, "127.0.0.1"
        );
        
        ProcessingJobEntity mockJob = new ProcessingJobEntity();
        mockJob.setJobId("job-456");

        when(ipBlockService.registerAnonymousUse(anyString(), anyInt())).thenReturn(true);
        when(pdfTextAggregationService.aggreateFullText()).thenReturn("Texto do PDF");
        when(queryJobService.createJob(any(), any(), anyLong(), anyInt(), any())).thenReturn(mockJob);
        
        AiExtractionStrategy mockStrategy = mock(AiExtractionStrategy.class);
        when(aiFactory.getStrategy(anyInt())).thenReturn(mockStrategy);
        when(mockStrategy.extract(any(), any())).thenThrow(new RuntimeException("IA Down"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> facadeService.processPdfToExcel(request));

        verify(processingJobService).failJob(eq("job-456"), contains("IA Down"));
        verify(ipBlockService).refundAnonymousUse(eq("127.0.0.1"), eq(1));
    }
}
