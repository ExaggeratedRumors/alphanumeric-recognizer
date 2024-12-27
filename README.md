# # alphanumeric-recognizer

![Android support](https://shields.io/badge/Kotlin-2.0-green) ![Model](https://shields.io/badge/JVM-20-purple) ![1.0-b](https://shields.io/badge/1.0b-blue)

Kotlin no-frameworks approach system classifying alphanumeric characters in images.


## Release

`
1.0 beta
`

## Technologies

- Gradle 8.7
- JVM 20
- Kotlin 2.0

## Images data

Source: <a href="https://www.kaggle.com/datasets/crawford/emnist/data">EMNIST</a>

Data is saved in `/data` directory.

## Executing

1. Make sure your JAVA_HOME is set to JDK 20.
2. Clone the repository:
```agsl
https://github.com/ExaggeratedRumors/alphanumeric-recognizer.git
```
3. Download EMNIST data and unzip in `/data` directory (only if training/ test):
4. Model training task:
```agsl
./gradlew trainModel --args='NEW_MODEL_NAME EPOCHS AMOUNT'

example:
./gradlew trainModel --args='balanced_50e_1c_1d 50 10000'
```
5. Model test task:
```agsl
./gradlew testModel --args='MODEL_NAME AMOUNT'

example:
./gradlew testModel --args='balanced_50e_1c_1d 10000'
```
6. Server run task:
```agsl
./gradlew runServer --args='PORT'

example:
./gradlew runServer --args='8080'
```
7. Output model data is saved in `/models` directory. Every model contains `.metadata` file with hyperparameters and `.model` file with weights.


## Endpoints

- GET `/models` - list all models with parameters.
- GET `/data` - list all available training and test data.
- GET `/status/{modelName}` - list status of training model.
- POST `/train` - train new model by parameters and available layers (Input, Conv, MaxPool, Flatten, Dense, Dropout):
```http
Headers:
Content-Type: application/json

Example content:
{
    "modelName": "testModel",
    "trainDataPath": "emnist-balanced-train-images-idx3-ubyte",
    "trainLabelsPath": "emnist-balanced-train-labels-idx1-ubyte",
    "trainDataSize": 1000,
    "testDataPath": "emnist-balanced-test-images-idx3-ubyte",
    "testLabelsPath": "emnist-balanced-test-labels-idx1-ubyte",
    "testDataSize": 1000,
    "epochs": 100,
    "learningRate": 0.01,
    "batchSize": 1,
    "layers": [
        {
            "type": "Input",
            "height": 28,
            "width": 28,
            "channels": 1
        },
        {
            "type": "Conv",
            "filtersAmount": 8,
            "kernel": 3,
            "stride": 1,
            "padding": 0,
            "activation": "relu",
            "weightRange": 0.01
        },
        {
            "type": "MaxPool",
            "poolSize": 2,
            "stride": 2
        },
        {
          "type": "Flatten"
        },
        {
            "type": "Dense",
            "neurons": 10,
            "activation": "relu",
            "weightRange": 0.01
        },
        {
            "type": "Dropout",
            "rate": 0.1
        },
        {
            "type": "Dense",
            "neurons": 47,
            "activation": "softmax"
        }
    ]
}
```
Available layers:
```agsl
- Input
- Conv
- MaxPool
- Flatten
- Dense
- Dropout

```

- POST `/classify` - classify image:
```bash
Headers:
Content-Type: image/png
Model-Name: MODEL_NAME

Content:
< ./image.png
```
- DELETE `/model/{name}` - delete model by name.