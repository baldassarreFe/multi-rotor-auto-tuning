# Rotor Model Identification System
## Sinossi
Questo progetto rappresenta un tool per l'esecuzione di routines su ESC (Electronic Speed Controller) e la raccolta e l'analisi dei dati della telemetria raccolti durante l'esecuzione.  
Grazie alle astrazioni introdotte è possibile utilizzare un modello qualsiasi di ESC. Le istruzioni sono astratte e standardizzate indipendentemente dall'ESC, per questo motivo inoltre routines sono indipendenti dai particolari comandi utilizzati dall'ESC. Con lo stesso principio anche la raccolta dei dati tramite telemetria è indipendente dal modello utilizzato. Su questi dati è possibile definire analisi personalizzate in base alle proprie necessità.
## Motivazione
Il progetto è nato dalla necessità di eseguire una serie di accelerazioni a corrente costante su un motore TMotor mt 2212 comandato da un Autoquad ESC32 v2 collegato al pc tramite seriale. Raccolti quindi i dati sulla velocità angolare, la corrente e la tensione del motore, questi vengono utilizzati per il calcolo di parametri propri del motore quali la *torque constant*, la *motor armature constant* e la *internal resistance* sulla base degli studi di Bangura et al. [1].  
Con l'obiettivo di permettere il tuning di motori ed ESC diversi, con comandi diversi e necessità di eseguire routine e analisi diverse, abbiamo deciso di sviluppare un'astrazione dell'intero processo, rendendolo così indipendente dal nostro specifico caso e riutilizzabile in un contesto di utilizzo più ampio.

## Funzionamento ed utilizzo
Dalla schermata principale è possibile utilizzare i seguenti componenti:
1. *Esecuzione di una routine*  
  1.a Selezione della porta seriale a cui è collegato l'ESC  
  1.b Selezione del modello di ESC da utilizzare  
  1.c Selezione della routine da eseguire  
  1.d Avvio della routine
2. *Analisi dei dati*  
  2.f Selezione del file dei dati  
  2.g Selezione del file di parametri per l'analisi  
  2.h Selezione del tipo di analisi  
  2.i Avvio dell'analisi  
  2.j Ricostruzione dei grafici  


  ![Finestra principale](/assets/mainFrame.png)

### Esecuzione di una routine

##### Selezione della porta seriale a cui è collegato l'ESC  
La comunicazione con l'ESC avviene tramite seriale. Se più seriali sono connesse al pc è possibile selezionare quella da utilizzare. La connessione avviene sempre a 230400 baud, 8 bit di dati, 1 bit di stop, 0 bit di parità. In future versioni sarà permesso personalizzare queste impostazioni.

##### Selezione del modello di ESC da utilizzare
Tutti gli ESC utilizzabili tramite questo programma devono estendere la classe astratta AbstractEsc, saranno così visibili e selezionabili in questo menù a tendina. I requisiti che deve soddisfare l'implementazione di un ESC sono: gestire l'esecuzione di tutte le istruzioni contenute nella classe Instruction, gestire la telemetria  selezionando i parametri da filtrare tra quelli contenuti nella classe TelemetryParameter e inviando i dati attraverso un PipedOutputStream. Come esempio di implementazione di un ESC si veda la classe AutoQuadEsc32.

##### Selezione della routine da eseguire
Le routines sono contenute in file .rou collocati nella cartella routines nel path in cui è stato eseguito il programma. Tutte le routines ben formate vengono automaticamente parsate all'avvio del programma e presentate in un menù a tendina. Per informazioni su come scrivere una routine si veda più avanti.

##### Avvio della routine
Se sono stati riempiti correttamente i campi soprastanti questo pulsante permette di avviare la routine sull'ESC. I valori della telemetry specificati nella routine verranno visualizzati in una finestra di questo tipo: 
IMMAGINE  
Inoltre vengono salvati in un file chiamato "Motor_data_yyyy-MM-dd_HH-mm-ss.csv" dove i campi del nome rappresentano la data e l'ora dell'esecuzione.

### Analisi dei dati

##### Selezione del file dei dati  
Si permette di selezionare un file di dati creato tramite l'esecuzione di una routine precedente. Di questi dati è possibile effettuare delle analisi oppure semplicemte visualizzarne i grafici (solo i valori numerici)

##### Selezione del file di parametri per l'analisi
Poichè per l'analisi possono essere necessari ulteriori parametri oltre ai dati della telemetry è possibile, anche se non obbligatorio, indicare qui un file dal quale caricare i valori dei parametri. In questo modo non sarà necessario inserirli a mano ogni volta che si vuole ripetere un'analisi. Il formato dei file segue le convenzioni dei .properties di Java, si veda tirocinio.properties come esempio.

##### Selezione del tipo di analisi
Tutte le analisi si possono utilizzare tramite questo applicativo devono estendere la classe Analyzer, questa fornisce i metodi di base per il caricamento dei dati ed eventualmente dei parametri da file .properties. Le sottoclassi devono implementare solamente il metodo calcola. Un Analyzer, almeno in questa prima versione del programma, è in grado di gestire solamente parametri numerici e fornire in output risultati numerici. Si veda come esempio di implementazione la classe TirocinioAnalyzer (i risultati principali di questa analisi sono mostrati nel pannello grafico, mentre i risultati intermedi utili ad un'analisi più approfondita vengono salvati in un file .csv con lo stesso nome del file di dati seguito dal suffisso "-ANALYSIS".

##### Avvio dell'analisi  
Se sono stati riempiti correttamente i campi soprastanti questo pulsante permette di aprire una finestra di analisi dei dati. In questa finestra sarà possibile modificare i parametri caricati dal file, inserire quelli mancanti e avviare l'analisi. Si noti che questa finestra è generata automaticamente a partire dalla specifica implementazione di Analyzer che si vuole utilizzare, pertanto non è necessario modificare le classi grafiche nel momento in cui si vuole implementare un Analyzer.
![Caricamento di parametri in una schermata di analisi](/assets/parametersLoading.png)  
![Risultati di un'analisi](/assets/analysisResults.png)  
    IMMAGINE (solo due parametri con sullo sfondo il .properties con 2 parametri, con i risultati e sullo sfondo il csv dei risultati intermedi)

##### Ricostruzione dei grafici
Alternativamente alla finestra di analisi dei dati è possibile visualizzare nuovamente i grafici a partire dai dati contenuti nel file. Per ovvi motivi solo i parametri numerici della telemetry vengono riprodotti.

## TelemetryParameter
I più comuni parametri della telemetria di un ESC, associati alla stringa che li contraddistingue nell'output di un AutoQuadEsc32 (in altre implementazioni è possibile ignorare questa stringa e mappare la propria telemetria sui parametri astratti nel modo corretto per lo specifico modello)

## InstructionType e Instruction 
Le più comuni istruzioni che è possibile inviare ad un esc. Alcune di esse probabilmente troveranno un corrispondente diretto nel set di istruzioni del proprio ESC, altre come ACCELERATE andranno gestite come istruzioni di più alto livello e scomposte in comandi più semplici. Alle istruzioni è anche possibile associare ulteriori parametri là dove è necessario, ad esempio per impostare la velocità ad un certo valore. 

## Routines
Le routines utilizzabili in questo applicativo sono scritte in file con estensioni .rou con il seguente formato:
* la prima riga contiene il nome della routine
* la seconda riga contiene una lista di parametri da utilizzare nella telemetry sottoforma di stringhe separate da virgole(vedere la classe TelemetryParameter per le specifiche stringhe). 
* le righe successive devono contenere una valida istruzione per l'ESC, così come specificate nella classe Instruction. Se l'istruzione necessita parametri aggiuntivi questi vanno specificati dopo il nome dell'istruzione e un ':', ogni parametro separato da uno spazio. Le linee vuote e quelle che iniziano per '#' vengono ignorate.  
Un esempio di file di routine valido è il seguente:  
```
    Rapid acceleration from 2000 to 6000 rpm  
    RPM, AMPS AVG, MOTOR VOLTS
    
    arm  
    start  
    rpm: 2000  
    sleep: 10000  
    telemetry: 50  
    sleep: 1000  
    
    # accelerate at 1200 rpm/s  
    accelerate: 2000 6000 1200  
    accelerate: 6000 1000 -400  
    
    sleep: 5000  
    stop telemetry  
    stop  
    disarm  
```
## Dependencies
Il codice è scritto utilizzando Java 1.7, accompagnato dalle seguenti librerie:
* RxTx rxtx.qbang.org
* JFreeChart www.jfree.org/jfreechart
* Commons Math commons.apache.org/proper/commons-math
* Reflections https://code.google.com/archive/p/reflections

[1] M. Bangura, H. Lim, H. Kim, and R. Mahony, "Aerodynamic power control for multirotor aerial vehicles", in *Robotics and Automation (ICRA), 2014 IEEE International Conference on*, May 2014, pp 529-536
