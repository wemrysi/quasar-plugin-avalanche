# Quasar Avalanche Plugins [![Discord](https://img.shields.io/discord/373302030460125185.svg?logo=discord)](https://discord.gg/pSSqJrr)

## Datasource Configuration

JSON configuration required for constructing an Avalanche datasource.

```
{
  "connection": <connection-configuration>
}
```

* `connection`: A [connection configuration](#connection-configuration) object

## Connection Configuration

JSON configurating describing how to connect to an Avalanche cluster.

```
{
  "serverName": String,
  "databaseName": String,
  ["maxConcurrency": Number,]
  ["maxLifetimeSecs": Number,]
  ["properties": Object]
}
```

A detailed description of many of these options may be found in the [Actian documentation for JDBC DataSource properties](https://docs.actian.com/avalanche/index.html#page/Connectivity%2FData_Source_Properties.htm%23).

* `serverName`: the server hostname (or network address) and port ("serverName" DataSource property)
* `databaseName`: the name of the database to use ("databaseName" DataSource property)
* `maxConcurrency` (optional): the maximum number of simultaneous connections to the database (default: 8)
* `maxLifetimeSecs` (optional): the maximum lifetime, in seconds, of idle connections. If your database or infrastructure imposes any limit on idle connections, make sure to set this value to at most a few seconds less than the limit (default: 210 seconds)
* `properties` (optional): any of the following DataSource properties
  * `user`
  * `password`
  * `dbmsUser`
  * `dbmsPassword`
  * `groupName`
  * `roleName`
  * `vnodeUsage`
  * `encryption`
  * `compression`
