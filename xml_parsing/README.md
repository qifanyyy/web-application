# xml_parsing

## Build

```shell script
mvn package
```

## Run

```shell script
java -ea -cp target/xml_parsing-1.0-SNAPSHOT-jar-with-dependencies.jar Main \
    2> report/inconsistency.txt > report/out.txt
```
