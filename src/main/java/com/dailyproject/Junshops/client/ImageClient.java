package com.dailyproject.Junshops.client;

import com.dailyproject.Junshops.dto.ImageDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
@Service
@RequiredArgsConstructor
public class ImageClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<ImageDto> uploadImages(Long productId, List<UploadFile> files) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        for (UploadFile f : files) {
            ByteArrayResource resource = new ByteArrayResource(f.bytes()) {
                @Override
                public String getFilename() {
                    return f.fileName();
                }
            };

            builder.part("files", resource)
                    .filename(f.fileName())
                    .contentType(MediaType.parseMediaType(f.contentType()));
        }

        builder.part("productId", productId.toString());

        MultiValueMap<String, HttpEntity<?>> multipart = builder.build();

        // ✅ Decode as JsonNode to handle the ["class", {...}] shape
        JsonNode root = webClient.post()
                .uri("/images/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(multipart)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (root == null) return new ArrayList<>();

        // Expect: [ "com...ApiResponse", { message:..., data: [...] } ]
        JsonNode payload = root.isArray() && root.size() >= 2 ? root.get(1) : root;

        JsonNode dataNode = payload.get("data");
        if (dataNode == null || dataNode.isNull()) return new ArrayList<>();

        // data is also typed: [ "java.util.ArrayList", [ [ "com...ImageDto", {...} ] ] ]
        JsonNode listNode = (dataNode.isArray() && dataNode.size() >= 2) ? dataNode.get(1) : dataNode;

        List<ImageDto> result = new ArrayList<>();
        if (listNode != null && listNode.isArray()) {
            for (JsonNode item : listNode) {
                // each item looks like: [ "com...ImageDto", { id:..., fileName:..., downloadUrl:... } ]
                JsonNode obj = (item.isArray() && item.size() >= 2) ? item.get(1) : item;
                result.add(objectMapper.convertValue(obj, ImageDto.class));
            }
        }
        return result;
    }

    public record UploadFile(String fileName, String contentType, byte[] bytes) {}

    public void updateImage(Long imageId, UploadFile file) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        ByteArrayResource resource = new ByteArrayResource(file.bytes()) {
            @Override
            public String getFilename() {
                return file.fileName();
            }
        };

        builder.part("file", resource) // field name MUST be "file"
                .filename(file.fileName())
                .contentType(MediaType.parseMediaType(file.contentType()));

        MultiValueMap<String, HttpEntity<?>> multipart = builder.build();

        // We don’t really care about body; just trigger request
        webClient.put()
                .uri("/images/image/{imageId}/update", imageId)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(multipart)
                .retrieve()
                .bodyToMono(JsonNode.class) // using JsonNode because your API responses include type metadata
                .block();
    }
}