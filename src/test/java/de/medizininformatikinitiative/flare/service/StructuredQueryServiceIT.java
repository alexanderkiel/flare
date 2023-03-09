package de.medizininformatikinitiative.flare.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.medizininformatikinitiative.flare.FlareApplication;
import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.mapping.MappingContext;
import de.medizininformatikinitiative.flare.model.sq.ConceptCriterion;
import de.medizininformatikinitiative.flare.model.sq.StructuredQuery;
import de.numcodex.sq2cql.model.TermCodeNode;
import de.numcodex.sq2cql.model.common.TermCode;
import de.numcodex.sq2cql.model.structured_query.Concept;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Testcontainers
@SpringBootTest
class StructuredQueryServiceIT {

    private static final TermCode C71 = TermCode.of("http://fhir.de/CodeSystem/bfarm/icd-10-gm", "Z94", "");

    private static final Logger logger = LoggerFactory.getLogger(StructuredQueryServiceIT.class);

    @Container
    @SuppressWarnings("resource")
    private static final GenericContainer<?> blaze = new GenericContainer<>("samply/blaze:0.20")
            .withImagePullPolicy(PullPolicy.alwaysPull())
            .withEnv("LOG_LEVEL", "debug")
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/health").forStatusCode(200))
            .withLogConsumer(new Slf4jLogConsumer(logger));

    @Configuration
    static class Config {

        @Bean
        public WebClient dataStoreClient() {
            var host = "%s:%d".formatted(blaze.getHost(), blaze.getFirstMappedPort());
            return WebClient.builder()
                    .baseUrl("http://%s/fhir".formatted(host))
                    .defaultHeader("Accept", "application/fhir+json")
                    .defaultHeader("X-Forwarded-Host", host)
                    .build();
        }

        @Bean
        public MappingContext mappingContext() throws Exception {
            var mapper = new ObjectMapper();
            var mappings = Arrays.stream(mapper.readValue(slurp("codex-term-code-mapping.json"), Mapping[].class))
                    .collect(Collectors.toMap(Mapping::key, v -> v));
            var conceptTree = mapper.readValue(slurp("codex-code-tree.json"), TermCodeNode.class);
            return MappingContext.of(mappings, conceptTree);
        }

        @Bean
        public FhirQueryService fhirQueryService(WebClient dataStoreClient) {
            return new DataStore(dataStoreClient, 1);
        }

        @Bean
        public Translator translator(MappingContext mappingContext) {
            return new Translator(mappingContext);
        }

        @Bean
        public StructuredQueryService service(FhirQueryService fhirQueryService, Translator translator) {
            return new StructuredQueryService(fhirQueryService, translator);
        }
    }

    @Autowired
    private WebClient dataStoreClient;

    @Autowired
    private StructuredQueryService service;

    @BeforeEach
    void setUp() throws Exception {
        dataStoreClient.post()
                .contentType(APPLICATION_JSON)
                .bodyValue(slurp("test-all-attributeFilterUpdate.json"))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    @Test
    void execute() throws JsonProcessingException {
        var query = StructuredQuery.of(List.of(List.of(ConceptCriterion.of(Concept.of(C71)))));
        System.out.println("new ObjectMapper().writeValueAsString(query) = " + new ObjectMapper().writeValueAsString(query));


        var result = service.execute(query).block();

        assertThat(result).isOne();
    }

    private static String slurp(String name) throws Exception {
        return Files.readString(resourcePath(name));
    }

    private static Path resourcePath(String name) throws URISyntaxException {
        return Paths.get(Objects.requireNonNull(FlareApplication.class.getResource(name)).toURI());
    }
}
