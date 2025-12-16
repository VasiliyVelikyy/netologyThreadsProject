Запуск docker compose
cd C:\Users\vasia\IdeaProjects\netologiaBankApp
docker compose up -d


Prometheus → http://localhost:9090
Grafana → http://localhost:3000 (логин: admin, пароль: admin)

http://localhost:8080/h2-console

http://localhost:8080/hello

http://localhost:8080/start-demon

http://localhost:8080/process-runnable

http://localhost:8080/process-waiting

http://localhost:8080/process-blocked

http://localhost:8080/process-park

### Сравнение производительности стримов и параллельных стримов

http://localhost:8080/start-stream
http://localhost:8080/start-parallel-stream
http://localhost:8080/start-parallel-stream/block
http://localhost:8080/start-fork-join-parallel-stream

### Примеры проблем многопоточности

http://localhost:8080/race-condition
http://localhost:8080/race-condition/sync
http://localhost:8080/transfer-deadlock
http://localhost:8080/transfer-livelock
http://localhost:8080/transfer-starvation

### Примеры Semaphore и lock

http://localhost:8080/process-withdrawal
http://localhost:8080/process-sms-notification
http://localhost:8080/process-semaphore-withdrawal
http://localhost:8080/process-withdraw-deposit-process
http://localhost:8080/process-downgrade

### Примеры Volatile

http://localhost:8080/write-and-read-volatile
http://localhost:8080/volatile-race-condition

### Примеры Atomic

http://localhost:8080/atomic-examples
http://localhost:8080/test-speed-sync-atomic
http://localhost:8080/atomic-reference


### Полная инфа клиента
http://localhost:8080/api/clients-full
http://localhost:8080/api/clients-full-with-email
http://localhost:8080/api/clients-full-with-email/with-virtual-threads
http://localhost:8080/api/clients-invoke-by-timeout
http://localhost:8080/api/clients-full-cancel


### forkjoin
http://localhost:8080/api/get-sum/all/iteration
http://localhost:8080/api/get-sum/all/recursive
http://localhost:8080/api/interest/all



http://localhost:8080/api/demo/vt

ПРоблема: почему error не показывает ошибки - старый запрос

```sum(rate(http_server_requests_seconds_count{application="$application", instance="$instance", status=~"5.."}[1m])) ```

Нужно выполнить в прометеусе http_server_requests_seconds_count{status="500"}
Посмотреть, какие label’ы есть у результата. Там нет label application
Вариант 1 (быстрый): Уберите фильтрацию по application и instance

``` sum(rate(http_server_requests_seconds_count{status=~"5.."}[1m])) ```
 
для графика rate выставить ```sum(rate(http_server_requests_seconds_count[1m]))```
запрос на количество ошибок за час  ```sum(increase(http_server_requests_seconds_count{status=~"5.."}[1h]))```

duration было 
```
sum(rate(http_server_requests_seconds_sum{application="$application", instance="$instance", status!~"5.."}[1m]))/sum(rate(http_server_requests_seconds_count{application="$application", instance="$instance", status!~"5.."}[1m]))
```
стало 

```
sum(rate(http_server_requests_seconds_sum{status!~"5.."}[1m]))
/
sum(rate(http_server_requests_seconds_count{status!~"5.."}[1m]))```


sum(jvm_memory_used_bytes{application="$application", instance="$instance", area="heap"})*100/sum(jvm_memory_max_bytes{application="$application",instance="$instance", area="heap"})
```


### Фронт
http://localhost:8080/ агрегация данных
http://localhost:8080/live.html → новый live-мониторинг




### Демонстрация проблем jpa
http://localhost:8080/jpa/problems/accounts-bad
http://localhost:8080/jpa/problems/accounts-good

POST  http://localhost:8080/jpa/problems/create-acc

Batch examples GET http://localhost:8080/import/batch?count=1000
Замер через wrk когда batch25 и 1
wrk -t1 -c1 -d10s "http://host.docker.internal:8080/import/batch?count=1000"


http://localhost:8080/mongo/init

wrk -t4 -c10 -d30s --latency "http://host.docker.internal:8080/comparison/sql/account/ACC001"
wrk -t4 -c10 -d30s --latency "http://host.docker.internal:8080/comparison/mongo/account/ACC001"
