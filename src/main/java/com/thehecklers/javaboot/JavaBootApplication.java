package com.thehecklers.javaboot;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
public class JavaBootApplication {
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

    public static void main(String[] args) {
        SpringApplication.run(JavaBootApplication.class, args);
    }

}

@Service
@AllArgsConstructor
class WxService {
    private final AirportRepository repo;
    private final WebClient client;

    Mono<ServerResponse> getAllAirports(ServerRequest req) {
        return ok().body(repo.findAll(), Airport.class);
    }

    Mono<ServerResponse> getAirportById(ServerRequest req) {
        return ok().body(repo.findById(req.pathVariable("id")), Airport.class);
    }

    Mono<ServerResponse> getMetarsForAirportById(ServerRequest req) {
        return ok()
                .contentType(TEXT_EVENT_STREAM)
                .body(Flux.interval(Duration.ofSeconds(1))
                        .flatMap(l -> client.get()
                                .uri("?loc=" + req.pathVariable("id"))
                                .retrieve()
                                .bodyToMono(METAR.class)
                                .defaultIfEmpty(new METAR("???", "METAR unavailable for this airport code"))), METAR.class);
    }
}

interface AirportRepository extends ReactiveCrudRepository<Airport, String> {
}

@Document
record Airport(@Id String id, String name) {}

record METAR(String flight_rules, String raw) {}
