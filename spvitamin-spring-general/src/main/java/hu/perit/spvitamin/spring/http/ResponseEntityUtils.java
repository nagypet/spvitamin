package hu.perit.spvitamin.spring.http;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResponseEntityUtils
{
    public static ResponseEntity<byte[]> createFileDownloadResponse(byte[] fileContent, String fileName)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(fileName)
                        .build()
        );
        return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
    }


    public static <T> T get(ResponseEntity<T> responseEntity)
    {
        if (responseEntity != null && responseEntity.hasBody())
        {
            return responseEntity.getBody();
        }

        throw new IllegalStateException("Invalid http response!");
    }
}
