<!--
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

# Casbin Couchdb Adapter

`casbin-couchdb-adapter` is an adapter for [jCasbin](https://github.com/casbin/jcasbin), a robust authorization library
that supports access control models like ACL, RBAC, ABAC in Java, using [CouchDB](https://couchdb.apache.org/) as a
storage backend.

## Features

- Efficient and secure data handling with CouchDB.
- Easy integration with jCasbin.
- Scalable for large datasets.
- Support for all jCasbin models (ACL, RBAC, ABAC).

## Build

Requirements:
- JDK 17

```bash
./gradlew build
```

## Installation

To install the `casbin-couchdb-adapter`, add the following dependency to your `build.gradle` file:

```groovy
dependencies {
    implementation 'io.github.java-casbin:casbin-couchdb-adapter'
}
```

Or if you are using Maven, add the following dependency to your `pom.xml` file:

```xml
<dependency>
    <groupId>io.github.java-casbin</groupId>
    <artifactId>casbin-couchdb-adapter</artifactId>
</dependency>
```

## Usage

Here's a basic example of how to use the `casbin-couchdb-adapter` with jCasbin:

```java
import org.casbin.jcasbin.main.Enforcer;
import com.github.yourusername.casbin.CouchDBAdapter;

public class Main {
    public static void main(String[] args) {
        CouchDbProperties properties = new CouchDbProperties()
                .setDbName("test_db")
                .setCreateDbIfNotExist(true)
                .setUsername("admin")
                .setPassword("admin")
                .setProtocol("http")
                .setHost("localhost")
                .setPort(5984)
                .setMaxConnections(100)
                .setConnectionTimeout(0);
        couchDbClient = new CouchDbClient(properties);
        String modelPath = "path/to/model.conf";
        CouchDBAdapter adapter = new CouchDBAdapter(couchDbClient, "policies");
        Enforcer enforcer = new Enforcer(modelPath, adapter);
        // use the enforcer...
    }
}
```

## Contributing

Contributions to `casbin-couchdb-adapter` are welcome! Please read our [contributing guidelines](CONTRIBUTING.md) before
getting started.

## License

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0

## Release

- Change the version to the next release version in `build.gradle`.
- Run `./bundle.sh` to build the bundle file. The bundle file will be located at `build/bundle.zip`.
- Upload the bundle file to https://central.sonatype.com/
- Create a new release on GitHub with the release notes
