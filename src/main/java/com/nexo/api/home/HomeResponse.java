package com.nexo.api.home;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HomeResponse {
  @JsonProperty("name")
  private String status;

  @JsonProperty("tagline")
  private String tagline;

  @JsonProperty("version")
  private VersionInfo version;

  @JsonProperty("build-timestamp")
  private String buildTimestamp;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class VersionInfo {
    @JsonProperty("nexo")
    private String nexo;

    @JsonProperty("tantivy")
    private String tantivy;
  }
}
