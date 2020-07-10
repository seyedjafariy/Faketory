# Faketory  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.worldsnas/faketory/badge.svg)](https://maven-badges.herokuapp.com/maven-central/cz.jirutka.rsql/rsql-parser) [![Awesome Kotlin Badge](https://kotlin.link/awesome-kotlin.svg)](https://github.com/KotlinBy/awesome-kotlin)
A generic fake factory for Java/Kotlin POJO/POKO.

This library can create an instance of your classes. The idea behind is to avoid creating instances of DTOs/Entities/Domain objects manually.

The main usecase is in tests. creating objects for tests is usually very time consuming and can deviate our attention from the tests. Changes in our POJO/POKOs will cause refactoring in our tests, even if we are not touching that part.
With Faketory you can focus on your test.


## Another mocking library?

Faketory is not a mocking library, because it creates real objects and does not control their behaviour. Also, it can not create abstract classes and interfaces (as they are usually not our real domain objects). 

## Usage

Import the dependency:

```Gradle
testImplementation "com.worldsnas:faketory:$latestVersion"
```

Start creating objects:

### Kotlin

```Kotlin
val objectOfYourClass = Faketory.create<YourClass>()
```

### Java

```Java
TypeReference<YourClass> reference = new TypeReference<YourClass>() {};

YourClass objectOfYourClass = Faketory.create(reference, config);
```

For more sample please check [test directory](https://github.com/worldsnas/Faketory/blob/master/lib/src/test/kotlin/com/worldsnas/faketory/FaketoryTest.kt).

#### Important: 
This library is mainly meant for tests 

### Configuration

This library is highly customizable. With the help of the Config object, you can customize every step of the object creation process.
The [default behavior](https://github.com/worldsnas/Faketory/blob/baaf2accb1462887feae8fb1f60701b79280e9c4/lib/src/main/kotlin/com/worldsnas/faketory/Faketory.kt#L103) is configured with static fields and generators to avoid extra object creation and speed up the whole instance creation.

You can override the default behavior in two ways:

1. Setting a new `Config` to static `Faketory.defaultConfig` field. You can use Kotlin `copy` function to avoid passing all the parameters:

```Kotlin
Faketory.defaultConfig = Faketory.defaultConfig.copy(
            useDefaultConstructor = false,
            setNull = false,
            useSubObjectsForSealeds = false
        )
```

This approach is good to setup Faketory once (for example: in @BeforeClass) and run all the tests with it.

2. If you want more control for each test, you can pass your new `Config` object to the `Faketory.create` function:

```Kotlin
val actual = Faketory.create<ClassWithDefault>(
            Faketory.defaultConfig.copy(useDefaultConstructor = true)
        )
```

#### Important:
Avoid using heavy and time-consuming generators as they will increase your test time


## Inspired by

Python [Model Bakery](https://pypi.org/project/model-bakery/)

## Contribution

I'm more than happy to discuss, so:

- Create issues
- Send PRs

Any contribution is more than welcome

## License

Faketory is [MIT-licensed](/LICENSE).
