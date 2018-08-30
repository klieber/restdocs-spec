package com.berkleytechnologyservices.restdocs.spec.generator.openapi_v2;

import com.berkleytechnologyservices.restdocs.spec.ApiDetails;
import com.berkleytechnologyservices.restdocs.spec.Specification;
import com.berkleytechnologyservices.restdocs.spec.SpecificationFormat;
import com.berkleytechnologyservices.restdocs.spec.generator.SpecificationGenerator;
import com.berkleytechnologyservices.restdocs.spec.generator.SpecificationGeneratorException;
import com.epages.restdocs.openapi.generator.OpenApi20Generator;
import com.epages.restdocs.openapi.generator.ResourceModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.models.Swagger;
import io.swagger.util.Json;
import io.swagger.util.Yaml;

import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named
public class OpenApi20SpecificationGenerator implements SpecificationGenerator {

  private static final JsonProcessingFunction YAML_GENERATOR = (swagger) -> Yaml.pretty().writeValueAsString(swagger);
  private static final JsonProcessingFunction JSON_GENERATOR = (swagger) -> Json.pretty().writeValueAsString(swagger);

  private static final Map<SpecificationFormat, JsonProcessingFunction> FORMAT_GENERATORS = createFormatGeneratorsMap();

  @Override
  public Specification getSpecification() {
    return Specification.OPENAPI_V2;
  }

  @Override
  public String generate(ApiDetails details, List<ResourceModel> models) throws SpecificationGeneratorException {
    Swagger spec = OpenApi20Generator.INSTANCE.generate(
        models,
        details.getBasePath(),
        details.getHost(),
        details.getSchemes(),
        details.getName(),
        details.getVersion(),
        null
    );

    try {
      return FORMAT_GENERATORS.getOrDefault(details.getFormat(), YAML_GENERATOR).apply(spec);
    } catch (JsonProcessingException e) {
      throw new SpecificationGeneratorException("Unable to generate specification.", e);
    }
  }

  @FunctionalInterface
  interface JsonProcessingFunction {
    String apply(Swagger t) throws JsonProcessingException;
  }

  private static Map<SpecificationFormat, JsonProcessingFunction> createFormatGeneratorsMap() {
    Map<SpecificationFormat, JsonProcessingFunction> generators = new HashMap<>();
    generators.put(SpecificationFormat.YAML, YAML_GENERATOR);
    generators.put(SpecificationFormat.JSON, JSON_GENERATOR);
    return generators;
  }
}
