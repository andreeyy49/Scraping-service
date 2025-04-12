package webscraping.crawlerservice.services;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import webscraping.crawlerservice.dto.S3PagesDto;
import webscraping.crawlerservice.enums.SiteDataType;
import webscraping.crawlerservice.model.Page;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final AmazonS3 s3Client;

    @Value("${aws.bucket-name}")
    private String bucket;

    private final EventService eventService;

    private final ObjectMapper objectMapper;

    private final ExecutorService executorService;

    public void uploadPagesJson(List<Page> pages, SiteDataType siteDataType) {
        int chunkSize = 50;

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < pages.size(); i += chunkSize) {
            int endIndex = Math.min(i + chunkSize, pages.size());
            List<Page> chunk = pages.subList(i, endIndex);
            int chunkNumber = i / chunkSize;

            futures.add(executorService.submit(() -> uploadChunkStreaming(chunk, siteDataType, chunkNumber)));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                log.error("Ошибка при загрузке чанка", e);
            }
        }

    }

    private void uploadChunkStreaming(List<Page> chunk, SiteDataType siteDataType, int chunkNumber) {
        String key = String.format("sites/%s/%s/part-%d.json",
                chunk.get(0).getSite().getId(),
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                chunkNumber);

        Map<String, String> pagesMap = new HashMap<>();
        chunk.forEach(page -> pagesMap.put(page.getPath(), page.getContent()));

        S3PagesDto dto = new S3PagesDto(pagesMap, siteDataType);

        try {
            String jsonContent = objectMapper.writeValueAsString(dto);
            byte[] bytes = jsonContent.getBytes(StandardCharsets.UTF_8);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("application/json");
            metadata.setContentLength(bytes.length);

            // Устанавливаем лимит для чтения
            PutObjectRequest request = new PutObjectRequest(bucket, key, new ByteArrayInputStream(bytes), metadata);

            // Устанавливаем read limit для ретраев
            request.getRequestClientOptions().setReadLimit(1024 * 1024 * 100); // 100 MB

            s3Client.putObject(request); // Загружаем в S3

            log.info("Uploaded {} pages to {}", chunk.size(), key);
            eventService.sendPagesToTopic(key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload chunk " + chunkNumber, e);
        }
    }

}
