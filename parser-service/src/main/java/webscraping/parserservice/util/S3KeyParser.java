package webscraping.parserservice.util;

import lombok.extern.slf4j.Slf4j;
import webscraping.parserservice.dto.S3KeyDto;

import java.sql.Date;

@Slf4j
public class S3KeyParser {

    public static S3KeyDto getS3KeyDto(String s3Key) {
        String[] split = s3Key.split("/");
        S3KeyDto s3KeyDto = new S3KeyDto();
        s3KeyDto.setSiteId(Long.parseLong(split[1]));
        s3KeyDto.setDate(Date.valueOf(split[2]));
        return s3KeyDto;
    }
}
