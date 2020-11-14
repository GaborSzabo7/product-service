package hu.gaszabo.product.service.application.product;

import java.io.ByteArrayInputStream;

import org.springframework.web.multipart.MultipartFile;

public interface ProductService {

	void uploadFile(MultipartFile file);

	ByteArrayInputStream synchronizeProducts(MultipartFile file);

}
