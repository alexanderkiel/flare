package de.medizininformatikinitiative.flare.model.sq;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import de.medizininformatikinitiative.flare.model.mapping.Mapping;
import de.medizininformatikinitiative.flare.model.mapping.ValueMappingNotFoundException;
import de.medizininformatikinitiative.flare.model.sq.expanded.ExpandedFilter;
import reactor.core.publisher.Mono;

import java.util.List;

import static java.util.Objects.requireNonNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ValueFilter(FilterPart filterPart) implements Filter {

    public ValueFilter {
        requireNonNull(filterPart);
    }

    public static ValueFilter ofConcept(TermCode firstConcept, TermCode... otherConcepts) {
        var filterPart = ConceptFilterPart.of(firstConcept);
        for (TermCode concept : otherConcepts) {
            filterPart = filterPart.appendConcept(concept);
        }
        return new ValueFilter(filterPart);
    }

    /**
     * Parses an attribute filterPart.
     *
     * @param node the JSON representation of the attribute filterPart
     * @return the parsed attribute filterPart
     * @throws IllegalArgumentException if the JSON isn't valid
     */
    public static ValueFilter fromJsonNode(JsonNode node) {
        return new ValueFilter(FilterPart.fromJsonNode(node));
    }

    @Override
    public Mono<List<ExpandedFilter>> expand(Mapping mapping) {
        return mapping.valueFilterMapping()
                .map(filterPart::expand)
                .orElseGet(() -> Mono.error(new ValueMappingNotFoundException(mapping.key())));
    }
}
