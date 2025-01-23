# # alphanumeric-recognizer

![Kotlin](https://shields.io/badge/Kotlin-2.0-green) ![Model](https://shields.io/badge/JVM-20-purple) ![1.0-b](https://shields.io/badge/1.0b-blue)

Kotlin no-frameworks approach system classifying alphanumeric characters in images.


## Release

`
1.0 beta
`

## Modules

- server - RESTful API for operating the classifier models
- client - API demonstration

## Execution

1. Clone the repository:
```agsl
https://github.com/ExaggeratedRumors/alphanumeric-recognizer.git
```

2. Download <a href="https://www.kaggle.com/api/v1/datasets/download/crawford/emnist">EMNIST</a> data and unzip in `/data` directory.
```agsl
#!/bin/bash
curl -L -o ~/Downloads/emnist.zip\
  https://www.kaggle.com/api/v1/datasets/download/crawford/emnist
```

3. Build project using Docker (in project root directory):
```agsl
docker compose up
```