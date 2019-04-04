# content-relationships-graph

### Set up and running

First make sure sure you have Docker installed. [Download and install](https://docs.docker.com/install/).

Then install neo4j

```
docker run \
    --publish=7474:7474 --publish=7687:7687 \
    --volume=$HOME/neo4j/data:/data \
    neo4j
```

The original `username / password` pair is `neo4j / neo4j`. You will need to set your own password.

Check out the `content-relationships-graph` repository and at create the file

```
src/main/resources/application.conf
``` 

with contents 

```
capi {
  key = "[REMOVED]"
}
database {
  uri = "bolt://localhost:7687"
  user = "neo4j"
  password = "[REMOVED]"
}
```

Ask a team member for the CAPI keys and specify your neo4j password.

 