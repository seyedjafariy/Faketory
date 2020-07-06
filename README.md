# Faketory  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.worldsnas/faketory/badge.svg)](https://maven-badges.herokuapp.com/maven-central/cz.jirutka.rsql/rsql-parser)
A generic fake factory for Java/Kotlin POJO/POKO.

This library can create an instance of your classes. The idea behind is to avoid creating instances of DTOs/Entities/Domain objects manually.

The main usecase is in tests. creating objects for tests is usually very time consuming and can deviate our attention from the tests. Changes in our POJO/POKOs will cause refactoring in our tests, even if we are not touching that part.
With Faketory you can focus on your test.


## Another mocking library?

Faketory is not a mocking library, because it creates real objects and does not control their behaviour. Also, it can not create abstract classes and interfaces (as they are usually not our real domain objects). 

## Usage

Import the dependency:

```
testImplementation "com.worldsnas:faketory:$latestVersion"
```

Start creating objects:

### Kotlin
```

val objectOfYourClass = Faketory.create<YourClass>()

```

### Java
```

TypeReference<YourClass> reference = new TypeReference<YourClass>() {};

YourClass objectOfYourClass = Faketory.create(reference);

```

For more sample please check [test directory](https://github.com/worldsnas/Faketory/blob/master/lib/src/test/kotlin/com/worldsnas/faketory/FaketoryTest.kt).

#### Important: 
This library is meant for tests mainly


## Inspired by

Python [Model Bakery](https://pypi.org/project/model-bakery/)

## Contribution

I'm more than happy to discuss, so:

- Create issues
- Send PRs

Any contribution is more than wellcome

## License

Faketory is [MIT-licensed](/LICENSE).
