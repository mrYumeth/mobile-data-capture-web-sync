package com.fieldsync.api.location;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService
        locationService;


    public LocationController(
            LocationService locationService
    ) {

        this.locationService =
            locationService;
    }


    @GetMapping
    public List<LocationResponse>
    getLocations() {

        return locationService
            .getLocations();
    }


    @PostMapping
    public ResponseEntity<LocationResponse>
    createLocation(
            @RequestBody LocationRequest request
    ) {

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                locationService
                    .createLocation(request)
            );
    }


    @PutMapping("/{id}")
    public LocationResponse updateLocation(
            @PathVariable String id,
            @RequestBody LocationRequest request
    ) {

        return locationService
            .updateLocation(
                parseLocationId(id),
                request
            );
    }


    @DeleteMapping("/{id}")
    public LocationDeleteResponse deleteLocation(
            @PathVariable String id
    ) {

        return locationService
            .deleteLocation(
                parseLocationId(id)
            );
    }


    @ExceptionHandler(LocationApiException.class)
    public ResponseEntity<Map<String, String>>
    handleLocationApiException(
            LocationApiException exception
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


    private Integer parseLocationId(
            String id
    ) {

        try {

            return Integer.valueOf(id);

        }
        catch (NumberFormatException exception) {

            throw new LocationApiException(
                HttpStatus.BAD_REQUEST,
                "Invalid location ID"
            );
        }
    }
}