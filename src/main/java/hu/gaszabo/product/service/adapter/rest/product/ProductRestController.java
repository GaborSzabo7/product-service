package hu.gaszabo.product.service.adapter.rest.product;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.Callable;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import hu.gaszabo.product.service.application.product.ProductService;
import hu.gaszabo.product.service.infrastructure.web.controller.rest.BaseRestController;

@RestController
@RequestMapping("/product")
public class ProductRestController extends BaseRestController {

	private final ProductService productService;

	public ProductRestController(final ProductService productService) {
		this.productService = requireNonNull(productService, "productService can't be null");
	}

	@PutMapping("/upload")
	public Callable<ResponseEntity<Void>> uploadFile(@RequestParam("file") MultipartFile file) {
		return put(() -> productService.uploadFile(file));
	}

	@PostMapping("/synchronize")
	public Callable<ResponseEntity<Resource>> synchronizeSpreadsheet(@RequestParam("file") MultipartFile file) {
		return resource(() -> productService.synchronizeProducts(file), file.getName(), file.getContentType());
	}

	@GetMapping("/ping")
	public Callable<ResponseEntity<String>> ping() {
		return get(() -> "OK");
	}

}
