package de.medizininformatikinitiative.flare.service;

import de.medizininformatikinitiative.flare.model.fhir.Query;
import de.medizininformatikinitiative.flare.model.mapping.MappingContext;
import de.medizininformatikinitiative.flare.model.sq.Criterion;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedCriterion;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Component
public class Translator {

    private final MappingContext mappingContext;

    public Translator(MappingContext mappingContext) {
        this.mappingContext = Objects.requireNonNull(mappingContext);
    }

    public Mono<List<Query>> toQuery(Criterion criterion) {
        return criterion.expand(mappingContext)
                .map(expandedCriteria -> expandedCriteria.stream().map(ExpandedCriterion::toQuery).toList());
    }
}
