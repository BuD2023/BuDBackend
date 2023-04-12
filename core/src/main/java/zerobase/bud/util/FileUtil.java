package zerobase.bud.util;

import static zerobase.bud.type.ErrorCode.INVALID_FILE_FORMAT;
import static zerobase.bud.util.Constants.DIRECTORY_SEPARATOR;
import static zerobase.bud.util.Constants.FILE_EXTENSION_SEPARATOR;
import static zerobase.bud.util.Constants.FILE_NAME_SEPARATOR;
import static zerobase.bud.util.Constants.REPLACE_EXPRESSION;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import zerobase.bud.exception.BudException;

@UtilityClass
public class FileUtil {

    public static String createFileName(String fileName) {
        StringBuilder sb = new StringBuilder();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        return sb.append(
                UUID.randomUUID().toString().replaceAll(REPLACE_EXPRESSION, ""))
                 .append(FILE_NAME_SEPARATOR)
                 .append(now)
                 .append(getFileExtension(fileName))
                 .toString();
    }

    public static String getFileExtension(String fileName) {
        try {
            return fileName.substring(
                fileName.lastIndexOf(FILE_EXTENSION_SEPARATOR));
        } catch (Exception e) {
            throw new BudException(INVALID_FILE_FORMAT);
        }
    }

    public static String createFilePath(String domain) {
        StringBuilder sb = new StringBuilder();
        return sb.append(domain)
            .append(DIRECTORY_SEPARATOR)
            .append(LocalDate.now())
            .append(DIRECTORY_SEPARATOR)
            .toString();
    }
}
