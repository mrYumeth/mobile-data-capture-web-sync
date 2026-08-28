package com.fieldsync.api.category;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService
        categoryService;


    public CategoryController(
            CategoryService categoryService
    ) {

        this.categoryService =
            categoryService;
    }


    @GetMapping
    public List<CategoryResponse>
    getCategories() {

        return categoryService
            .getActiveCategories();
    }


    @PostMapping
    public ResponseEntity<CategoryResponse>
    createCategory(
            @RequestBody CategoryRequest request
    ) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                categoryService
                    .createCategory(request)
            );
    }


    @PutMapping("/{id}")
    public CategoryResponse
    updateCategory(
            @PathVariable String id,
            @RequestBody CategoryRequest request
    ) {

        return categoryService
            .updateCategory(
                parseCategoryId(id),
                request
            );
    }


    @DeleteMapping("/{id}")
    public CategoryDeleteResponse
    deleteCategory(
            @PathVariable String id
    ) {

        return categoryService
            .deleteCategory(
                parseCategoryId(id)
            );
    }


    @ExceptionHandler(CategoryApiException.class)
    public ResponseEntity<Map<String, String>>
    handleCategoryApiException(
            CategoryApiException exception
    ) {

        return ResponseEntity
            .status(
                exception.getStatus()
            )
            .body(
                Map.of(
                    "message",
                    exception.getMessage()
                )
            );
    }


    private Integer parseCategoryId(
            String id
    ) {

        try {

            return Integer.valueOf(id);

        }
        catch (NumberFormatException exception) {

            throw new CategoryApiException(
                HttpStatus.BAD_REQUEST,
                "Invalid category ID"
            );
        }
    }
}