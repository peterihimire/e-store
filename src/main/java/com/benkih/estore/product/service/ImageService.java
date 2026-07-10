package com.benkih.estore.product.service;

import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.product.dto.response.ImageDto;
import com.benkih.estore.product.entity.Image;
import com.benkih.estore.product.repository.ImageRepository;
//import com.benkih.estore.product.dto.response.ProductsResponseDto;
import com.benkih.estore.product.entity.Product;
import com.benkih.estore.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

//import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
//import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ImageService implements IImageService {
  private final ImageRepository imageRepository;
  private final IProductService productService;
  private final ProductRepository productRepository;

  @Value("${app.upload.dir}")
  private String uploadDir;


  @Override
  public Image getImageBySlug(String slug) {
    return imageRepository.findBySlug(slug)
        .orElseThrow(() -> new ResourceNotFoundException("No image found with id: " + slug));
  }

  @Override
  public void deleteImageById(String slug) {
    imageRepository.findBySlug(slug).ifPresentOrElse(imageRepository :: delete, () -> {
      throw new ResourceNotFoundException("No image found with this id:" + slug);
    });
  }

  public Product getProductEntityBySlug(String slug) {
    return productRepository.findBySlug(slug)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
  }

  @Override
  public List<ImageDto> addImages(List<MultipartFile> files, String slug) {
    Product product = getProductEntityBySlug(slug);
    List<ImageDto> savedImageDto = new ArrayList<>();

    try {
      Path uploadPath = Paths.get(uploadDir);

      if (!Files.exists(uploadPath)) {
        Files.createDirectories(uploadPath);
      }

      for (MultipartFile file : files) {
        if (file.isEmpty()) {
          continue;
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
          fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String storedFileName = UUID.randomUUID() + fileExtension;
        Path filePath = uploadPath.resolve(storedFileName);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Image image = new Image();
        image.setFileName(originalFileName);
        image.setFileType(file.getContentType());
        image.setFilePath(filePath.toString());
        image.setProduct(product);

        Image savedImage = imageRepository.save(image);

        savedImage.setDownloadUrl("/api/v1/images/image/download/" + savedImage.getSlug());
        imageRepository.save(savedImage);

        ImageDto imageDto = new ImageDto();
        imageDto.setImageSlug(savedImage.getSlug());
        imageDto.setImageName(savedImage.getFileName());
        imageDto.setDownloadUrl(savedImage.getDownloadUrl());

        savedImageDto.add(imageDto);
      }

      return savedImageDto;

    } catch (IOException e) {
      throw new RuntimeException("Could not save image file", e);
    }

  }


  @Override
  public void updateImage(MultipartFile file, String imageSlug) {
    Image image = getImageBySlug(imageSlug);

    try {
      String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
      String fileExtension = "";

      if (originalFileName != null && originalFileName.contains(".")) {
        fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
      }

      String storedFileName = UUID.randomUUID() + fileExtension;

      Path uploadPath = Paths.get(uploadDir);

      if (!Files.exists(uploadPath)) {
        Files.createDirectories(uploadPath);
      }

      Path filePath = uploadPath.resolve(storedFileName);

      Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

      image.setFileName(originalFileName);
      image.setFileType(file.getContentType());
      image.setFilePath(filePath.toString());

      imageRepository.save(image);

    } catch (IOException e) {
      throw new RuntimeException("Could not update image file", e);
    }
  }
}
