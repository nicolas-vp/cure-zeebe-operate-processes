# Суть проблемы

При нарушении работы Zeebe Operate, потери части данных индексов Zeebe Operate нарушается работа процесса импортирования. 
Проект позволяет воссоздать часть индексов Operate из доступных источников.

1. **operate-process-<версия>**
Индекс содержит определения процессов (xml код процессов и путь прохождения процессов).
Данный индекс можно извлечь напрямую из партиций Zeebe, а затем загрузить в индекс.
Процессы содержатся во внутренней базе rockDb, с помощью утилиты zdb их можно извлечь, затем данный проект их может прочитать, обработать и передать их в elasticsearch в формате, понятном для Zeebe Operate.

2. **operate-list-view-<версия>**
Индекс содержит запущенные и завершенные инстансы процесса для того, чтобы найти их в поиске.

3. **operate-flownode-instance-<версия>**
Индекс содержит элементы инстанса процесса, которые были выполнены при его прохождении. Индекс используется при просмотре конкретного инстанса процесса.

Индексы 2 и 3 заполняются при обработки записей из индекса zeebe-record_process-instance_<версия>_<дата>.

## Как Zeebe Operate хранит процессы

В elasticsearch есть индекс, с названием operate-process-<номер версии>_. 
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

* Java (минимум 17) (apt install openjdk-17-jre)
* JQ - терминальный процессор json файлов (apt install jq)
* Maven - если требуется пересобрать проект (apt install maven)

## Как применять

1. Извлечь процессы в виде файлов

Для удобства был написан bash скрипт get-process.sh.
Для его использования нужно задать JAVA_HOME (напомню, не меньше 17й версии) и передать путь к партиции Zeebe (либо к папке CURRENT, либо к определенному снепшоту).

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

2. Задать параметры в application.yml или передать любым другим удобным способом. Можно переопределить url elasticsearch, индекс в который необходимо сохранить, а так же папку, где сохранены выгруженные процессы и др.

Пример application.yml
```yml
elasticsearch:
  url: http://localhost:9200
  create-process: ${elasticsearch.url}/operate-process-8.1.8_/_doc/{id}
  get-process-instance-count: 50
  get-process-instance: ${elasticsearch.url}/zeebe-record_process-instance_8.2.*/_search?size=${elasticsearch.get-process-instance-count}
  get-process-single-instance: ${elasticsearch.url}/zeebe-record-process-instance*/_search?q=value.processInstanceKey:{id}
  get-flow-node-single: ${elasticsearch.url}/zeebe-record-process-instance*/_search?q=key:{id}
  create-list-view: ${elasticsearch.url}/operate-list-view-8.1.0_/_doc/{id}
  create-flow-node: ${elasticsearch.url}/operate-flownode-instance-8.2.0_/_doc/{id}

app:
  partition: 1
  folder: ./processes
  # Доступно 4 процедуры, по-умолчанию выключено все, нужную процедуру нужно включить соответсвующей настройкой переводом в true
  # Процедура импорта процессов
  import-process: false
  # Процедура воссоздания всех ListView
  import-list-view: false
  # Процедура импортирования одного инстанса процесса (нужно указать его ID, например 2000000000000000)
  import-single-instance: false
  import-single-instance-id: 2000000000000000
  # Процедура импортирования одного элемента инстанса процесса (нужно указать его ID, например 3000000000000000)
  import-single-flow-node: false
  import-single-flow-node-id: 3000000000000000
```

3. Собрать проект с помощью maven 
```bash
mvn clean package
```

4. Запустить java -jar

4.1. Импортирование процессов

```bash
java -jar ./target/cure-zeebe-operate-processes-0.0.1.jar --app.import-process=true
```

4.2. Автоматическое воссоздание list-view и flownode-instance.

Требуется задать количество записей по которым будет произведен поиск.

```bash
java -jar ./target/cure-zeebe-operate-processes-0.0.1.jar --app.import-list-view=true --elasticsearch.get-process-instance-count=100
```

4.3. Ипорт одного инстанса процесса

Требуется задать идентификатор инстанса.

```bash
java -jar ./target/cure-zeebe-operate-processes-0.0.1.jar --app.import-single-instance=true --app.import-single-instance-id=2000000000000000
```

4.4. Импорт одного элемента инстанса процесса

Требуется задать идентификатор элемента инстанса процесса.

```bash
java -jar ./target/cure-zeebe-operate-processes-0.0.1.jar --app.import-single-flow-node=true --app.import-single-flow-node-id=3000000000000000
```