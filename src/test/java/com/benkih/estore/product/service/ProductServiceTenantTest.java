package com.benkih.estore.product.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.benkih.estore.common.exceptions.ResourceNotFoundException;
import com.benkih.estore.product.repository.ProductRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTenantTest {

    @Mock private ProductRepository productRepository;

    @InjectMocks private ProductMutationService productMutationService;

    @Test
    void deleteProductLooksUpWithinTheTenant() {
        when(productRepository.findBySlugAndBusinessId("slug", 42L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> productMutationService.deleteProductById("slug", 42L));

        verify(productRepository, never()).findBySlug("slug");
    }
}
