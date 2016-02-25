# Rotor Model Identification System
## Sinossi
Questo progetto rappresenta un tool per l'esecuzione di routines su ESC (Electronic Speed Controller) e la raccolta e l'analisi dei dati della telemetria raccolti durante l'esecuzione.  
Grazie alle astrazioni introdotte � possibile utilizzare un modello qualsiasi di ESC. Le istruzioni sono astratte e standardizzate indipendentemente dall'ESC, per questo motivo inoltre routines sono indipendenti dai particolari comandi utilizzati dall'ESC. Con lo stesso principio anche la raccolta dei dati tramite telemetria � indipendente dal modello utilizzato. Su questi dati � possibile definire analisi personalizzate in base alle proprie necessit�.
## Motivazione
Il progetto � nato dalla necessit� di eseguire una serie di accelerazioni a corrente costante su un motore TMotor mt 2212 comandato da un Autoquad ESC32 v2 collegato al pc tramite seriale. Raccolti quindi i dati sulla velocit� angolare, la corrente e la tensione del motore, questi vengono utilizzati per il calcolo di parametri propri del motore quali la *torque constant*, la *motor armature constant* e la *internal resistance* sulla base degli studi di Bangura et al. [1].  
Con l'obiettivo di permettere il tuning di motori ed ESC diversi, con comandi diversi e necessit� di eseguire routine e analisi diverse, abbiamo deciso di sviluppare un'astrazione dell'intero processo, rendendolo cos� indipendente dal nostro specifico caso e riutilizzabile in un contesto di utilizzo pi� ampio.

## Funzionamento ed utilizzo
Dalla schermata principale � possibile utilizzare i seguenti componenti:
1. *Esecuzione di una routine*  
  1.a Selezione della porta seriale a cui � collegato l'ESC  
  1.b Selezione del modello di ESC da utilizzare  
  1.c Selezione della routine da eseguire  
  1.d Avvio della routine
2. *Analisi dei dati*  
  2.f Selezione del file dei dati  
  2.g Selezione del file di parametri per l'analisi  
  2.h Selezione del tipo di analisi  
  2.i Avvio dell'analisi  
  2.j Ricostruzione dei grafici  


  IMMAGINE
### Esecuzione di una routine
##### Selezione della porta seriale a cui � collegato l'ESC  
La comunicazione con l'ESC avviene tramite seriale. Se pi� seriali sono connesse al pc � possibile selezionare quella da utilizzare. La connessione avviene sempre a 230400 baud, 8 bit di dati, 1 bit di stop, 0 bit di parit�. In future versioni sar� permesso personalizzare queste impostazioni.
##### Selezione del modello di ESC da utilizzare
Tutti gli ESC utilizzabili tramite questo programma devono estendere la classe astratta AbstractEsc, saranno cos� visibili e selezionabili in questo men� a tendina. I requisiti che deve soddisfare l'implementazione di un ESC sono: gestire l'esecuzione di tutte le istruzioni contenute nella classe Instruction, gestire la telemetria  selezionando i parametri da filtrare tra quelli contenuti nella classe TelemetryParameter e inviando i dati attraverso un PipedOutputStream. Come esempio di implementazione di un ESC si veda la classe AutoQuadEsc32.
##### Selezione della routine da eseguire
Le routines sono contenute in file .rou collocati nella cartella routines nel path in cui � stato eseguito il programma. Tutte le routines ben formate vengono automaticamente parsate all'avvio del programma e presentate in un men� a tendina. Per informazioni su come scrivere una routine si veda pi� avanti.
##### Avvio della routine
Se sono stati riempiti correttamente i campi soprastanti questo pulsante permette di avviare la routine sull'ESC. I valori della telemetry specificati nella routine verranno visualizzati in una finestra di questo tipo: 
IMMAGINE  
Inoltre vengono salvati in un file chiamato "Motor_data_yyyy-MM-dd_HH-mm-ss.csv" dove i campi del nome rappresentano la data e l'ora dell'esecuzione.
### Analisi dei dati
##### Selezione del file dei dati  
Si permette di selezionare un file di dati creato tramite l'esecuzione di una routine precedente. Di questi dati � possibile effettuare delle analisi oppure semplicemte visualizzarne i grafici (solo i valori numerici)
##### Selezione del file di parametri per l'analisi
Poich� per l'analisi possono essere necessari ulteriori parametri oltre ai dati della telemetry � possibile, anche se non obbligatorio, indicare qui un file dal quale caricare i valori dei parametri. In questo modo non sar� necessario inserirli a mano ogni volta che si vuole ripetere un'analisi. Il formato dei file segue le convenzioni dei .properties di Java, si veda tirocinio.properties come esempio.
##### Selezione del tipo di analisi
Tutte le analisi che � possibile utilizzare tramite questo applicativo devono estendere la classe Analyzer, questa fornisce i metodi di base per il caricamento dei dati ed eventualmente dei parametri da file .properties. Le sottoclassi devono implementare solamente il metodo calcola. Un Analyzer, almeno in questa prima versione del programma, � in grado di gestire solamente parametri numerici e fornire in output risultati numerici. Si veda come esempio di implementazione la classe TirocinioAnalyzer (i risultati principali di questa analisi sono mostrati nel pannello grafico, mentre i risultati intermedi utili ad un'analisi pi� approfondita vengono salvati in un file .csv con lo stesso nome del file di dati seguito dal suffisso "-ANALYSIS".
##### Avvio dell'analisi  
Se sono stati riempiti correttamente i campi soprastanti questo pulsante permette di aprire una finestra di analisi dei dati. In questa finestra sar� possibile modificare i parametri caricati dal file, inserire quelli mancanti e avviare l'analisi. Si noti che questa finestra � generata automaticamente a partire dalla specifica implementazione di Analyzer che si vuole utilizzare, pertanto non � necessario modificare le classi grafiche nel momento in cui si vuole implementare un Analyzer.
IMMAGINE (solo due parametri con sullo sfondo il .properties con 2 parametri, con i risultati e sullo sfondo il csv dei risultati intermedi)
##### Ricostruzione dei grafici
Alternativamente alla finestra di analisi dei dati � possibile visualizzare nuovamente i grafici a partire dai dati contenuti nel file. Per ovvi motivi solo i parametri numerici della telemetry vengono riprodotti.
## TelemetryParameter
I pi� comuni parametri della telemetria di un ESC, associati alla stringa che li contraddistingue nell'output di un AutoQuadEsc32 (in altre implementazioni � possibile ignorare questa stringa e mappare la propria telemetria sui parametri astratti nel modo corretto per lo specifico modello)
## InstructionType e Instruction 
Le pi� comuni istruzioni che � possibile inviare ad un esc. Alcune di esse probabilmente troveranno un corrispondente diretto nel set di istruzioni del proprio ESC, altre come ACCELERATE andranno gestite come istruzioni di pi� alto livello e scomposte in comandi pi� semplici. Alle istruzioni � anche possibile associare ulteriori parametri l� dove � necessario, ad esempio per impostare la velocit� ad un certo valore. 
## Routines

## Dependencies
Il codice � scritto utilizzando Java 1.7, accompagnato dalle seguenti librerie:
* RxTx rxtx.qbang.org
* JFreeChart www.jfree.org/jfreechart
* Commons Math commons.apache.org/proper/commons-math
* Reflections https://code.google.com/archive/p/reflections

[1] M. Bangura, H. Lim, H. Kim, and R. Mahony, "Aerodynamic power control for multirotor aerial vehicles", in *Robotics and Automation (ICRA), 2014 IEEE International Conference on*, May 2014, pp 529-536