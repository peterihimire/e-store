package com.benkih.estore.product.service;

import com.benkih.estore.product.dto.response.ImageDto;
import com.benkih.estore.product.entity.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IImageService {
  //  Image getImageById(Long id);
  Image getImageBySlug(String slug);
  void deleteImageById(String slug);
  List<ImageDto> addImages(List<MultipartFile> file, String slug);
  void updateImage(MultipartFile file, String imageId);
}
