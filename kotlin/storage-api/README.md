# simple-storage
A simple REST/GraphQL based service for storing metadata on top of S3 or compatible file storage.

## Dependencies
 - Java 1.8
 - Kotlin 1.3
 - PostgreSql
 - S3 compatible service such as Minio https://github.com/minio/minio
 
## Build Deployment
Start Minio:
```
brew install minio/stable/minio
./minio server /data
```

## Setup Credentials
```
export S3_BUCKET=artifacts
export S3_PREFIX=temps
export S3_REGION=
export S3_ENDPOINT=http://127.0.0.1:9000
export S3_EXPIRE_MINUTES=1000
export AWS_ACCESS_KEY_ID=See Minio logs
export AWS_SECRET_ACCESS_KEY=See Minio logs
```

## Start server
```
./gradlew bootRun
```

## Data structure
Each file is stored as artifact with following attributes:
- organization: organization name/id for the artifact
- system: system name/id for the artifact
- subsystem: subsystem/job/group for the artifact
- name : name of the artifact
- username: username for the artifact
- contentType: contentType of the artifact
- size : size of the artifact
- platform: platform of the artifact
- digest: sha256 hash
- userAgent: sender's id
- labels: list of searchable lables
- properties: dictionary of user-defined properties (string type)

**Note:** Combination of organization, system, subsystem and name should be globally unique.

## APIs

### Upload File
Create multipart file
```
curl -F "labels=search1,search2" -F "prop1=value1" -F "file=@test.jpg" http://localhost:8080/api/myorg/bigsystem/job1
```
returns
```
{"organization":"myorg","system":"bigsystem","subsystem":"job1","name":"test.jpg","digest":"f30fd292876e1e2af8bdff22b5df2237abdb83073575a065c12cf814b3071646","size":34516,"platform":null,"username":"","contentType":"image/jpeg","userAgent":"curl/7.52.1","labels":["search1","search2"],"properties":{"prop1":"value1"},"createdAt":1548036745850,"updatedAt":1548036745850,"id":"8365f12d66fb94de655bde3494b362326849ea2b8c3c471cad14d78d6721d1b7"}
```
Note: The artifact is immutable except labels/properties but you can overwrite it by uploading it again with overwrite=true parameter

### Update properties and labels
```
curl -X PUT -F "labels=brand1,brand2" -F "prop2=value2" http://localhost:8080/api/8365f12d66fb94de655bde3494b362326849ea2b8c3c471cad14d78d6721d1b7
```
returns
```
{"organization":"myorg","system":"bigsystem","subsystem":"job1","name":"test.jpg","digest":"f30fd292876e1e2af8bdff22b5df2237abdb83073575a065c12cf814b3071646","size":34516,"platform":null,"username":"","contentType":"image/jpeg","userAgent":"curl/7.52.1","labels":["brand1","brand2","search1","search2"],"properties":{"prop1":"value1","prop2":"value2"},"createdAt":1548036745850,"updatedAt":1548036745850,"id":"8365f12d66fb94de655bde3494b362326849ea2b8c3c471cad14d78d6721d1b7"}
```
**Note** You have to pass id of artifact such as 8365f12d66fb94de655bde3494b362326849ea2b8c3c471cad14d78d6721d1b7
### View Artifact metadata
```
curl  http://localhost:8080/api/8365f12d66fb94de655bde3494b362326849ea2b8c3c471cad14d78d6721d1b7
```
returns
```
{"organization":"myorg","system":"bigsystem","subsystem":"job1","name":"test.jpg","digest":"f30fd292876e1e2af8bdff22b5df2237abdb83073575a065c12cf814b3071646","size":34516,"platform":null,"username":"","contentType":"image/jpeg","userAgent":"curl/7.52.1","labels":["brand1","brand2","search1","search2"],"properties":{"prop1":"value1","prop2":"value2"},"createdAt":1548036745850,"updatedAt":1548036745850,"id":"8365f12d66fb94de655bde3494b362326849ea2b8c3c471cad14d78d6721d1b7"}
```


### View Artifact metadata for a org/system/subsystem
```
curl http://localhost:8080/api/myorg/bigsystem/job1
```
returns
```
[{"organization":"myorg","system":"bigsystem","subsystem":"job1","name":"test.jpg","digest":"f30fd292876e1e2af8bdff22b5df2237abdb83073575a065c12cf814b3071646","size":34516,"platform":null,"username":"","contentType":"image/jpeg","userAgent":"curl/7.52.1","labels":["brand1","brand2","search1","search2"],"properties":{"prop1":"value1","prop2":"value2"},"createdAt":1548036745850,"updatedAt":1548036745850,"id":"8365f12d66fb94de655bde3494b362326849ea2b8c3c471cad14d78d6721d1b7"}]
```
**Note**: Above API returns list of artifacts

### Download File
```
curl http://localhost:8080/api/8365f12d66fb94de655bde3494b362326849ea2b8c3c471cad14d78d6721d1b7/download
```
**Note** You have to pass id of artifact such as 8365f12d66fb94de655bde3494b362326849ea2b8c3c471cad14d78d6721d1b7

### Query
```
curl http://localhost:8080/api?organization=myorg&subsystem=job1
```
returns paginated result
```
{"pageNumber":1,"pageSize":20,"totalRecords":1,"records":[{"organization":"myorg","system":"bigsystem","subsystem":"job1","name":"test.jpg","digest":"f30fd292876e1e2af8bdff22b5df2237abdb83073575a065c12cf814b3071646","size":34516,"platform":null,"username":"","contentType":"image/jpeg","userAgent":"curl/7.52.1","labels":["brand1","brand2","search1","search2"],"properties":{"prop1":"value1","prop2":"value2"},"createdAt":1548036745850,"updatedAt":1548036745850,"id":"8365f12d66fb94de655bde3494b362326849ea2b8c3c471cad14d78d6721d1b7"}]}
```
### GraphQL
The endpoint is http://localhost:8080/api/gq
Sample Query
```
{
  query {
    page
    pageSize
    totalRecords
    records {
      organization
      system
      subsystem
      name
      username
      userAgent
      name
      labels
      properties {
        key
        value
      }
    }
  }
}
```
Full Schema is available in github

