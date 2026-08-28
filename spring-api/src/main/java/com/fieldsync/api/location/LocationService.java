package com.fieldsync.api.location;

import com.fieldsync.api.domain.entity.LocationEntity;
import com.fieldsync.api.domain.entity.TenantEntity;

import com.fieldsync.api.domain.repository.LocationRepository;
import com.fieldsync.api.domain.repository.TenantRepository;

import com.fieldsync.api.security.user.AuthenticatedFieldSyncUser;
import com.fieldsync.api.security.user.CurrentUserService;

import com.fieldsync.api.tenant.TenantContextExecutor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class LocationService {

    private final LocationRepository
        locationRepository;

    private final TenantRepository
        tenantRepository;

    private final CurrentUserService
        currentUserService;

    private final TenantContextExecutor
        tenantContextExecutor;


    public LocationService(
            LocationRepository locationRepository,
            TenantRepository tenantRepository,
            CurrentUserService currentUserService,
            TenantContextExecutor tenantContextExecutor
    ) {

        this.locationRepository =
            locationRepository;

        this.tenantRepository =
            tenantRepository;

        this.currentUserService =
            currentUserService;

        this.tenantContextExecutor =
            tenantContextExecutor;
    }


    public List<LocationResponse>
    getLocations() {

        AuthenticatedFieldSyncUser currentUser =
            currentUserService
                .requireCurrentUser();

        Integer tenantId =
            currentUser.tenantId();

        return tenantContextExecutor.execute(
            tenantId,
            () ->
                locationRepository
                    .findAllByTenant_IdAndActiveTrueOrderByIdDesc(
                        tenantId
                    )
                    .stream()
                    .map(LocationResponse::from)
                    .toList()
        );
    }


    public LocationResponse createLocation(
            LocationRequest request
    ) {

        AuthenticatedFieldSyncUser currentUser =
            currentUserService
                .requireCurrentUser();

        requireWebAccess(currentUser);

        String name =
            requireName(request);

        String address =
            normalizeAddress(
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
                                new LocationApiException(
                                    HttpStatus.FORBIDDEN,
                                    "Tenant is not active"
                                )
                        );

                LocationEntity location =
                    LocationEntity.create(
                        tenant,
                        name,
                        address
                    );

                return LocationResponse.from(
                    locationRepository
                        .saveAndFlush(location)
                );
            }
        );
    }


    public LocationResponse updateLocation(
            Integer locationId,
            LocationRequest request
    ) {

        AuthenticatedFieldSyncUser currentUser =
            currentUserService
                .requireCurrentUser();

        requireWebAccess(currentUser);

        String name =
            requireName(request);

        String address =
            normalizeAddress(
                request.address()
            );

        Integer tenantId =
            currentUser.tenantId();


        return tenantContextExecutor.execute(
            tenantId,
            () -> {

                LocationEntity location =
                    locationRepository
                        .findByIdAndTenant_Id(
                            locationId,
                            tenantId
                        )
                        .orElseThrow(
                            () ->
                                new LocationApiException(
                                    HttpStatus.NOT_FOUND,
                                    "Location not found"
                                )
                        );

                location.update(
                    name,
                    address
                );

                return LocationResponse.from(
                    locationRepository
                        .saveAndFlush(location)
                );
            }
        );
    }


    public LocationDeleteResponse deleteLocation(
            Integer locationId
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

                LocationEntity location =
                    locationRepository
                        .findByIdAndTenant_Id(
                            locationId,
                            tenantId
                        )
                        .orElseThrow(
                            () ->
                                new LocationApiException(
                                    HttpStatus.NOT_FOUND,
                                    "Location not found"
                                )
                        );

                LocationResponse deleted =
                    LocationResponse.from(
                        location
                    );

                locationRepository.delete(
                    location
                );

                locationRepository.flush();

                return new LocationDeleteResponse(
                    "Location deleted successfully",
                    deleted
                );
            }
        );
    }


    private String requireName(
            LocationRequest request
    ) {

        if (
            request == null ||
            request.name() == null ||
            request.name().trim().isEmpty()
        ) {

            throw new LocationApiException(
                HttpStatus.BAD_REQUEST,
                "Location name is required"
            );
        }

        return request.name().trim();
    }


    private String normalizeAddress(
            String address
    ) {

        if (
            address == null ||
            address.isEmpty()
        ) {
            return null;
        }

        return address;
    }


    private void requireWebAccess(
            AuthenticatedFieldSyncUser user
    ) {

        if (
            !user.isAdmin() &&
            !user.accessWeb()
        ) {

            throw new LocationApiException(
                HttpStatus.FORBIDDEN,
                "Web application access is required"
            );
        }
    }


    private void requireAdmin(
            AuthenticatedFieldSyncUser user
    ) {

        if (!user.isAdmin()) {

            throw new LocationApiException(
                HttpStatus.FORBIDDEN,
                "Admin access is required"
            );
        }
    }
}