# simple-storage
Simple implementation for storing metadata on top of S3 or compatible file storage

## Dependencies
 - Java 1.8
 - Kotlin 1.3
 - PostgreSql
 - S3 compatible service such as Minio https://github.com/minio/minio
 
## Build Deployment
Start Minio:
brew install minio/stable/minio
./minio server /data

## Setup Credentials
export S3_BUCKET=artifacts
export S3_PREFIX=temps
export S3_REGION=
export S3_ENDPOINT=http://127.0.0.1:9000
export S3_EXPIRE_MINUTES=1000
export AWS_ACCESS_KEY_ID=FGVKVN4XMIAQD68DH55S
export AWS_SECRET_ACCESS_KEY=Um30rBGfXZLJnpO9tZ1UK0woAfi+GFJsrq0JM4Rp

## Start server
./gradlew bootRun

## Data structure
Each file is stored as artifact with following attributes:
- app: application name for the artifact
- job: job/group for the artifact
- name : name of the artifact
- contentType: contentType of the artifact
- size : size of the artifact
- platform: platform of the artifact
- digest: sha256 hash
- userAgent: sender's id
- labels: list of searchable lables
- properties: dictionary of user-defined properties (string type)

## APIs

### Upload File
Create multipart file
curl -F "labels=search1,search2" -F "prop1=value1" -F "file=@/home/user1/Desktop/test.jpg" http://localhost:8080/api/myapp/myjob
returns
{"application":"myapp","job":"myjob","name":"test.jpg","digest":"f30fd","platform":null,"contentType":"application/octet-stream","size":34516,"url":"http://127.0.0.1:9000/artifacts/temps/myapp/myjob/frog.png?X-Am..","userAgent":"curl/7.52.1","createdAt":"2019-01-20T05:04:10.508+0000","updatedAt":"2019-01-20T05:04:10.508+0000","labels":["search1","search2"],"properties":{"prop1":"value1"},"id":"myapp/myjob/test.jpg"}

### View Artifact metadata
curl  http://localhost:8080/api/myapp/myjob/test.jpg
returns
{"application":"myapp","job":"myjob","name":"test.jpg","digest":"f30fd","platform":null,"contentType":"application/octet-stream","size":34516,"url":"http://127.0.0.1:9000/artifacts/temps/myapp/myjob/frog.png?X-Am..","userAgent":"curl/7.52.1","createdAt":"2019-01-20T05:04:10.508+0000","updatedAt":"2019-01-20T05:04:10.508+0000","labels":["search1","search2"],"properties":{"prop1":"value1"},"id":"myapp/myjob/test.jpg"}


### View Artifact metadata for a job
curl  http://localhost:8080/api/myapp/myjob
returns
[{"application":"myapp","job":"myjob","name":"test.jpg","digest":"f30fd","platform":null,"contentType":"application/octet-stream","size":34516,"url":"http://127.0.0.1:9000/artifacts/temps/myapp/myjob/frog.png?X-Am..","userAgent":"curl/7.52.1","createdAt":"2019-01-20T05:04:10.508+0000","updatedAt":"2019-01-20T05:04:10.508+0000","labels":["search1","search2"],"properties":{"prop1":"value1"},"id":"myapp/myjob/test.jpg"}]

### Download File
curl  http://localhost:8080/api/myapp/myjob/test.jpg/download

### Query
curl  http://localhost:8080/api?app=myapp
returns paginated result
{"pageNumber":1,"pageSize":20,"totalRecords":1,"records":[{"application":"myapp","job":"myjob","name":"test.jpg","digest":"f30fd","platform":null,"contentType":"application/octet-stream","size":34516,"url":"http://127.0.0.1:9000/artifacts/temps/myapp/myjob/test.jpg?X-...","userAgent":"curl/7.52.1","createdAt":"2019-01-20T05:04:10.508+0000","updatedAt":"2019-01-20T05:04:10.508+0000","labels":["search1","search2"],"properties":{"prop1":"value1"},"id":"myapp/myjob/test.jpg"}]}

### GraphQL
WIP

