# Pattern Register

One row per pattern **actually used**. Each row points to real classes and a test.
`scripts/check-deliverables.sh` counts rows.

| # | Pattern | Lecture | Problem in this project (the smell it removes) | Key classes | Test | Alternative considered |
|---|---------|---------|-----------------------------------------------|-------------|------|------------------------|
| 1 | Factory Method (Creational) | 3-дәріс | Әр пациент хабарламаны өз арнасымен алады: SMS немесе email. Factory пациенттің таңдауына қарай керек арнаны жасайды, ал жаңа арна жаңа класпен қосылады | `ReminderChannelFactory`, `ReminderChannel` | `ChannelFactoryAndAdapterTest` | **Сервисте switch (арна түрі бойынша).** Алмадық, себебі әр жаңа арна сервисті өзгертеді |
| 2 | Adapter (Structural) | 4-дәріс | Сыртқы SMS қызметі біздің жүйеден басқаша жұмыс істейді: нөмірді басқа форматта күтеді, жауапты сан кодымен береді. Adapter оны біздің хабарлама арнасына сай етеді | `SmsChannelAdapter`, `KazSmsGatewayClient` | `ChannelFactoryAndAdapterTest` | **SMS қызметін сервистен тікелей шақыру.** Алмадық, себебі жүйе бір SMS жеткізушіге байланып қалады |
| 3 | Observer (Behavioral) | 5-дәріс | Бір оқиға болғанда (жазылды, бас тартылды, шақырылды) кезек экраны жаңаруы және пациентке хабарлама кетуі керек. Сервис тек «оқиға болды» деп хабарлайды, ал экран мен хабарлама өздері жаңарады | `EventPublisher`, `ClinicEventListener`, `QueueBoard`, `PatientNotifier` | `ObserverTest`, `ServiceEventsTest` | **Сервистен экранды және SMS-ті тікелей шақыру.** Алмадық, себебі сервис бәрін біліп кетеді, ал жаңа тыңдаушы қосу сервисті өзгертеді |
| 4 | Command (Behavioral) | 5-дәріс | Тіркеуші қателесіп жазылудан бас тартса, оны дәл бұрынғыдай қайтару керек: күйі, талоны, слоты. Command әр әрекетті жеке объект етеді, ол бұрынғы күйді есте сақтайды | `Command`, `CancelAppointmentCommand`, `BookAppointmentCommand`, `CommandHistory` | `CommandUndoTest` | **Сервисте «соңғы бас тартылған жазылу» өрісін сақтау.** Алмадық, себебі ол тек бір әрекетті қайтарады, ал талон жоғалады |
| 5 | Template Method (Behavioral) | 6-дәріс | Дәрігер кестесі екі түрде шығады: мәтін және CSV. Екеуінде де қадамдар бірдей (тақырып → жолдар → қорытынды). Ортақ қаңқа бір жерде, әр формат тек өз бөлігін жазады | `ScheduleReport`, `TextScheduleReport`, `CsvScheduleReport` | `ScheduleReportTest` | **Әр форматты бөлек толық жазу.** Алмадық, себебі бірдей код қайталанады |

## Rejected patterns
At least two patterns you considered and decided NOT to use, with the reason (YAGNI). Link the ADR.

| Pattern | Why rejected | ADR |
|---------|--------------|-----|
| TODO | TODO | TODO |
