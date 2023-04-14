package zerobase.bud.util;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import zerobase.bud.util.FileUtil;

import java.io.File;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsS3Api {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3Client amazonS3Client;

    public String uploadFileImage(File file, String domain) {
        String filePath = FileUtil.createFilePath(domain);
        String fileName = FileUtil.createFileName(file.getName());
        amazonS3Client.putObject(new PutObjectRequest(bucket, filePath + fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        log.info("uploadImage finished " + LocalDateTime.now() + " ImagePath : "
                + filePath + fileName);
        return filePath + fileName;
    }


}
