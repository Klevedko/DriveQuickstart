Статический отчет Googe Drive Files

Приложение запускается из CronBuild , в определенный день рекурсивно сканирует папку 
"String startFolderId = "<FOLDER ID>" в классе StaticReport и все подпапки, составляет лист из ID файлов,
периодически формируя потоки по каждому ID, который получает списки владельцев REST-ом. 

Файл сохраняется на жесткий диск под именем "Ыtatic_audit_result_01_01_2001.xls".
Файл загружается классом CreateGoogleFile на Google Drive в папку
File googleFile = createGoogleFile(<FOLDER ID>).

Отправляется e-mail в классе SendMail ( все параметры Sender\Reciever внутри класса )
с ссылкой на загруженный файл.

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
    Класс по очереди использует Apiv1 Apiv3 для POST запросов для ДИНАМИЧЕСКОГО отчета.
        ( получение данных и их анализ, всю логику, запись в файл, отправка Email).
        В данной задаче не используется.
4. SendMail и SimpleEmail
    Классы для настройки и отправки Email ( ч\з Google SMTP)  в случае успеха\ошибки.
5. AuditMap
    Класс для хранения считываемых данных с переопределенным методом isEquals().
        В данной задаче не используется.
6. TestCheckSum 
    Проверка HASH выходного файла. Не используется.
6. CreateGoogleFile
    Отправка файла на Google Disk. Не используется.

---------------------------------------------------------------------------------------

Настройка и запуск.

Все действия уже произведены:

1. https://console.developers.google.com
    Создать проект и сгенерить в учетных данных oApp ключ.
    Скачать json . Положить его в resources/credentials.json.
    Положить его в resources/credentials.json.
Повторить шаг, но положить новый файл уже в resources/credentialss.json

( При чистой установке, при первом запуске jar дважды откроется 
    окно подтверждения для получения token в папки token_v1 и token_v3 )

2. Включить у проекта https://console.developers.google.com нужные API:
             Admin SDK		
             Apps Activity API		
             Google Ads API		
             Google Drive API

3. Настроить Login\Pass от учетки Google в SendMail.
   Перейти по https://myaccount.google.com/lesssecureapps и дать доступ приложению 
    отправлять EMAIL от имени учетки, указанной в SendMail.

4. В Google Disk выбрать папку для мониторинга. Вставить её ID из строки браузера в 
    String FileId + "<FOLDER ID> в классе StaticReport.

5. Выкачать при необходимости зависимости Gradle.
    Собрать jar используя Gradle->Tasks->Other->fatJar.
6. Запустить jar без дополнительных параметров.