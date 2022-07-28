package info.gezielcarvalho.dscatalog.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import info.gezielcarvalho.dscatalog.dto.CategoryDTO;
import info.gezielcarvalho.dscatalog.dto.ProductDTO;
import info.gezielcarvalho.dscatalog.entities.Category;
import info.gezielcarvalho.dscatalog.entities.Product;
import info.gezielcarvalho.dscatalog.repositories.CategoryRepository;
import info.gezielcarvalho.dscatalog.repositories.ProductRepository;
import info.gezielcarvalho.dscatalog.services.exceptions.DatabaseException;
import info.gezielcarvalho.dscatalog.services.exceptions.ResourceNotFoundException;

@Service
public class ProductService {
	
	@Autowired
	private ProductRepository repository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(PageRequest pageRequest) {
		Page<Product> list = repository.findAll(pageRequest);
		Page<ProductDTO> listDTO = list.map(item -> new ProductDTO(item,item.getCategories()));
		return listDTO;
	}
	
	@Transactional(readOnly = true)
	public List<ProductDTO> findAll() {
		List<Product> list = repository.findAll();
		List<ProductDTO> listDTO = list.stream().map(item -> new ProductDTO(item, item.getCategories())).collect(Collectors.toList());
		return listDTO;
	}

	@Transactional(readOnly = true)
	public ProductDTO findById(Long id) {
		Optional<Product> result = repository.findById(id);
		Product product = result.orElseThrow(() -> new ResourceNotFoundException("Entity Not Found!!"));
		return new ProductDTO(product, product.getCategories());
	}

	@Transactional
	public ProductDTO insert(ProductDTO product) {
		Product entity = new Product();
		copyDtoToEntity(product, entity);
		entity = repository.save(entity);
		return new ProductDTO(entity, entity.getCategories());
	}

	private void copyDtoToEntity(ProductDTO product, Product entity) {
		entity.setName(product.getName());
		entity.setDescription(product.getDescription());
		entity.setDate(product.getDate());
		entity.setImgUrl(product.getImgUrl());
		entity.setPrice(product.getPrice());
		entity.getCategories().clear();
		//product.getCategories().forEach(item -> entity.getCategories().add(new Category(item)));
		for (CategoryDTO catDto: product.getCategories()) {
			Category category = categoryRepository.getOne(catDto.getId());
			entity.getCategories().add(category);
		}
	}

	@Transactional
	public ProductDTO update(Long id, ProductDTO product) {
		try {
			Product entity = repository.getOne(id);
			copyDtoToEntity(product, entity);
			entity = repository.save(entity);
			return new ProductDTO(entity, entity.getCategories());
		} catch(EntityNotFoundException enf) {
			throw new ResourceNotFoundException("Id not found: "+id);
		}
	}
	
	public void delete(Long id) {
		try {
			repository.deleteById(id);
		} catch(EmptyResultDataAccessException erdae) {
			throw new ResourceNotFoundException("Id not found: "+id);
		} catch(DataIntegrityViolationException dive) {
			throw new DatabaseException("");
		}
	}
	
}
