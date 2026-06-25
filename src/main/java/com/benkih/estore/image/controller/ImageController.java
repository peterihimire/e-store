package com.benkih.estore.image.controller;

import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.common.response.ApiResponse;
import com.benkih.estore.image.dto.ImageDto;
import com.benkih.estore.image.entity.Image;
import com.benkih.estore.image.service.IImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/images")
public class ImageController {
  private final IImageService imageService;

  @PostMapping("/upload")
  public ResponseEntity<ApiResponse> addImages(
      @RequestParam List<MultipartFile> files, @RequestParam String slug){
    try {
      List<ImageDto> imageDtos = imageService.addImages(files, slug);
      return ResponseEntity.ok(new ApiResponse("success","File upload success", imageDtos));
    }catch(Exception e){
     return ResponseEntity.status(INTERNAL_SERVER_ERROR)
          .body(new ApiResponse("fail","File upload failed", e.getMessage()));
    }
  }

//  @GetMapping("/image/download/{imageId}")
//  public ResponseEntity<Resource> downloadImage(@PathVariable String imageId) throws SQLException {
//    Image image = imageService.getImageBySlug(imageId);
//    ByteArrayResource resource = new ByteArrayResource(image.getImage());
//    return ResponseEntity.ok()
//        .contentType(MediaType.parseMediaType(image.getFileType()))
//        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getFileName() + "\"")
//        .body(resource);
//  }

  @GetMapping("/image/download/{imageId}")
  public ResponseEntity<Resource> downloadImage(@PathVariable String imageId) throws IOException {
    Image image = imageService.getImageBySlug(imageId);
    String safeFileName = image.getFileName()
        .replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");

    Path filePath = Paths.get(image.getFilePath());
    Resource resource = new UrlResource(filePath.toUri());

    if (!resource.exists() || !resource.isReadable()) {
      throw new RuntimeException("Image file not found or not readable");
    }

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(image.getFileType()))
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + safeFileName + "\""
        )
        .body(resource);
  }

  @PutMapping("/image/{imageId}/update")
  public ResponseEntity<ApiResponse> updateImage(@PathVariable String imageId, @RequestBody MultipartFile file){
    try {
      Image image = imageService.getImageBySlug(imageId);
      if(image != null){
        imageService.updateImage(file, imageId);
        return ResponseEntity.ok(new ApiResponse("success","Image update success", null));
      }
    } catch (ResourceNotFoundException e) {
     return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("fail",e.getMessage(), null));
    }
    return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("fail","Update failed", INTERNAL_SERVER_ERROR));
  }

  @DeleteMapping("/image/{imageId}/delete")
  public ResponseEntity<ApiResponse> deleteImage(@PathVariable String imageId){
    try {
      Image image = imageService.getImageBySlug(imageId);
      if(image != null){
        imageService.deleteImageById(imageId);
        return ResponseEntity.ok(new ApiResponse("success","Image delete success", null));
      }
    } catch (ResourceNotFoundException e) {
      return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("fail",e.getMessage(), null));
    }
    return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("fail","Delete failed", INTERNAL_SERVER_ERROR));
  }
}
