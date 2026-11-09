package com.gdghongik.commerce.presentation.product;

<<<<<<< HEAD:commerce/src/main/java/com/gdghongik/commerce/presentation/product/ProductController.java
import com.gdghongik.commerce.application.product.ProductService;
import com.gdghongik.commerce.presentation.product.dto.DecreaseStockRequest;
import com.gdghongik.commerce.presentation.product.dto.ProductCreateRequest;
import com.gdghongik.commerce.presentation.product.dto.ProductResponse;
=======
import com.gdghongik.commerce.dto.DecreaseStockRequest;
import com.gdghongik.commerce.dto.ProductCreateRequest;
import com.gdghongik.commerce.dto.ProductResponse;
import com.gdghongik.commerce.entity.Quantity;
import com.gdghongik.commerce.service.ProductService;
>>>>>>> 8dcae08bde7f15116e8e272b5e3fde7b6fff8234:commerce/src/main/java/com/gdghongik/commerce/controller/ProductController.java
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<ProductResponse> findAll() {
        return productService.findAll().stream()
                .map(ProductResponse::from)
                .toList();
    }

    @GetMapping("/{productId}")
    public ProductResponse findById(@PathVariable Long productId) {
        return ProductResponse.from(productService.findById(productId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductCreateRequest request) {
        return ProductResponse.from(
                productService.create(request.name(), request.price(), request.stock()));
    }

    @PostMapping("/{productId}/decrease-stock")
    public ProductResponse decreaseStock(@PathVariable Long productId,
                                         @RequestBody DecreaseStockRequest request) {
        productService.decreaseStock(productId, Quantity.of(request.quantity()));
        return ProductResponse.from(productService.findById(productId));
    }
}
