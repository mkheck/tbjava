package com.thehecklers.javaboot;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@SpringBootApplication
public class JavaBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaBootApplication.class, args);
    }

    @Bean
    CommandLineRunner loadData(MetarRepository repo) {
        return args -> {
//            for (int x = 1; x < 11; x++) {
//                repo.save(new METAR("VFR", "Weather summary number " + x));
//            }
            Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                    .forEach(x -> repo.save(new METAR("IFR", "Weather summary number " + x)));
        };
    }
}

@RestController
class MetarController {
    private final MetarRepository repo;

    MetarController(MetarRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    Iterable<METAR> getAllMetars() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    Optional<METAR> getMetarById(@PathVariable String id) {
        return repo.findById(id);
    }
}

interface MetarRepository extends CrudRepository<METAR, String> {}

@Document
class METAR {
    @Id
    String id;
    String flight_rules;
    String raw;

    public METAR() {
    }

    public METAR(String flight_rules,
                 String raw) {
        this.flight_rules = flight_rules;
        this.raw = raw;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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