package com.thehecklers.javaboot;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@SpringBootApplication
public class JavaBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaBootApplication.class, args);
    }

    @Bean
    CommandLineRunner clr(AirportRepository repo) {
        return args -> {
            repo.saveAll(List.of(new Airport("KSTL", "St. Louis Lambert International Airport"),
                    new Airport("KORD", "Chicago O'Hare International Airport"),
                    new Airport("KFAT", "Fresno Yosemite Airport"),
                    new Airport("KGAG", "Gage Airport"),
                    new Airport("KLOL", "Derby Field"),
                    new Airport("KSUX", "Sioux Gateway/Brig General Bud Day Field"),
                    new Airport("KLOL", "Derby Field"),
                    new Airport("KBUM", "Butler Memorial Airport")));
        };
    }
}

@RestController
class MetarController {
    private final WxService service;

    MetarController(WxService service) {
        this.service = service;
    }

    @GetMapping
    Iterable<Airport> getAllMetars() {
        return service.getAllAirports();
    }

    @GetMapping("/{id}")
    Optional<Airport> getAirportById(@PathVariable String id) {
        return service.getAirportById(id);
    }
}

@Service
class WxService {
    private final AirportRepository repo;

    WxService(AirportRepository repo) {
        this.repo = repo;
    }

    Iterable<Airport> getAllAirports() {
        return repo.findAll();
    }

    Optional<Airport> getAirportById(String id) {
        return repo.findById(id);
    }
}

interface AirportRepository extends CrudRepository<Airport, String> {}

//interface MetarRepository extends CrudRepository<METAR, String> {}

@Document
class Airport {
    @Id
    private final String id;
    private final String name;

    public Airport(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Airport airport = (Airport) o;
        return Objects.equals(id, airport.id) && Objects.equals(name, airport.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Airport{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

//@Document
class METAR {
//    @Id
//    String id;
    String flight_rules;
    String raw;

    public METAR() {
    }

    public METAR(String flight_rules,
                 String raw) {
        this.flight_rules = flight_rules;
        this.raw = raw;
    }

//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }

    public String getFlightRules() {
        return flight_rules;
    }

    public void setFlightRules(String flight_rules) {
        this.flight_rules = flight_rules;
    }

    public String getRaw() {
        return raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        METAR metar = (METAR) o;
        return Objects.equals(flight_rules, metar.flight_rules) && Objects.equals(raw, metar.raw);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flight_rules, raw);
    }

    @Override
    public String toString() {
        return "METAR{" +
                "flight_rules='" + flight_rules + '\'' +
                ", raw='" + raw + '\'' +
                '}';
    }
}