# Суть проблемы

При нарушении работы Zeebe Operate могут быть не экспортированы модели процессов (process definition). Их уже может не быть в zeebe-records (сработал Curator), но они могут еще содержаться в самой Zeebe, во внутренней базе rockDb.

Задача этого проекта извлечь процессы из Zeebe и передать их в elasticsearch в формате, понятном для Zeebe Operate.

## Как Zeebe Operate хранит процессы

В elasticsearch есть индекс, с названием
operate-process-<номер версии>_
В нем процессы представлены в виде:
```json
{
  "id": "2251799813685254",
  "key": 2251799813685254,
  "partitionId": 1,
  "name": "\"Пустой процесс\"",
  "version": 1,
  "bpmnProcessId": "process-mock",
  "bpmnXml": "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n .. Длинный текст исходног XML процесса .. </bpmn:definitions>\n",
  "resourceName": "process-mock.bpmn",
  "flowNodes": [
    { "id": "Activity_1unr0y6", "name": null  },
    { "id": "StartEvent_1",     "name": null  },
    { "id": "Event_00iazs8",    "name": null  }
  ]
}
```
Из примера видно, что индекс хранит как исходный текст xml процесса, так и извлеченные элементы процесса в виде массива flowNodes.
Данный проект парсит xml процесса и извелекает элементы процесса, воссоздавая структуру flowNodes.


## Как это работает:

В проекте присутствует собранный [Zeebe Debugging Tool](https://github.com/Zelldon/zdb), который может обращаться непосредственно к файлам raft partition-ам Zeebe и извлекать от туда данные. Данные выгружаются в виде набора json файлов.

## Что необходимо перед работой:

* Java (минимум 17)
* JQ - терминальный процессор json файлов

## Как применять

1. Извлечь процессы в виде файлов

Для удобства был написан bash скрипт get-process.sh.
Для его использования нужно задачать JAVA_HOME (напомню, не меньше 17й версии) и передать путь к партиции Zeebe (либо к папке CURRENT, либо к определенному снепшоту).

Пример:
```bash
./get-processes.sh /opt/zeebe/data/raft-partition/partitions/1/snapshots/14477-2-14540-14539

извлекаю 2251799813692506
извлекаю 2251799813685250
извлекаю 2251799813685253
извлекаю 2251799813685252
извлекаю 2251799813685249
извлекаю 2251799813685251
извлекаю 2251799813685254
```
Процессы извлекаются в папку ./processes

2. Задать параметры в application.yml или передать любым другим удобным способом для определения url elasticsearch, индекса в который необходимо сохранить, а так же папку, где сохранены выгруженные процессы.

Пример application.yml
```yml
elasticsearch:
  url: http://localhost:9200/operate-process-8.1.8_/_doc/{id}

app:
  folder: ./processes
  partition: 1
```

3. Собрать проект с помощью maven 
```bash
mvn clean package
```

4. и запустить java -jar
```bash
java -jar ./target/cure-zeebe-operate-processes-0.0.1.jar
```