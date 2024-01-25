package io.github.java_casbin.couchdb.filter;

import io.github.java_casbin.couchdb.Policy;

public interface Filter {
    boolean filterPolicy(Policy policy);
}
