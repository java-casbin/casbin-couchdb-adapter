package com.zikeyang.casbin.couchdb.filter;

import com.zikeyang.casbin.couchdb.Policy;

public interface Filter {
    boolean filterPolicy(Policy policy);
}
