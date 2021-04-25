# hello-codebase

Aimed end result of the [Codebase](https://hamatti.org/codebase/)
[Episode 2 live coding session](https://www.youtube.com/watch?v=7q6udGF6a28)
by [@ykarikos](https://twitter.com/ykarikos) and [@hamatti](https://twitter.com/Hamatti).

Based on the [lein-web-lite](https://github.com/kwrooijen/lein-web-lite) template that
uses [reitit](https://github.com/metosin/reitit/). 
Uses [MetaWeather API](https://www.metaweather.com/api/) as the data source.

## Prerequisities

Install
- Java
- [Leiningen](https://leiningen.org/)

## Setup

```sh
lein run
curl localhost:3000
```

Check the weather API at e.g.
http://localhost:3000/api/weather/Helsinki

## Build

```sh
lein uberjar
java -jar target/hello-codebase.jar
```

## License

Licensed with [MIT License](LICENSE).
