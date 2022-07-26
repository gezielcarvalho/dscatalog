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
import info.gezielcarvalho.dscatalog.entities.Category;
import info.gezielcarvalho.dscatalog.repositories.CategoryRepository;
import info.gezielcarvalho.dscatalog.services.exceptions.DatabaseException;
import info.gezielcarvalho.dscatalog.services.exceptions.ResourceNotFoundException;

@Service
public class CategoryService {
	
	@Autowired
	private CategoryRepository repository;
	
	@Transactional(readOnly = true)
	public List<CategoryDTO> findAll() {
		List<Category> list = repository.findAll();
		List<CategoryDTO> listDTO = list.stream().map(item -> new CategoryDTO(item)).collect(Collectors.toList());
		return listDTO;
	}

	@Transactional(readOnly = true)
	public CategoryDTO findById(Long id) {
		Optional<Category> result = repository.findById(id);
		Category category = result.orElseThrow(() -> new ResourceNotFoundException("Entity Not Found!!"));
		return new CategoryDTO(category);
	}

	@Transactional
	public CategoryDTO insert(CategoryDTO category) {
		Category entity = new Category();
		entity.setName(category.getName());
		entity = repository.save(entity);
		return new CategoryDTO(entity);
	}

	@Transactional
	public CategoryDTO update(Long id, CategoryDTO category) {
		try {
			Category entity = repository.getOne(id);
			entity.setName(category.getName());
			entity = repository.save(entity);
			return new CategoryDTO(entity);
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

	public Page<CategoryDTO> findAllPaged(PageRequest pageRequest) {
		Page<Category> list = repository.findAll(pageRequest);
		Page<CategoryDTO> listDTO = list.map(item -> new CategoryDTO(item));
		return listDTO;
	}
	
}
