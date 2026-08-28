package com.fieldsync.api.customer;

import com.fieldsync.api.domain.entity.CustomerEntity;
import com.fieldsync.api.domain.entity.TenantEntity;

import com.fieldsync.api.domain.repository.CustomerRepository;
import com.fieldsync.api.domain.repository.TenantRepository;

import com.fieldsync.api.security.user.AuthenticatedFieldSyncUser;
import com.fieldsync.api.security.user.CurrentUserService;

import com.fieldsync.api.tenant.TenantContextExecutor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CustomerService {

    private final CustomerRepository
        customerRepository;

    private final TenantRepository
        tenantRepository;

    private final CurrentUserService
        currentUserService;

    private final TenantContextExecutor
        tenantContextExecutor;


    public CustomerService(
            CustomerRepository customerRepository,
            TenantRepository tenantRepository,
            CurrentUserService currentUserService,
            TenantContextExecutor tenantContextExecutor
    ) {

        this.customerRepository =
            customerRepository;

        this.tenantRepository =
            tenantRepository;

        this.currentUserService =
            currentUserService;

        this.tenantContextExecutor =
            tenantContextExecutor;
    }


    public List<CustomerResponse>
    getCustomers() {

        AuthenticatedFieldSyncUser currentUser =
            currentUserService
                .requireCurrentUser();

        Integer tenantId =
            currentUser.tenantId();

        return tenantContextExecutor.execute(
            tenantId,
            () ->
                customerRepository
                    .findAllByTenant_IdOrderByIdDesc(
                        tenantId
                    )
                    .stream()
                    .map(CustomerResponse::from)
                    .toList()
        );
    }


    public CustomerResponse createCustomer(
            CustomerRequest request
    ) {

        AuthenticatedFieldSyncUser currentUser =
            currentUserService
                .requireCurrentUser();

        requireWebAccess(currentUser);

        String name =
            requireName(request);

        String phone =
            normalizePhone(
                request.phone()
            );

        String email =
            normalizeOptional(
                request.email()
            );

        String address =
            normalizeOptional(
                request.address()
            );

        Integer tenantId =
            currentUser.tenantId();


        return tenantContextExecutor.execute(
            tenantId,
            () -> {

                TenantEntity tenant =
                    tenantRepository
                        .findByIdAndActiveTrue(
                            tenantId
                        )
                        .orElseThrow(
                            () ->
                                new CustomerApiException(
                                    HttpStatus.FORBIDDEN,
                                    "Tenant is not active"
                                )
                        );

                CustomerEntity customer =
                    CustomerEntity.create(
                        tenant,
                        name,
                        phone,
                        email,
                        address
                    );

                return CustomerResponse.from(
                    customerRepository
                        .saveAndFlush(customer)
                );
            }
        );
    }


    public CustomerResponse updateCustomer(
            Integer customerId,
            CustomerRequest request
    ) {

        AuthenticatedFieldSyncUser currentUser =
            currentUserService
                .requireCurrentUser();

        requireWebAccess(currentUser);

        String name =
            requireName(request);

        String phone =
            normalizePhone(
                request.phone()
            );

        String email =
            normalizeOptional(
                request.email()
            );

        String address =
            normalizeOptional(
                request.address()
            );

        Integer tenantId =
            currentUser.tenantId();


        return tenantContextExecutor.execute(
            tenantId,
            () -> {

                CustomerEntity customer =
                    customerRepository
                        .findByIdAndTenant_Id(
                            customerId,
                            tenantId
                        )
                        .orElseThrow(
                            () ->
                                new CustomerApiException(
                                    HttpStatus.NOT_FOUND,
                                    "Customer not found"
                                )
                        );

                customer.update(
                    name,
                    phone,
                    email,
                    address
                );

                return CustomerResponse.from(
                    customerRepository
                        .saveAndFlush(customer)
                );
            }
        );
    }


    public CustomerDeleteResponse deleteCustomer(
            Integer customerId
    ) {

        AuthenticatedFieldSyncUser currentUser =
            currentUserService
                .requireCurrentUser();

        requireAdmin(currentUser);

        Integer tenantId =
            currentUser.tenantId();


        return tenantContextExecutor.execute(
            tenantId,
            () -> {

                CustomerEntity customer =
                    customerRepository
                        .findByIdAndTenant_Id(
                            customerId,
                            tenantId
                        )
                        .orElseThrow(
                            () ->
                                new CustomerApiException(
                                    HttpStatus.NOT_FOUND,
                                    "Customer not found"
                                )
                        );

                CustomerResponse deleted =
                    CustomerResponse.from(
                        customer
                    );

                customerRepository.delete(
                    customer
                );

                customerRepository.flush();

                return new CustomerDeleteResponse(
                    "Customer deleted successfully",
                    deleted
                );
            }
        );
    }


    private String requireName(
            CustomerRequest request
    ) {

        if (
            request == null ||
            request.name() == null ||
            request.name().trim().isEmpty()
        ) {

            throw new CustomerApiException(
                HttpStatus.BAD_REQUEST,
                "Customer name is required"
            );
        }

        return request.name().trim();
    }


    private String normalizePhone(
            String phone
    ) {

        if (
            phone == null ||
            phone.isEmpty()
        ) {
            return null;
        }

        if (!phone.matches("\\d{10}")) {

            throw new CustomerApiException(
                HttpStatus.BAD_REQUEST,
                "Phone number must contain exactly 10 digits"
            );
        }

        return phone;
    }


    private String normalizeOptional(
            String value
    ) {

        if (
            value == null ||
            value.isEmpty()
        ) {
            return null;
        }

        return value;
    }


    private void requireWebAccess(
            AuthenticatedFieldSyncUser user
    ) {

        if (
            !user.isAdmin() &&
            !user.accessWeb()
        ) {

            throw new CustomerApiException(
                HttpStatus.FORBIDDEN,
                "Web application access is required"
            );
        }
    }


    private void requireAdmin(
            AuthenticatedFieldSyncUser user
    ) {

        if (!user.isAdmin()) {

            throw new CustomerApiException(
                HttpStatus.FORBIDDEN,
                "Admin access is required"
            );
        }
    }
}