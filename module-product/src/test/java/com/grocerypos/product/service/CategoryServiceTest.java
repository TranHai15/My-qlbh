package com.grocerypos.product.service;

import com.grocerypos.core.exception.ValidationException;
import com.grocerypos.product.entity.Category;
import com.grocerypos.product.repository.CategoryRepository;
import com.grocerypos.product.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        categoryService = new CategoryServiceImpl(categoryRepo);
    }

    @Test
    void save_ValidCategory_Success() {
        Category category = Category.builder().name("Đồ uống").isActive(true).build();
        when(categoryRepo.save(any(Category.class))).thenReturn(1L);

        Category saved = categoryService.save(category);

        assertNotNull(saved);
        assertEquals(1L, saved.getId());
        verify(categoryRepo, times(1)).save(category);
    }

    @Test
    void save_EmptyName_ShouldThrowException() {
        Category category = Category.builder().name("").isActive(true).build();

        assertThrows(ValidationException.class, () -> categoryService.save(category));
        verify(categoryRepo, never()).save(any());
    }

    @Test
    void update_CyclicHierarchy_Direct_ShouldThrowException() {
        Category category = Category.builder().name("Đồ uống").isActive(true).build();
        category.setId(1L);
        category.setParentId(1L); // Self parent

        assertThrows(ValidationException.class, () -> categoryService.update(category));
        verify(categoryRepo, never()).update(any());
    }

    @Test
    void update_CyclicHierarchy_Indirect_ShouldThrowException() {
        // A (id=1) -> B (id=2) -> C (id=3)
        // Try to set A's parent to C
        Category catA = Category.builder().name("A").parentId(null).build(); catA.setId(1L);
        Category catB = Category.builder().name("B").parentId(1L).build(); catB.setId(2L);
        Category catC = Category.builder().name("C").parentId(2L).build(); catC.setId(3L);

        // When checking for A (id=1) with new parent C (id=3)
        when(categoryRepo.findById(3L)).thenReturn(Optional.of(catC));
        when(categoryRepo.findById(2L)).thenReturn(Optional.of(catB));
        when(categoryRepo.findById(1L)).thenReturn(Optional.of(catA));

        Category updateA = Category.builder().name("A").parentId(3L).build();
        updateA.setId(1L);

        ValidationException ex = assertThrows(ValidationException.class, () -> categoryService.update(updateA));
        assertTrue(ex.getMessage().contains("vòng lặp"));
        verify(categoryRepo, never()).update(any());
    }

    @Test
    void delete_WithChildren_ShouldThrowException() {
        Category parent = Category.builder().name("Cha").build(); parent.setId(1L);
        Category child = Category.builder().name("Con").parentId(1L).build(); child.setId(2L);

        when(categoryRepo.findByParentId(1L)).thenReturn(List.of(child));

        assertThrows(ValidationException.class, () -> categoryService.delete(1L));
        verify(categoryRepo, never()).delete(anyLong());
    }

    @Test
    void delete_NoChildren_Success() {
        when(categoryRepo.findByParentId(1L)).thenReturn(Collections.emptyList());

        categoryService.delete(1L);

        verify(categoryRepo, times(1)).delete(1L);
    }
}
