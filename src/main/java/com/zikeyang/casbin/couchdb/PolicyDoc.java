package com.zikeyang.casbin.couchdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PolicyDoc {
    @JsonProperty("_id")
    private @SerializedName("_id") String id; // CouchDB document ID
    @JsonProperty("_rev")
    private @SerializedName("_rev") String rev; // CouchDB document revision
    private List<Policy> policies = Collections.emptyList();

    public PolicyDoc(String policy) {
        List<Policy> policies = Arrays.stream(policy.split("\n"))
                .filter(line -> !line.isEmpty())
                .map(line -> {
                    List<String> tokens = Arrays.stream(line.split(","))
                            .map(String::trim).toList();
                    return new Policy(tokens.get(0).substring(0, 1), tokens.get(0),
                            tokens.stream().skip(1).collect(Collectors.toList()));
                })
                .toList();

        this.setPolicies(policies);
    }
}
