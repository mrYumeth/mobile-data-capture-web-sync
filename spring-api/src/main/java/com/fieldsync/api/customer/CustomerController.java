package com.fieldsync.api.customer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService
        customerService;


    public CustomerController(
            CustomerService customerService
    ) {

        this.customerService =
            customerService;
    }


    @GetMapping
    public List<CustomerResponse>
    getCustomers() {

        return customerService
            .getCustomers();
    }


    @PostMapping
    public ResponseEntity<CustomerResponse>
    createCustomer(
            @RequestBody CustomerRequest request
    ) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                customerService
                    .createCustomer(request)
            );
    }


    @PutMapping("/{id}")
    public CustomerResponse updateCustomer(
            @PathVariable String id,
            @RequestBody CustomerRequest request
    ) {

        return customerService
            .updateCustomer(
                parseCustomerId(id),
                request
            );
    }


    @DeleteMapping("/{id}")
    public CustomerDeleteResponse deleteCustomer(
            @PathVariable String id
    ) {

        return customerService
            .deleteCustomer(
                parseCustomerId(id)
            );
    }


    @ExceptionHandler(CustomerApiException.class)
    public ResponseEntity<Map<String, String>>
    handleCustomerApiException(
            CustomerApiException exception
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


    private Integer parseCustomerId(
            String id
    ) {

        try {

            return Integer.valueOf(id);

        }
        catch (NumberFormatException exception) {

            throw new CustomerApiException(
                HttpStatus.BAD_REQUEST,
                "Invalid customer ID"
            );
        }
    }
}