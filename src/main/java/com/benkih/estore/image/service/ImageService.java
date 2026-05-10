package com.benkih.estore.image.service;

import com.benkih.estore.common.exceptions.NotFoundException;
import com.benkih.estore.image.dto.ImageDto;
import com.benkih.estore.image.entity.Image;
import com.benkih.estore.image.repository.ImageRepository;
import com.benkih.estore.product.entity.Product;
import com.benkih.estore.product.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ImageService implements IImageService{
  private final ImageRepository imageRepository;
  private final IProductService productService;


  @Override
  public Image getImageById(String slug) {
    return imageRepository.findById(slug)
        .orElseThrow(() -> new NotFoundException("No image found with id: " + slug));
  }

  @Override
  public void deleteImageById(String slug) {
    imageRepository.findById(slug).ifPresentOrElse(imageRepository :: delete, () -> {
      throw new NotFoundException("No image found with this id:" + slug);
    });
  }

  @Override
  public List<ImageDto> addImages(List<MultipartFile> files, String productId) {
    Product product = productService.getProductById(productId);
    List<ImageDto> savedImageDto = new ArrayList<>();

    for (MultipartFile file: files) {
      try {
        Image image = new Image();
        image.setFileName(file.getOriginalFilename());
        image.setFileType(file.getContentType());
        image.setImage(new SerialBlob(file.getBytes()));
        image.setProduct(product);

        String buildDownloadUrl = "/api/v1/images/image/download/";

        String downloadUrl = buildDownloadUrl + image.getId();
        image.setDownloadUrl(downloadUrl);
        imageRepository.save(image);

        Image savedImage = imageRepository.save(image);
        savedImage.setDownloadUrl(buildDownloadUrl + savedImage.getId());
        imageRepository.save(savedImage);

        ImageDto imageDto = new ImageDto();
        imageDto.setImageId(savedImage.getSlug());
        imageDto.setImageName(savedImage.getFileName());
        imageDto.setDownloadUrl(savedImage.getDownloadUrl());
        savedImageDto.add(imageDto);

      } catch (IOException | SQLException e) {
        throw new RuntimeException(e.getMessage());
      }
    }
    return savedImageDto;
  }

  @Override
  public void updateImage(MultipartFile file, String imageId) {
    Image image = getImageById(imageId);
    try {
      image.setFileName(file.getOriginalFilename());
      image.setImage(new SerialBlob(file.getBytes()));
      imageRepository.save(image);
    } catch (SQLException e) {
      throw new RuntimeException(e.getMessage());
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
  }
}
