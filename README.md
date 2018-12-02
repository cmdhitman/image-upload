# Image upload test

Тестирование загрузки картинок

## Особенности реализации

- Асинхронное сохранение картинок с помощью пула потоков
- Единая точка входа в апи (по требованию в задаче). В жизни я бы, наверное, разделил multipart загрузку в отдельный метод, так как application/json принимается сейчас в виде строки
- Graceful завершение через опцию awaitTerminationSeconds. Остальное выполняет spring - у него есть встроенные механизмы завершения gracefully
- Основной упор сделал на интеграционные тесты
- В апи возвращаю коды ошибок вместе с сообщением по умолчанию

## Как запустить 

- gradlew clean assemble
- docker-compose up --build

## Запуск тестов

gradle check

## Основные технологии 

* [Spring boot](https://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/) - Веб фреймворк
* [Thumbnailator](https://github.com/coobird/thumbnailator) - Генерация превью

