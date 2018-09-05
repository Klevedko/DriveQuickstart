Приложение отслеживает изменения файлов в Google disk в заданной папке.
При запуске jar читает историю ВСЕХ файлов и записывает в audit_results.xlsx.
Каждые 10 секунд происходит обновление файла ( если была активность в Google disk)

Под активностью понимается любое действие любого пользователя с файлами\правами.

Если замечается активность в виде раздаче прав, 
и в списке владельцев файла появляется человек НЕ из домена I-Novus ( далее - "ЧУЖОЙ" ),
происходит отправка сообщения адресату с отчетом (и активность в топе) ч\з SMTP Google.

Если у чужого забирают права-он пропадает из списка владельцев файла, письмо НЕ отправляется.

---------------------------------------------------------------------------------------
Описание классов:
1. CronBuild:
    Quartz, в нем задается класс для запуска и настройка триггера.
2. Apiv1
    Класс для работы с  Google Drive Rest Api V1, инициализирующий получение 
    токена на основе json ключа ( подготавливается заранее).
2. Apiv3
    Класс для работы с  Google Drive Rest Api V3, инициализирующий получение 
    токена на основе json ключа ( подготавливается заранее).
3. Apiv1v3cron
    Класс по очереди использует Apiv1 Apiv3 для POST запросов 
        ( получение данных и их анализ, всю логику, запись в файл, отправка Email).
4. SendMail
    Класс для настройки и отправки Email ( ч\з Google SMTP).
5. AuditMap
    Класс для хранения считываемых данных с переопределенным методом isEquals().
6. TestCheckSum 
    Проверка HASH выходного файла. Не используется.
6. CreateGoogleFile
    Отправка файла на Google Disk. Не используется.

---------------------------------------------------------------------------------------

Настройка и запуск.

Все действия уже произведены:

1. https://console.developers.google.com
    Создать проект и сгенерить в учетных данных oApp ключ.
    Скачать json . положить в папку и положить его в resources/credentials.json.
    Повторить шаг, но положить новый файл уже в resources/credentialss.json
( При чистой установке, при первом запуске jar дважды откроется 
    окно подтверждения для получения token в папки token_v1 и token_v3 )

2. Включить у проекта API:
             Admin SDK		
             Apps Activity API		
             Google Ads API		
             Google Drive API

3. Настроить Login\Pass от учетки Google в SendMail.
   Перейти по https://myaccount.google.com/lesssecureapps и дать доступ приложению 
    отправлять EMAIL от имени учетки, указанной в SendMail.

4. В Google Disk выбрать папку для мониторинга. Вставить её ID из строки браузера в 
    setDriveAncestorId("YOUR_ID") в классе Apiv1v3cron.

5. Выкачать при необходимости зависимости Gradle.
    Собрать jar используя Gradle->Tasks->Other->fatJar.
6. Запустить jar.