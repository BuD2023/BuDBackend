package zerobase.bud.awss3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import zerobase.bud.common.exception.BudException;
import zerobase.bud.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

import static zerobase.bud.common.type.ErrorCode.FAILED_UPLOAD_FILE;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsS3Api {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3Client amazonS3Client;

    public String uploadImage(
            MultipartFile multipartFile
            , String domain
    ) {
        log.info("uploadImage started " + LocalDateTime.now());
        String filePath = FileUtil.createFilePath(domain);
        String fileName = FileUtil.createFileName(
                multipartFile.getOriginalFilename());

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(multipartFile.getSize());
        objectMetadata.setContentType(multipartFile.getContentType());

        try (InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3Client.putObject(
                    new PutObjectRequest(bucket, filePath + fileName, inputStream,
                            objectMetadata).withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new BudException(FAILED_UPLOAD_FILE);
        }
        log.info("uploadImage finished " + LocalDateTime.now() + " ImagePath : "
                + filePath + fileName);
        return filePath + fileName;
    }

    public String getImageUrl(String ImagePath) {
        return amazonS3Client.getUrl(bucket, ImagePath).toString();
    }

    public void deleteImage(String ImagePath) {
        amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, ImagePath));
    }

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
