# Подсистема запуска отложенных задач (version 1.0)
> 📄 [Описание задачи](Описание.html)

<a name="содержание"></a>
## 📚 Содержание

- [1. Установка и настройка](#1-установка-и-настройка)
- [2. Формирование своей задачи](#2-формирование-своей-задачи)
- [3. Работа с задачами через REST API](#3-работа-с-задачами-через-rest-api)
  - [3.1. Создание задачи (POST /tasks)](#31)
  - [3.2. Получение информации о задаче (GET /tasks/{id}?category=...)](#32)
  - [3.3. Отмена задачи (DELETE /tasks/{id}?category=...)](#33)
  - [3.4. Перезагрузка задач в категории (POST /tasks/reload?category=...)](#34)
  - [3.5. Инициализация пула для категории (POST /tasks/init-pool?category=...)](#35)
- [Диаграмма классов](#диаграмма-классов)
- [Повторные попытки (Retry Policy)](#️-повторные-попытки-retry-policy)
- [Изоляция по категориям](#изоляция)
- [Структура проекта](#структура)
- [Чеклист по Описанию задачи](#чеклист-по-описанию-задачи)

---

## Основные возможности

- **Создание пользовательских задач с различными параметрами (наследуемые Java-классы с логикой)**
    Реализация через наследование от `AbstractTask`, с возможностью передачи параметров.
- **Отложенное выполнение**
    Запуск задачи в строго заданное время (`scheduledTime`), независимо от количества экземпляров приложения.
- **Изоляция по категориям** 
    Каждая категория — это отдельная таблица в базе данных и независимый пул потоков с настраиваемым размером (по умолчанию — 4 потока).
- **Многопоточность и безопасность**  
  Параллельная обработка задач без конфликтов. Используется SQL-блокировка для предотвращения дублирующего выполнения при работе в нескольких инстансах.
- **Возможность повторного запуска задачи в случае неудачного выполнения**
  Политика повторов с возможностью настройки стратегий:
  - `CONSTANT` — фиксированная задержка между попытками (постоянная величина в миллисекундах);
  - `EXPONENT` — экспоненциальная функция `y = a^x`, где `y` - время, через которое нужно повторить попытку, `a` - основание (по умолчанию это число Эйлера `e`), `x` - номер попытки (1,2,3..).
- **Отмена задач до исполнения**
    Возможность отмены задачи через REST-запрос, если время её выполнения не наступило.
- **Масштабирование — задачи не дублируются при нескольких инстансах**
- **Простейший REST API для создания, отмены и получения статуса задачи**

---

## 🛠️ Стек технологий

- **Java** — основной язык разработки
- **IntelliJ IDEA Ultimate Edition 2025.1** — основная IDE для разработки 
- **Spring Boot 3** — каркас приложения  
- **Spring JDBC** — работа с базой данных (динамические таблицы)
- **MySQL Workbench 8.0 CE** — БД, хранилище задач (отдельные таблицы по категориям)  
- **HikariCP** — пул соединений к БД  
- **REST (Spring Web)** — интерфейс для взаимодействия с системой  
- **Jackson** — сериализация параметров задач  
- **Maven** — управление зависимостями  
- **SLF4J + Logback** — логирование  
- **Postman App** - тестирование REST API
- **Git** - для контроля версий
- **JMX** - мониторинг активностей

---
## 📖 Как пользоваться

### 1. Установка и настройка

1. Клонировать проект
```
git clone https://github.com/RHT-dev/Practice-Delayed-Task-System.git
```

2. Установить все необходимые зависимости.


3. Настроить подключение к БД (MySQL)
- В `application.properties` есть пример подключения к локальной (тестовой) БД.

> **[⬆️ Вернуться к содержанию](#содержание)**

### 2. Формирование своей задачи

1. Создать Java-класс, унаследованный от `AbstractTask`.
2. Переопределить метод `execute(Map<String, Object> params)`.
3. Сделать аннотацию `@Component` к классу, чтобы Spring мог его найти.

```java
@Component
public class MyCustomTask extends AbstractTask {
    @Override
    public void execute(Map<String, Object> params) {
        String message = (String) params.get("msg");
        System.out.println("Running my task: " + message);
    }
}
```
> **[⬆️ Вернуться к содержанию](#содержание)**

### 3. Работа с задачами через REST API

Для отправки запросов необходимо запустить проект. Все запросы в рамках тестирования проводились c помощью приложения Postman.
<a name="31"></a>
#### 🔄 3.1. Создание задачи (POST `/tasks`). Примеры.

### **Endpoint:**  
    POST http://localhost:8080/tasks

### **Headers:** 
    Content-Type: application/json

### **JSON Body:**

##### — CONSTANT (без исключений)
```json
{
  "taskClassName": "com.example.demo.task.SampleTask",
  "paramsJSON": "{\"message\": \"Hi!\"}",
  "retryParamsJSON": "{\"delay\": 5000}",
  "retryType": "CONSTANT",
  "category": "send_push",
  "maxAttempts": 3,
  "scheduledTime": "2025-05-22T21:48:00"
}
```

##### — CONSTANT (с исключением — тестирование повтора)
```json
{
  "taskClassName": "com.example.demo.task.SampleTaskWithException",
  "paramsJSON": "{\"message\": \"Hi!\"}",
  "retryParamsJSON": "{\"delay\": 5000}",
  "retryType": "CONSTANT",
  "category": "send_push",
  "maxAttempts": 3,
  "scheduledTime": "2025-05-22T21:48:00"
}
```
##### — CONSTANT (без scheduledTime — запускается сразу)
```json
{
  "taskClassName": "com.example.demo.task.SampleTask",
  "paramsJSON": "{\"message\": \"Hi!\"}",
  "retryParamsJSON": "{\"delay\": 5000}",
  "retryType": "CONSTANT",
  "category": "send_push",
  "maxAttempts": 3
}
```
##### — EXPONENT (без исключений)
```json
{
  "taskClassName": "com.example.demo.task.SampleTask",
  "paramsJSON": "{\"message\": \"Hello\"}",
  "retryParamsJSON": "{\"exponent\": 5, \"maxDelay\": 15000}",
  "retryType": "EXPONENT",
  "category": "send_push",
  "maxAttempts": 5,
  "scheduledTime": "2025-05-22T21:48:00"
}
```

##### — EXPONENT (с исключением — тестирование повтора)
```json
{
  "taskClassName": "com.example.demo.task.SampleTaskWithException",
  "paramsJSON": "{\"message\": \"Hello\"}",
  "retryParamsJSON": "{\"exponent\": 5, \"maxDelay\": 15000}",
  "retryType": "EXPONENT",
  "category": "send_push",
  "maxAttempts": 5,
  "scheduledTime": "2025-05-22T21:48:00"
}
```
##### — EXPONENT (без scheduledTime — запускается сразу)
```json
{
  "taskClassName": "com.example.demo.task.SampleTask",
  "paramsJSON": "{\"message\": \"Hello\"}",
  "retryParamsJSON": "{\"exponent\": 5, \"maxDelay\": 15000}",
  "retryType": "EXPONENT",
  "category": "send_push",
  "maxAttempts": 5
}
```
##### — Без retryType и retryParamsJSON (без повторных попыток)
```json
{
  "taskClassName": "com.example.demo.task.SampleTaskWithException",
  "paramsJSON": "{\"message\": \"Hi!\"}",
  "category": "send_push",
  "maxAttempts": 3
}
```

### 📌 Поля запроса для создания задачи (POST /tasks)

| Поле              | Тип       | Обязательное | Описание                                                                 |
|-------------------|-----------|---------------|--------------------------------------------------------------------------|
| `taskClassName`   | string    | ✅            | Полное имя Java-класса, реализующего задачу (`implements AbstractTask`) Обязателен — иначе невозможно выполнить через `Class.forName()`. |
| `category`        | string    | ✅            | Название категории (используется для изоляции в пул и таблицу)           |
| `paramsJSON`      | string    | ❌            | JSON-строка с параметрами, передаваемыми в метод `execute()`. Может быть `null`.           |
| `scheduledTime`   | ISO-8601  | ❌            | Время запуска задачи (Если не указано — будет проставлено `LocalDateTime.now()`)            |
| `maxAttempts`     | integer   | ❌            | Максимальное количество попыток при ошибке (по умолчанию `1`)            |
| `retryType`       | string    | ❌            | Тип стратегии повтора: `CONSTANT` или `EXPONENT`. Если ничего не передавать, то retryType примет тип NONE - т.е БЕЗ повторных попыток  |
| `retryParamsJSON` | string    | ❌            | JSON-строка с параметрами повтора. Может быть `null`, но если `retryType` задан — желательно указывать нужные параметры:<br>🔹 `delay` — для CONSTANT<br>🔹 `exponent`, `maxDelay` — для EXPONENT.  |

#### Дополнительные замечания
- Повторные попытки активируются при неудачном выполнении.
- Если не задавать тип политики повторов и её параметры, то примется значение по умолчанию - `RetryType.NONE`. Это значит, что в случае неудачного выполнения повторные попытки выполняться НЕ будут.
> **[⬆️ Вернуться к содержанию](#содержание)**



<a name="32"></a>
#### 🔄 3.2. Получение информации о задачи (`GET /tasks/{id}?category={название вашей категории}`). Примеры.

**Параметры:**

| Название   | Где передаётся | Тип    | Обязательно | Описание                                |
|------------|----------------|--------|-------------|-----------------------------------------|
| `id`       | path           | long   | ✅           | Идентификатор задачи                    |
| `category` | query param    | string | ✅           | Имя категории, в которой создана задача |


**Запрос:**
```http
GET /tasks/1?category=email
```
- Запрос возвращает информацию о задачи по её `id` и `category`.

**Ответ:**
```json
{
    "id": 1,
    "taskClassName": "com.example.demo.task.SampleTaskWithException",
    "category": "email",
    "paramsJSON": "{\"message\": \"Hello\"}",
    "scheduledTime": "2025-06-03T18:08:53",
    "attemptCount": 5,
    "maxAttempts": 5,
    "retryType": "CONSTANT",
    "status": "FAILED",
    "retryParamsJSON": "{\"delay\": 15000}"
}

```
- 404 Not Found — если задачи с таким id не существует в указанной категории
> **[⬆️ Вернуться к содержанию](#содержание)**


<a name="33"></a>
#### 🔄 3.3. Отмена задачи (`DELETE /tasks/{id}`). Примеры.

Отменяет задачу по её `id` и `category`.

**Важно:** отмена работает только если задача ещё не была запущена (`status = CONSIDERED`).

**Параметры:**

| Название   | Где передаётся | Тип    | Обязательно | Описание             |
|------------|----------------|--------|-------------|----------------------|
| `id`       | path           | long   | ✅           | Идентификатор задачи |
| `category` | query param    | string | ✅           | Имя категории задачи |

**Запрос:**

```http
DELETE /tasks/1?category=send_push
```

**Ответ:**
- 204 No Content — задача успешно отменена
- 404 Not Found — задача не существует или уже была запущена (status ≠ CONSIDERED)

**Замечания**:
- GET и DELETE не проверяют владельца/автора задачи — вся логика сейчас техническая и не содержит авторизации/аутентификации
> **[⬆️ Вернуться к содержанию](#содержание)**

<a name="34"></a>
#### 🔄 3.4. Перезагрузка задач в категории (`POST /tasks/reload?category={название вашей категории}`). 
Перезагружает задачи выбранной категории: задачи снова ставятся в очередь на выполнение, если их время выполнения **БОЛЬШЕ**, чем время, когда был сделан `reload` (еще не "свершившиеся задачи"). 
Задачам, у которых время **МЕНЬШЕ**, чем время запуска reload, присваивается статус `FAILED` (старые задачи, неактуальные, time expired).

Примеры.
| Название   | Где передаётся | Тип    | Обязательно | Описание             |
|------------|----------------|--------|-------------|----------------------|
| `category` | query param    | string | ✅           | Имя категории задачи |

**Запрос:**
```http
POST /tasks/reload?category=some_test
```

**Ответ:**
```json
Reloaded 16 tasks for category some_test
```
> **[⬆️ Вернуться к содержанию](#содержание)**


<a name="35"></a>
#### 🔄 3.5. Инициализация пула для категории (`POST /tasks/init-pool?category={название вашей категории}`). Примеры.
Запрос, позволяющий вручную инициализировать пул для конкретной категории (в случае ошибок; UB; для отладочного тестирования)

**Параметры:**
| Название   | Где передаётся | Тип    | Обязательно | Описание             |
|------------|----------------|--------|-------------|----------------------|
| `category` | query param    | string | ✅           | Имя категории задачи |

**Запрос:**
```http
POST /tasks/init-pool?category=some_test
```

**Ответ:**
```json
Pool initialized for category: some_test
```
> **[⬆️ Вернуться к содержанию](#содержание)**


---
## Диаграмма классов

![alt text](<Java Class Diagram.png?v=2>)

> **[⬆️ Вернуться к содержанию](#содержание)**

---
## ⚙️ Повторные попытки (Retry Policy)

Задача может выполняться несколько раз, если завершилась неудачно (например, из-за исключения). Повторные попытки настраиваются через поля `retryType`, `retryParamsJSON` и `maxAttempts`.

### Поддерживаемые типы повторов:

| Тип        | Описание                                                           |
| ---------- | ------------------------------------------------------------------ |
| `CONSTANT` | Фиксированная задержка между попытками                             |
| `EXPONENT` | Экспоненциальная задержка с параметром роста и максимумом задержки |

### Параметры:

* `retryType` – тип повтора: `CONSTANT` или `EXPONENT`
* `retryParamsJSON` – параметры повтора в виде строки JSON:

  * для `CONSTANT`: `{"delay": 5000}` — задержка в миллисекундах
  * для `EXPONENT`: `{"exponent": 2, "maxDelay": 15000}` — множитель и максимум задержки
* `maxAttempts` – максимальное количество попыток, включая первую

### Поведение:

* Если `retryType` и `retryParamsJSON` **не указаны**, повторов не будет (`maxAttempts` = 1 по умолчанию и RetryType = NONE)
* Повтор сработает **только при исключении**, выброшенном внутри `execute()`
* Задача будет иметь статус `FAILED`, если попытки исчерпаны
> **[⬆️ Вернуться к содержанию](#содержание)**

---
<a name="изоляция"></a>
## 🧩 Изоляция по категориям

Каждая категория задач имеет **свою собственную таблицу в базе** и **отдельный пул воркеров**.

### Как это работает:

* При создании задачи в поле `category` указывается имя категории (например, `"send_push"`)
* Таблица создаётся автоматически с именем `task_category_<имя>` (например: `task_category_send_push`)
* Планировщик использует изолированный `WorkerPool` на каждый тип категории
* Очередь, статусы и повторные попытки не влияют друг на друга между категориями

### Особенности:

* Задачи разных типов не блокируют друг друга
* У каждой категории может быть собственная политика нагрузки
> **[⬆️ Вернуться к содержанию](#содержание)**

---
<a name="структура"></a>
## 📂 Структура проекта
```md
java/com.example.demo/
├── config/
│   └── JdbcConfig               # Конфиг подключения к БД
├── controller/
│   └── TaskController           # REST-контроллер задач
├── dto/
│   └── TaskRequest              # DTO для создания задач
├── entity/
│   ├── RetryType                # Типы стратегий повтора
│   ├── TaskEntity               # Сущность задачи
│   └── TaskStatus               # Статусы задач
├── jmx/
│   ├── TaskDispatcherMBean      # Интерфейс для корректной работы класса в JMX
│   ├── TaskDispatcherMBeanImpl  # Класс для проверки работы JMX
├── repository/
│   └── DynamicTaskTableDao      # DAO для динамических таблиц по категориям
├── retry/
│   ├── ConstantRetryPolicy      # Повтор с фиксированной задержкой
│   ├── ExponentRetryPolicy      # Повтор с экспоненциальной задержкой
│   ├── RetryPolicy              # Интерфейс повторов
│   └── RetryPolicyResolver      # Выбор стратегии повтора
├── scheduler/
│   ├── DelayedTaskWrapper       # Реализация очереди вместо опросника `FixedDelay`
│   └── TaskDispatcher           # Планировщик задач по категориям
├── service/
│   └── TaskLifecycleService     # Основная логика: create/cancel/status
├── worker/
│   ├── WorkerPool               # Пул потоков для категории
│   └── WorkerPoolRegistry       # Реестр и управление пулами
├── task/
│   ├── AbstractTask             # Базовый класс задачи
│   ├── SampleTask               # Пример простой задачи
│   └── SampleTaskWithException  # Пример задачи с ошибкой
└── DelayedTaskApplication       # Главный класс Spring Boot приложения
```
> **[⬆️ Вернуться к содержанию](#содержание)**

--- 

## Чеклист по Описанию задачи

###### 1. Я как разработчик могу реализовать бизнес-логику в скрипте (наследовавшись от класса системы отложенных задач) чтобы иметь возможность запустить её в заданное время с произвольными значениями определенных параметров

✅ Пример: 
```java
public class SampleTask implements AbstractTask {
    private static final Logger log = LoggerFactory.getLogger(SampleTask.class);
    @Override
    public void execute(Map<String, Object> params) {
        log.info("SampleTask выполняется с параметрами: {}", params);
    }
}
```
`AbstractTask` предоставляет шаблон `execute(params)`, где `params` - определенные произвольные параметры

###### 2. Я как разработчик могу инициализировать воркеры с указанием количества потоков

✅ Пример:
 ```java
 // Конструктор:
 public WorkerPool(int threads) {
        this.executor = Executors.newFixedThreadPool(threads);
  }
...
// Использование в классе WorkerPoolRegistry:
public WorkerPool getPool(String category) {
        return pools.computeIfAbsent(category.toLowerCase(Locale.ROOT),
                cat -> new WorkerPool(4)); // "классическое" значение в 4 потока
  }
```

###### 3. Я как разработчик могу создать задачу с определенными параметрами чтобы она выполнилась в заданное время
✅ Примеры REST-запросов из пунктов выше.

###### 4. Я как разработчик могу отменить ранее созданную задачу чтобы она не выполнилась в заданное время
✅ Пример:
```java
public boolean cancel(long id, String category) {
        return dao.updateStatus(id, category,
                TaskStatus.CONSIDERED, TaskStatus.CANCELED) == 1;
  }
```

###### 5. Хранение данных в бд
✅ Используется JdbcTemplate и динамически создаваемые таблицы `task_category_<category>`. Пример создания таблицы: `DynamicTaskTableDao.ensureTable(category)`

###### 6. Возможность многопоточной обработки задач
✅ Каждая категория имеет свой WorkerPool, где задачи выполняются по принципу многопоточной очереди.

###### 7. Добавление выполнения задачи на определенное время с возможностью повторного запуска в случае неудачного выполнения (по-умолчанию без повторного запуска), указывая:
- количество попыток запуска
- политика определения времени через которое повторить попытку
а) постоянная величина в миллисекундах
б) экспоненциальная функция y=a^x + возможность указать верхний предел длинее которого не будет паузы

✅ Пример:
```
"maxAttempts": 3,
"retryType": "EXPONENT",
"retryParamsJSON": "{\"exponent\": 2, \"maxDelay\": 30000}"
```
###### 8. Возможность делить задачи на категории которые не будут никак связаны между собой (выполнение в разных пулах, хранение в разных таблицах)

✅ Задачи хранятся в разных таблицах в зависимости от категорий. Для каждой категории работает свой отдельный WorkerPool. Пример: категории ``email``, ``notifications`` — каждая работает независимо со своим WorkerPool.

###### 9. Возможность разными экземплярами приложения обрабатывать задачи таким образом, чтобы одна и та же задача гарантировано обрабатывалась только одним обработчиком
✅ При извлечении задачи из таблицы в методе fetchReadyTask(...) используется SQL-запрос:
```sql
SELECT id FROM task_category_<category>
WHERE status = 'CONSIDERED' AND scheduled_time <= ?
ORDER BY scheduled_time ASC
LIMIT 1
FOR UPDATE
```
`FOR UPDATE`:
- Блокирует выбранную строку на уровне СУБД (MySQL) для текущей транзакции.
- Пока один экземпляр держит блокировку, другие не могут получить ту же задачу.
- Блокировка снимается только при завершении транзакции.

> **[⬆️ Вернуться к содержанию](#содержание)**
