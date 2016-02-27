# Identificazione dei coefficienti di un motore T-Motor MT2212

## Strumenti
- Il motore fornito per le prove sperimentali è un: T-Motor MT2212 (http://www.rctigermotor.com/html/2013/Professional_0912/44.html)
- L'ESC (electronic speed controller) utilizzato per la comunicazione con il rotore è un: AutoQuad ESC32 V2 (http://autoquad.org/wiki/wiki/aq-esc32/esc32/)
- Per la comunicazione seriale tra ESC e calcolatore, si è utilizzato una breakout board da FTDI a serial IC modello: SparkFun FTDI Basic Breakout - 3.3V (https://www.sparkfun.com/products/9873)

## Procedura di analisi
La procedura di analisi utilizzata si basa sui lavori svolti da Bangura et al. [1] per l'identificazione del modello dei rotori di un quadricottero. Ai fini dell'identificazione è necessario stimare per ogni rotore i coefficienti caratteristici presenti in questa equazione:  
![v_a = K_e\omega + R_a i_a + L_a \frac{di_a}{dt}](/assets/eq1.gif)  		
In cui Ke è la costante delle armature del motore proporzionale al campo elettromagnetico generato, Ra è la restistenza del motore e La la sua induttanza. Al fine di semplificare la procedura per l'identificazione di questi paramtetri si considera il motore in *steady state*, operante cioè in condizioni di corrente costante, in questo modo la derivata della corrente è nulla ed La si semplifica dall'equazione. Nelle suddette condizioni il motore opera erogando una coppia e quindi un'accelerazione angolare costanti, legate tra loro dalla seguente relazione, in cui si introduce anche il parametro Kq, costante di coppia del rotore:  		
![\tau = K_q i_a = I \dot{\omega}](/assets/eq2.gif)  		
Tramite l'ESC è possibile eseguire una routine ad accelerazione, ottenendo istante per istante una stima della corrente circolante nel motore, della tensione applicata e degli rpm.  		
Tramite regressione lineare si valuta l'accelerazione angolare del rotore, Unitamente al momento di interzia di un disco applicato al motore, l'accelerazione è usata per stimare la coppia generata. Successivamente si stima la corrente media, supposta costante e la si usa per calcolare infine il parametro Kq.		
![K_q = \frac{\tau}{\overline{i_a}} = \frac{I \dot{\omega}}{\overline{i_a}}](/assets/eq3.gif)  		
Inoltre, tramite regressione lineare tra gli rpm e la tensione del motore si ottengono Ke e Ra, rispettivamente come coefficiente angolare e ordinata all'origine divisa per la corrente media:  		
![v_a = K_e\omega + R_a i_a](/assets/eq4.gif)  		
L'analisi qui descritta è condotta tramite un software appositamente sviluppato per gestire la comunicazione tramite seriale con un motore collegato a un ESC, la raccolta dei dati della telemetria e la loro analisi.

## Osservazioni sull'analisi
Come già descritto nella precedente sezione, per effettuare le procedure di calcolo dei coefficienti sul motore, è necessario avere profili di velocità crescente con accelerazione e corrente costante. Per questo motivo la routine scelta è composta da una serie di 5 accelerazioni tra 2000 e 3000 rpm, con vari profili di accelerazione costante (da 10 a 50 rpm/s), ripetute per due volte. Tra ogni accelerazione e la successiva vi è una decelerazione tra 3000 e 2000, alla massima rapidità consentita dal motore, ininfluente per il computo dei dati. Si osserva che per mantenere la corrente che scorre nel motore costante, è necessario mantenere velocità angolari e accelerazioni angolari basse.
```
accelerate: 2000 3000 10
accelerate: 3000 2000 -400
accelerate: 2000 3000 20
accelerate: 3000 2000 -400
accelerate: 2000 3000 30
accelerate: 3000 2000 -400
accelerate: 2000 3000 40
accelerate: 3000 2000 -400
accelerate: 2000 3000 50
accelerate: 3000 2000 -400

accelerate: 2000 3000 10
accelerate: 3000 2000 -400
accelerate: 2000 3000 20
accelerate: 3000 2000 -400
accelerate: 2000 3000 30
accelerate: 3000 2000 -400
accelerate: 2000 3000 40
accelerate: 3000 2000 -400
accelerate: 2000 3000 50
accelerate: 3000 2000 -400
```
Per quanto riguarda la telemetria, si è scelto di utilizzare una frequenza di 30 Hz, che dopo alcune prove sperimentali risulta essere il limite superiore dettato dall'ESC.   
Durante la sperimentazione e lo studio dei dati, si osserva che in questo modello di ESC vi è un limite alla decelerazione di circa -400 rpm/s. Infatti si nota che, se si impongono cambiamenti più rapidi, l'esc pone a 0V i motor volts e non ottiene la decelerazione richiesta. Per quanto riguarda l'accelerazione invece, questa sarà limitata superiormente dal valore per il quale l'esc utiliza una tensione pari a 15 V, ossia la tensione di alimentazione.  
Si osserva inoltre che i dati ottenuti durante un'accelerazione con valori molto bassi, con una frequenza di telemetria abbastanza elevata, sono significativamente scattered, ossia senza un andamento strettamente crescente come dovrebbe essere, ma con valori che oscillanti. Questo è probabilmente dettato dalla limitata accuratezza dell'analisi dell'ESC e dalla scelta implementativa della procedura di accelerazione in codice. 

## Risultati ottenuti
- come abbiamo calcolato le incertezze
- ogni tanto l'esc sbanana la telemetry, ma la procedura è in grado di scartare i valori spuri
- foto
- commento sulla valutazione delle incertezze
- l'ipotesi di corrente costante è verificata solo per il ristretto range 2000-3000 quindi i coeff che calcoliamo hanno significato fisico solo in questo range, per usare il motore in situazioni diverse, questi parametri possono non essere validi

## Possibili sviluppi
La procedura di identificazione è progettata per essere indipendente dall'ESC utilizzato, che rappresenta solamente uno strumento di comunicazione e misura. Pertanto ripetere l'analisi sullo stesso motore con ESC diversi deve risultare in valori compatibili dei parametri. L'ESC ha incidenza solamente sulle performance delle routines e sugli errori associati alle misure della telemetria, ma in linea di massima non dovrebbe modificare il comportamento di un motore. Grazie alle astrazioni introdotte nel software è possibile testare altri ESC scrivendo solamente la classe che li implementa, lasciando inalterato il resto del codice.
Un ulteriore sviluppo futuro di questa procedura di analisi è testare altri motori per stimarne i coefficienti, confrontandoli inoltre con quelli forniti dal datasheet del motore se disponibili. In questo modo è possibile valutare l'accuratezza e il campo di validità dei metodi utilizzati.
