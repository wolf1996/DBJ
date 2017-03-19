package DBPackage.controllers;

import DBPackage.services.ServiceService;
import DBPackage.models.ServiceModel;
import DBPackage.models.ServiceModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by ksg on 10.03.17.
 */
@RestController
@RequestMapping("/api/service")
class ServiceController {
    private final ServiceService service;

    public ServiceController(final ServiceService service) {
        this.service = service;
    }

    @RequestMapping("/status")
    public final ResponseEntity<Object> serverStatus() {
        return service.serverStatus();
    }

    @RequestMapping("/clear")
    public final ResponseEntity<Object> clearService() {
        return service.clearService();
    }
}