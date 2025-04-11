package webscraping.parserservice.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import webscraping.parserservice.dto.S3PagesDto;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final AmazonS3 s3Client;

    @Value("${aws.bucket-name}")
    private String bucket;

    public S3PagesDto downloadHtml(String key) {
//        S3Object s3Object = s3Client.getObject(new GetObjectRequest(bucket, key));
//
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(s3Object.getObjectContent()))) {
//            StringBuilder stringBuilder = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                stringBuilder.append(line).append("\n");
//            }
//            return stringBuilder.toString();
//        } catch (IOException e) {
//            log.error(e.getMessage());
//            throw new RuntimeException(e);
//        }

        try (S3Object s3Object = s3Client.getObject(bucket, key);
             InputStream inputStream = s3Object.getObjectContent()) {

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(inputStream, S3PagesDto.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to download/parse", e);
        }
    }
}
