package com.thehecklers.javaboot;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.http.MediaType.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;
import static org.springframework.web.reactive.function.server.ServerResponse.*;

@SpringBootApplication
public class JavaBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaBootApplication.class, args);
    }

    @Bean
    CommandLineRunner clr(AirportRepository repo) {
        return args -> {
            repo.deleteAll()
                    .thenMany(Flux.just(new Airport("KSTL", "St. Louis Lambert International Airport"),
                            new Airport("KORD", "Chicago O'Hare International Airport"),
                            new Airport("KFAT", "Fresno Yosemite Airport"),
                            new Airport("KGAG", "Gage Airport"),
                            new Airport("KLOL", "Derby Field"),
                            new Airport("KSUX", "Sioux Gateway/Brig General Bud Day Field"),
                            new Airport("KBUM", "Butler Memorial Airport")))
                    .flatMap(repo::save)
                    .subscribe();
        };
    }

    @Bean
    WebClient client() {
        return WebClient.create("http://localhost:9876/metar");
    }

    @Bean
    RouterFunction<ServerResponse> routerFunction(WxService svc) {
        return route(GET("/"), svc::getAllAirports)
                .andRoute(GET("/{id}"), svc::getAirportById)
                .andRoute(GET("/metar/{id}"), svc::getMetarsForAirportById);
    }
}

//@RestController
class MetarController {
//    private final WxService service;
//
//    MetarController(WxService service) {
//        this.service = service;
//    }
//
//    @GetMapping
//    Iterable<Airport> getAllAirports() {
//        return service.getAllAirports();
//    }
//
//    @GetMapping("/{id}")
//    Optional<Airport> getAirportById(@PathVariable String id) {
//        return service.getAirportById(id);
//    }
//
//    @GetMapping(value = "/metar/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    Flux<METAR> getMetarsForAirport(@PathVariable String id) {
//        return service.getMetarsForAirportById(id);
//    }
}

@Service
class WxService {
    private final AirportRepository repo;
    private final WebClient client;

    WxService(AirportRepository repo, WebClient client) {
        this.repo = repo;
        this.client = client;
    }

    // req -> ok().body(svc.getAirportById(req.pathVariable("id")), Airport.class)
    Mono<ServerResponse> getAllAirports(ServerRequest req) {
        return ok()
                .body(repo.findAll(), Airport.class);
    }

    //req -> ok().contentType(MediaType.TEXT_EVENT_STREAM).body(svc.getMetarsForAirportById(req.pathVariable("id")), METAR.class)
    Mono<ServerResponse> getAirportById(ServerRequest req) {
        return ok()
                .body(repo.findById(req.pathVariable("id")), Airport.class);
    }

    Mono<ServerResponse> getMetarsForAirportById(ServerRequest req) {
        return ok()
                .contentType(TEXT_EVENT_STREAM)
                .body(Flux.interval(Duration.ofSeconds(1))
                        .flatMap(l -> client.get()
                                .uri("?loc=" + req.pathVariable("id"))
                                .retrieve()
                                .bodyToMono(METAR.class)
                                .defaultIfEmpty(
                                        new METAR("???", "METAR unavailable for this airport code"))), METAR.class)
                ;
    }
}

interface AirportRepository extends ReactiveCrudRepository<Airport, String> {
}

@Document
@Value
class Airport {
    @Id
    String id;
    String name;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class METAR {
    @JsonProperty("flight_rules")
    private String flight_rules;
    private String raw;
}
